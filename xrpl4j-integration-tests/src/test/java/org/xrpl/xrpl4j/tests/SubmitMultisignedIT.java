package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitMultiSignedResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SubmitMultisignedIT extends AbstractIT {

  protected final ObjectMapper objectMapper = ObjectMapperFactory.create();
  protected final XrplBinaryCodec binaryCodec = new XrplBinaryCodec();
  protected final KeyPairService keyPairService = DefaultKeyPairService.getInstance();

  /////////////////////////////
  // Create four accounts, one for the multisign account owner, one for their two friends,
  // and one to send a Payment to.
  Wallet sourceWallet = createRandomAccount();
  Wallet aliceWallet = createRandomAccount();
  Wallet bobWallet = createRandomAccount();
  Wallet destinationWallet = createRandomAccount();

  FeeResult feeResult;
  AccountInfoResult sourceAccountInfoAfterSignerListSet;
  SubmitResult<SignerListSet> signerListSetResult;

  @BeforeEach
  public void setUp() throws JsonRpcClientErrorException {

    /////////////////////////////
    // Wait for all of the accounts to show up in a validated ledger
    final AccountInfoResult sourceAccountInfo = scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );
    scanForResult(() -> this.getValidatedAccountInfo(aliceWallet.classicAddress()));
    scanForResult(() -> this.getValidatedAccountInfo(bobWallet.classicAddress()));
    scanForResult(() -> this.getValidatedAccountInfo(destinationWallet.classicAddress()));

    /////////////////////////////
    // And validate that the source account has not set up any signer lists
    assertThat(sourceAccountInfo.accountData().signerLists()).isEmpty();

    /////////////////////////////
    // Then submit a SignerListSet transaction to add alice and bob as signers on the account
    feeResult = xrplClient.fee();
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourceWallet.classicAddress())
      .fee(getComputedNetworkFee(feeResult))
      .sequence(sourceAccountInfo.accountData().sequence())
      .signerQuorum(UnsignedInteger.valueOf(2))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(aliceWallet.classicAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(bobWallet.classicAddress())
            .signerWeight(UnsignedInteger.ONE)
            .build()
        )
      )
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    /////////////////////////////
    // Validate that the transaction was submitted successfully
    signerListSetResult = xrplClient.submit(sourceWallet, signerListSet);
    assertThat(signerListSetResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(signerListSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(signerListSetResult.transactionResult().hash());
    logger.info(
      "SignerListSet transaction successful: https://testnet.xrpl.org/transactions/" +
        signerListSetResult.transactionResult().hash()
    );

    /////////////////////////////
    // Then wait until the transaction enters a validated ledger and the source account's signer list
    // exists
    sourceAccountInfoAfterSignerListSet = scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress()),
      infoResult -> infoResult.accountData().signerLists().size() == 1
    );

    assertThat(
      sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        .signerEntries().stream()
        .sorted(Comparator.comparing(entry -> entry.signerEntry().account()))
        .collect(Collectors.toList())
    ).isEqualTo(signerListSet.signerEntries().stream()
      .sorted(Comparator.comparing(entry -> entry.signerEntry().account()))
      .collect(Collectors.toList()));
  }

  @Test
  public void submitMultisignedAndVerifyHash() throws JsonRpcClientErrorException, JsonProcessingException {

    /////////////////////////////
    // Construct an unsigned Payment transaction to be multisigned
    Payment unsignedPayment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(
        FeeUtils.computeMultiSigFee(
          feeResult.drops().openLedgerFee(),
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        )
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(destinationWallet.classicAddress())
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys
    List<SignerWrapper> signers = Lists.newArrayList(aliceWallet, bobWallet).stream()
      .map(wallet -> {
          try {
            String unsignedJson = objectMapper.writeValueAsString(unsignedPayment);

            String unsignedBinaryHex = binaryCodec.encodeForMultiSigning(unsignedJson, wallet.classicAddress().value());
            String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey()
              .orElseThrow(() -> new RuntimeException("Wallet must provide a private key to sign the transaction.")));
            return SignerWrapper.of(Signer.builder()
              .account(wallet.classicAddress())
              .signingPublicKey(wallet.publicKey())
              .transactionSignature(signature)
              .build()
            );
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        }
      )
      .collect(Collectors.toList());

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    Payment multiSigPayment = Payment.builder()
      .from(unsignedPayment)
      .signers(signers)
      .build();

    SignedTransaction<Payment> signedTransaction = SignedTransaction.<Payment>builder()
      .signedTransaction(multiSigPayment)
      .signedTransactionBlob(binaryCodec.encode(objectMapper.writeValueAsString(multiSigPayment)))
      .build();
    String libraryCalculatedHash = signedTransaction.hash().value();

    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);

    assertThat(paymentResult.transaction().hash().value()).isEqualTo(libraryCalculatedHash);

    assertThat(paymentResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(signerListSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(signerListSetResult.transactionResult().hash());
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" +
        paymentResult.transaction().hash()
    );
  }

  @Test
  public void submitMultisignedWithSignersInDescOrderAndVerifyHash() throws
    JsonRpcClientErrorException, JsonProcessingException {

    /////////////////////////////
    // Construct an unsigned Payment transaction to be multisigned
    Payment unsignedPayment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(
        FeeUtils.computeMultiSigFee(
          feeResult.drops().openLedgerFee(),
          sourceAccountInfoAfterSignerListSet.accountData().signerLists().get(0)
        )
      )
      .sequence(sourceAccountInfoAfterSignerListSet.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .destination(destinationWallet.classicAddress())
      .signingPublicKey("")
      .build();

    /////////////////////////////
    // Alice and Bob sign the transaction with their private keys
    List<SignerWrapper> signers = Lists.newArrayList(aliceWallet, bobWallet).stream()
      .map(wallet -> {
          try {
            String unsignedJson = objectMapper.writeValueAsString(unsignedPayment);

            String unsignedBinaryHex = binaryCodec.encodeForMultiSigning(unsignedJson, wallet.classicAddress().value());
            String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey()
              .orElseThrow(() -> new RuntimeException("Wallet must provide a private key to sign the transaction.")));
            return SignerWrapper.of(Signer.builder()
              .account(wallet.classicAddress())
              .signingPublicKey(wallet.publicKey())
              .transactionSignature(signature)
              .build()
            );
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        }
      )
      .collect(Collectors.toList());

    final AddressCodec addressCodec = AddressCodec.getInstance();
    signers = signers.stream().sorted(
      Comparator.comparing(
        signature -> new BigInteger(addressCodec.decodeAccountId(
          Address.of(signature.signer().account().value())
        ).hexValue(), 16),
        Comparator.reverseOrder()
      )
    ).collect(Collectors.toList());

    /////////////////////////////
    // Then we add the signatures to the Payment object and submit it
    Payment multiSigPayment = Payment.builder()
      .from(unsignedPayment)
      .signers(signers)
      .build();

    SignedTransaction<Payment> signedTransaction = SignedTransaction.<Payment>builder()
      .signedTransaction(multiSigPayment)
      .signedTransactionBlob(binaryCodec.encode(objectMapper.writeValueAsString(multiSigPayment)))
      .build();
    String libraryCalculatedHash = signedTransaction.hash().value();

    SubmitMultiSignedResult<Payment> paymentResult = xrplClient.submitMultisigned(multiSigPayment);

    assertThat(paymentResult.transaction().hash().value()).isEqualTo(libraryCalculatedHash);

    assertThat(paymentResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(signerListSetResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(signerListSetResult.transactionResult().hash());
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" +
        paymentResult.transaction().hash()
    );
  }
}

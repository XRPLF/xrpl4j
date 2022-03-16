package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.DerivedKeysSignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCode;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.security.Key;
import java.security.KeyStore;
import java.util.Objects;

/**
 * Integration tests for submitting payment transactions to the XRPL using a {@link DerivedKeysSignatureService} for all
 * signing operations.
 */
public class SubmitPaymentUsingSignatureService extends AbstractIT {

  private static Wallet sourceWallet;
  private static Wallet destinationWallet;

  private static AddressCodec addressCodec;
  private static KeyPairService keyPairService;

  private static SignatureService signatureService;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @BeforeEach
  public void setUp() throws Exception {
    final String jksFileName = "crypto/crypto.p12";
    final char[] jksPassword = "password".toCharArray();
    final KeyStore keyStore = JavaKeystoreLoader.loadFromClasspath(jksFileName, jksPassword);
    final Key secretKey = keyStore.getKey("secret0", "password".toCharArray());
    signatureService = new DerivedKeysSignatureService(secretKey::getEncoded, VersionType.ED25519);

    keyPairService = new DefaultKeyPairService();
    addressCodec = new AddressCodec();

    // sourceWallet is created in each unit test...
    destinationWallet = this.newSecp256k1WalletFromSignatureService(signatureService, "destinationWallet");
  }

  @Test
  public void sendPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    sourceWallet = this.newEd25519WalletFromSignatureService(signatureService, "sourceWallet");

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.classicAddress()));
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    final KeyMetadata sourceKeyMetadata = this.keyMetadata("sourceWallet");

    SignedTransaction<Payment> signedTransaction = signatureService.sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
    assertThat(result.result()).isEqualTo(TransactionResultCode.TES_SUCCESS);
    assertThat(result.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(result.transactionResult().hash());
    logger.info(
      "Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().hash());

    this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult().hash(),
        Payment.class)
    );
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    sourceWallet = this.newSecp256k1WalletFromSignatureService(signatureService, "sourceWallet");

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.classicAddress()));
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    final KeyMetadata sourceKeyMetadata = this.keyMetadata("sourceWallet");

    SignedTransaction<Payment> transactionWithSignature = signatureService.sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(transactionWithSignature);
    assertThat(result.result()).isEqualTo(TransactionResultCode.TES_SUCCESS);
    assertThat(result.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(result.transactionResult().hash());
    logger.info(
      "Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().hash()
    );

    this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult().hash(),
        Payment.class)
    );
  }

  //////////////////
  // Private Helpers
  //////////////////

  private Wallet newEd25519WalletFromSignatureService(final SignatureService signatureService, final String walletId) {
    Objects.requireNonNull(signatureService);
    Objects.requireNonNull(walletId);

    final PublicKey publicKey = signatureService.getPublicKey(keyMetadata(walletId));
    return newWalletFromSignatureService(publicKey);
  }

  private Wallet newSecp256k1WalletFromSignatureService(
    final SignatureService signatureService, final String walletId
  ) {
    Objects.requireNonNull(signatureService);
    Objects.requireNonNull(walletId);

    final PublicKey publicKey = signatureService.getPublicKey(keyMetadata(walletId));
    return newWalletFromSignatureService(publicKey);
  }

  private Wallet newWalletFromSignatureService(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);

    final Address classicAddress = keyPairService.deriveAddress(publicKey.value());
    final Wallet wallet = Wallet.builder()
      .publicKey(publicKey.base16Encoded())
      .isTest(true)
      .classicAddress(classicAddress)
      .xAddress(addressCodec.classicAddressToXAddress(classicAddress, true))
      .build();

    this.fundAccount(wallet);

    return wallet;
  }

  private KeyMetadata keyMetadata(final String walletId) {
    Objects.requireNonNull(walletId);

    return KeyMetadata.builder()
      .platformIdentifier("jks")
      .keyringIdentifier("n/a")
      .keyIdentifier(walletId)
      .keyVersion("1")
      .keyPassword("password")
      .build();
  }
}

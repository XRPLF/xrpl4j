package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.bc.DerivedKeyDelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.ServerSecret;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.security.Key;
import java.security.KeyStore;
import java.util.Objects;

/**
 * Integration tests for submitting payment transactions to the XRPL using a {@link DelegatedSignatureService} for all
 * signing operations.
 */
public class SubmitPaymentUsingSignatureService extends AbstractIT {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @BeforeEach
  public void setUp() {
  }

  @Test
  public void sendPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    final DelegatedSignatureService delegatedSignatureService = this.constructSignatureServiceEd25519();

    final KeyMetadata sourceKeyMetadata = constructKeyMetadata("sourceWallet");
    final PublicKey sourceWalletPublicKey = delegatedSignatureService.getPublicKey(sourceKeyMetadata);
    final Address sourceWalletAddress = AddressUtils.getInstance().deriveAddress(sourceWalletPublicKey);
    this.fundAccount(sourceWalletAddress);

    final KeyMetadata destinationKeyMetadata = constructKeyMetadata("destinationWallet");
    final PublicKey destinationWalletPublicKey = delegatedSignatureService.getPublicKey(destinationKeyMetadata);
    final Address destinationWalletAddress = AddressUtils.getInstance().deriveAddress(destinationWalletPublicKey);
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.hexValue())
      .build();

    SingleSingedTransaction<Payment> signedTransaction = delegatedSignatureService.sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    final DelegatedSignatureService delegatedSignatureService = this.constructSignatureServiceSecp256k1();

    final KeyMetadata sourceKeyMetadata = constructKeyMetadata("sourceWallet");
    final PublicKey sourceWalletPublicKey = delegatedSignatureService.getPublicKey(sourceKeyMetadata);
    final Address sourceWalletAddress = AddressUtils.getInstance().deriveAddress(sourceWalletPublicKey);
    this.fundAccount(sourceWalletAddress);

    final KeyMetadata destinationKeyMetadata = constructKeyMetadata("destinationWallet");
    final PublicKey destinationWalletPublicKey = delegatedSignatureService.getPublicKey(destinationKeyMetadata);
    final Address destinationWalletAddress = AddressUtils.getInstance().deriveAddress(destinationWalletPublicKey);
    this.fundAccount(destinationWalletAddress);

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWalletAddress));
    Payment payment = Payment.builder()
      .account(sourceWalletAddress)
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWalletAddress)
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWalletPublicKey.hexValue())
      .build();

    SingleSingedTransaction<Payment> transactionWithSignature = delegatedSignatureService
      .sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(transactionWithSignature);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  //////////////////
  // Private Helpers
  //////////////////

  private KeyMetadata constructKeyMetadata(final String keyIdentifier) {
    Objects.requireNonNull(keyIdentifier);
    return KeyMetadata.builder()
      .platformIdentifier("jks")
      .keyringIdentifier("n/a")
      .keyIdentifier(keyIdentifier)
      .keyVersion("1")
      .keyPassword("password")
      .build();
  }

  private DelegatedSignatureService constructSignatureServiceSecp256k1() {
    try {
      final Key secretKey = loadKeyStore().getKey("secret0", "password".toCharArray());
      return new DerivedKeyDelegatedSignatureService(
        () -> ServerSecret.of(secretKey.getEncoded()), VersionType.SECP256K1
      );
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private DelegatedSignatureService constructSignatureServiceEd25519() {
    try {
      final Key secretKey = loadKeyStore().getKey("secret0", "password".toCharArray());
      return new DerivedKeyDelegatedSignatureService(
        () -> ServerSecret.of(secretKey.getEncoded()), VersionType.ED25519
      );
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private KeyStore loadKeyStore() {
    final String jksFileName = "crypto/crypto.p12";
    final char[] jksPassword = "password".toCharArray();
    return JavaKeystoreLoader.loadFromClasspath(jksFileName, jksPassword);
  }
}

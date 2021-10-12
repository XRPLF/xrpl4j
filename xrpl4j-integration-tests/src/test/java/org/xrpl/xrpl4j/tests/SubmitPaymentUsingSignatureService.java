package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.bc.DerivedKeyDelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.ServerSecret;
import org.xrpl.xrpl4j.crypto.core.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedSignatureService;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
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

  private static Wallet sourceWallet;
  private static Wallet destinationWallet;

  private static AddressCodec addressCodec;
  private static Ed25519KeyPairService ed25519KeyPairService;
  private static Secp256k1KeyPairService secp256k1KeyPairService;

  private static AddressUtils addressService;
  private static DelegatedSignatureService signatureService;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @BeforeEach
  public void setUp() throws Exception {
    final String jksFileName = "crypto/crypto.p12";
    final char[] jksPassword = "password".toCharArray();
    final KeyStore keyStore = JavaKeystoreLoader.loadFromClasspath(jksFileName, jksPassword);
    final Key secretKey = keyStore.getKey("secret0", "password".toCharArray());
    signatureService = new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(secretKey.getEncoded()), VersionType.ED25519
    );

    ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();
    addressService = new DefaultAddressService();

    addressCodec = new AddressCodec();

    // sourceWallet is created in each unit test...
    destinationWallet = this.newSecp256k1WalletFromSignatureService(signatureService, "destinationWallet");
  }

  @Test
  public void sendPaymentFromEd25519Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    sourceWallet = this.newEd25519WalletFromSignatureService(signatureService, "sourceWallet");

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.address()));
    Payment payment = Payment.builder()
      .account(sourceWallet.address())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.address())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWallet.publicKey().hexValue())
      .build();

    final KeyMetadata sourceKeyMetadata = this.keyMetadata("sourceWallet");

    SingleSingedTransaction<Payment> signedTransaction = signatureService.sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(signedTransaction);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().transaction().hash());

    this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult()
          .transaction()
          .hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash.")),
        Payment.class)
    );
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    sourceWallet = this.newSecp256k1WalletFromSignatureService(signatureService, "sourceWallet");

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this
      .scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.address()));
    Payment payment = Payment.builder()
      .account(sourceWallet.address())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.address())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWallet.publicKey().hexValue())
      .build();

    final KeyMetadata sourceKeyMetadata = this.keyMetadata("sourceWallet");

    SingleSingedTransaction<Payment> transactionWithSignature = signatureService.sign(sourceKeyMetadata, payment);
    SubmitResult<Payment> result = xrplClient.submit(transactionWithSignature);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "Payment successful: https://testnet.xrpl.org/transactions/" + result.transactionResult().transaction().hash()
    );

    this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult()
          .transaction()
          .hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash.")),
        Payment.class)
    );
  }

  //////////////////
  // Private Helpers
  //////////////////

  private Wallet newEd25519WalletFromSignatureService(
    final DelegatedSignatureService signatureService, final String walletId
  ) {
    Objects.requireNonNull(signatureService);
    Objects.requireNonNull(walletId);

    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of(walletId));
    final KeyPair keyPair = ed25519KeyPairService.deriveKeyPair(seed);
    final Address classicAddress = addressService.deriveAddress(keyPair.publicKey());

    return Wallet.builder()
      .privateKey(keyPair.privateKey())
      .publicKey(keyPair.publicKey())
      .isTest(true)
      .classicAddress(classicAddress)
      .xAddress(addressCodec.classicAddressToXAddress(classicAddress, true))
      .build();
  }

  private Wallet newSecp256k1WalletFromSignatureService(
    final DelegatedSignatureService signatureService, final String walletId
  ) {
    Objects.requireNonNull(signatureService);
    Objects.requireNonNull(walletId);

    final Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of(walletId));
    final KeyPair keyPair = secp256k1KeyPairService.deriveKeyPair(seed);
    final Address classicAddress = addressService.deriveAddress(keyPair.publicKey());

    return Wallet.builder()
      .privateKey(keyPair.privateKey())
      .publicKey(keyPair.publicKey())
      .isTest(true)
      .classicAddress(classicAddress)
      .xAddress(addressCodec.classicAddressToXAddress(classicAddress, true))
      .build();
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

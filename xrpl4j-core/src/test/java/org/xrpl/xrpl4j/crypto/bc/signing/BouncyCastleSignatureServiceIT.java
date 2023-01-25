package org.xrpl.xrpl4j.crypto.bc.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An Integration Test for {@link BcSignatureService} that uses actual implementations.
 */
class BouncyCastleSignatureServiceIT {

  private KeyPair ed25519KeyPair;
  private KeyPair ed25519KeyPairOther;

  private KeyPair secp256k1KeyPair;
  private KeyPair secp256k1KeyPairOther;

  private Payment payment;

  private BcSignatureService signatureService;

  @BeforeEach
  public void setUp() {
    ed25519KeyPair = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello")).deriveKeyPair();
    assertThat(ed25519KeyPair.publicKey().base16Value()).isEqualTo(
      "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");

    ed25519KeyPairOther = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello_other")).deriveKeyPair();
    assertThat(ed25519KeyPair.publicKey().base16Value()).isEqualTo(
      "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");

    secp256k1KeyPair = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello")).deriveKeyPair();
    assertThat(secp256k1KeyPair.publicKey().base16Value()).isEqualTo(
      "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9");
    secp256k1KeyPairOther = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello_other")).deriveKeyPair();

    assertThat(secp256k1KeyPairOther.publicKey().base16Value()).isEqualTo(
      "02576CB07495A6A03F1B5BD812721E2358304A6FA31ADF7781B8A0F9F59D3726DC");

    this.payment = Payment.builder().account(ed25519KeyPair.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.of(UnsignedLong.valueOf(10L))).sequence(UnsignedInteger.ONE)
      .destination(secp256k1KeyPair.publicKey().deriveAddress()).amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(ed25519KeyPair.publicKey().base16Value()).build();

    this.signatureService = new BcSignatureService();
  }

  ////////////////
  // Sign & Verify
  ////////////////

  @Test
  void signAndVerifyEd() {
    SingleSignedTransaction<Transaction> signedTransaction = signatureService.sign(ed25519KeyPair.privateKey(),
      payment);
    assertThat(signedTransaction).isNotNull();
    assertThat(signedTransaction.unsignedTransaction()).isEqualTo(payment);

    boolean actual = signatureService.verify(
      SignatureWithPublicKey.builder().transactionSignature(signedTransaction.signature())
        .signingPublicKey(ed25519KeyPair.publicKey()).build(), payment);
    assertThat(actual).isTrue();

    actual = signatureService.verify(
      SignatureWithPublicKey.builder().transactionSignature(signedTransaction.signature())
        .signingPublicKey(ed25519KeyPairOther.publicKey()).build(), payment);
    assertThat(actual).isFalse();

  }

  @Test
  void signAndVerifyEc() {
    SingleSignedTransaction<Transaction> signedTransaction = signatureService.sign(secp256k1KeyPair.privateKey(),
      payment);
    assertThat(signedTransaction).isNotNull();

    boolean actual = signatureService.verify(
      SignatureWithPublicKey.builder().transactionSignature(signedTransaction.signature())
        .signingPublicKey(secp256k1KeyPair.publicKey()).build(), payment);
    assertThat(actual).isTrue();

    actual = signatureService.verify(
      SignatureWithPublicKey.builder().transactionSignature(signedTransaction.signature())
        .signingPublicKey(ed25519KeyPair.publicKey()).build(), payment);
    assertThat(actual).isFalse();
  }

  @Test
  void multiSignAndVerifyEd() {
    final Signature signature = signatureService.multiSign(ed25519KeyPair.privateKey(), payment);
    assertThat(signature).isNotNull();

    boolean actual = signatureService.verifyMultiSigned(Sets.newLinkedHashSet(
      SignatureWithPublicKey.builder().transactionSignature(signature).signingPublicKey(ed25519KeyPair.publicKey())
        .build()), payment, 1);
    assertThat(actual).isTrue();

    actual = signatureService.verifyMultiSigned(Sets.newLinkedHashSet(
      SignatureWithPublicKey.builder().transactionSignature(signature).signingPublicKey(ed25519KeyPairOther.publicKey())
        .build()), payment, 1);
    assertThat(actual).isFalse();
  }

  @Test
  void multiSignAndVerifyEc() {
    final Signature signature = signatureService.multiSign(secp256k1KeyPair.privateKey(), payment);
    assertThat(signature).isNotNull();

    boolean actual = signatureService.verifyMultiSigned(Sets.newLinkedHashSet(
      SignatureWithPublicKey.builder().transactionSignature(signature).signingPublicKey(secp256k1KeyPair.publicKey())
        .build()), payment, 1);
    assertThat(actual).isTrue();

    actual = signatureService.verifyMultiSigned(Sets.newLinkedHashSet(
      SignatureWithPublicKey.builder().transactionSignature(signature)
        .signingPublicKey(secp256k1KeyPairOther.publicKey()).build()), payment, 1);
    assertThat(actual).isFalse();
  }

  @Test
  void signAndVerifyEdMultithreaded() {
    final Callable<Boolean> signedTxCallable = () -> {
      SingleSignedTransaction<Payment> signedTx = signatureService.sign(ed25519KeyPair.privateKey(), payment);
      return signatureService.verify(
        SignatureWithPublicKey.builder().transactionSignature(signedTx.signature())
          .signingPublicKey(ed25519KeyPair.publicKey()).build(), payment);
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  @Test
  void multiSignAndVerifyEdMultiThreaded() {
    ///////////////////////
    // Create Three Signers
    final KeyPair keyPair1 = Seed.ed25519SeedFromPassphrase(Passphrase.of("user1")).deriveKeyPair();
    final KeyPair keyPair2 = Seed.ed25519SeedFromPassphrase(Passphrase.of("user2")).deriveKeyPair();
    final KeyPair keyPair3 = Seed.ed25519SeedFromPassphrase(Passphrase.of("user3")).deriveKeyPair();

    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature1 = signatureService.multiSign(keyPair1.privateKey(), payment);
      Signature signature2 = signatureService.multiSign(keyPair2.privateKey(), payment);
      Signature signature3 = signatureService.multiSign(keyPair3.privateKey(), payment);

      Set<SignatureWithPublicKey> sigsWithKeys = Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder().signingPublicKey(keyPair1.publicKey()).transactionSignature(signature1)
          .build(),
        SignatureWithPublicKey.builder().signingPublicKey(keyPair2.publicKey()).transactionSignature(signature2)
          .build(),
        SignatureWithPublicKey.builder().signingPublicKey(keyPair3.publicKey()).transactionSignature(signature3)
          .build());

      boolean noSigners = signatureService.verifyMultiSigned(Sets.newHashSet(), payment, 3);

      boolean oneOfThree = signatureService.verifyMultiSigned(sigsWithKeys, payment, 1);

      boolean twoOfThree = signatureService.verifyMultiSigned(sigsWithKeys, payment, 2);

      boolean threeOfThree = signatureService.verifyMultiSigned(sigsWithKeys, payment, 3);

      return !noSigners && oneOfThree && twoOfThree && threeOfThree;
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  @Test
  void multiSignAndVerifyEcMultiThreaded() {
    ///////////////////////
    // Create Three Signers
    final KeyPair keyPair1 = Seed.secp256k1SeedFromPassphrase(Passphrase.of("user1")).deriveKeyPair();
    final KeyPair keyPair2 = Seed.secp256k1SeedFromPassphrase(Passphrase.of("user2")).deriveKeyPair();
    final KeyPair keyPair3 = Seed.secp256k1SeedFromPassphrase(Passphrase.of("user3")).deriveKeyPair();

    final Callable<Boolean> signedTxCallable = () -> {
      Signature signesignature1 = signatureService.multiSign(keyPair1.privateKey(), payment);
      Signature signature2 = signatureService.multiSign(keyPair2.privateKey(), payment);
      Signature signature3 = signatureService.multiSign(keyPair3.privateKey(), payment);

      Set<SignatureWithPublicKey> sigsWithKeys = Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder().signingPublicKey(keyPair1.publicKey()).transactionSignature(signesignature1)
          .build(),
        SignatureWithPublicKey.builder().signingPublicKey(keyPair2.publicKey()).transactionSignature(signature2)
          .build(),
        SignatureWithPublicKey.builder().signingPublicKey(keyPair3.publicKey()).transactionSignature(signature3)
          .build());

      boolean noSigners = signatureService.verifyMultiSigned(Sets.newHashSet(), payment, 3);

      boolean oneOfThree = signatureService.verifyMultiSigned(sigsWithKeys, payment, 1);

      boolean twoOfThree = signatureService.verifyMultiSigned(sigsWithKeys, payment, 2);

      boolean threeOfThree = signatureService.verifyMultiSigned(sigsWithKeys, payment, 3);

      return !noSigners && oneOfThree && twoOfThree && threeOfThree;
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  @Test
  void signAndVerifyEcMultiThreaded() {
    final Callable<Boolean> signedTxCallable = () -> {

      SingleSignedTransaction<Payment> signedTx = signatureService.sign(secp256k1KeyPair.privateKey(), payment);
      return signatureService.verify(
        SignatureWithPublicKey.builder().transactionSignature(signedTx.signature())
          .signingPublicKey(secp256k1KeyPair.publicKey()).build(), payment);
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  /**
   * Helper method to execute the same callable in parallel.
   *
   * @param signedTxCallable A {@link Callable} that returns a {@link Boolean}.
   */
  private void multiThreadedHelper(final Callable<Boolean> signedTxCallable) {
    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 500; i++) {
      futureSeeds.add(pool.submit(signedTxCallable));
    }

    futureSeeds.stream().map($ -> {
      try {
        return $.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }).forEach(validSig -> assertThat(validSig).isTrue());
  }
}
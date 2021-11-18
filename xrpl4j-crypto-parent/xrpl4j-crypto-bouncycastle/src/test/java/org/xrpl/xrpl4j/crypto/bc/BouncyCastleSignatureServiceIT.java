package org.xrpl.xrpl4j.crypto.bc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
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
 * An Integration Test for {@link BouncyCastleSignatureService} that use actual implementations.
 */
class BouncyCastleSignatureServiceIT {

  private Wallet ed25519Wallet;
  private Wallet ed25519WalletOther;

  private Wallet secp256k1Wallet;
  private Wallet secp256k1WalletOther;

  private Payment payment;

  private BouncyCastleSignatureService signatureService;

  @BeforeEach
  public void setUp() {
    final Ed25519KeyPairService ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    final KeyPair ed25519KeyPair = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))
    );
    assertThat(ed25519KeyPair.publicKey().base16Value())
      .isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");

    final KeyPair ed25519KeyPairOther = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("hello_other"))
    );
    assertThat(ed25519KeyPair.publicKey().base16Value())
      .isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");

    Secp256k1KeyPairService secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();
    KeyPair secp256k1KeyPair = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"))
    );
    assertThat(secp256k1KeyPair.publicKey().base16Value())
      .isEqualTo("027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9");
    KeyPair secp256k1KeyPairOther = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello_other"))
    );
    assertThat(secp256k1KeyPairOther.publicKey().base16Value())
      .isEqualTo("02576CB07495A6A03F1B5BD812721E2358304A6FA31ADF7781B8A0F9F59D3726DC");

    ed25519Wallet = DefaultWalletFactory.getInstance().fromKeyPair(ed25519KeyPair);
    ed25519WalletOther = DefaultWalletFactory.getInstance().fromKeyPair(ed25519KeyPairOther);
    secp256k1Wallet = DefaultWalletFactory.getInstance().fromKeyPair(secp256k1KeyPair);
    secp256k1WalletOther = DefaultWalletFactory.getInstance().fromKeyPair(secp256k1KeyPairOther);

    this.payment = Payment.builder()
      .account(ed25519Wallet.address())
      .fee(XrpCurrencyAmount.of(UnsignedLong.valueOf(10L)))
      .sequence(UnsignedInteger.ONE)
      .destination(secp256k1Wallet.address())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(ed25519Wallet.publicKey().base16Value())
      .build();

    this.signatureService = new BouncyCastleSignatureService();
  }

  ////////////////
  // Sign & Verify
  ////////////////

  @Test
  void signAndVerifyEd() {
    SingleSingedTransaction<Transaction> signedTransaction = signatureService.sign(
      ed25519Wallet.privateKey(),
      payment
    );
    assertThat(signedTransaction).isNotNull();
    assertThat(signedTransaction.unsignedTransaction()).isEqualTo(payment);

    boolean actual = signatureService.verify(
      SignatureWithPublicKey.builder()
        .transactionSignature(signedTransaction.signature())
        .signingPublicKey(ed25519Wallet.publicKey())
        .build(),
      payment
    );
    assertThat(actual).isTrue();

    actual = signatureService.verify(
      SignatureWithPublicKey.builder()
        .transactionSignature(signedTransaction.signature())
        .signingPublicKey(ed25519WalletOther.publicKey())
        .build(),
      payment
    );
    assertThat(actual).isFalse();

  }

  @Test
  void signAndVerifyEc() {
    SingleSingedTransaction<Transaction> signedTransaction = signatureService.sign(
      secp256k1Wallet.privateKey(),
      payment
    );
    assertThat(signedTransaction).isNotNull();

    boolean actual = signatureService.verify(
      SignatureWithPublicKey.builder()
        .transactionSignature(signedTransaction.signature())
        .signingPublicKey(secp256k1Wallet.publicKey())
        .build(),
      payment
    );
    assertThat(actual).isTrue();

    actual = signatureService.verify(
      SignatureWithPublicKey.builder()
        .transactionSignature(signedTransaction.signature())
        .signingPublicKey(ed25519Wallet.publicKey())
        .build(),
      payment
    );
    assertThat(actual).isFalse();
  }

  @Test
  void multiSignAndVerifyEd() {
    final Signature signature = signatureService.multiSign(
      ed25519Wallet.privateKey(),
      payment
    );
    assertThat(signature).isNotNull();

    boolean actual = signatureService.verify(
      Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder()
          .transactionSignature(signature)
          .signingPublicKey(ed25519Wallet.publicKey())
          .build()
      ),
      payment,
      1
    );
    assertThat(actual).isTrue();

    actual = signatureService.verify(
      Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder()
          .transactionSignature(signature)
          .signingPublicKey(ed25519WalletOther.publicKey())
          .build()
      ),
      payment,
      1
    );
    assertThat(actual).isFalse();
  }

  @Test
  void multiSignAndVerifyEc() {
    final Signature signature = signatureService.multiSign(
      secp256k1Wallet.privateKey(),
      payment
    );
    assertThat(signature).isNotNull();

    boolean actual = signatureService.verify(
      Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder()
          .transactionSignature(signature)
          .signingPublicKey(secp256k1Wallet.publicKey())
          .build()
      ),
      payment,
      1
    );
    assertThat(actual).isTrue();

    actual = signatureService.verify(
      Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder()
          .transactionSignature(signature)
          .signingPublicKey(secp256k1WalletOther.publicKey())
          .build()
      ),
      payment,
      1
    );
    assertThat(actual).isFalse();
  }

  @Test
  void signAndVerifyEdMultithreaded() {
    final Callable<Boolean> signedTxCallable = () -> {
      SingleSingedTransaction<Payment> signedTx = signatureService.sign(ed25519Wallet.privateKey(), payment);
      return signatureService.verify(
        SignatureWithPublicKey.builder()
          .transactionSignature(signedTx.signature())
          .signingPublicKey(ed25519Wallet.publicKey())
          .build(),
        payment);
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  @Test
  void multiSignAndVerifyEdMultiThreaded() {
    ///////////////////////
    // Create Three Signers
    final Ed25519KeyPairService ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    final KeyPair keyPair1 = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("user1"))
    );
    final KeyPair keyPair2 = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("user2"))
    );
    final KeyPair keyPair3 = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("user3"))
    );

    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature1 = signatureService.multiSign(
        keyPair1.privateKey(), payment
      );
      Signature signature2 = signatureService.multiSign(
        keyPair2.privateKey(), payment
      );
      Signature signature3 = signatureService.multiSign(
        keyPair3.privateKey(), payment
      );

      Set<SignatureWithPublicKey> sigsWithKeys = Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder()
          .signingPublicKey(keyPair1.publicKey())
          .transactionSignature(signature1)
          .build(),
        SignatureWithPublicKey.builder()
          .signingPublicKey(keyPair2.publicKey())
          .transactionSignature(signature2)
          .build(),
        SignatureWithPublicKey.builder()
          .signingPublicKey(keyPair3.publicKey())
          .transactionSignature(signature3)
          .build()
      );

      boolean noSigners = signatureService.verify(
        Sets.newHashSet(),
        payment,
        3
      );

      boolean oneOfThree = signatureService.verify(
        sigsWithKeys,
        payment,
        1
      );

      boolean twoOfThree = signatureService.verify(
        sigsWithKeys,
        payment,
        2
      );

      boolean threeOfThree = signatureService.verify(
        sigsWithKeys,
        payment,
        3
      );

      return !noSigners && oneOfThree && twoOfThree && threeOfThree;
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  @Test
  void multiSignAndVerifyEcMultiThreaded() {
    ///////////////////////
    // Create Three Signers
    final Secp256k1KeyPairService secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();
    final KeyPair keyPair1 = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("user1"))
    );
    final KeyPair keyPair2 = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("user2"))
    );
    final KeyPair keyPair3 = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("user3"))
    );

    final Callable<Boolean> signedTxCallable = () -> {
      Signature signesignature1 = signatureService.multiSign(
        keyPair1.privateKey(), payment
      );
      Signature signature2 = signatureService.multiSign(
        keyPair2.privateKey(), payment
      );
      Signature signature3 = signatureService.multiSign(
        keyPair3.privateKey(), payment
      );

      Set<SignatureWithPublicKey> sigsWithKeys = Sets.newLinkedHashSet(
        SignatureWithPublicKey.builder()
          .signingPublicKey(keyPair1.publicKey())
          .transactionSignature(signesignature1)
          .build(),
        SignatureWithPublicKey.builder()
          .signingPublicKey(keyPair2.publicKey())
          .transactionSignature(signature2)
          .build(),
        SignatureWithPublicKey.builder()
          .signingPublicKey(keyPair3.publicKey())
          .transactionSignature(signature3)
          .build()
      );

      boolean noSigners = signatureService.verify(
        Sets.newHashSet(),
        payment,
        3
      );

      boolean oneOfThree = signatureService.verify(
        sigsWithKeys,
        payment,
        1
      );

      boolean twoOfThree = signatureService.verify(
        sigsWithKeys,
        payment,
        2
      );

      boolean threeOfThree = signatureService.verify(
        sigsWithKeys,
        payment,
        3
      );

      return !noSigners && oneOfThree && twoOfThree && threeOfThree;
    };
    this.multiThreadedHelper(signedTxCallable);
  }

  @Test
  void signAndVerifyEcMultiThreaded() {
    final Callable<Boolean> signedTxCallable = () -> {

      SingleSingedTransaction<Payment> signedTx = signatureService.sign(secp256k1Wallet.privateKey(), payment);
      return signatureService.verify(
        SignatureWithPublicKey.builder()
          .transactionSignature(signedTx.signature())
          .signingPublicKey(secp256k1Wallet.publicKey())
          .build(),
        payment
      );
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

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(validSig -> assertThat(validSig).isTrue());
  }
}
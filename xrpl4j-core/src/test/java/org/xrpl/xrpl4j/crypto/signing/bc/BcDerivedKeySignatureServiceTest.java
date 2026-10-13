package org.xrpl.xrpl4j.crypto.signing.bc;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.crypto.ServerSecret;
import org.xrpl.xrpl4j.crypto.ServerSecretSupplier;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.AddressConstants;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.flags.BatchFlags;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.AttestationClaim;
import org.xrpl.xrpl4j.model.ledger.AttestationCreateAccount;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Amount;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.RawTransactionWrapper;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Unit tests for {@link BcDerivedKeySignatureService}.
 */
class BcDerivedKeySignatureServiceTest {

  private static final String sourceClassicAddressEd = "rLg3vY8w2tTZz1WVYjub32V4SGynLWnNRw";
  private static final String sourceClassicAddressEc = "rDt78kzcAfRf5NwmwL4f3E5pK14iM4CxRi";

  // Dest address
  private static final String destinationClassicAddress = "rKdi2esXfU7VmZyvRtMKZFFMVESBLE1iiw";

  private BcDerivedKeySignatureService derivedKeySignatureService;

  @BeforeEach
  public void setUp() {
    final ServerSecretSupplier serverSecretSupplier = () -> ServerSecret.of("happy".getBytes(StandardCharsets.UTF_8));
    this.derivedKeySignatureService = new BcDerivedKeySignatureService(serverSecretSupplier);
  }

  @Test
  void constructorWithNulls() {
    // 2-arg Constructor
    assertThrows(NullPointerException.class, () -> new BcDerivedKeySignatureService(null));

    // 2-arg Constructor
    assertThrows(NullPointerException.class, () -> new BcDerivedKeySignatureService(
      null,
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new BcDerivedKeySignatureService(
      () -> ServerSecret.of(new byte[32]),
      null
    ));
  }

  @Test
  void constructorWithExternalCaffeineConfig() {
    // This test merely assert that construction succeeds.
    new BcDerivedKeySignatureService(
      () -> ServerSecret.of(new byte[32]),
      CaffeineSpec.parse("maximumSize=200,expireAfterWrite=300s")
    );
  }

  /**
   * Note: this test runs in a loop solely to exercise concurrent correctness.
   */
  @Test
  void signAndVerifyEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      SingleSignedTransaction<Payment> signedTx = this.derivedKeySignatureService.sign(privateKeyReference,
        paymentTransaction);
      return this.derivedKeySignatureService.verify(
        Signer.builder()
          .transactionSignature(signedTx.signature())
          .signingPublicKey(publicKey)
          .build(),
        paymentTransaction
      );
    };

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

  @Test
  void signAndVerifyEdFailure() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    SingleSignedTransaction<Payment> signedTx = this.derivedKeySignatureService.sign(
      privateKeyReference, paymentTransaction
    );
    final boolean verified = this.derivedKeySignatureService.verify(
      Signer.builder()
        .transactionSignature(Signature.builder().from(signedTx.signature())
          .value(UnsignedByteArray.fromHex("00000000000000000000000000000000"))
          .build()
        )
        .signingPublicKey(publicKey)
        .build(),
      paymentTransaction
    );

    assertThat(verified).isFalse();
  }


  /**
   * Note: this test runs in a loop solely to exercise concurrency correctness.
   */
  @Test
  void multiSignAndVerifyEd() {
    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    final PrivateKeyReference privateKeyReferenceFoo = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKeyFoo = this.derivedKeySignatureService.derivePublicKey(privateKeyReferenceFoo);
    final PrivateKeyReference privateKeyReferenceBar = privateKeyReference("bar", KeyType.ED25519);
    final PublicKey publicKeyBar = this.derivedKeySignatureService.derivePublicKey(privateKeyReferenceBar);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallableFoo = () -> {
      Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReferenceFoo, payment);
      assertThat(signature.base16Value()).isEqualTo(
        "5970DC88AE8E364856CD9BB0920EF850CC34A61B3BD41F10114944FF9DA8C16EC3397033C2E02DCC18F74D1727B8FBD760BDC0165F" +
          "4AF27D03A5C3980FDEFB08"
      );

      boolean result = this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(Signer.builder()
          .transactionSignature(signature)
          .signingPublicKey(publicKeyFoo)
          .build()),
        payment,
        1
      );
      assertThat(result).isTrue();

      result = this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(Signer.builder()
          .transactionSignature(signature)
          .signingPublicKey(publicKeyFoo)
          .build()),
        payment,
        2
      );
      assertThat(result).isFalse();

      return true;
    };

    final Callable<Boolean> signedTxCallableBar = () -> {
      Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReferenceBar, payment);
      assertThat(signature.base16Value()).isEqualTo(
        "DF221CF7BFE4A1C77D929A490A2C9576D838730E0D6110B4D45DB65AB40FB323A8ECEEF08ED8CB7E585EEDCF76BF3F90A9D7AB1B6" +
          "EEE7A534BC1AD0808C0E609"
      );

      boolean result = this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(Signer.builder()
          .transactionSignature(signature)
          .signingPublicKey(publicKeyBar)
          .build()),
        payment,
        1);
      assertThat(result).isTrue();

      result = this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(Signer.builder()
          .transactionSignature(signature)
          .signingPublicKey(publicKeyBar)
          .build()),
        payment,
        2
      );
      assertThat(result).isFalse();

      return true;
    };

    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 500; i++) {
      futureSeeds.add(pool.submit(signedTxCallableFoo));
      futureSeeds.add(pool.submit(signedTxCallableBar));
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

  /**
   * Note: this test runs in a loop solely to exercise concurrent correctness.
   */
  @Test
  void multiSignEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);

      assertThat(signature.base16Value()).isEqualTo(
        "3045022100CE588A59AFB33317A606E977B99E3A855143D7D16451B0691FBBA99981FC4C4702205287A29C0FCF47ED3564AAEE72CB" +
          "543237EC54D3483515A199ED55471889F87C"
      );

      boolean result = this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(Signer.builder()
          .transactionSignature(signature)
          .signingPublicKey(publicKey)
          .build()), payment, 1);
      assertThat(result).isTrue();

      result = this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(Signer.builder()
          .transactionSignature(signature)
          .signingPublicKey(publicKey)
          .build()),
        payment,
        2);
      assertThat(result).isFalse();

      return true;
    };

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

  @Test
  void signAndVerifyEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {

      SingleSignedTransaction<Payment> signedTx
        = this.derivedKeySignatureService.sign(privateKeyReference, paymentTransaction);
      return this.derivedKeySignatureService.verify(
        Signer.builder()
          .transactionSignature(signedTx.signature())
          .signingPublicKey(publicKey)
          .build(),
        paymentTransaction
      );
    };

    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 300; i++) {
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

  @Test
  void signAndVerifyEcFailure() {
    final PrivateKeyReference privateKeyReferenceFoo = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKeyFoo = this.derivedKeySignatureService.derivePublicKey(privateKeyReferenceFoo);

    final PrivateKeyReference privateKeyReferenceBar = privateKeyReference("bar", KeyType.SECP256K1);
    final PublicKey publicKeyBar = this.derivedKeySignatureService.derivePublicKey(privateKeyReferenceBar);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKeyFoo)
      .build();
    SingleSignedTransaction<Payment> signedTx
      = this.derivedKeySignatureService.sign(privateKeyReferenceFoo, paymentTransaction);

    final boolean verified = this.derivedKeySignatureService.verify(
      Signer.builder()
        .transactionSignature(signedTx.signature())
        .signingPublicKey(publicKeyBar)
        .build(),
      paymentTransaction
    );
    assertThat(verified).isFalse();
  }

  @Test
  void getPublicKeyEd() {
    PublicKey actualEcPublicKey = this.derivedKeySignatureService.derivePublicKey(
      privateKeyReference("ed_key", KeyType.ED25519)
    );
    assertThat(actualEcPublicKey.base16Value())
      .isEqualTo("ED9909CDE4F59EA84686FCEE2149BE37CC05317F6C4F1434D96EE0E476F78C4C70");
    assertThat(actualEcPublicKey.base58Value()).isEqualTo("aKEvqcjfwFvcRSUd6fF5QL6N14xxNzNMDVZ2xmspAknpzf2LJfTy");
    assertThat(actualEcPublicKey.keyType()).isEqualTo(KeyType.ED25519);
  }

  @Test
  void getPublicKeyEc() {
    PublicKey actualEcPublicKey = this.derivedKeySignatureService.derivePublicKey(
      privateKeyReference("ec_key", KeyType.SECP256K1));
    assertThat(actualEcPublicKey.base16Value())
      .isEqualTo("021ABFB4DDB4F25162D858BD02289D5B7D0F4D143082C1781DEFBC5EF9662E6263");
    assertThat(actualEcPublicKey.base58Value()).isEqualTo("aB4wHG4rW8bF9HSXg6Q7BNxddwNQ1EtvgF2AyPqQyeAgZ27tQxSP");
    assertThat(actualEcPublicKey.keyType()).isEqualTo(KeyType.SECP256K1);
  }

  @Test
  void signUnsignedClaimEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);

    final UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .channel(Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString()))
      .amount(XrpCurrencyAmount.of(UnsignedLong.ONE))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sign(privateKeyReference, unsignedClaim);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "2600C6672DF81452E2FE3CBE2D1DC45000F7C1380C43CE3AC24591A43060EE82E7B1EF65B933786D40BF66B82019E4E1EB1B0" +
          "434705410EFE956E9E213267109"
      );
      return true;
    };

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

  @Test
  void signUnsignedClaimEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);

    final UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .channel(Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString()))
      .amount(XrpCurrencyAmount.of(UnsignedLong.ONE))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sign(privateKeyReference, unsignedClaim);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "304402201D8C29FF455AFCD80F09892057B7A2A2E956A2B4B505B46722AC14ED3D6ACC5B02204EB2DF84D97AF5C4A83" +
          "3D2BA5442F6D906BDE466C32A9E58A5474A0CEA6B4534"
      );
      return true;
    };

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

  @Test
  void signAttestationClaimEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);

    final AttestationClaim unsignedAttestation = AttestationClaim.builder()
      .xChainBridge(
        XChainBridge.builder()
          .lockingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .lockingChainIssue(Issue.XRP)
          .issuingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .issuingChainIssue(Issue.XRP)
          .build()
      )
      .otherChainSource(AddressConstants.GENESIS_ACCOUNT)
      .amount(XrpCurrencyAmount.ofDrops(10))
      .attestationRewardAccount(AddressConstants.GENESIS_ACCOUNT)
      .wasLockingChainSend(true)
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .destination(AddressConstants.GENESIS_ACCOUNT)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sign(privateKeyReference, unsignedAttestation);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "C436AA1F579C2ADCE04143A8BCF77C8A10BD2EA2ADD9989FD381AD65123C977294C248157686149D9D8552C2A35A90" +
          "28D577B244CE079020372A229D06D03504"
      );
      return true;
    };

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

  @Test
  void signAttestationClaimEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);

    final AttestationClaim unsignedAttestation = AttestationClaim.builder()
      .xChainBridge(
        XChainBridge.builder()
          .lockingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .lockingChainIssue(Issue.XRP)
          .issuingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .issuingChainIssue(Issue.XRP)
          .build()
      )
      .otherChainSource(AddressConstants.GENESIS_ACCOUNT)
      .amount(XrpCurrencyAmount.ofDrops(10))
      .attestationRewardAccount(AddressConstants.GENESIS_ACCOUNT)
      .wasLockingChainSend(true)
      .xChainClaimId(XChainClaimId.of(UnsignedLong.ONE))
      .destination(AddressConstants.GENESIS_ACCOUNT)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sign(privateKeyReference, unsignedAttestation);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "30440220078E2379E68E59D60DFF4054FE0F988A95595E7A0DB2DB7215A3B7C03232CC7C022021BDB527050084BD9" +
          "533BD21FAC0ABB63FD21754AC87C5F580AC583DDF1A9740"
      );
      return true;
    };

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

  @Test
  void signAttestationCreateAccountEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);

    final AttestationCreateAccount unsignedAttestation = AttestationCreateAccount.builder()
      .xChainBridge(
        XChainBridge.builder()
          .lockingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .lockingChainIssue(Issue.XRP)
          .issuingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .issuingChainIssue(Issue.XRP)
          .build()
      )
      .otherChainSource(AddressConstants.GENESIS_ACCOUNT)
      .amount(XrpCurrencyAmount.ofDrops(10))
      .attestationRewardAccount(AddressConstants.GENESIS_ACCOUNT)
      .wasLockingChainSend(true)
      .destination(AddressConstants.GENESIS_ACCOUNT)
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ONE))
      .signatureReward(XrpCurrencyAmount.ofDrops(200))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sign(privateKeyReference, unsignedAttestation);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "D44CFFD228B1A17DEC47433F4E14DD3FD844513129EB68725D45A3D9FA89AFDD45C433579BC9B31FFC109181" +
          "AF565BD63B49011BC6784F9861C33BD7B1235007"
      );
      return true;
    };

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

  @Test
  void signAttestationCreateAccountEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);

    final AttestationCreateAccount unsignedAttestation = AttestationCreateAccount.builder()
      .xChainBridge(
        XChainBridge.builder()
          .lockingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .lockingChainIssue(Issue.XRP)
          .issuingChainDoor(AddressConstants.GENESIS_ACCOUNT)
          .issuingChainIssue(Issue.XRP)
          .build()
      )
      .otherChainSource(AddressConstants.GENESIS_ACCOUNT)
      .amount(XrpCurrencyAmount.ofDrops(10))
      .attestationRewardAccount(AddressConstants.GENESIS_ACCOUNT)
      .wasLockingChainSend(true)
      .destination(AddressConstants.GENESIS_ACCOUNT)
      .xChainAccountCreateCount(XChainCount.of(UnsignedLong.ONE))
      .signatureReward(XrpCurrencyAmount.ofDrops(200))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sign(privateKeyReference, unsignedAttestation);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "3044022024A4E54773DCC751082D2F9DAEC60C06960E2DE40BC2896B9690D917DA310EB902202547C4569E817" +
          "C4505C4B672D65CF24B93B3EB6748564E819EB26E8ED5A20790"
      );
      return true;
    };

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

  @Test
  void generateEd25519XrplSeed() {
    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Seed> seedCheck = () -> this.derivedKeySignatureService.generateEd25519XrplSeed("test_account");

    final List<Future<Seed>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      futureSeeds.add(pool.submit(seedCheck));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(seed -> {
        // Expect 16 bytes for the bytes()
        assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("7D4F3F711A719BDA9FEC7359DA96D0F7");

        UnsignedByteArray entropy = seed.decodedSeed().bytes();
        String expectedBase58 = AddressBase58.encode(
          entropy,
          Lists.newArrayList(Version.ED25519_SEED),
          UnsignedInteger.valueOf(entropy.length())
        );
        assertThat(expectedBase58).isEqualTo("sEd7sYDb1EARo6GFwHFnW3ShnefjGKW");
      });
  }

  @Test
  void generateSecp256k1Seed() {
    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Seed> seedCheck = () -> this.derivedKeySignatureService.generateSecp256k1Seed("test_account");

    final List<Future<Seed>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      futureSeeds.add(pool.submit(seedCheck));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      //.forEach(seed -> assertThat(seed.value()).isEqualTo("shSZD6BGMy5Pv8RhtvDuVXZGGjt9m"));
      .forEach(seed -> {
        // Expect 16 bytes for the bytes()
        assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("7D4F3F711A719BDA9FEC7359DA96D0F7");

        UnsignedByteArray entropy = seed.decodedSeed().bytes();
        String expectedBase58 = AddressBase58.encode(
          entropy,
          Lists.newArrayList(Version.FAMILY_SEED),
          UnsignedInteger.valueOf(entropy.length())
        );
        assertThat(expectedBase58).isEqualTo("shSZD6BGMy5Pv8RhtvDuVXZGGjt9m");
      });
  }

  @Test
  void signInnerEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Batch batchTransaction = createBatchTransaction(publicKey);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedBatchCallable = () -> {
      Signature signature = this.derivedKeySignatureService.signInner(
        privateKeyReference, batchTransaction, publicKey.deriveAddress()
      );
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isNotEmpty();
      // Verify signature is deterministic
      Signature signature2 = this.derivedKeySignatureService.signInner(
        privateKeyReference, batchTransaction, publicKey.deriveAddress()
      );
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedBatchCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void signInnerEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Batch batchTransaction = createBatchTransaction(publicKey);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedBatchCallable = () -> {
      Signature signature = this.derivedKeySignatureService.signInner(
        privateKeyReference, batchTransaction, publicKey.deriveAddress()
      );
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isNotEmpty();
      // Verify signature is deterministic for SECP256K1
      Signature signature2 = this.derivedKeySignatureService.signInner(
        privateKeyReference, batchTransaction, publicKey.deriveAddress()
      );
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedBatchCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void multiSignInnerEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Batch batchTransaction = createBatchTransaction(publicKey);
    final Address batchSignerAddress = Address.of(sourceClassicAddressEd);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedBatchCallable = () -> {
      Signature signature = this.derivedKeySignatureService.multiSignInner(
        privateKeyReference, batchTransaction, batchSignerAddress
      );
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isNotEmpty();
      // Verify signature is deterministic
      Signature signature2 = this.derivedKeySignatureService.multiSignInner(
        privateKeyReference, batchTransaction, batchSignerAddress
      );
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedBatchCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void multiSignInnerEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Batch batchTransaction = createBatchTransaction(publicKey);
    final Address batchSignerAddress = Address.of(sourceClassicAddressEc);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedBatchCallable = () -> {
      Signature signature = this.derivedKeySignatureService.multiSignInner(
        privateKeyReference, batchTransaction, batchSignerAddress
      );
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isNotEmpty();
      // Verify signature is deterministic for SECP256K1
      Signature signature2 = this.derivedKeySignatureService.multiSignInner(
        privateKeyReference, batchTransaction, batchSignerAddress
      );
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedBatchCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void counterpartySignEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final LoanSet loanSet = createLoanSetTransaction(publicKey);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.counterpartySign(privateKeyReference, loanSet);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "C499CCC65166E3005078416B358EE2185C3DFD5DD2348ABA27EA1EEEFB6D4DCC5609240221EC920E01541296D4D03872B7" +
          "7B39CA59519FF2FCF0A81C9C79A500"
      );
      // Verify signature is deterministic
      Signature signature2 = this.derivedKeySignatureService.counterpartySign(privateKeyReference, loanSet);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void counterpartySignEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final LoanSet loanSet = createLoanSetTransaction(publicKey);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.counterpartySign(privateKeyReference, loanSet);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "30450221009030188E569A0D6E5CC47939D7D97B333860B3E04FFECE15A91F4DACB217AB14022015D86943FCAD7495ABCE" +
          "716DFB0C16AA52575710C92B44ED5D10E34C246FA2EB"
      );
      // Verify signature is deterministic for SECP256K1
      Signature signature2 = this.derivedKeySignatureService.counterpartySign(privateKeyReference, loanSet);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void counterpartyMultiSignEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final LoanSet loanSet = createLoanSetTransaction(publicKey);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.counterpartyMultiSign(privateKeyReference, loanSet);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "74DDE36F5A79E4E00304A5B1273AEB244B0F7E442ABA22512C4D61CF58D7AFDD0617A5BF49A06C9CC8AE144462723A0FC7" +
          "69F714BC13D1D0C285630263BE9006"
      );
      // Verify signature is deterministic
      Signature signature2 = this.derivedKeySignatureService.counterpartyMultiSign(privateKeyReference, loanSet);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void counterpartyMultiSignEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final LoanSet loanSet = createLoanSetTransaction(publicKey);

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.counterpartyMultiSign(privateKeyReference, loanSet);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "304402201D25A0CE2AEFA9CD5EFB82125C1DA001D06BC94A00E4A39B5063CEDB5F70065002205C8D20CAD9BF064BE7934E" +
          "58A933FB9791A600EAC8B1568B9803BD7773DFD501"
      );
      // Verify signature is deterministic for SECP256K1
      Signature signature2 = this.derivedKeySignatureService.counterpartyMultiSign(privateKeyReference, loanSet);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void sponsorSignEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sponsorSign(privateKeyReference, paymentTransaction);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "A8F0B2A3B9D82E59FA542D2D3EED3A4CB93A79A0CAE75BB78610000C22F0C0EDA10540FAC463F68B6B41E78094675DF31EEB8" +
          "17CE7671DBA2160F632B7D24D05"
      );
      // Verify signature is deterministic
      Signature signature2 = this.derivedKeySignatureService.sponsorSign(privateKeyReference, paymentTransaction);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void sponsorSignEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sponsorSign(privateKeyReference, paymentTransaction);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "3044022068850A7470BCDE32C4A1D31A0D921D1D3716FC9E11B24F973ED827BC2153610C02200FF49D0F1B341F46ADAC58B9B14" +
          "0933D8A07ECBBC00234D036A39E7DBE5DC8AC"
      );
      // Verify signature is deterministic for SECP256K1
      Signature signature2 = this.derivedKeySignatureService.sponsorSign(privateKeyReference, paymentTransaction);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void sponsorMultiSignEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sponsorMultiSign(privateKeyReference, paymentTransaction);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "D56AACF5A60B02E6ED36480B5246C517B57886CAF2188D3916F194253DA1D81ED791026D5E624744F2EB9735FC7D2EF59F850" +
          "0180DCA3D1FDE8BD928F0EC7104"
      );
      // Verify signature is deterministic
      Signature signature2 = this.derivedKeySignatureService.sponsorMultiSign(privateKeyReference, paymentTransaction);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void sponsorMultiSignEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey)
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedCallable = () -> {
      Signature signature = this.derivedKeySignatureService.sponsorMultiSign(privateKeyReference, paymentTransaction);
      assertThat(signature).isNotNull();
      assertThat(signature.base16Value()).isEqualTo(
        "3045022100E143879BEBA0179096D485B1864882F010D78E5A4E285EA378587EB2DB9DDC2402202B0B925B0671B34ED95EF162" +
          "94B16579F5EF26A5E80A0D9BE4DBD8B8F48933BD"
      );
      // Verify signature is deterministic for SECP256K1
      Signature signature2 = this.derivedKeySignatureService.sponsorMultiSign(privateKeyReference, paymentTransaction);
      assertThat(signature.base16Value()).isEqualTo(signature2.base16Value());
      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void multiSignToSignerEd() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signer signer = this.derivedKeySignatureService.multiSignToSigner(privateKeyReference, payment);
      assertThat(signer).isNotNull();
      assertThat(signer.signingPublicKey()).isEqualTo(publicKey);
      assertThat(signer.transactionSignature()).isNotNull();
      assertThat(signer.transactionSignature().base16Value()).isNotEmpty();

      // Verify the signer's account is derived from the public key
      assertThat(signer.account()).isEqualTo(publicKey.deriveAddress());

      // Verify signature matches multiSign
      Signature multiSignSignature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);
      assertThat(signer.transactionSignature()).isEqualTo(multiSignSignature);

      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedTxCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void multiSignToSignerEc() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.SECP256K1);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signer signer = this.derivedKeySignatureService.multiSignToSigner(privateKeyReference, payment);
      assertThat(signer).isNotNull();
      assertThat(signer.signingPublicKey()).isEqualTo(publicKey);
      assertThat(signer.transactionSignature()).isNotNull();
      assertThat(signer.transactionSignature().base16Value()).isNotEmpty();

      // Verify the signer's account is derived from the public key
      assertThat(signer.account()).isEqualTo(publicKey.deriveAddress());

      // Verify signature matches multiSign
      Signature multiSignSignature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);
      assertThat(signer.transactionSignature()).isEqualTo(multiSignSignature);

      return true;
    };

    final List<Future<Boolean>> futureResults = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      futureResults.add(pool.submit(signedTxCallable));
    }

    futureResults.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(result -> assertThat(result).isTrue());
  }

  @Test
  void verifyMultiSignedWithZeroMinSigners() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);
    Signer signer = Signer.builder()
      .transactionSignature(signature)
      .signingPublicKey(publicKey)
      .build();

    // Test with minSigners = 0 should throw IllegalArgumentException
    assertThrows(IllegalArgumentException.class, () -> {
      this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(signer),
        payment,
        0
      );
    });
  }

  @Test
  void verifyMultiSignedWithNegativeMinSigners() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);
    Signer signer = Signer.builder()
      .transactionSignature(signature)
      .signingPublicKey(publicKey)
      .build();

    // Test with minSigners = -1 should throw IllegalArgumentException
    assertThrows(IllegalArgumentException.class, () -> {
      this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(signer),
        payment,
        -1
      );
    });
  }

  @Test
  void verifyMultiSignedWithNullSignerSet() {
    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    // Test with null signerSet should throw NullPointerException
    assertThrows(NullPointerException.class, () -> {
      this.derivedKeySignatureService.verifyMultiSigned(
        null,
        payment,
        1
      );
    });
  }

  @Test
  void verifyMultiSignedWithNullTransaction() {
    final PrivateKeyReference privateKeyReference = privateKeyReference("foo", KeyType.ED25519);
    final PublicKey publicKey = this.derivedKeySignatureService.derivePublicKey(privateKeyReference);

    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);
    Signer signer = Signer.builder()
      .transactionSignature(signature)
      .signingPublicKey(publicKey)
      .build();

    // Test with null transaction should throw NullPointerException
    assertThrows(NullPointerException.class, () -> {
      this.derivedKeySignatureService.verifyMultiSigned(
        Sets.newHashSet(signer),
        null,
        1
      );
    });
  }

  /**
   * Regression test for RXB-382: a {@link ServerSecretSupplier} that wraps a persistent, reused {@code byte[]} (the
   * documented usage pattern -- e.g., a secret loaded once from a keystore or environment variable) must keep
   * producing correct, secret-derived keys across multiple derivations. Before the fix, the first derivation would
   * destroy the caller's shared array (via {@link ServerSecret#destroy()} zeroing an un-copied backing array), so any
   * subsequent derivation would silently use an all-zero HMAC key instead of the real secret.
   */
  @Test
  void persistentArrayBackedSupplierProducesCorrectKeysAcrossMultipleDerivations() {
    final byte[] persistentSecret = "a-persistent-server-secret-loaded-once".getBytes(StandardCharsets.UTF_8);
    final byte[] originalSecretCopy = Arrays.copyOf(persistentSecret, persistentSecret.length);

    // Mimics the natural/documented usage pattern: the supplier wraps the *same* backing array on every call.
    final ServerSecretSupplier persistentSupplier = () -> ServerSecret.of(persistentSecret);
    final BcDerivedKeySignatureService serviceUnderTest = new BcDerivedKeySignatureService(persistentSupplier);

    // First derivation. Pre-fix, this would zero out `persistentSecret` as a side-effect.
    final Seed seedA = serviceUnderTest.generateEd25519XrplSeed("identifier-A");
    assertThat(seedA).isNotNull();

    // The caller's persistent array must be untouched by the derivation above.
    assertThat(persistentSecret).isEqualTo(originalSecretCopy);

    // Second derivation, using a different identifier, from the same (still-intact) persistent array.
    final Seed seedB = serviceUnderTest.generateEd25519XrplSeed("identifier-B");

    // Independently derive the expected seed for "identifier-B" from a supplier that returns a fresh copy of the
    // original secret bytes every time (i.e., a supplier immune to the bug even before the fix).
    final BcDerivedKeySignatureService independentService = new BcDerivedKeySignatureService(
      () -> ServerSecret.of(Arrays.copyOf(originalSecretCopy, originalSecretCopy.length))
    );
    final Seed expectedSeedB = independentService.generateEd25519XrplSeed("identifier-B");

    assertThat(seedB.decodedSeed().bytes()).isEqualTo(expectedSeedB.decodedSeed().bytes());

    // Sanity check: this must NOT equal what a zeroed-out secret would derive for "identifier-B" (i.e., the bug).
    final BcDerivedKeySignatureService zeroSecretService = new BcDerivedKeySignatureService(
      () -> ServerSecret.of(new byte[originalSecretCopy.length])
    );
    final Seed zeroSecretSeedB = zeroSecretService.generateEd25519XrplSeed("identifier-B");
    assertThat(seedB.decodedSeed().bytes()).isNotEqualTo(zeroSecretSeedB.decodedSeed().bytes());

    // Repeat the same check for the secp256k1 derivation path, which has the identical destroy()-in-finally pattern.
    final Seed secp256k1SeedC = serviceUnderTest.generateSecp256k1Seed("identifier-C");
    assertThat(persistentSecret).isEqualTo(originalSecretCopy);

    final Seed expectedSecp256k1SeedC = independentService.generateSecp256k1Seed("identifier-C");
    assertThat(secp256k1SeedC.decodedSeed().bytes()).isEqualTo(expectedSecp256k1SeedC.decodedSeed().bytes());

    final Seed zeroSecretSecp256k1SeedC = zeroSecretService.generateSecp256k1Seed("identifier-C");
    assertThat(secp256k1SeedC.decodedSeed().bytes()).isNotEqualTo(zeroSecretSecp256k1SeedC.decodedSeed().bytes());
  }

  /**
   * Regression test for RXB-382, covering a second variant of the same root cause: a {@link ServerSecretSupplier}
   * that caches and returns the exact same {@link ServerSecret} instance on every call (e.g., a singleton Spring
   * bean) must also keep producing correct, secret-derived keys across multiple derivations. This service must never
   * call {@link ServerSecret#destroy()} on the object the supplier returns, since doing so would permanently zero
   * out a secret that's shared across every future derivation.
   */
  @Test
  void cachedServerSecretInstanceSupplierProducesCorrectKeysAcrossMultipleDerivations() {
    final byte[] originalSecret = "another-persistent-secret-value".getBytes(StandardCharsets.UTF_8);
    final ServerSecret cachedServerSecret = ServerSecret.of(originalSecret);

    // Mimics a supplier that always returns the same, already-constructed ServerSecret instance.
    final ServerSecretSupplier cachingSupplier = () -> cachedServerSecret;
    final BcDerivedKeySignatureService serviceUnderTest = new BcDerivedKeySignatureService(cachingSupplier);

    final Seed seedA = serviceUnderTest.generateEd25519XrplSeed("identifier-A");
    assertThat(seedA).isNotNull();
    // Pre-fix, the line above would have destroyed `cachedServerSecret`'s internal state.
    assertThat(cachedServerSecret.isDestroyed()).isFalse();

    final Seed seedB = serviceUnderTest.generateEd25519XrplSeed("identifier-B");

    final BcDerivedKeySignatureService independentService = new BcDerivedKeySignatureService(
      () -> ServerSecret.of(Arrays.copyOf(originalSecret, originalSecret.length))
    );
    final Seed expectedSeedB = independentService.generateEd25519XrplSeed("identifier-B");

    assertThat(seedB.decodedSeed().bytes()).isEqualTo(expectedSeedB.decodedSeed().bytes());
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Helper function to generate Key meta-data based upon the supplied inputs.
   *
   * @param keyIdentifier A {@link String} identifying the key.
   *
   * @return A {@link PrivateKeyReference}.
   */
  private PrivateKeyReference privateKeyReference(final String keyIdentifier, final KeyType keyType) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(keyType);

    return new PrivateKeyReference() {
      @Override
      public String keyIdentifier() {
        return keyIdentifier;
      }

      @Override
      public KeyType keyType() {
        return keyType;
      }
    };
  }

  /**
   * Helper function to create a LoanSet transaction for testing counterpartySign and counterpartyMultiSign.
   *
   * @param publicKey The {@link PublicKey} to use for the transaction.
   *
   * @return A {@link LoanSet} transaction.
   */
  private LoanSet createLoanSetTransaction(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);

    return LoanSet.builder()
      .account(publicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(30))
      .sequence(UnsignedInteger.ONE)
      .loanBrokerId(Hash256.of(com.google.common.base.Strings.padStart("ABC123", 64, '0')))
      .principalRequested(Amount.of("50000"))
      .signingPublicKey(publicKey)
      .build();
  }

  /**
   * Helper function to create a Batch transaction for testing signInner and multiSignInner.
   *
   * @param publicKey The {@link PublicKey} to use for the batch transaction.
   *
   * @return A {@link Batch} transaction.
   */
  private Batch createBatchTransaction(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);

    // Create two inner payment transactions
    final Payment payment1 = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(0)) // Must be 0 for inner transactions
      .sequence(UnsignedInteger.valueOf(1))
      .flags(PaymentFlags.builder().tfInnerBatchTxn(true).build())
      .build();

    final Payment payment2 = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(2000))
      .fee(XrpCurrencyAmount.ofDrops(0)) // Must be 0 for inner transactions
      .sequence(UnsignedInteger.valueOf(2))
      .flags(PaymentFlags.builder().tfInnerBatchTxn(true).build())
      .build();

    return Batch.builder()
      .account(publicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.valueOf(10))
      .flags(BatchFlags.ALL_OR_NOTHING)
      .addRawTransactions(RawTransactionWrapper.of(payment1), RawTransactionWrapper.of(payment2))
      .signingPublicKey(publicKey)
      .build();
  }
}

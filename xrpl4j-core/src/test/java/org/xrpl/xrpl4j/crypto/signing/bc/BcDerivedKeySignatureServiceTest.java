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
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.AttestationClaim;
import org.xrpl.xrpl4j.model.ledger.AttestationCreateAccount;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;
import org.xrpl.xrpl4j.model.transactions.XChainClaimId;
import org.xrpl.xrpl4j.model.transactions.XChainCount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
      .fee(XrpCurrencyAmount.ofDrops(0))
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
        "E2ACD61C90D93433402B1F704DA38DF72876B6788C2C05B3196E14BC711AECFF14A7D6276439A198D8B4880EE2DB544CF351A8CE23" +
          "1B3340F42F9BF1EDBF5104"
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
        "55A7B3AD35E01774A85BBB81958F505C1AF8DB67318420239AAEA32AD4A9D6B6AF920159314D5A5C93490C696C7F2BB3CEA76A4" +
          "6FDF4E03514070FB994EFFF08"
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
      .fee(XrpCurrencyAmount.ofDrops(0))
      .flags(PaymentFlags.of(TransactionFlags.FULLY_CANONICAL_SIG.getValue()))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.derivedKeySignatureService.multiSign(privateKeyReference, payment);

      assertThat(signature.base16Value()).isEqualTo(
        "3045022100ED9BF3764ACF7AFC39E75AEDC5825EF667B498305A469CFCE3CF76E7580CC2F902204A4B1317103459EE777B0406D04ED" +
          "5C60942D962B6FB60BB589E15636817086E"
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
}

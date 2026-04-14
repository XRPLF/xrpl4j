package org.xrpl.xrpl4j.crypto.signing;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_PUBLIC_KEY;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PUBLIC_KEY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.TestConstants;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.ledger.Attestation;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Batch;
import org.xrpl.xrpl4j.model.transactions.LoanSet;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link AbstractTransactionSigner}.
 */
public class AbstractTransactionSignerTest {

  @Mock
  SignatureUtils signatureUtilsMock;
  @Mock
  PrivateKeyable privateKeyableMock;
  @Mock
  PublicKey publicKeyMock;
  @Mock
  Transaction transactionMock;
  @Mock
  Signer signerMock;
  @Mock
  Batch batchMock;
  @Mock
  LoanSet loanSetMock;

  private Signature fauxEd25519Signature;
  private Signature fauxSecp256k1Signature;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private KeyType keyType;

  private AbstractTransactionSigner<PrivateKeyable> transactionSigner;

  @BeforeEach
  void setUp() {
    openMocks(this);

    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    // Create real signature objects instead of mocks
    fauxEd25519Signature = Signature.fromBase16(
      "ED6F91CCF14EE94EB072C7671A397A313E3E5CBDAFE773BB6B2F07A0E75A7E65F84B5516268DAEE12902265256" +
        "EA1EF046B200148E14FF4E720C06519FD7F40F"
    );
    fauxSecp256k1Signature = Signature.fromBase16(
      "304502210093257D8E88D2A92CE55977641F72CCD235AB76B1AE189BE3377F30A69B131C49" +
        "02200B79836114069F0D331418D05818908D85DE755AE5C2DDF42E9637FE1C11754F"
    );

    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toSignableInnerBytes(any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableInnerBytes(any(), any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toCounterpartyMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toSponsorSignableBytes(any())).thenReturn(UnsignedByteArray.empty());

    when(signerMock.signingPublicKey()).thenReturn(publicKeyMock);
    when(signerMock.transactionSignature()).thenReturn(fauxEd25519Signature);

    this.transactionSigner = new AbstractTransactionSigner<PrivateKeyable>(signatureUtilsMock) {
      @Override
      protected Signature edDsaSign(PrivateKeyable privateKeyable, UnsignedByteArray signableTransactionBytes) {
        return fauxEd25519Signature;
      }

      @Override
      protected Signature ecDsaSign(PrivateKeyable privateKeyable, UnsignedByteArray signableTransactionBytes) {
        return fauxSecp256k1Signature;
      }

      @Override
      public PublicKey derivePublicKey(PrivateKeyable privateKeyable) {
        return keyType == KeyType.ED25519 ? ED_PUBLIC_KEY : EC_PUBLIC_KEY;
      }
    };
  }

  // /////////////////
  // Sign (Transaction)
  // ////////////////

  @Test
  void signWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.sign(null, transactionMock));
  }

  @Test
  void signWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> transactionSigner.sign(privateKeyableMock, (Transaction) null));
  }

  @Test
  void signEd25519() {
    final Payment payment = Payment.builder()
      .destination(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(1000))
      .signingPublicKey(ED_PUBLIC_KEY)
      .build();

    keyType = KeyType.ED25519;

    SingleSignedTransaction<Payment> singleSignedTransaction = transactionSigner.sign(privateKeyableMock, payment);

    verify(signatureUtilsMock).toSignableBytes(payment);
    verifyNoMoreInteractions(signatureUtilsMock);

    assertThat(singleSignedTransaction.unsignedTransaction()).isEqualTo(payment);
    assertThat(singleSignedTransaction.signature()).isEqualTo(fauxEd25519Signature);
    assertThat(singleSignedTransaction.signedTransaction().transactionSignature()).contains(fauxEd25519Signature);
  }

  @Test
  void signSecp256k1() {
    final Payment payment = Payment.builder()
      .destination(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(1000))
      .signingPublicKey(ED_PUBLIC_KEY)
      .build();

    keyType = KeyType.SECP256K1;

    SingleSignedTransaction<Payment> singleSignedTransaction = transactionSigner.sign(privateKeyableMock, payment);

    verify(signatureUtilsMock).toSignableBytes(payment);
    verifyNoMoreInteractions(signatureUtilsMock);

    assertThat(singleSignedTransaction.unsignedTransaction()).isEqualTo(payment);
    assertThat(singleSignedTransaction.signature()).isEqualTo(fauxSecp256k1Signature);
    assertThat(singleSignedTransaction.signedTransaction().transactionSignature()).contains(fauxSecp256k1Signature);
  }

  // /////////////////
  // Sign (UnsignedClaim)
  // /////////////////

  @Test
  void signUnsignedClaimWithNullMetadata() {
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    assertThrows(NullPointerException.class, () -> transactionSigner.sign(null, unsignedClaimMock));
  }

  @Test
  void signUnsignedClaimWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> transactionSigner.sign(privateKeyableMock, (UnsignedClaim) null));
  }

  @Test
  void signUnsignedClaimEd25519() {
    keyType = KeyType.ED25519;
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(privateKeyableMock, unsignedClaimMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(fauxEd25519Signature);
  }

  @Test
  void signUnsignedClaimSecp256k1() {
    keyType = KeyType.SECP256K1;
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(privateKeyableMock, unsignedClaimMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(fauxSecp256k1Signature);
  }

  // /////////////////
  // Sign (Attestation)
  // /////////////////

  @Test
  void signAttestationWithNullMetadata() {
    Attestation unsignedAttestationMock = mock(Attestation.class);
    assertThrows(NullPointerException.class, () -> transactionSigner.sign(null, unsignedAttestationMock));
  }

  @Test
  void signAttestationWithNullTransaction() {
    assertThrows(NullPointerException.class, () -> transactionSigner.sign(privateKeyableMock, (Attestation) null));
  }

  @Test
  void signAttestationEd25519() {
    keyType = KeyType.ED25519;
    Attestation unsignedAttestationMock = mock(Attestation.class);
    when(signatureUtilsMock.toSignableBytes(unsignedAttestationMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(privateKeyableMock, unsignedAttestationMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedAttestationMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(fauxEd25519Signature);
  }

  @Test
  void signAttestationClaimSecp256k1() {
    keyType = KeyType.SECP256K1;
    Attestation unsignedAttestationMock = mock(Attestation.class);
    when(signatureUtilsMock.toSignableBytes(unsignedAttestationMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(privateKeyableMock, unsignedAttestationMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedAttestationMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(fauxSecp256k1Signature);
  }

  // /////////////////
  // MultiSign to Signature
  // /////////////////

  @Test
  void multiSignWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.multiSign(null, transactionMock));
  }

  @Test
  void multiSignWithNullTransaction() {
    assertThrows(NullPointerException.class, () -> transactionSigner.multiSign(privateKeyableMock, null));
  }

  @Test
  void multiSignEd25519() {
    keyType = KeyType.ED25519;

    Signature signature = transactionSigner.multiSign(privateKeyableMock, transactionMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.ED_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.multiSign(privateKeyableMock, transactionMock);

    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.EC_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // MultiSign to Signer
  // /////////////////

  @Test
  void multiSignToSignerWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.multiSignToSigner(null, transactionMock));
  }

  @Test
  void multiSignToSignerWithNullTransaction() {
    assertThrows(NullPointerException.class, () -> transactionSigner.multiSignToSigner(privateKeyableMock, null));
  }

  @Test
  void multiSignToSignerEd25519() {
    keyType = KeyType.ED25519;

    Signer signer = transactionSigner.multiSignToSigner(privateKeyableMock, transactionMock);
    assertThat(signer.signingPublicKey()).isEqualTo(ED_PUBLIC_KEY);
    assertThat(signer.account()).isEqualTo(ED_PUBLIC_KEY.deriveAddress());
    assertThat(signer.transactionSignature()).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.ED_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignToSignerSecp256k1() {

    keyType = KeyType.SECP256K1;

    Signer signer = transactionSigner.multiSignToSigner(privateKeyableMock, transactionMock);
    assertThat(signer.signingPublicKey()).isEqualTo(EC_PUBLIC_KEY);
    assertThat(signer.account()).isEqualTo(EC_PUBLIC_KEY.deriveAddress());
    assertThat(signer.transactionSignature()).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.EC_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // SignInner
  // /////////////////

  @Test
  void signInnerWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.signInner(null, batchMock));
  }

  @Test
  void signInnerWithNullBatch() {
    assertThrows(NullPointerException.class, () -> transactionSigner.signInner(privateKeyableMock, null));
  }

  @Test
  void signInnerEd25519() {
    keyType = KeyType.ED25519;

    Signature signature = transactionSigner.signInner(privateKeyableMock, batchMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toSignableInnerBytes(batchMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signInnerSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.signInner(privateKeyableMock, batchMock);
    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toSignableInnerBytes(batchMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // MultiSignInner
  // /////////////////

  @Test
  void multiSignInnerWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.multiSignInner(null, batchMock));
  }

  @Test
  void multiSignInnerWithNullBatch() {
    assertThrows(NullPointerException.class, () -> transactionSigner.multiSignInner(privateKeyableMock, null));
  }

  @Test
  void multiSignInnerEd25519() {
    keyType = KeyType.ED25519;

    Signature signature = transactionSigner.multiSignInner(privateKeyableMock, batchMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toMultiSignableInnerBytes(batchMock, TestConstants.ED_ADDRESS);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignInnerSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.multiSignInner(privateKeyableMock, batchMock);
    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toMultiSignableInnerBytes(batchMock, TestConstants.EC_ADDRESS);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // CounterpartySign
  // /////////////////

  @Test
  void counterpartySignWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.counterpartySign(null, loanSetMock));
  }

  @Test
  void counterpartySignWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> transactionSigner.counterpartySign(privateKeyableMock, null));
  }

  @Test
  void counterpartySignEd25519() {
    keyType = KeyType.ED25519;

    Signature signature = transactionSigner.counterpartySign(privateKeyableMock, loanSetMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toSignableBytes(loanSetMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void counterpartySignSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.counterpartySign(privateKeyableMock, loanSetMock);
    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toSignableBytes(loanSetMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // CounterpartyMultiSign
  // /////////////////

  @Test
  void counterpartyMultiSignWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.counterpartyMultiSign(null, loanSetMock));
  }

  @Test
  void counterpartyMultiSignWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> transactionSigner.counterpartyMultiSign(privateKeyableMock, null));
  }

  @Test
  void counterpartyMultiSignEd25519() {
    keyType = KeyType.ED25519;

    Signature signature = transactionSigner.counterpartyMultiSign(privateKeyableMock, loanSetMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toCounterpartyMultiSignableBytes(loanSetMock, TestConstants.ED_ADDRESS);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void counterpartyMultiSignSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.counterpartyMultiSign(privateKeyableMock, loanSetMock);
    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toCounterpartyMultiSignableBytes(loanSetMock, TestConstants.EC_ADDRESS);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // SponsorSign
  // /////////////////

  @Test
  void sponsorSignWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.sponsorSign(null, transactionMock));
  }

  @Test
  void sponsorSignWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> transactionSigner.sponsorSign(privateKeyableMock, null));
  }

  @Test
  void sponsorSignEd25519() {
    keyType = KeyType.ED25519;

    Signature signature = transactionSigner.sponsorSign(privateKeyableMock, transactionMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toSponsorSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void sponsorSignSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.sponsorSign(privateKeyableMock, transactionMock);
    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toSponsorSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // SponsorMultiSign
  // /////////////////

  @Test
  void sponsorMultiSignWithNullMetadata() {
    assertThrows(NullPointerException.class, () -> transactionSigner.sponsorMultiSign(null, transactionMock));
  }

  @Test
  void sponsorMultiSignWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> transactionSigner.sponsorMultiSign(privateKeyableMock, null));
  }

  @Test
  void sponsorMultiSignEd25519() {
    keyType = KeyType.ED25519;
    when(signatureUtilsMock.toSponsorMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());

    Signature signature = transactionSigner.sponsorMultiSign(privateKeyableMock, transactionMock);
    assertThat(signature).isEqualTo(fauxEd25519Signature);

    verify(signatureUtilsMock).toSponsorMultiSignableBytes(transactionMock, TestConstants.ED_ADDRESS);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void sponsorMultiSignSecp256k1() {
    keyType = KeyType.SECP256K1;
    when(signatureUtilsMock.toSponsorMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());

    Signature signature = transactionSigner.sponsorMultiSign(privateKeyableMock, transactionMock);
    assertThat(signature).isEqualTo(fauxSecp256k1Signature);

    verify(signatureUtilsMock).toSponsorMultiSignableBytes(transactionMock, TestConstants.EC_ADDRESS);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // EdDsaSign
  // /////////////////

  @Test
  void edDsaSign() {
    this.keyType = KeyType.ED25519;

    Signature actual = transactionSigner.edDsaSign(privateKeyableMock, UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(fauxEd25519Signature);
    assertThat(ed25519VerifyCalled.get()).isFalse();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  // /////////////////
  // EcDsaSign
  // /////////////////

  @Test
  void ecDsaSign() {
    this.keyType = KeyType.SECP256K1;

    Signature actual = transactionSigner.ecDsaSign(privateKeyableMock, UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(fauxSecp256k1Signature);

    assertThat(secp256k1VerifyCalled.get()).isFalse();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

}

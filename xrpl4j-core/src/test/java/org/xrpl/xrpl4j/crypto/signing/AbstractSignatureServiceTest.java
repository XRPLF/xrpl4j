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

import org.assertj.core.util.Sets;
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
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link AbstractSignatureService}.
 */
public class AbstractSignatureServiceTest {

  @Mock
  SignatureUtils signatureUtilsMock;
  @Mock
  Signature ed25519SignatureMock;
  @Mock
  Signature secp256k1SignatureMock;
  @Mock
  Transaction transactionMock;
  @Mock
  SingleSignedTransaction<Transaction> signedTransactionMock;
  @Mock
  Signer signerMock;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private AbstractSignatureService<PrivateKeyable> signatureService;

  @BeforeEach
  public void setUp() throws Exception {

    openMocks(this);

    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    when(signedTransactionMock.unsignedTransaction()).thenReturn(transactionMock);
    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.addSignatureToTransaction(any(), any())).thenReturn(signedTransactionMock);

    this.signatureService = new AbstractSignatureService<PrivateKeyable>(signatureUtilsMock) {

      @Override
      protected Signature edDsaSign(PrivateKeyable privateKey, UnsignedByteArray signableTransactionBytes) {
        return ed25519SignatureMock;
      }

      @Override
      protected Signature ecDsaSign(PrivateKeyable privateKey, UnsignedByteArray signableTransactionBytes) {
        return secp256k1SignatureMock;
      }

      @Override
      protected boolean edDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        ed25519VerifyCalled.set(true);
        return ed25519VerifyCalled.get();
      }

      @Override
      protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        secp256k1VerifyCalled.set(true);
        return secp256k1VerifyCalled.get();
      }

      @Override
      public PublicKey derivePublicKey(PrivateKeyable privateKeyable) {
        return privateKeyable.keyType() == KeyType.ED25519 ? TestConstants.ED_PUBLIC_KEY
          : TestConstants.EC_PUBLIC_KEY;
      }
    };
  }

  ///////////////////
  // Sign (privateKey, Transaction)
  ///////////////////

  @Test
  public void nullConstructorSigner() {
    assertThrows(NullPointerException.class,
      () -> new AbstractSignatureService<PrivateKeyable>(null, mock(AbstractTransactionVerifier.class)) {
        @Override
        protected Signature edDsaSign(PrivateKeyable privateKey, UnsignedByteArray signableTransactionBytes) {
          return null;
        }

        @Override
        protected Signature ecDsaSign(PrivateKeyable privateKey, UnsignedByteArray signableTransactionBytes) {
          return null;
        }

        @Override
        protected boolean edDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
          return false;
        }

        @Override
        protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
          return false;
        }

        @Override
        public PublicKey derivePublicKey(PrivateKeyable privateKey) {
          return null;
        }
      });
  }

  @Test
  public void nullConstructorVerifier() {
    assertThrows(NullPointerException.class,
      () -> new AbstractSignatureService<PrivateKeyable>(mock(AbstractTransactionSigner.class), null) {
        @Override
        protected Signature edDsaSign(PrivateKeyable privateKey, UnsignedByteArray signableTransactionBytes) {
          return null;
        }

        @Override
        protected Signature ecDsaSign(PrivateKeyable privateKey, UnsignedByteArray signableTransactionBytes) {
          return null;
        }

        @Override
        protected boolean edDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
          return false;
        }

        @Override
        protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
          return false;
        }

        @Override
        public PublicKey derivePublicKey(PrivateKeyable privateKey) {
          return null;
        }
      });
  }

  ///////////////////
  // Sign (privateKey, Transaction)
  ///////////////////

  @Test
  public void signWithNullPrivateKey() {
    assertThrows(NullPointerException.class, () -> signatureService.sign(null, transactionMock));
  }

  @Test
  public void signWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> signatureService.sign(TestConstants.getEdPrivateKey(), (Transaction) null));
  }

  @Test
  public void signEd25519() {
    signatureService.sign(TestConstants.getEdPrivateKey(), transactionMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void signSecp256k1() {
    signatureService.sign(TestConstants.getEcPrivateKey(), transactionMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);

    final Signature signature = signatureService.multiSign(TestConstants.getEdPrivateKey(), transactionMock);
    assertThat(signature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.ED_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);

    final Signature signature = signatureService.multiSign(TestConstants.getEcPrivateKey(), transactionMock);
    assertThat(signature).isEqualTo(secp256k1SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.EC_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // Sign (privateKey, UnsignedClaim)
  ///////////////////

  @Test
  public void signUnsignedClaimWithNullPrivateKey() {
    assertThrows(NullPointerException.class, () -> signatureService.sign(null, mock(UnsignedClaim.class)));
  }

  @Test
  public void signUnsignedClaimWithNullUnsignedClaim() {
    assertThrows(NullPointerException.class,
      () -> signatureService.sign(TestConstants.getEdPrivateKey(), (UnsignedClaim) null));
  }

  @Test
  public void signUnsignedClaimEd25519() {
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actualSignature = signatureService.sign(TestConstants.getEdPrivateKey(), unsignedClaimMock);
    assertThat(actualSignature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // Sign (privateKey, Attestation)
  ///////////////////

  @Test
  public void signAttestationWithNullPrivateKey() {
    assertThrows(NullPointerException.class, () -> signatureService.sign(null, mock(Attestation.class)));
  }

  @Test
  public void signAttestationWithNullUnsignedClaim() {
    assertThrows(NullPointerException.class,
      () -> signatureService.sign(TestConstants.getEdPrivateKey(), (Attestation) null));
  }

  @Test
  public void signAttestationEd25519() {
    Attestation unsignedAttestationMock = mock(Attestation.class);
    when(signatureUtilsMock.toSignableBytes(unsignedAttestationMock)).thenReturn(UnsignedByteArray.empty());

    Signature actualSignature = signatureService.sign(TestConstants.getEdPrivateKey(), unsignedAttestationMock);
    assertThat(actualSignature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(unsignedAttestationMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify
  ///////////////////

  @Test
  public void verifyWithNullSigPubKey() {
    assertThrows(NullPointerException.class,
      () -> signatureService.verify(null, transactionMock));
  }

  @Test
  public void verifyWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> signatureService.verify(mock(Signer.class), null));
  }

  @Test
  public void verifyEd25519() {
    when(signerMock.signingPublicKey()).thenReturn(TestConstants.ED_PUBLIC_KEY);

    boolean actual = signatureService.verify(signerMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifySecp256k1() {
    when(signerMock.signingPublicKey()).thenReturn(TestConstants.EC_PUBLIC_KEY);

    boolean actual = signatureService.verify(signerMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiEd25519() {
    Signer signerMock = mock(Signer.class);
    when(signerMock.signingPublicKey()).thenReturn(TestConstants.ED_PUBLIC_KEY);
    when(signerMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    final Set<Signer> signatureWithPublicKeys = Sets.newLinkedHashSet(signerMock);

    boolean actual = signatureService.verifyMultiSigned(signatureWithPublicKeys, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiSecp256k1() {
    Signer signerMock = mock(Signer.class);
    when(signerMock.signingPublicKey()).thenReturn(TestConstants.EC_PUBLIC_KEY);
    when(signerMock.transactionSignature()).thenReturn(secp256k1SignatureMock);

    final Set<Signer> signatureWithPublicKeys = Sets.newLinkedHashSet(signerMock);

    boolean actual = signatureService.verifyMultiSigned(signatureWithPublicKeys, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EdDsaSign
  ///////////////////

  @Test
  public void edDsaSign() {
    Signature actual = signatureService.edDsaSign(TestConstants.getEdPrivateKey(), UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(ed25519SignatureMock);
    assertThat(ed25519VerifyCalled.get()).isFalse();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EcDsaSign
  ///////////////////

  @Test
  public void ecDsaSign() {
    Signature actual = signatureService.ecDsaSign(TestConstants.getEcPrivateKey(), UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(secp256k1SignatureMock);

    assertThat(secp256k1VerifyCalled.get()).isFalse();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // edDsaVerify
  ///////////////////

  @Test
  public void edDsaVerify() {
    boolean actual = signatureService.edDsaVerify(
      TestConstants.ED_PUBLIC_KEY, UnsignedByteArray.empty(), ed25519SignatureMock
    );

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // ecDsaVerify
  ///////////////////

  @Test
  public void ecDsaVerify() {
    boolean actual = signatureService.ecDsaVerify(
      TestConstants.EC_PUBLIC_KEY, UnsignedByteArray.empty(), secp256k1SignatureMock
    );

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }
}

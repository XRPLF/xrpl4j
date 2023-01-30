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
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link AbstractTransactionSigner}.
 */
public class AbstractTransactionSignerTest {

  @Mock
  SignatureUtils signatureUtilsMock;
  @Mock
  Signature ed25519SignatureMock;
  @Mock
  Signature secp256k1SignatureMock;
  @Mock
  PrivateKeyable privateKeyableMock;
  @Mock
  PublicKey publicKeyMock;
  @Mock
  Transaction transactionMock;
  @Mock
  SignatureWithPublicKey signatureWithPublicKeyMock;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private KeyType keyType;

  private AbstractTransactionSigner transactionSigner;

  @BeforeEach
  void setUp() {
    openMocks(this);

    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());

    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(publicKeyMock);
    when(signatureWithPublicKeyMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    this.transactionSigner = new AbstractTransactionSigner(signatureUtilsMock) {
      @Override
      protected Signature edDsaSign(PrivateKeyable privateKeyable, UnsignedByteArray signableTransactionBytes) {
        return ed25519SignatureMock;
      }

      @Override
      protected Signature ecDsaSign(PrivateKeyable privateKeyable, UnsignedByteArray signableTransactionBytes) {
        return secp256k1SignatureMock;
      }

      @Override
      public PublicKey derivePublicKey(PrivateKeyable privateKeyable) {
        return keyType == KeyType.ED25519 ? TestConstants.ED_PUBLIC_KEY : TestConstants.EC_PUBLIC_KEY;
      }
    };
  }

  ///////////////////
  // Sign (Transaction)
  ///////////////////

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
    keyType = KeyType.ED25519;
    transactionSigner.sign(privateKeyableMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signSecp256k1() {
    keyType = KeyType.SECP256K1;

    transactionSigner.sign(privateKeyableMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // Sign (UnsignedClaim)
  ///////////////////

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
    assertThat(actual).isEqualTo(ed25519SignatureMock);
  }

  @Test
  void signUnsignedClaimSecp256k1() {
    keyType = KeyType.SECP256K1;
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(privateKeyableMock, unsignedClaimMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(secp256k1SignatureMock);
  }

  ///////////////////
  // MultiSign
  ///////////////////

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
    assertThat(signature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.ED_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    keyType = KeyType.SECP256K1;

    Signature signature = transactionSigner.multiSign(privateKeyableMock, transactionMock);

    assertThat(signature).isEqualTo(secp256k1SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.EC_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EdDsaSign
  ///////////////////

  @Test
  void edDsaSign() {
    this.keyType = KeyType.ED25519;

    Signature actual = transactionSigner.edDsaSign(privateKeyableMock, UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(ed25519SignatureMock);
    assertThat(ed25519VerifyCalled.get()).isFalse();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EcDsaSign
  ///////////////////

  @Test
  void ecDsaSign() {
    this.keyType = KeyType.SECP256K1;

    Signature actual = transactionSigner.ecDsaSign(privateKeyableMock, UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(secp256k1SignatureMock);

    assertThat(secp256k1VerifyCalled.get()).isFalse();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

}

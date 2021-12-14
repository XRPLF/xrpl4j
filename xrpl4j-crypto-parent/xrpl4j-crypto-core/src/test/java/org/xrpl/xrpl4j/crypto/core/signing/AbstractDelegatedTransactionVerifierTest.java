package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.TestConstants;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractDelegatedTransactionVerifierTest {

  @Mock
  SignatureUtils signatureUtilsMock;
  @Mock
  Signature ed25519SignatureMock;
  @Mock
  Signature secp256k1SignatureMock;
  @Mock
  KeyMetadata keyMetadataMock;
  @Mock
  Transaction transactionMock;
  @Mock
  AddressUtils addressUtilsMock;
  @Mock
  SignatureWithKeyMetadata signatureWithKeyMetaMock;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private AbstractDelegatedTransactionVerifier transactionVerifier;

  @BeforeEach
  void setUp() {
    openMocks(this);

    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());

    when(signatureWithKeyMetaMock.signingKeyMetadata()).thenReturn(keyMetadataMock);
    when(signatureWithKeyMetaMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    this.transactionVerifier = new AbstractDelegatedTransactionVerifier(signatureUtilsMock, addressUtilsMock) {
      @Override
      protected boolean edDsaVerify(
        KeyMetadata keyMetadata, UnsignedByteArray signableTransactionBytes, Signature transactionSignature
      ) {
        ed25519VerifyCalled.set(true);
        return ed25519VerifyCalled.get();
      }

      @Override
      protected boolean ecDsaVerify(
        KeyMetadata keyMetadata, UnsignedByteArray signableTransactionBytes, Signature transactionSignature
      ) {
        secp256k1VerifyCalled.set(true);
        return secp256k1VerifyCalled.get();
      }

      @Override
      public PublicKey getPublicKey(KeyMetadata keyMetadata) {
        return keyMetadata.keyIdentifier().equals("EC") ? TestConstants.EC_PUBLIC_KEY : TestConstants.ED_PUBLIC_KEY;
      }
    };
  }

  ///////////////////
  // verify (signatureWithKeyMetadata, transaction)
  ///////////////////

  @Test
  void verifyWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class,
      () -> transactionVerifier.verifySingleSigned(null, transactionMock));
  }

  @Test
  void verifyWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> transactionVerifier.verifySingleSigned(signatureWithKeyMetaMock, null));
  }

  @Test
  void verifyEd25519() {
    when(keyMetadataMock.keyIdentifier()).thenReturn("ED");

    boolean actual = transactionVerifier.verifySingleSigned(signatureWithKeyMetaMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void verifySecp256k1() {
    when(keyMetadataMock.keyIdentifier()).thenReturn("EC");
    boolean actual = transactionVerifier.verifySingleSigned(signatureWithKeyMetaMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify (signatureWithKeyMetadata, transaction)
  ///////////////////

  @Test
  void verifyMultiWithNullSet() {
    Assertions.assertThrows(NullPointerException.class,
      () -> transactionVerifier.verifyMultiSigned(null, transactionMock));
  }

  @Test
  void verifyMultiWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> transactionVerifier.verifyMultiSigned(Sets.newHashSet(), null));
  }

  @Test
  void verifyMultiWithEmptySet() {
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> transactionVerifier.verifyMultiSigned(Sets.newHashSet(), transactionMock));
  }

  @Test
  void verifyMultiEd25519() {
    when(keyMetadataMock.keyIdentifier()).thenReturn("ED");
    when(signatureWithKeyMetaMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet = Sets.newLinkedHashSet(signatureWithKeyMetaMock);
    boolean actual = transactionVerifier.verifyMultiSigned(signatureWithKeyMetadataSet, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(addressUtilsMock).deriveAddress(TestConstants.ED_PUBLIC_KEY);
    verifyNoMoreInteractions(addressUtilsMock);
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void verifyMultiSecp256k1() {
    when(keyMetadataMock.keyIdentifier()).thenReturn("EC");
    when(signatureWithKeyMetaMock.transactionSignature()).thenReturn(secp256k1SignatureMock);

    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet = Sets.newLinkedHashSet(signatureWithKeyMetaMock);
    boolean actual = transactionVerifier.verifyMultiSigned(signatureWithKeyMetadataSet, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(addressUtilsMock).deriveAddress(TestConstants.EC_PUBLIC_KEY);
    verifyNoMoreInteractions(addressUtilsMock);
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // edDsaVerify
  ///////////////////

  @Test
  void edDsaVerify() {
    when(keyMetadataMock.keyIdentifier()).thenReturn("ED");
    boolean actual = transactionVerifier.edDsaVerify(keyMetadataMock, UnsignedByteArray.empty(), ed25519SignatureMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // ecDsaVerify
  ///////////////////

  @Test
  void ecDsaVerify() {
    when(keyMetadataMock.keyIdentifier()).thenReturn("EC");
    boolean actual = transactionVerifier.ecDsaVerify(
      keyMetadataMock, UnsignedByteArray.empty(), secp256k1SignatureMock
    );

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }
}

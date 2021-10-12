package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.KeyStoreType;
import org.xrpl.xrpl4j.crypto.core.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link AbstractDelegatedSignatureService}.
 */
class AbstractDelegatedSignatureServiceTest {

  @Mock
  private SignatureUtils signatureUtilsMock;
  @Mock
  private Signature ed25519SignatureMock;
  @Mock
  private Signature secp256k1SignatureMock;
  @Mock
  private KeyMetadata keyMetadataMock;
  @Mock
  private Transaction transactionMock;
  @Mock
  private AddressUtils addressServiceMock;
  @Mock
  private SignatureWithKeyMetadata signatureWithKeyMetaMock;

  private KeyPair ed25519KeyPair;
  private Address ed25519SignerAddress;

  private KeyPair secp256k1KeyPair;
  private Address secp256k1SignerAddress;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;
  private VersionType keyType;

  private AbstractDelegatedSignatureService signatureService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    AddressUtils addressService = AddressUtils.getInstance();
    Ed25519KeyPairService ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    this.ed25519KeyPair = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))
    );
    this.ed25519SignerAddress = addressService.deriveAddress(ed25519KeyPair.publicKey());

    Secp256k1KeyPairService secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();
    this.secp256k1KeyPair = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"))
    );
    this.secp256k1SignerAddress = addressService.deriveAddress(secp256k1KeyPair.publicKey());

    when(addressServiceMock.deriveAddress(any())).thenReturn(ed25519SignerAddress);

    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());

    when(signatureWithKeyMetaMock.signingKeyMetadata()).thenReturn(keyMetadataMock);
    when(signatureWithKeyMetaMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    this.signatureService = new AbstractDelegatedSignatureService(
      KeyStoreType.DERIVED_SERVER_SECRET,
      signatureUtilsMock,
      addressServiceMock
    ) {
      @Override
      public PublicKey getPublicKey(KeyMetadata keyMetadata) {
        return keyType == VersionType.ED25519 ? ed25519KeyPair.publicKey() : secp256k1KeyPair.publicKey();
      }

      @Override
      protected Signature edDsaSign(KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes) {
        return ed25519SignatureMock;
      }

      @Override
      protected Signature ecDsaSign(KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes) {
        return secp256k1SignatureMock;
      }

      @Override
      protected boolean edDsaVerify(
        KeyMetadata keyMetadata, UnsignedByteArray signableTransactionBytes, Signature transactionSignature
      ) {
        ed25519VerifyCalled.set(true);
        return ed25519VerifyCalled.get();
      }

      @Override
      protected boolean ecDsaVerify(KeyMetadata keyMetadata, UnsignedByteArray signableTransactionBytes,
        Signature transactionSignature) {
        secp256k1VerifyCalled.set(true);
        return secp256k1VerifyCalled.get();
      }
    };
  }

  ///////////////////
  // KeyStoreType
  ///////////////////

  @Test
  void keyStoreType() {
    assertThat(signatureService.keyStoreType()).isEqualTo(KeyStoreType.DERIVED_SERVER_SECRET);
  }

  ///////////////////
  // Sign
  ///////////////////

  @Test
  void signWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(null, transactionMock));
  }

  @Test
  void signWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(keyMetadataMock, null));
  }

  @Test
  void signEd25519() {
    keyType = VersionType.ED25519;
    signatureService.sign(keyMetadataMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signSecp256k1() {
    keyType = VersionType.SECP256K1;

    signatureService.sign(keyMetadataMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // MultiSign
  ///////////////////

  @Test
  void multiSignWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.multiSign(null, transactionMock));
  }

  @Test
  void multiSignWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.multiSign(keyMetadataMock, null));
  }

  @Test
  void multiSignEd25519() {
    keyType = VersionType.ED25519;
    when(addressServiceMock.deriveAddress(any())).thenReturn(ed25519SignerAddress);
    final SignatureWithPublicKey signedTransaction = signatureService.multiSign(
      keyMetadataMock,
      transactionMock
    );
    assertThat(signedTransaction.transactionSignature()).isEqualTo(ed25519SignatureMock);

    verify(addressServiceMock).deriveAddress(any());
    verifyNoMoreInteractions(addressServiceMock);
    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, ed25519SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    keyType = VersionType.SECP256K1;
    when(addressServiceMock.deriveAddress(any())).thenReturn(secp256k1SignerAddress);
    final SignatureWithPublicKey signedTransaction = signatureService.multiSign(
      keyMetadataMock,
      transactionMock
    );
    assertThat(signedTransaction.transactionSignature()).isEqualTo(secp256k1SignatureMock);

    verify(addressServiceMock).deriveAddress(any());
    verifyNoMoreInteractions(addressServiceMock);
    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, secp256k1SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify (signatureWithKeyMetadata, transaction)
  ///////////////////

  @Test
  void verifyWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.verify((SignatureWithKeyMetadata) null, transactionMock));
  }

  @Test
  void verifyWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.verify(signatureWithKeyMetaMock, null));
  }

  @Test
  void verifyEd25519() {
    keyType = VersionType.ED25519;
    boolean actual = signatureService.verify(signatureWithKeyMetaMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void verifySecp256k1() {
    keyType = VersionType.SECP256K1;
    boolean actual = signatureService.verify(signatureWithKeyMetaMock, transactionMock);

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
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.verify(
      (Set<SignatureWithKeyMetadata>) null, transactionMock
    ));
  }

  @Test
  void verifyMultiWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.verify(Sets.newHashSet(), null));
  }

  @Test
  void verifyMultiWithEmptySet() {
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> signatureService.verify(Sets.newHashSet(), transactionMock));
  }

  @Test
  void verifyMultiEd25519() {
    keyType = VersionType.ED25519;
    when(signatureWithKeyMetaMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet = Sets.newLinkedHashSet(signatureWithKeyMetaMock);
    boolean actual = signatureService.verify(signatureWithKeyMetadataSet, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(addressServiceMock).deriveAddress(ed25519KeyPair.publicKey());
    verifyNoMoreInteractions(addressServiceMock);
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void verifyMultiSecp256k1() {
    keyType = VersionType.SECP256K1;
    when(signatureWithKeyMetaMock.transactionSignature()).thenReturn(secp256k1SignatureMock);

    final Set<SignatureWithKeyMetadata> signatureWithKeyMetadataSet = Sets.newLinkedHashSet(signatureWithKeyMetaMock);
    boolean actual = signatureService.verify(signatureWithKeyMetadataSet, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(addressServiceMock).deriveAddress(secp256k1KeyPair.publicKey());
    verifyNoMoreInteractions(addressServiceMock);
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EdDsaSign
  ///////////////////

  @Test
  void edDsaSign() {
    this.keyType = VersionType.ED25519;

    Signature actual = signatureService.edDsaSign(keyMetadataMock,
      UnsignedByteArray.empty());

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
    this.keyType = VersionType.SECP256K1;

    Signature actual = signatureService.ecDsaSign(keyMetadataMock, UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(secp256k1SignatureMock);

    assertThat(secp256k1VerifyCalled.get()).isFalse();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // edDsaVerify
  ///////////////////

  @Test
  void edDsaVerify() {
    this.keyType = VersionType.ED25519;

    boolean actual = signatureService.edDsaVerify(
      keyMetadataMock, UnsignedByteArray.empty(), ed25519SignatureMock
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
  void ecDsaVerify() {
    this.keyType = VersionType.SECP256K1;

    boolean actual = signatureService.ecDsaVerify(
      keyMetadataMock, UnsignedByteArray.empty(), secp256k1SignatureMock
    );

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }
}
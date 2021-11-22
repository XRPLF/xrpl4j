package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.concurrent.atomic.AtomicBoolean;

public class AbstractDelegatedTransactionSignerTest {

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

  private AbstractDelegatedTransactionSigner transactionSigner;

  @BeforeEach
  void setUp() {
    openMocks(this);

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

    this.transactionSigner = new AbstractDelegatedTransactionSigner(
      signatureUtilsMock,
      addressServiceMock
    ) {
      @Override
      protected Signature edDsaSign(KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes) {
        return ed25519SignatureMock;
      }

      @Override
      protected Signature ecDsaSign(KeyMetadata privateKeyMetadata, UnsignedByteArray signableTransactionBytes) {
        return secp256k1SignatureMock;
      }

      @Override
      public PublicKey getPublicKey(KeyMetadata keyMetadata) {
        return keyType == VersionType.ED25519 ? ed25519KeyPair.publicKey() : secp256k1KeyPair.publicKey();
      }
    };
  }

  ///////////////////
  // Sign (Transaction)
  ///////////////////

  @Test
  void signWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class, () -> transactionSigner.sign(null, transactionMock));
  }

  @Test
  void signWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> transactionSigner.sign(keyMetadataMock, (Transaction) null));
  }

  @Test
  void signEd25519() {
    keyType = VersionType.ED25519;
    transactionSigner.sign(keyMetadataMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signSecp256k1() {
    keyType = VersionType.SECP256K1;

    transactionSigner.sign(keyMetadataMock, transactionMock);

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
    Assertions.assertThrows(NullPointerException.class, () -> transactionSigner.sign(null, unsignedClaimMock));
  }

  @Test
  void signUnsignedClaimWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> transactionSigner.sign(keyMetadataMock, (UnsignedClaim) null));
  }

  @Test
  void signUnsignedClaimEd25519() {
    keyType = VersionType.ED25519;
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(keyMetadataMock, unsignedClaimMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(ed25519SignatureMock);
  }

  @Test
  void signUnsignedClaimSecp256k1() {
    keyType = VersionType.SECP256K1;
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actual = transactionSigner.sign(keyMetadataMock, unsignedClaimMock);

    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
    assertThat(actual).isEqualTo(secp256k1SignatureMock);
  }

  ///////////////////
  // MultiSign
  ///////////////////

  @Test
  void multiSignWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class, () -> transactionSigner.multiSign(null, transactionMock));
  }

  @Test
  void multiSignWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> transactionSigner.multiSign(keyMetadataMock, null));
  }

  @Test
  void multiSignEd25519() {
    keyType = VersionType.ED25519;
    when(addressServiceMock.deriveAddress(any())).thenReturn(ed25519SignerAddress);

    SignatureWithKeyMetadata signatureWithKeyMetadata = transactionSigner.multiSign(
      keyMetadataMock,
      transactionMock
    );
    assertThat(signatureWithKeyMetadata.transactionSignature()).isEqualTo(ed25519SignatureMock);
    assertThat(signatureWithKeyMetadata.signingKeyMetadata()).isEqualTo(keyMetadataMock);

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

    SignatureWithKeyMetadata signatureWithKeyMetadata = transactionSigner.multiSign(
      keyMetadataMock,
      transactionMock
    );

    assertThat(signatureWithKeyMetadata.transactionSignature()).isEqualTo(secp256k1SignatureMock);
    assertThat(signatureWithKeyMetadata.signingKeyMetadata()).isEqualTo(keyMetadataMock);

    verify(addressServiceMock).deriveAddress(any());
    verifyNoMoreInteractions(addressServiceMock);
    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, secp256k1SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EdDsaSign
  ///////////////////

  @Test
  void edDsaSign() {
    this.keyType = VersionType.ED25519;

    Signature actual = transactionSigner.edDsaSign(keyMetadataMock,
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

    Signature actual = transactionSigner.ecDsaSign(keyMetadataMock, UnsignedByteArray.empty());

    assertThat(actual).isEqualTo(secp256k1SignatureMock);

    assertThat(secp256k1VerifyCalled.get()).isFalse();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

}

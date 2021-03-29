package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link AbstractSignatureService}.
 */
@SuppressWarnings("CheckStyle")
public class AbstractSignatureServiceTest {

  @Mock
  private SignatureUtils signatureUtilsMock;

  @Mock
  private PrivateKey privateKeyMock;
  @Mock
  private PublicKey publicKeyMock;
  @Mock
  private Signature ed25519SignatureMock;
  @Mock
  private Signature secp256k1SignatureMock;
  @Mock
  private KeyMetadata keyMetadataMock;
  @Mock
  private Transaction transactionMock;
  @Mock
  private SignedTransaction transactionWithSignatureMock;
  @Mock
  private KeyPairService keyPairServiceMock;

  private Address signerAddress;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private AbstractSignatureService signatureService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    when(transactionWithSignatureMock.unsignedTransaction()).thenReturn(transactionMock);
    when(signatureUtilsMock.toSignableBytes(any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());

    when(publicKeyMock.value()).thenReturn(UnsignedByteArray.empty());
    signerAddress = Address.of("");
    when(keyPairServiceMock.deriveAddress(publicKeyMock.value())).thenReturn(signerAddress);

    this.signatureService = new AbstractSignatureService(KeyStoreType.DERIVED_SERVER_SECRET, signatureUtilsMock, keyPairServiceMock) {
      @Override
      public PublicKey getPublicKey(KeyMetadata keyMetadata) {
        return publicKeyMock;
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
        KeyMetadata keyMetadata,
        SignedTransaction transactionWithSignature,
        UnsignedByteArray signableTransactionBytes
      ) {
        ed25519VerifyCalled.set(true);
        return ed25519VerifyCalled.get();
      }

      @Override
      protected boolean ecDsaVerify(
        KeyMetadata keyMetadata,
        SignedTransaction transactionWithSignature,
        UnsignedByteArray signableTransactionBytes) {
        secp256k1VerifyCalled.set(true);
        return secp256k1VerifyCalled.get();
      }
    };
  }

  ///////////////////
  // KeyStoreType
  ///////////////////

  @Test
  public void keyStoreType() {
    assertThat(signatureService.keyStoreType()).isEqualTo(KeyStoreType.DERIVED_SERVER_SECRET);
  }

  ///////////////////
  // Sign
  ///////////////////

  @Test
  public void signWithNullMetadata() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(null, transactionMock));
  }

  @Test
  public void signWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(keyMetadataMock, null));
  }

  @Test
  public void signEd25519() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.ED25519);

    signatureService.sign(keyMetadataMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void signSecp256k1() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

    signatureService.sign(keyMetadataMock, transactionMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signWithSingleBehaviorEd25519() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.ED25519);

    final Signature signature = signatureService.signWithBehavior(keyMetadataMock, transactionMock, SigningBehavior.SINGLE);
    assertThat(signature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(transactionMock, signerAddress.value());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signWithSingleBehaviorSecp256k1() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

    final Signature signature = signatureService.signWithBehavior(keyMetadataMock, transactionMock, SigningBehavior.SINGLE);
    assertThat(signature).isEqualTo(secp256k1SignatureMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(transactionMock, signerAddress.value());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signWithMultiBehaviorEd25519() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.ED25519);

    final Signature signature = signatureService.signWithBehavior(keyMetadataMock, transactionMock, SigningBehavior.MULTI);
    assertThat(signature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, signerAddress.value());
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signWithMultiBehaviorSecp256k1() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

    final Signature signature = signatureService.signWithBehavior(keyMetadataMock, transactionMock, SigningBehavior.MULTI);
    assertThat(signature).isEqualTo(secp256k1SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, signerAddress.value());
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify
  ///////////////////

  @Test
  public void verifyWithNullMetadata() {
    Assertions
      .assertThrows(NullPointerException.class, () -> signatureService.verify(null, transactionWithSignatureMock));
  }

  @Test
  public void verifyWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.verify(keyMetadataMock, null));
  }

  @Test
  public void verifyEd25519() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.ED25519);

    boolean actual = signatureService.verify(keyMetadataMock, transactionWithSignatureMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifySecp256k1() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

    boolean actual = signatureService.verify(keyMetadataMock, transactionWithSignatureMock);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EdDsaSign
  ///////////////////

  @Test
  public void edDsaSign() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.ED25519);

    Signature actual = signatureService.edDsaSign(keyMetadataMock, UnsignedByteArray.empty());

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
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

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
  public void edDsaVerify() {
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

    boolean actual = signatureService.edDsaVerify(
      keyMetadataMock, transactionWithSignatureMock, UnsignedByteArray.empty()
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
    when(publicKeyMock.versionType()).thenReturn(VersionType.SECP256K1);

    boolean actual = signatureService.ecDsaVerify(
      keyMetadataMock, transactionWithSignatureMock, UnsignedByteArray.empty()
    );

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }
}

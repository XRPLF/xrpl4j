package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.xrpl.xrpl4j.crypto.core.AddressUtils;
import org.xrpl.xrpl4j.crypto.core.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
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
@SuppressWarnings("CheckStyle")
public class AbstractSignatureServiceTest {

  @Mock
  private SignatureUtils signatureUtilsMock;
  @Mock
  private Signature ed25519SignatureMock;
  @Mock
  private Signature secp256k1SignatureMock;
  @Mock
  private Transaction transactionMock;
  @Mock
  private SingleSingedTransaction<Transaction> signedTransactionMock;
  @Mock
  private AddressUtils addressServiceMock;
  @Mock
  private SignatureWithPublicKey signatureWithPublicKeyMock;

  private KeyPair ed25519KeyPair;
  private Address ed25519SignerAddress;

  private KeyPair secp256k1KeyPair;
  private Address secp256k1SignerAddress;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private AbstractSignatureService signatureService;

  @BeforeEach
  public void setUp() {

    MockitoAnnotations.openMocks(this);
    ed25519VerifyCalled = new AtomicBoolean(false);
    secp256k1VerifyCalled = new AtomicBoolean(false);

    AddressUtils addressUtils = AddressUtils.getInstance();
    Ed25519KeyPairService ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    this.ed25519KeyPair = ed25519KeyPairService.deriveKeyPair(
      Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))
    );
    this.ed25519SignerAddress = addressUtils.deriveAddress(ed25519KeyPair.publicKey());

    Secp256k1KeyPairService secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();
    this.secp256k1KeyPair = secp256k1KeyPairService.deriveKeyPair(
      Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"))
    );
    this.secp256k1SignerAddress = addressUtils.deriveAddress(secp256k1KeyPair.publicKey());

    when(addressServiceMock.deriveAddress(any())).thenReturn(ed25519SignerAddress);
    when(signedTransactionMock.unsignedTransaction()).thenReturn(transactionMock);
    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.addSignatureToTransaction(any(), any())).thenReturn(signedTransactionMock);

    this.signatureService = new AbstractSignatureService(signatureUtilsMock, addressServiceMock) {
      @Override
      protected Signature edDsaSign(PrivateKey privateKey, UnsignedByteArray signableTransactionBytes) {
        return ed25519SignatureMock;
      }

      @Override
      protected Signature ecDsaSign(PrivateKey privateKey, UnsignedByteArray signableTransactionBytes) {
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
      protected PublicKey derivePublicKey(PrivateKey privateKey) {
        return ed25519KeyPair.publicKey();
      }
    };
  }

  ///////////////////
  // Sign (privateKey, Transaction)
  ///////////////////

  @Test
  public void signWithNullPrivateKey() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(null, transactionMock));
  }

  @Test
  public void signWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.sign(ed25519KeyPair.privateKey(), (Transaction) null));
  }

  @Test
  public void signEd25519() {
    signatureService.sign(ed25519KeyPair.privateKey(), transactionMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void signSecp256k1() {
    signatureService.sign(secp256k1KeyPair.privateKey(), transactionMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);
    when(addressServiceMock.deriveAddress(any())).thenReturn(ed25519SignerAddress);

    final Signature signature = signatureService.multiSign(
      ed25519KeyPair.privateKey(),
      transactionMock
    );
    assertThat(signature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, ed25519SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);
    when(addressServiceMock.deriveAddress(any())).thenReturn(secp256k1SignerAddress);

    final Signature signature = signatureService.multiSign(
      secp256k1KeyPair.privateKey(),
      transactionMock
    );
    assertThat(signature).isEqualTo(secp256k1SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, secp256k1SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // Sign (privateKey, UnsignedClaim)
  ///////////////////
  // TODO: FIXME

  ///////////////////
  // verify
  ///////////////////

  @Test
  public void verifyWithNullSigPubKey() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      SignatureWithPublicKey nullSignatureWithPublicKey = null;
      signatureService.verify(nullSignatureWithPublicKey, transactionMock);
    });
  }

  @Test
  public void verifyWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.verify(mock(SignatureWithPublicKey.class), null));
  }

  @Test
  public void verifyEd25519() {
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(ed25519KeyPair.publicKey());

    boolean actual = signatureService.verify(signatureWithPublicKeyMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifySecp256k1() {
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(secp256k1KeyPair.publicKey());

    boolean actual = signatureService.verify(signatureWithPublicKeyMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiEd25519() {
    SignatureWithPublicKey signatureWithPublicKeyMock = mock(SignatureWithPublicKey.class);
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(ed25519KeyPair.publicKey());
    when(signatureWithPublicKeyMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    final Set<SignatureWithPublicKey> signatureWithPublicKeys = Sets.newLinkedHashSet(signatureWithPublicKeyMock);

    boolean actual = signatureService.verify(signatureWithPublicKeys, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(addressServiceMock).deriveAddress(ed25519KeyPair.publicKey());
    verifyNoMoreInteractions(addressServiceMock);
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiSecp256k1() {
    SignatureWithPublicKey signatureWithPublicKeyMock = mock(SignatureWithPublicKey.class);
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(secp256k1KeyPair.publicKey());
    when(signatureWithPublicKeyMock.transactionSignature()).thenReturn(secp256k1SignatureMock);

    final Set<SignatureWithPublicKey> signatureWithPublicKeys = Sets.newLinkedHashSet(signatureWithPublicKeyMock);

    boolean actual = signatureService.verify(signatureWithPublicKeys, transactionMock, 1);

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
  public void edDsaSign() {
    Signature actual = signatureService.edDsaSign(ed25519KeyPair.privateKey(), UnsignedByteArray.empty());

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
    Signature actual = signatureService.ecDsaSign(secp256k1KeyPair.privateKey(), UnsignedByteArray.empty());

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
      ed25519KeyPair.publicKey(), UnsignedByteArray.empty(), ed25519SignatureMock
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
      secp256k1KeyPair.publicKey(), UnsignedByteArray.empty(), secp256k1SignatureMock
    );

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);
  }
}

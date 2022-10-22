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
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.TestConstants;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
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
  SignatureWithPublicKey signatureWithPublicKeyMock;

  private AtomicBoolean ed25519VerifyCalled;
  private AtomicBoolean secp256k1VerifyCalled;

  private AbstractSignatureService<PrivateKeyable> signatureService;

  @BeforeEach
  public void setUp() throws Exception {

    MockitoAnnotations.openMocks(this);

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
        return privateKeyable.versionType() == VersionType.ED25519 ? TestConstants.ED_PUBLIC_KEY
          : TestConstants.EC_PUBLIC_KEY;
      }
    };
  }

  ///////////////////
  // Sign (privateKey, Transaction)
  ///////////////////

  @Test
  public void nullConstructorSigner() {
    Assertions.assertThrows(NullPointerException.class,
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
    Assertions.assertThrows(NullPointerException.class,
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
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(null, transactionMock));
  }

  @Test
  public void signWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.sign(TestConstants.ED_PRIVATE_KEY, (Transaction) null));
  }

  @Test
  public void signEd25519() {
    signatureService.sign(TestConstants.ED_PRIVATE_KEY, transactionMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, ed25519SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void signSecp256k1() {
    signatureService.sign(TestConstants.EC_PRIVATE_KEY, transactionMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, secp256k1SignatureMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);

    final Signature signature = signatureService.multiSign(TestConstants.ED_PRIVATE_KEY, transactionMock);
    assertThat(signature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, TestConstants.ED_ADDRESS);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);

    final Signature signature = signatureService.multiSign(TestConstants.EC_PRIVATE_KEY, transactionMock);
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
    Assertions.assertThrows(NullPointerException.class, () -> signatureService.sign(null, mock(UnsignedClaim.class)));
  }

  @Test
  public void signUnsignedClaimWithNullUnsignedClaim() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.sign(TestConstants.ED_PRIVATE_KEY, (UnsignedClaim) null));
  }

  @Test
  public void signUnsignedClaimEd25519() {
    UnsignedClaim unsignedClaimMock = mock(UnsignedClaim.class);
    when(signatureUtilsMock.toSignableBytes(unsignedClaimMock)).thenReturn(UnsignedByteArray.empty());

    Signature actualSignature = signatureService.sign(TestConstants.ED_PRIVATE_KEY, unsignedClaimMock);
    assertThat(actualSignature).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(any(), any());
    verify(signatureUtilsMock).toSignableBytes(unsignedClaimMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify
  ///////////////////

  @Test
  public void verifyWithNullSigPubKey() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.verifySingleSigned(null, transactionMock));
  }

  @Test
  public void verifyWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureService.verifySingleSigned(mock(SignatureWithPublicKey.class), null));
  }

  @Test
  public void verifyEd25519() {
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(TestConstants.ED_PUBLIC_KEY);

    boolean actual = signatureService.verifySingleSigned(signatureWithPublicKeyMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifySecp256k1() {
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(TestConstants.EC_PUBLIC_KEY);

    boolean actual = signatureService.verifySingleSigned(signatureWithPublicKeyMock, transactionMock);

    assertThat(actual).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isTrue();
    assertThat(ed25519VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiEd25519() {
    SignatureWithPublicKey signatureWithPublicKeyMock = mock(SignatureWithPublicKey.class);
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(TestConstants.ED_PUBLIC_KEY);
    when(signatureWithPublicKeyMock.transactionSignature()).thenReturn(ed25519SignatureMock);

    final Set<SignatureWithPublicKey> signatureWithPublicKeys = Sets.newLinkedHashSet(signatureWithPublicKeyMock);

    boolean actual = signatureService.verifyMultiSigned(signatureWithPublicKeys, transactionMock, 1);

    assertThat(actual).isTrue();
    assertThat(ed25519VerifyCalled.get()).isTrue();
    assertThat(secp256k1VerifyCalled.get()).isFalse();
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiSecp256k1() {
    SignatureWithPublicKey signatureWithPublicKeyMock = mock(SignatureWithPublicKey.class);
    when(signatureWithPublicKeyMock.signingPublicKey()).thenReturn(TestConstants.EC_PUBLIC_KEY);
    when(signatureWithPublicKeyMock.transactionSignature()).thenReturn(secp256k1SignatureMock);

    final Set<SignatureWithPublicKey> signatureWithPublicKeys = Sets.newLinkedHashSet(signatureWithPublicKeyMock);

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
    Signature actual = signatureService.edDsaSign(TestConstants.ED_PRIVATE_KEY, UnsignedByteArray.empty());

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
    Signature actual = signatureService.ecDsaSign(TestConstants.EC_PRIVATE_KEY, UnsignedByteArray.empty());

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

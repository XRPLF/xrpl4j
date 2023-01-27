package org.xrpl.xrpl4j.crypto.bc.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.google.common.io.BaseEncoding;
import org.assertj.core.util.Sets;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.crypto.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.math.BigInteger;

/**
 * Unit tests for {@link BcSignatureService}.
 */
class BcSignatureServiceTest {

  @Mock
  private SignatureUtils signatureUtilsMock;
  @Mock
  private Signature ed25519SignatureMock;
  @Mock
  private Signature secp256k1SignatureMock;
  @Mock
  private AccountSet transactionMock;
  @Mock
  private SingleSignedTransaction<AccountSet> signedTransactionMock;
  @Mock
  private Ed25519Signer ed25519SignerMock;
  @Mock
  private ECDSASigner ecdsaSignerMock;

  private KeyPair ed25519KeyPair;
  private Address ed25519SignerAddress;

  private KeyPair secp256k1KeyPair;
  private Address secp256k1SignerAddress;

  private BcSignatureService signatureService;

  @BeforeEach
  public void setUp() {
    openMocks(this);

    this.ed25519KeyPair = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello")).deriveKeyPair();
    this.ed25519SignerAddress = ed25519KeyPair.publicKey().deriveAddress();

    this.secp256k1KeyPair = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello")).deriveKeyPair();
    this.secp256k1SignerAddress = secp256k1KeyPair.publicKey().deriveAddress();

    when(signedTransactionMock.unsignedTransaction()).thenReturn(transactionMock);
    when(signatureUtilsMock.toSignableBytes(Mockito.<Transaction>any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.toMultiSignableBytes(any(), any())).thenReturn(UnsignedByteArray.empty());
    when(signatureUtilsMock.addSignatureToTransaction(Mockito.<AccountSet>any(), any())).thenReturn(
      signedTransactionMock);

    when(ed25519SignatureMock.value()).thenReturn(UnsignedByteArray.of(new byte[32]));

    // This is necessary because the verify method does DER decoding on the signature, so we can't easily mock it.
    final byte[] secpSigBytes = BaseEncoding.base16().decode(
      "3045022100CD8BFD66AAF0957149E49307FA23F6757DD57E002D577D57E5BC85318048FD7802206AEE28081BC49C2D287652C158" +
        "E2F8957E2E062D833875DDABD57DB518A66D3B");
    when(secp256k1SignatureMock.value()).thenReturn(UnsignedByteArray.of(secpSigBytes));

    when(ed25519SignerMock.generateSignature()).thenReturn(new byte[32]);

    BigInteger[] bigInts = new BigInteger[32];
    for (int i = 0; i < 32; i++) {
      bigInts[i] = BigInteger.TEN;
    }
    when(ecdsaSignerMock.generateSignature(any())).thenReturn(bigInts);

    this.signatureService = new BcSignatureService(signatureUtilsMock, ed25519SignerMock, ecdsaSignerMock);
  }

  @Test
  void defaultConstructor() {
    // Obvious test, but nothing else covers the default constructor
    BcSignatureService signatureService = new BcSignatureService();
    assertThat(signatureService).isInstanceOf(BcSignatureService.class);
  }

  @Test
  void constructorWithNulls() {
    // 4-arg Constructor
    assertThrows(NullPointerException.class, () -> new BcSignatureService(null, ed25519SignerMock, ecdsaSignerMock));
    assertThrows(NullPointerException.class, () -> new BcSignatureService(signatureUtilsMock, null, ecdsaSignerMock));
    assertThrows(NullPointerException.class, () -> new BcSignatureService(signatureUtilsMock, ed25519SignerMock, null));
  }

  ///////////////////
  // Sign
  ///////////////////

  @Test
  public void signWithNullPrivateKey() {
    assertThrows(NullPointerException.class, () -> signatureService.sign(null, transactionMock));
  }

  @Test
  public void signWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> signatureService.sign(ed25519KeyPair.privateKey(), (Transaction) null));
  }

  @Test
  public void signAndVerifySecp256k1() {
    when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);
    SingleSignedTransaction<Transaction> signedTransaction = signatureService.sign(secp256k1KeyPair.privateKey(),
      transactionMock);
    assertThat(signedTransaction.signature()).isEqualTo(secp256k1SignatureMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(transactionMock, secp256k1SignerAddress);
    verify(signatureUtilsMock).toSignableBytes(transactionMock);

    final Signature expectedSecp256k1Signatur = Signature.builder()
      .value(UnsignedByteArray.of(BaseEncoding.base16().decode("300602010A02010A"))).build();
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, expectedSecp256k1Signatur);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void signAndVerifyEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);
    SingleSignedTransaction<Transaction> signedTransaction = signatureService.sign(ed25519KeyPair.privateKey(),
      transactionMock);
    assertThat(signedTransaction.signature()).isEqualTo(ed25519SignatureMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verify(signatureUtilsMock, times(0)).toMultiSignableBytes(transactionMock, ed25519SignerAddress);
    verify(signatureUtilsMock).toSignableBytes(transactionMock);

    final Signature expectedSecp256k1Signature = Signature.builder().value(UnsignedByteArray.of(new byte[32])).build();
    verify(signatureUtilsMock).addSignatureToTransaction(transactionMock, expectedSecp256k1Signature);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);

    final Signature signature = signatureService.multiSign(ed25519KeyPair.privateKey(), transactionMock);
    assertThat(signature.base16Value()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, ed25519SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);

    ed25519SignatureMock = Signature.builder().value(UnsignedByteArray.of(new byte[32])).build();
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  void multiSignSecp256k1() {
    //when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);

    final Signature signature = signatureService.multiSign(secp256k1KeyPair.privateKey(), transactionMock);
    assertThat(signature.base16Value()).isEqualTo("300602010A02010A");

    verify(signatureUtilsMock).toMultiSignableBytes(transactionMock, secp256k1SignerAddress);
    verify(signatureUtilsMock, times(0)).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify (single)
  ///////////////////

  @Test
  public void verifyWithNullMetadata() {
    assertThrows(NullPointerException.class,
      () -> signatureService.verify(null, transactionMock));
  }

  @Test
  public void verifyWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> signatureService.verify(mock(SignatureWithPublicKey.class), null));
  }

  @Test
  public void verifyEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);
    when(ed25519SignerMock.verifySignature(any())).thenReturn(true);

    SignatureWithPublicKey signatureWithPublicKey = SignatureWithPublicKey.builder()
      .transactionSignature(ed25519SignatureMock).signingPublicKey(ed25519KeyPair.publicKey()).build();

    boolean actual = signatureService.verify(signatureWithPublicKey, transactionMock);

    assertThat(actual).isTrue();
    verify(ed25519SignerMock).reset();
    verify(ed25519SignerMock).init(anyBoolean(), any());
    verify(ed25519SignerMock).update(any(), anyInt(), anyInt());
    verify(ed25519SignerMock).verifySignature(any());
    verifyNoMoreInteractions(ed25519SignerMock);
    verifyNoMoreInteractions(secp256k1SignatureMock);

    verify(signatureUtilsMock).toSignableBytes(transactionMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifySecp256k1() {
    when(ecdsaSignerMock.verifySignature(any(), any(), any())).thenReturn(true);
    when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);
    SignatureWithPublicKey signatureWithPublicKey = SignatureWithPublicKey.builder()
      .transactionSignature(secp256k1SignatureMock).signingPublicKey(secp256k1KeyPair.publicKey()).build();

    boolean actual = signatureService.verify(signatureWithPublicKey, transactionMock);

    assertThat(actual).isTrue();
    verify(ecdsaSignerMock).init(anyBoolean(), any());
    verify(ecdsaSignerMock).verifySignature(any(), any(), any());
    verify(signatureUtilsMock).toSignableBytes(transactionMock);

    verify(secp256k1SignatureMock).value();
    verifyNoMoreInteractions(secp256k1SignatureMock);
    verifyNoMoreInteractions(ed25519SignerMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // verify (multi)
  ///////////////////

  @Test
  public void verifyMultiWithNullSigs() {
    assertThrows(NullPointerException.class,
      () -> signatureService.verifyMultiSigned(null, mock(Transaction.class), 1));
  }

  @Test
  public void verifyMultiWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> signatureService.verifyMultiSigned(Sets.newHashSet(), null, 1));
  }

  @Test
  public void verifyMultiWith0MinSigners() {
    assertThrows(IllegalArgumentException.class,
      () -> signatureService.verifyMultiSigned(Sets.newHashSet(), transactionMock, 0));
  }

  @Test
  public void verifyMultiWithNegativeMinSigners() {
    assertThrows(IllegalArgumentException.class,
      () -> signatureService.verifyMultiSigned(Sets.newHashSet(), transactionMock, -1));
  }

  @Test
  public void verifyMultiEd25519() {
    when(signedTransactionMock.signature()).thenReturn(ed25519SignatureMock);
    when(ed25519SignerMock.verifySignature(any())).thenReturn(true);

    boolean actual = signatureService.verifyMultiSigned(Sets.newLinkedHashSet(
      SignatureWithPublicKey.builder().transactionSignature(ed25519SignatureMock)
        .signingPublicKey(ed25519KeyPair.publicKey()).build()), transactionMock, 1);

    assertThat(actual).isTrue();
    verify(ed25519SignerMock).reset();
    verify(ed25519SignerMock).init(anyBoolean(), any());
    verify(ed25519SignerMock).update(any(), anyInt(), anyInt());
    verify(ed25519SignerMock).verifySignature(any());
    verifyNoMoreInteractions(ed25519SignerMock);
    verifyNoMoreInteractions(secp256k1SignatureMock);

    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  @Test
  public void verifyMultiSecp256k1() {
    when(ecdsaSignerMock.verifySignature(any(), any(), any())).thenReturn(true);
    when(signedTransactionMock.signature()).thenReturn(secp256k1SignatureMock);

    boolean actual = signatureService.verifyMultiSigned(Sets.newLinkedHashSet(
      SignatureWithPublicKey.builder().transactionSignature(secp256k1SignatureMock)
        .signingPublicKey(secp256k1KeyPair.publicKey()).build()), transactionMock, 1);

    assertThat(actual).isTrue();
    verify(ecdsaSignerMock).init(anyBoolean(), any());
    verify(ecdsaSignerMock).verifySignature(any(), any(), any());
    verify(signatureUtilsMock).toMultiSignableBytes(any(), any());

    verify(secp256k1SignatureMock).value();
    verifyNoMoreInteractions(secp256k1SignatureMock);
    verifyNoMoreInteractions(ed25519SignerMock);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EdDsaSign
  ///////////////////

  @Test
  public void edDsaSign() {
    Signature actual = signatureService.edDsaSign(ed25519KeyPair.privateKey(), UnsignedByteArray.empty());

    assertThat(actual.value().toByteArray()).isEqualTo(new byte[32]);
    verifyNoMoreInteractions(signatureUtilsMock);
  }

  ///////////////////
  // EcDsaSign
  ///////////////////

  @Test
  public void ecDsaSign() {
    BigInteger[] bigIntegerArray = new BigInteger[2];
    bigIntegerArray[0] = BigInteger.TEN;
    bigIntegerArray[1] = BigInteger.TEN;
    when(ecdsaSignerMock.generateSignature(any())).thenReturn(bigIntegerArray);
    Signature actual = signatureService.ecDsaSign(secp256k1KeyPair.privateKey(), UnsignedByteArray.empty());

    assertThat(actual.value().length()).isEqualTo(8);
    assertThat(actual.value().hexValue()).isEqualTo("300602010A02010A");

    verifyNoMoreInteractions(signatureUtilsMock);
    verify(ecdsaSignerMock).init(anyBoolean(), any());
    verify(ecdsaSignerMock).generateSignature(any());
    verifyNoMoreInteractions(ecdsaSignerMock);
    verifyNoMoreInteractions(ed25519SignerMock);
  }

  ///////////////////
  // edDsaVerify
  ///////////////////

  @Test
  public void edDsaVerifyTrue() {
    when(ed25519SignatureMock.value()).thenReturn(UnsignedByteArray.empty());
    when(ed25519SignerMock.verifySignature(any())).thenReturn(true);
    boolean actual = signatureService.edDsaVerify(ed25519KeyPair.publicKey(), UnsignedByteArray.empty(),
      ed25519SignatureMock);

    assertThat(actual).isTrue();
    verifyNoMoreInteractions(signatureUtilsMock);

    verify(ed25519SignerMock).reset();
    verify(ed25519SignerMock).init(anyBoolean(), any());
    verify(ed25519SignerMock).update(any(), anyInt(), anyInt());
    verify(ed25519SignerMock).verifySignature(any());

    verifyNoMoreInteractions(ecdsaSignerMock);
    verifyNoMoreInteractions(ed25519SignerMock);
  }

  @Test
  public void edDsaVerifyFalse() {
    when(ed25519SignatureMock.value()).thenReturn(UnsignedByteArray.empty());
    when(ed25519SignerMock.verifySignature(any())).thenReturn(false);
    boolean actual = signatureService.edDsaVerify(ed25519KeyPair.publicKey(), UnsignedByteArray.empty(),
      ed25519SignatureMock);

    assertThat(actual).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);

    verify(ed25519SignerMock).reset();
    verify(ed25519SignerMock).init(anyBoolean(), any());
    verify(ed25519SignerMock).update(any(), anyInt(), anyInt());
    Mockito.verify(ed25519SignerMock).verifySignature(any());

    Mockito.verifyNoMoreInteractions(ecdsaSignerMock);
    Mockito.verifyNoMoreInteractions(ed25519SignerMock);
  }

  ///////////////////
  // ecDsaVerify
  ///////////////////

  @Test
  public void ecDsaVerifyTrue() {
    Mockito.when(secp256k1SignatureMock.value()).thenReturn(UnsignedByteArray.of(BaseEncoding.base16().decode(
      "3044022053E28F87B16D34307F8D68CE1B7065FC112AE4B3209109C5326F41DC52E2E5CB022052F3F3E2FFFF2D6023843CA8BC" +
        "A39D6E5B7DE54C4CD91BB76AA952538647A40F")));
    Mockito.when(ecdsaSignerMock.verifySignature(any(), any(), any())).thenReturn(true);

    boolean actual = signatureService.ecDsaVerify(secp256k1KeyPair.publicKey(), UnsignedByteArray.empty(),
      secp256k1SignatureMock);

    assertThat(actual).isTrue();
    verifyNoMoreInteractions(signatureUtilsMock);

    Mockito.verify(ecdsaSignerMock).init(anyBoolean(), any());
    Mockito.verify(ecdsaSignerMock).verifySignature(any(), any(), any());

    Mockito.verifyNoMoreInteractions(ecdsaSignerMock);
    Mockito.verifyNoMoreInteractions(ed25519SignerMock);
  }

  @Test
  public void ecDsaVerifyFalse() {
    Mockito.when(secp256k1SignatureMock.value()).thenReturn(UnsignedByteArray.of(BaseEncoding.base16().decode(
      "3044022053E28F87B16D34307F8D68CE1B7065FC112AE4B3209109C5326F41DC52E2E5CB022052F3F3E2FFFF2D6023843CA8" +
        "BCA39D6E5B7DE54C4CD91BB76AA952538647A40F")));
    Mockito.when(ecdsaSignerMock.verifySignature(any(), any(), any())).thenReturn(false);

    boolean actual = signatureService.ecDsaVerify(secp256k1KeyPair.publicKey(), UnsignedByteArray.empty(),
      secp256k1SignatureMock);

    assertThat(actual).isFalse();
    verifyNoMoreInteractions(signatureUtilsMock);

    Mockito.verify(ecdsaSignerMock).init(anyBoolean(), any());
    Mockito.verify(ecdsaSignerMock).verifySignature(any(), any(), any());

    Mockito.verifyNoMoreInteractions(ecdsaSignerMock);
    Mockito.verifyNoMoreInteractions(ed25519SignerMock);
  }

}
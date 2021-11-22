package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.HashSet;

/**
 * Unit tests for {@link AbstractDelegatedSignatureService}.
 */
class AbstractDelegatedSignatureServiceTest {

  @Mock
  DelegatedTransactionSigner delegatedTransactionSignerMock;

  @Mock
  DelegatedTransactionVerifier delegatedTransactionVerifierMock;

  @Mock
  KeyMetadata keyMetadataMock;

  @Mock
  PublicKey publicKeyMock;

  @Mock
  private SignatureWithKeyMetadata signatureWithKeyMetaMock;

  @Mock
  private Signature signatureMock;

  @Mock
  private Transaction transactionMock;

  @Mock
  private SingleSingedTransaction<Transaction> singleSignedTransactionMock;

  private AbstractDelegatedSignatureService delegatedSignatureService;

  @BeforeEach
  void setUp() {
    openMocks(this);

    when(delegatedTransactionSignerMock.getPublicKey(keyMetadataMock)).thenReturn(publicKeyMock);
    when(delegatedTransactionSignerMock.sign(keyMetadataMock, transactionMock)).thenReturn(singleSignedTransactionMock);
    when(delegatedTransactionSignerMock.sign(eq(keyMetadataMock), any(UnsignedClaim.class))).thenReturn(signatureMock);
    when(delegatedTransactionSignerMock.multiSign(keyMetadataMock, transactionMock))
      .thenReturn(signatureWithKeyMetaMock);

    this.delegatedSignatureService = new AbstractDelegatedSignatureService(
      delegatedTransactionSignerMock,
      delegatedTransactionVerifierMock
    ) { };
  }

  @Test
  void getPublicKey() {
    PublicKey actual = delegatedSignatureService.getPublicKey(keyMetadataMock);
    assertThat(actual).isEqualTo(publicKeyMock);
  }

  @Test
  void signTransaction() {
    SingleSingedTransaction<Transaction> actual = delegatedSignatureService.sign(keyMetadataMock, transactionMock);
    assertThat(actual).isEqualTo(singleSignedTransactionMock);
    verify(delegatedTransactionSignerMock).sign(keyMetadataMock, transactionMock);
  }

  @Test
  void signClaim() {
    UnsignedClaim mockUnsignedClaim = mock(UnsignedClaim.class);
    Signature actual = delegatedSignatureService.sign(keyMetadataMock, mockUnsignedClaim);
    assertThat(actual).isEqualTo(signatureMock);
    verify(delegatedTransactionSignerMock).sign(keyMetadataMock, mockUnsignedClaim);
  }

  @Test
  void multiSignTransaction() {
    SignatureWithKeyMetadata actual = delegatedSignatureService.multiSign(keyMetadataMock, transactionMock);
    assertThat(actual).isEqualTo(signatureWithKeyMetaMock);
    verify(delegatedTransactionSignerMock).multiSign(keyMetadataMock, transactionMock);
  }

  @Test
  void verifySingleSigned() {
    delegatedSignatureService.verifySingleSigned(signatureWithKeyMetaMock, transactionMock);
    verify(delegatedTransactionVerifierMock).verifySingleSigned(signatureWithKeyMetaMock, transactionMock);
  }

  @Test
  void verifyMultiSignedWithoutMinSigners() {
    HashSet<SignatureWithKeyMetadata> signatureSet = Sets.newHashSet(signatureWithKeyMetaMock);
    delegatedSignatureService.verifyMultiSigned(signatureSet, transactionMock);
    verify(delegatedTransactionVerifierMock).verifyMultiSigned(signatureSet, transactionMock);
  }

  @Test
  void verifyMultiSignedWithMinSigners() {
    HashSet<SignatureWithKeyMetadata> signatureSet = Sets.newHashSet(signatureWithKeyMetaMock);
    int minSigners = 1;
    delegatedSignatureService.verifyMultiSigned(signatureSet, transactionMock, minSigners);
    verify(delegatedTransactionVerifierMock).verifyMultiSigned(signatureSet, transactionMock, minSigners);
  }
}

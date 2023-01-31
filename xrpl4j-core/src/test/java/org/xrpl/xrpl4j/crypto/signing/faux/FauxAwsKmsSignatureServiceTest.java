package org.xrpl.xrpl4j.crypto.signing.faux;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.google.common.collect.Sets;
import org.immutables.value.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.AbstractSignatureService;
import org.xrpl.xrpl4j.crypto.signing.AbstractTransactionSigner;
import org.xrpl.xrpl4j.crypto.signing.AbstractTransactionVerifier;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;

/**
 * Tests the contract that a GCP KMS SignatureService might use.
 */
class FauxAwsKmsSignatureServiceTest {

  @Mock
  AbstractTransactionSigner<AwsKmsPrivateKeyReference> abstractTransactionSigner;

  @Mock
  AbstractTransactionVerifier abstractTransactionVerifier;

  @Mock
  AwsKmsPrivateKeyReference privateKeyReferenceMock;

  @Mock
  PublicKey publicKeyMock;

  @Mock
  private Signer signerMock;

  @Mock
  private Signature signatureMock;

  @Mock
  private Transaction transactionMock;

  @Mock
  private SingleSignedTransaction<Transaction> singleSignedTransactionMock;

  private SignatureService<AwsKmsPrivateKeyReference> signatureService;

  @BeforeEach
  void setUp() {
    openMocks(this);

    when(abstractTransactionSigner.derivePublicKey(privateKeyReferenceMock)).thenReturn(publicKeyMock);
    when(abstractTransactionSigner.sign(privateKeyReferenceMock, transactionMock)).thenReturn(
      singleSignedTransactionMock);
    when(abstractTransactionSigner.sign(eq(privateKeyReferenceMock), any(UnsignedClaim.class))).thenReturn(
      signatureMock);
    when(abstractTransactionSigner.multiSign(privateKeyReferenceMock, transactionMock)).thenReturn(signatureMock);

    this.signatureService = new AbstractSignatureService<AwsKmsPrivateKeyReference>(
      abstractTransactionSigner, abstractTransactionVerifier
    ) {
      @Override
      public PublicKey derivePublicKey(AwsKmsPrivateKeyReference privateKeyable) {
        return publicKeyMock;
      }

      @Override
      protected Signature edDsaSign(AwsKmsPrivateKeyReference privateKey, UnsignedByteArray signableTransactionBytes) {
        return null;
      }

      @Override
      protected Signature ecDsaSign(AwsKmsPrivateKeyReference privateKey, UnsignedByteArray signableTransactionBytes) {
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
    };
  }

  @Test
  void getPublicKey() {
    PublicKey actual = signatureService.derivePublicKey(privateKeyReferenceMock);
    assertThat(actual).isEqualTo(publicKeyMock);
  }

  @Test
  void signTransaction() {
    SingleSignedTransaction<Transaction> actual = signatureService.sign(privateKeyReferenceMock, transactionMock);
    assertThat(actual).isEqualTo(singleSignedTransactionMock);
    verify(abstractTransactionSigner).sign(privateKeyReferenceMock, transactionMock);
  }

  @Test
  void signClaim() {
    UnsignedClaim mockUnsignedClaim = mock(UnsignedClaim.class);
    Signature actual = signatureService.sign(privateKeyReferenceMock, mockUnsignedClaim);
    assertThat(actual).isEqualTo(signatureMock);
    verify(abstractTransactionSigner).sign(privateKeyReferenceMock, mockUnsignedClaim);
  }

  @Test
  void multiSignTransaction() {
    Signature actual = signatureService.multiSign(privateKeyReferenceMock, transactionMock);
    assertThat(actual).isEqualTo(signatureMock);
    verify(abstractTransactionSigner).multiSign(privateKeyReferenceMock, transactionMock);
  }

  @Test
  void verifySingleSigned() {
    signatureService.verify(signerMock, transactionMock);
    verify(abstractTransactionVerifier).verify(signerMock, transactionMock);
  }

  @Test
  void verifyMultiSignedWithoutMinSigners() {
    Set<Signer> signerSet = Sets.newHashSet(signerMock);
    signatureService.verifyMultiSigned(signerSet, transactionMock);
    verify(abstractTransactionVerifier).verifyMultiSigned(signerSet, transactionMock, 1);
  }

  @Test
  void verifyMultiSignedWithMinSigners() {
    Set<Signer> signerSet = Sets.newHashSet(signerMock);
    int minSigners = 1;
    signatureService.verifyMultiSigned(signerSet, transactionMock, minSigners);
    verify(abstractTransactionVerifier).verifyMultiSigned(signerSet, transactionMock, minSigners);
  }

  /**
   * Exists only to prove out the {@link PrivateKeyReference} interface.
   */
  @Value.Immutable
  public interface AwsKmsPrivateKeyReference extends PrivateKeyReference {

    /**
     * A Builder for immutables.
     *
     * @return A {@link ImmutableAwsKmsPrivateKeyReference.Builder}.
     */
    static ImmutableAwsKmsPrivateKeyReference.Builder builder() {
      return ImmutableAwsKmsPrivateKeyReference.builder();
    }

    /**
     * The unique identifier of the platform that can decode this secret.
     *
     * @return A {@link String}.
     */
    String platformIdentifier();
  }
}

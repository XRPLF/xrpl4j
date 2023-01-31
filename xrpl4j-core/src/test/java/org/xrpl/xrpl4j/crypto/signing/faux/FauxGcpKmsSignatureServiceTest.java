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

import java.util.Optional;
import java.util.Set;

/**
 * Tests the contract that a GCP KMS SignatureService might use.
 */
class FauxGcpKmsSignatureServiceTest {

  @Mock
  AbstractTransactionSigner<GcpKmsPrivateKeyReference> gcpKmsTransactionSignerMock;

  @Mock
  AbstractTransactionVerifier gcpKmsTransactionVerifierMock;

  @Mock
  GcpKmsPrivateKeyReference privateKeyReferenceMock;

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

  private SignatureService<GcpKmsPrivateKeyReference> gcpKmsSignatureService;

  @BeforeEach
  void setUp() {
    openMocks(this);

    when(gcpKmsTransactionSignerMock.derivePublicKey(privateKeyReferenceMock)).thenReturn(publicKeyMock);
    when(gcpKmsTransactionSignerMock.sign(privateKeyReferenceMock, transactionMock)).thenReturn(
      singleSignedTransactionMock);
    when(gcpKmsTransactionSignerMock.sign(eq(privateKeyReferenceMock), any(UnsignedClaim.class))).thenReturn(
      signatureMock);
    when(gcpKmsTransactionSignerMock.multiSign(privateKeyReferenceMock, transactionMock)).thenReturn(signatureMock);

    this.gcpKmsSignatureService = new AbstractSignatureService<GcpKmsPrivateKeyReference>(
      gcpKmsTransactionSignerMock, gcpKmsTransactionVerifierMock
    ) {
      @Override
      public PublicKey derivePublicKey(GcpKmsPrivateKeyReference privateKeyable) {
        return publicKeyMock;
      }

      @Override
      protected Signature edDsaSign(GcpKmsPrivateKeyReference privateKey, UnsignedByteArray signableTransactionBytes) {
        return signatureMock;
      }

      @Override
      protected Signature ecDsaSign(GcpKmsPrivateKeyReference privateKey, UnsignedByteArray signableTransactionBytes) {
        return signatureMock;
      }

      @Override
      protected boolean edDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        return true;
      }

      @Override
      protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        return true;
      }
    };
  }

  @Test
  void derivePublicKey() {
    PublicKey actual = gcpKmsSignatureService.derivePublicKey(privateKeyReferenceMock);
    assertThat(actual).isEqualTo(publicKeyMock);
  }

  @Test
  void signTransaction() {
    SingleSignedTransaction<Transaction> actual = gcpKmsSignatureService.sign(privateKeyReferenceMock, transactionMock);
    assertThat(actual).isEqualTo(singleSignedTransactionMock);
    verify(gcpKmsTransactionSignerMock).sign(privateKeyReferenceMock, transactionMock);
  }

  @Test
  void signClaim() {
    UnsignedClaim mockUnsignedClaim = mock(UnsignedClaim.class);
    Signature actual = gcpKmsSignatureService.sign(privateKeyReferenceMock, mockUnsignedClaim);
    assertThat(actual).isEqualTo(signatureMock);
    verify(gcpKmsTransactionSignerMock).sign(privateKeyReferenceMock, mockUnsignedClaim);
  }

  @Test
  void multiSignTransaction() {
    Signature actual = gcpKmsSignatureService.multiSign(privateKeyReferenceMock, transactionMock);
    assertThat(actual).isEqualTo(signatureMock);
    verify(gcpKmsTransactionSignerMock).multiSign(privateKeyReferenceMock, transactionMock);
  }

  @Test
  void verifySingleSigned() {
    gcpKmsSignatureService.verify(signerMock, transactionMock);
    verify(gcpKmsTransactionVerifierMock).verify(signerMock, transactionMock);
  }

  @Test
  void verifyMultiSignedWithoutMinSigners() {
    Set<Signer> signerSet = Sets.newHashSet(signerMock);
    gcpKmsSignatureService.verifyMultiSigned(signerSet, transactionMock);
    verify(gcpKmsTransactionVerifierMock).verifyMultiSigned(signerSet, transactionMock, 1);
  }

  @Test
  void verifyMultiSignedWithMinSigners() {
    Set<Signer> signerSet = Sets.newHashSet(signerMock);
    int minSigners = 1;
    gcpKmsSignatureService.verifyMultiSigned(signerSet, transactionMock, minSigners);
    verify(gcpKmsTransactionVerifierMock).verifyMultiSigned(signerSet, transactionMock, minSigners);
  }

  /**
   * Exists only to prove out the {@link PrivateKeyReference} interface.
   */
  @Value.Immutable
  interface GcpKmsPrivateKeyReference extends PrivateKeyReference {

    /**
     * A Builder for immutables.
     *
     * @return A {@link ImmutableGcpKmsPrivateKeyReference.Builder}.
     */
    static ImmutableGcpKmsPrivateKeyReference.Builder builder() {
      return ImmutableGcpKmsPrivateKeyReference.builder();
    }

    /**
     * The unique identifier of the platform that can decode this secret.
     *
     * @return A {@link String}.
     */
    String platformIdentifier();

    /**
     * The unique identifier of the keyring that holds the the private-key used to encrypt this encoded secret.
     *
     * @return A {@link String}.
     */
    String keyringIdentifier();

    /**
     * The unique identifier for the private-key used to encrypt this encoded secret.
     *
     * @return A {@link String}.
     */
    String keyIdentifier();

    /**
     * The version of the encryption key used to encrypt this secret.
     *
     * @return A {@link String}.
     */
    String keyVersion();

    /**
     * An optional password that will unlock this particular key.
     *
     * @return An {@link Optional} of type {@link String}.
     */
    Optional<String> keyPassword();
  }
}

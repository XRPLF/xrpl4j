package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;

import java.util.Collections;

/**
 * Unit tests for {@link SponsorSignature}.
 */
public class SponsorSignatureTest {

  private static final String TEST_PUBLIC_KEY =
    "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A";
  private static final String TEST_SIGNATURE =
    "3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
      "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA";

  @Test
  public void buildWithSingleSignature() {
    PublicKey publicKey = PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY);
    Signature signature = Signature.fromBase16(TEST_SIGNATURE);

    SponsorSignature sponsorSignature = SponsorSignature.builder()
      .signingPublicKey(publicKey)
      .transactionSignature(signature)
      .build();

    assertThat(sponsorSignature.signingPublicKey()).isPresent();
    assertThat(sponsorSignature.signingPublicKey().get()).isEqualTo(publicKey);
    assertThat(sponsorSignature.transactionSignature()).isPresent();
    assertThat(sponsorSignature.signers()).isEmpty();
  }

  @Test
  public void buildWithMultiSignature() {
    Signer signer = Signer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
      .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
      .build();

    SponsorSignature signature = SponsorSignature.builder()
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .signers(Collections.singletonList(SignerWrapper.of(signer)))
      .build();

    assertThat(signature.signingPublicKey()).isPresent();
    assertThat(signature.signingPublicKey().get()).isEqualTo(PublicKey.MULTI_SIGN_PUBLIC_KEY);
    assertThat(signature.transactionSignature()).isEmpty();
    assertThat(signature.signers()).isPresent();
    assertThat(signature.signers().get()).hasSize(1);
  }

  @Test
  public void buildWithBothSignatureTypesFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
        .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
        .signers(Collections.singletonList(
          SignerWrapper.of(Signer.builder()
            .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
            .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
            .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
            .build())
        ))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorSignature must have either TxnSignature or Signers, but not both");
  }

  @Test
  public void buildWithNeitherSignatureTypeFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorSignature must have either TxnSignature or Signers");
  }

  @Test
  public void buildWithEmptySigningPublicKeyAndSingleSignatureFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
        .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SigningPubKey must be non-empty when using TxnSignature");
  }

  @Test
  public void buildWithNonEmptySigningPublicKeyAndMultiSignatureFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
        .signers(Collections.singletonList(
          SignerWrapper.of(Signer.builder()
            .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
            .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
            .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
            .build())
        ))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SigningPubKey must be empty when using Signers");
  }

  @Test
  public void buildWithMissingSigningPublicKeyAndSingleSignatureFails() {
    // Test the case where transactionSignature is present but signingPublicKey is missing
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SigningPubKey must be non-empty when using TxnSignature");
  }

  @Test
  public void buildWithMissingSigningPublicKeyAndMultiSignatureFails() {
    // Test the case where signers is present but signingPublicKey is missing
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signers(Collections.singletonList(
          SignerWrapper.of(Signer.builder()
            .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
            .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
            .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
            .build())
        ))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SigningPubKey must be empty when using Signers");
  }

}


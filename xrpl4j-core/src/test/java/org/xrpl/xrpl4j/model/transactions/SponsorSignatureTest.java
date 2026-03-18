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

import java.util.Collections;

/**
 * Unit tests for {@link SponsorSignature}.
 */
public class SponsorSignatureTest {

  @Test
  public void buildWithSingleSignature() {
    SponsorSignature signature = SponsorSignature.builder()
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .transactionSignature("3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
        "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA")
      .build();

    assertThat(signature.signingPublicKey()).isEqualTo("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A");
    assertThat(signature.transactionSignature()).isPresent();
    assertThat(signature.signers()).isEmpty();
  }

  @Test
  public void buildWithMultiSignature() {
    Signer signer = Signer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      .transactionSignature("3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
        "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA")
      .build();

    SponsorSignature signature = SponsorSignature.builder()
      .signingPublicKey("")
      .signers(Collections.singletonList(signer))
      .build();

    assertThat(signature.signingPublicKey()).isEqualTo("");
    assertThat(signature.transactionSignature()).isEmpty();
    assertThat(signature.signers()).isPresent().get().hasSize(1);
  }

  @Test
  public void buildWithBothSignatureTypesFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
        .transactionSignature("3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
          "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA")
        .signers(Collections.singletonList(
          Signer.builder()
            .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
            .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
            .transactionSignature("3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
              "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA")
            .build()
        ))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorSignature must have either TxnSignature or Signers, but not both");
  }

  @Test
  public void buildWithNeitherSignatureTypeFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorSignature must have either TxnSignature or Signers");
  }

  @Test
  public void buildWithEmptySigningPublicKeyAndSingleSignatureFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey("")
        .transactionSignature("3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
          "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA")
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SigningPubKey must be non-empty when using TxnSignature");
  }

  @Test
  public void buildWithNonEmptySigningPublicKeyAndMultiSignatureFails() {
    assertThatThrownBy(() ->
      SponsorSignature.builder()
        .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
        .signers(Collections.singletonList(
          Signer.builder()
            .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
            .signingPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
            .transactionSignature("3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
              "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA")
            .build()
        ))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SigningPubKey must be empty when using Signers");
  }

}


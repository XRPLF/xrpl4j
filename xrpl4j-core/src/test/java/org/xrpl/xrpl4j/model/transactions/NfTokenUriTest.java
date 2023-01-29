package org.xrpl.xrpl4j.model.transactions;

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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NfTokenUriTest {

  @Test
  public void nfTokenUriEquality() {

    String hexUri = "697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634646675796" +
      "C71616266336F636C67747179353566627A6469";
    String plaintextUri = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";

    assertThat(NfTokenUri.of(hexUri).equals(NfTokenUri.of(hexUri)));

    assertThat(NfTokenUri.ofPlainText(plaintextUri).equals(NfTokenUri.ofPlainText(plaintextUri)));

    assertThat(NfTokenUri.of("0000000000000000000000000000000000000000000000000000000000000000")).isNotEqualTo(null);
    Assertions.assertThat(NfTokenUri.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(new Object());

    assertThat(NfTokenUri.ofPlainText(plaintextUri).equals(NfTokenUri.of(hexUri)));

    assertThat(NfTokenUri.ofPlainText(plaintextUri).value().equals(NfTokenUri.of(hexUri).value()));

    String uri2 = "https://upload.wikimedia.org/wikipedia/commons/4/43/Valdai_IverskyMon_asv2018_img47.jpg";

    assertThat(NfTokenUri.ofPlainText(uri2).equals(NfTokenUri.ofPlainText(uri2)));

    String hexUri2 = "68747470733A2F2F75706C6F61642E77696B696D656469612E6F72672F77696B6970656469612F636F6D6" +
      "D6F6E732F342F34332F56616C6461695F49766572736B794D6F6E5F617376323031385F696D6734372E6A7067";

    Assertions.assertThat(NfTokenUri.of(hexUri2).equals(NfTokenUri.of(hexUri2)));

    assertThat(NfTokenUri.ofPlainText(uri2).equals(NfTokenUri.of(hexUri2)));

  }

}

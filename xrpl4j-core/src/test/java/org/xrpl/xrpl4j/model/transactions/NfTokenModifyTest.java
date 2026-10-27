package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NfTokenModifyTest {

  private static final NfTokenId NFTOKEN_ID =
    NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");

  @Test
  public void buildTx() {
    NfTokenModify nfTokenModify = NfTokenModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .nfTokenId(NFTOKEN_ID)
      .build();

    assertThat(nfTokenModify.transactionType()).isEqualTo(TransactionType.NFTOKEN_MODIFY);
    assertThat(nfTokenModify.nfTokenId()).isEqualTo(NFTOKEN_ID);
    assertThat(nfTokenModify.owner()).isEmpty();
    assertThat(nfTokenModify.uri()).isEmpty();
  }

  @Test
  public void buildTxWithOwnerAndUri() {
    Address owner = Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn");
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");
    NfTokenModify nfTokenModify = NfTokenModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .nfTokenId(NFTOKEN_ID)
      .owner(owner)
      .uri(uri)
      .build();

    assertThat(nfTokenModify.owner()).hasValue(owner);
    assertThat(nfTokenModify.uri()).hasValue(uri);
  }

  @Test
  public void buildTxWithMissingNfTokenId() {
    assertThrows(
      IllegalStateException.class,
      () -> NfTokenModify.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .build()
    );
  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    NfTokenModify nfTokenModify = NfTokenModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .nfTokenId(NFTOKEN_ID)
      .build();

    assertThat(nfTokenModify.transactionFlags()).isEqualTo(nfTokenModify.flags());
    assertThat(nfTokenModify.transactionFlags().isEmpty()).isTrue();
  }
}

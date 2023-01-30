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

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

public class UnlModifyTest {

  @Test
  public void testBuilder() {
    String validator = "EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539";
    UnlModify unlModify = UnlModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator(validator)
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    assertThat(unlModify.transactionType()).isEqualTo(TransactionType.UNL_MODIFY);
    assertThat(unlModify.account()).isEqualTo(UnlModify.ACCOUNT_ZERO);
    assertThat(unlModify.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(unlModify.sequence()).isEqualTo(UnsignedInteger.valueOf(2470665));
    assertThat(unlModify.ledgerSequence()).isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(67850752)));
    assertThat(unlModify.unlModifyValidator()).isEqualTo(validator);
  }
}

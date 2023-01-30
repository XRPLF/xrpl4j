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

import java.util.Optional;

public class EnableAmendmentTest {

  @Test
  public void testBuilder() {

    Hash256 amendment = Hash256.of("42426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369F97ABF044EE");
    EnableAmendment enableAmendment = EnableAmendment.builder()
      .account(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .amendment(amendment)
      .ledgerSequence(Optional.of(LedgerIndex.of(UnsignedInteger.valueOf(67850752))))
      .build();

    assertThat(enableAmendment.transactionType()).isEqualTo(TransactionType.ENABLE_AMENDMENT);
    assertThat(enableAmendment.account()).isEqualTo(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"));
    assertThat(enableAmendment.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(enableAmendment.sequence()).isEqualTo(UnsignedInteger.valueOf(2470665));
    assertThat(enableAmendment.ledgerSequence()).isNotEmpty().get()
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(67850752)));
    assertThat(enableAmendment.amendment()).isEqualTo(amendment);
  }
}

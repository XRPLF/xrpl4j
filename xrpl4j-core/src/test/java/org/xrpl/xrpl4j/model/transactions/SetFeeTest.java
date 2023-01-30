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

public class SetFeeTest {

  @Test
  public void testBuilder() {
    SetFee setFee = SetFee.builder()
      .account(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .baseFee("000000000000000A")
      .referenceFeeUnits(UnsignedInteger.valueOf(10))
      .reserveBase(UnsignedInteger.valueOf(20000000))
      .reserveIncrement(UnsignedInteger.valueOf(5000000))
      .ledgerSequence(Optional.of(LedgerIndex.of(UnsignedInteger.valueOf(67850752))))
      .build();

    assertThat(setFee.transactionType()).isEqualTo(TransactionType.SET_FEE);
    assertThat(setFee.account()).isEqualTo(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"));
    assertThat(setFee.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(setFee.sequence()).isEqualTo(UnsignedInteger.valueOf(2470665));
    assertThat(setFee.ledgerSequence()).isNotEmpty().get().isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(67850752)));
    assertThat(setFee.referenceFeeUnits()).isEqualTo(UnsignedInteger.valueOf(10));
    assertThat(setFee.reserveIncrement()).isEqualTo(UnsignedInteger.valueOf(5000000));
    assertThat(setFee.reserveBase()).isEqualTo(UnsignedInteger.valueOf(20000000));
  }
}

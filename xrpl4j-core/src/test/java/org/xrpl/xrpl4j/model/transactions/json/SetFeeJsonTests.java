package org.xrpl.xrpl4j.model.transactions.json;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.SetFee;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

public class SetFeeJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
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

    String json = "{" +
      "\"Account\":\"rrrrrrrrrrrrrrrrrrrrrhoLvTp\"," +
      "\"Fee\":\"12\"," +
      "\"LedgerSequence\":67850752," +
      "\"Sequence\":2470665," +
      "\"SigningPubKey\":\"\"," +
      "\"TransactionType\":\"SetFee\"," +
      "\"ReserveIncrement\":5000000," +
      "\"ReserveBase\":20000000," +
      "\"ReferenceFeeUnits\":10," +
      "\"BaseFee\":\"000000000000000A\"}";

    assertCanSerializeAndDeserialize(setFee, json);
  }
}

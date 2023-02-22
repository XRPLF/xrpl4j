package org.xrpl.xrpl4j.model.client.accounts;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountTransactionsResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountTransactionsResult result = AccountTransactionsResult.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerIndexMaximum(LedgerIndexBound.of(57112019))
      .ledgerIndexMinimum(LedgerIndexBound.of(56248229))
      .limit(UnsignedInteger.valueOf(2))
      .marker(Marker.of("{\"ledger\":57112007,\"seq\":13}"))
      .status("success")
      .addTransactions(
        AccountTransactionsTransactionResult.builder()
          .resultTransaction(AccountTransactionsTransaction.builder()
            .transaction(
              Payment.builder()
                .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
                .amount(XrpCurrencyAmount.ofDrops(455643030))
                .destination(Address.of("raLPjTYeGezfdb6crXZzcC8RkLBEwbBHJ5"))
                .destinationTag(UnsignedInteger.valueOf(18240312))
                .fee(XrpCurrencyAmount.ofDrops(40))
                .flags(PaymentFlags.of(2147483648L))
                .lastLedgerSequence(UnsignedInteger.valueOf(57112037))
                .sequence(UnsignedInteger.valueOf(702819))
                .signingPublicKey(
                  PublicKey.fromBase16EncodedPublicKey(
                    "020A46D8D02AC780C59853ACA309EAA92E7D8E02DD72A0B6AC315A7D18A6C3276A"
                  )
                )
                .transactionSignature(Signature.fromBase16(
                  "30450221008602B2E390C0C7B65182C6DBC86292052C1961B2BEFB79C2C8431722C0ADB9110" +
                    "22024B74DCF910A4C8C95572CF662EB7F5FF67E1AC4D7B9B7BFE2A8EE851EC16576"
                ))
                .build()
            )
            .hash(Hash256.of("08EF5BDA2825D7A28099219621CDBECCDECB828FEA202DEB6C7ACD5222D36C2C"))
            .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(57112015)))
            .closeDate(UnsignedLong.valueOf(666212460))
            .build())
          .metadata(
            TransactionMetadata.builder()
              .transactionIndex(UnsignedInteger.valueOf(12))
              .transactionResult(TransactionResultCodes.TES_SUCCESS)
              .deliveredAmount(XrpCurrencyAmount.ofDrops(455643030))
              .build()
          )
          .validated(true)
          .build(),
        AccountTransactionsTransactionResult.builder()
          .resultTransaction(AccountTransactionsTransaction.builder()
            .transaction(Payment.builder()
              .account(Address.of("rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg"))
              .amount(XrpCurrencyAmount.ofDrops(499500387))
              .destination(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
              .destinationTag(UnsignedInteger.valueOf(1))
              .fee(XrpCurrencyAmount.ofDrops(40))
              .flags(PaymentFlags.of(2147483648L))
              .lastLedgerSequence(UnsignedInteger.valueOf(57112032))
              .sequence(UnsignedInteger.valueOf(466334))
              .signingPublicKey(
                PublicKey.fromBase16EncodedPublicKey(
                  "0381575032E254BF4D699C3D8D6EFDB63B3A71F97475C6F6885BC7DAEEE55D9A01"
                )
              )
              .transactionSignature(Signature.fromBase16(
                "3045022100C7EA1701FE48C75508EEBADBC9864CD3FFEDCEB48AB99AEA960BFA360AE163" +
                  "ED0220453C9577502924C9E1A9A450D4B950A44016813BC70E1F16A65A402528D730B7"
              ))
              .build())
            .hash(Hash256.of("7C031FD5B710E3C048EEF31254089BEEC505900BCC9A842257A0319453333998"))
            .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(57112010)))
            .build())
          .metadata(
            TransactionMetadata.builder()
              .transactionIndex(UnsignedInteger.valueOf(33))
              .transactionResult(TransactionResultCodes.TES_SUCCESS)
              .deliveredAmount(XrpCurrencyAmount.ofDrops(499500387))
              .build()
          )
          .validated(true)
          .build()
      )
      .validated(true)
      .build();

    String json = "{\n" +
      "        \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "        \"ledger_index_max\": 57112019,\n" +
      "        \"ledger_index_min\": 56248229,\n" +
      "        \"limit\": 2,\n" +
      "        \"marker\": {\n" +
      "            \"ledger\": 57112007,\n" +
      "            \"seq\": 13\n" +
      "        },\n" +
      "        \"status\": \"success\",\n" +
      "        \"transactions\": [\n" +
      "            {\n" +
      "                \"meta\": {\n" +
      "                    \"TransactionIndex\": 12,\n" +
      "                    \"TransactionResult\": \"tesSUCCESS\",\n" +
      "                    \"delivered_amount\": \"455643030\"\n" +
      "                },\n" +
      "                \"tx\": {\n" +
      "                    \"Account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "                    \"Amount\": \"455643030\",\n" +
      "                    \"Destination\": \"raLPjTYeGezfdb6crXZzcC8RkLBEwbBHJ5\",\n" +
      "                    \"DestinationTag\": 18240312,\n" +
      "                    \"Fee\": \"40\",\n" +
      "                    \"Flags\": 2147483648,\n" +
      "                    \"LastLedgerSequence\": 57112037,\n" +
      "                    \"Sequence\": 702819,\n" +
      "                    \"SigningPubKey\": " +
      "\"020A46D8D02AC780C59853ACA309EAA92E7D8E02DD72A0B6AC315A7D18A6C3276A\",\n" +
      "                    \"TransactionType\": \"Payment\",\n" +
      "                    \"TxnSignature\": \"30450221008602B2E390C0C7B65182C6DBC86292052C1961B2BEFB" +
      "79C2C8431722C0ADB911022024B74DCF910A4C8C95572CF662EB7F5FF67E1AC4D7B9B7BFE2A8EE851EC16576\",\n" +
      "                    \"hash\": \"08EF5BDA2825D7A28099219621CDBECCDECB828FEA202DEB6C7ACD5222D36C2C\",\n" +
      "                    \"date\": 666212460,\n" +
      "                    \"ledger_index\": 57112015\n" +
      "                },\n" +
      "                \"validated\": true\n" +
      "            },\n" +
      "            {\n" +
      "                \"meta\": {\n" +
      "                    \"TransactionIndex\": 33,\n" +
      "                    \"TransactionResult\": \"tesSUCCESS\",\n" +
      "                    \"delivered_amount\": \"499500387\"\n" +
      "                },\n" +
      "                \"tx\": {\n" +
      "                    \"Account\": \"rw2ciyaNshpHe7bCHo4bRWq6pqqynnWKQg\",\n" +
      "                    \"Amount\": \"499500387\",\n" +
      "                    \"Destination\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "                    \"DestinationTag\": 1,\n" +
      "                    \"Fee\": \"40\",\n" +
      "                    \"Flags\": 2147483648,\n" +
      "                    \"LastLedgerSequence\": 57112032,\n" +
      "                    \"Sequence\": 466334,\n" +
      "                    \"SigningPubKey\": " +
      "\"0381575032E254BF4D699C3D8D6EFDB63B3A71F97475C6F6885BC7DAEEE55D9A01\",\n" +
      "                    \"TransactionType\": \"Payment\",\n" +
      "                    \"TxnSignature\": \"3045022100C7EA1701FE48C75508EEBADBC9864CD3FFEDCEB4" +
      "8AB99AEA960BFA360AE163ED0220453C9577502924C9E1A9A450D4B950A44016813BC70E1F16A65A402528D730B7\",\n" +
      "                    \"hash\": \"7C031FD5B710E3C048EEF31254089BEEC505900BCC9A842257A0319453333998\",\n" +
      "                    \"ledger_index\": 57112010\n" +
      "                },\n" +
      "                \"validated\": true\n" +
      "            }\n" +
      "        ],\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {
    AccountTransactionsResult result = AccountTransactionsResult.builder()
      .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
      .ledgerIndexMaximum(LedgerIndexBound.of(57112019))
      .ledgerIndexMinimum(LedgerIndexBound.of(56248229))
      .status("success")
      .validated(true)
      .build();

    String json = "{\n" +
      "        \"account\": \"rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w\",\n" +
      "        \"ledger_index_max\": 57112019,\n" +
      "        \"ledger_index_min\": 56248229,\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}

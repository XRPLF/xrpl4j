package org.xrpl.xrpl4j.model.transactions.json;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenModify;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class NfTokenModifyJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalNfTokenModifyJson() throws JsonProcessingException, JSONException {
    NfTokenModify nfTokenModify = NfTokenModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.valueOf(12))
      .nfTokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .networkId(NetworkId.of(1024))
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenModify\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"NFTokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"NetworkID\": 1024,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenModify, json);
  }

  @Test
  public void testNfTokenModifyJsonWithOwnerAndUri() throws JsonProcessingException, JSONException {
    NfTokenModify nfTokenModify = NfTokenModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.valueOf(12))
      .nfTokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .uri(NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenModify\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"NFTokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"URI\": \"697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634" +
      "646675796C71616266336F636C67747179353566627A6469\",\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\"\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenModify, json);
  }

  /**
   * Deserializes a validated, delegated {@link NfTokenModify} from Testnet (BDAC8AD4..., ledger 21867517) into the
   * model and checks that re-serializing it yields exactly the binary that xrpld recorded on ledger.
   */
  @Test
  public void testTestnetNfTokenModifyEncodesToXrpldBinary() throws JsonProcessingException {
    String json = "{\n" +
      "    \"Account\": \"rJoRnWmGuySwcKK61CULzxAPn75gFQ8qSk\",\n" +
      "    \"Delegate\": \"rJm2W7c1RwrURVUYfRAJ4YumkwmNHpbwhV\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 0,\n" +
      "    \"LastLedgerSequence\": 21867536,\n" +
      "    \"NFTokenID\": \"00190000C3415F2890DE754DA8EF18EB4E7F218787E5D7EA33F3E9A1014DABF4\",\n" +
      "    \"Sequence\": 21867509,\n" +
      "    \"SigningPubKey\": \"ED89E58C2B6C2E47281B77A215D1A4BC14634B314D747F5C4E08605534DF7DAB16\",\n" +
      "    \"TransactionType\": \"NFTokenModify\",\n" +
      "    \"TxnSignature\": \"F5078FB7BBBF0C1605CD449DE61F77F3EC4FDFA42210D86B499261FADF1FF1FCD78DAF4B4F0EF251" +
      "761CB793D13625A3E9947B5632FD6B1F2B8CD97046E0600C\",\n" +
      "    \"URI\": \"68747470733A2F2F687669332E636F6D2F746573742D34362D7632\"\n" +
      "}";
    String xrpldBinary =
      "12003D220000000024014DABF5201B014DAC105A00190000C3415F2890DE754DA8EF18EB4E7F218787E5D7EA33F3E9A1014D" +
      "ABF468400000000000000C7321ED89E58C2B6C2E47281B77A215D1A4BC14634B314D747F5C4E08605534DF7DAB167440F507" +
      "8FB7BBBF0C1605CD449DE61F77F3EC4FDFA42210D86B499261FADF1FF1FCD78DAF4B4F0EF251761CB793D13625A3E9947B56" +
      "32FD6B1F2B8CD97046E0600C751B68747470733A2F2F687669332E636F6D2F746573742D34362D76328114C3415F2890DE75" +
      "4DA8EF18EB4E7F218787E5D7EA8C14C2E869717E7BE1DD92BD2BD383A341A9EE4E2C2C";

    Transaction transaction = objectMapper.readValue(json, Transaction.class);

    assertThat(transaction).isInstanceOf(NfTokenModify.class);
    NfTokenModify nfTokenModify = (NfTokenModify) transaction;
    assertThat(nfTokenModify.delegate()).hasValue(Address.of("rJm2W7c1RwrURVUYfRAJ4YumkwmNHpbwhV"));
    assertThat(nfTokenModify.uri()).hasValue(NfTokenUri.of("68747470733A2F2F687669332E636F6D2F746573742D34362D7632"));
    assertThat(XrplBinaryCodec.getInstance().encode(objectMapper.writeValueAsString(nfTokenModify)))
      .isEqualTo(xrpldBinary);
  }
}
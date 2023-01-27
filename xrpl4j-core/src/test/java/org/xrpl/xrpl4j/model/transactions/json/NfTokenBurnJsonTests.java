package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenBurn;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class NfTokenBurnJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalNfTokenBurnJson() throws JsonProcessingException, JSONException {

    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.valueOf(12))
      .nfTokenId(id)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenBurn\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"NFTokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Flags\": 2147483648\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenBurn, json);
  }
}

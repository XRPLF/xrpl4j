package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;

class MpTokenIssuanceSetTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenIssuanceSet issuanceSet = MpTokenIssuanceSet.builder()
      .account(Address.of("rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd"))
      .sequence(UnsignedInteger.valueOf(335))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4"))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148")
      )
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rBcfczVUsaQTGNVGQ63hGZHmLNNzJr3gMd\",\n" +
      "  \"TransactionType\" : \"MPTokenIssuanceSet\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 335,\n" +
      "  \"SigningPubKey\" : \"ED6EC29EF994F886D623A58B4CDB36DAFDBB7812C289E17B770EDF7E3B2F53E148\",\n" +
      "  \"MPTokenIssuanceID\" : \"0000014D745557D1E15173E54C7A8445DA5B28C50E90C7D4\"\n" +
      "}";

    assertCanSerializeAndDeserialize(issuanceSet, json);
  }

}
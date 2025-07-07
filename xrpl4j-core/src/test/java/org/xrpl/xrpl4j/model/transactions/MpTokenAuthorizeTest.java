package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;

class MpTokenAuthorizeTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(Address.of("rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(432))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7")
      )
      .flags(MpTokenAuthorizeFlags.UNAUTHORIZE)
      .mpTokenIssuanceId(MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41"))
      .build();

    String json =
      "{\n" +
      "  \"Account\" : \"rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV\",\n" +
      "  \"TransactionType\" : \"MPTokenAuthorize\",\n" +
      "  \"Fee\" : \"15\",\n" +
      "  \"Sequence\" : 432,\n" +
      "  \"SigningPubKey\" : \"ED0C0B9B7D5F80868A701693D7C994385EB4DC661D9E7A2DD95E8199EDC5C211E7\",\n" +
      "  \"Flags\" : 1,\n" +
      "  \"MPTokenIssuanceID\" : \"00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41\"\n" +
      "}";

    assertCanSerializeAndDeserialize(authorize, json);
  }


}
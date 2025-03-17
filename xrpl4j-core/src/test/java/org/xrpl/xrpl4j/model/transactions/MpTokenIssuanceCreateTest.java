package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;

class MpTokenIssuanceCreateTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(Address.of("rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(321))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710")
      )
      .flags(MpTokenIssuanceCreateFlags.builder()
        .tfMptCanLock(true)
        .tfMptCanEscrow(true)
        .tfMptCanTrade(true)
        .tfMptCanTransfer(true)
        .tfMptCanClawback(true)
        .build()
      )
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.of(UnsignedInteger.valueOf(10)))
      .maximumAmount(MpTokenObjectAmount.of(Long.MAX_VALUE))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .build();
    String json = "{\n" +
                  "  \"Account\" : \"rhqFECTUUqYYQouPHojLfrtjdx1WZ5jqrZ\",\n" +
                  "  \"TransactionType\" : \"MPTokenIssuanceCreate\",\n" +
                  "  \"Fee\" : \"15\",\n" +
                  "  \"Sequence\" : 321,\n" +
                  "  \"SigningPubKey\" : \"EDFE73FB561109EDCFB27C07B1870731849B4FC7718A8DCC9F9A1FB4E974874710\",\n" +
                  "  \"Flags\" : 2147483770,\n" +
                  "  \"AssetScale\" : 2,\n" +
                  "  \"TransferFee\" : 10,\n" +
                  "  \"MaximumAmount\" : \"9223372036854775807\",\n" +
                  "  \"MPTokenMetadata\" : \"ABCD\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(issuanceCreate, json);
  }


}
package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

class MpTokenIssuanceDestroyTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    MpTokenIssuanceDestroy destroy = MpTokenIssuanceDestroy.builder()
      .account(Address.of("rH3piYVvQk1tygbJe8Nrqko4aeMgHxaHda"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(322))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDD1DD428E0656E439796F562F7DAD52C32E7947D2E21DA79C8D69A8BEFB3EF73B")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000013EB0D678D9D2575D304611B9CFDA5644295AA7EAA8"))
      .build();

    String json = "{\n" +
                  "  \"Account\" : \"rH3piYVvQk1tygbJe8Nrqko4aeMgHxaHda\",\n" +
                  "  \"TransactionType\" : \"MPTokenIssuanceDestroy\",\n" +
                  "  \"Fee\" : \"15\",\n" +
                  "  \"Sequence\" : 322,\n" +
                  "  \"SigningPubKey\" : \"EDD1DD428E0656E439796F562F7DAD52C32E7947D2E21DA79C8D69A8BEFB3EF73B\",\n" +
                  "  \"MPTokenIssuanceID\" : \"0000013EB0D678D9D2575D304611B9CFDA5644295AA7EAA8\"\n" +
                  "}";

    assertCanSerializeAndDeserialize(destroy, json);
  }

  @Test
  void transactionFlagsReturnsEmptyFlags() {
    MpTokenIssuanceDestroy destroy = MpTokenIssuanceDestroy.builder()
      .account(Address.of("rH3piYVvQk1tygbJe8Nrqko4aeMgHxaHda"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(322))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDD1DD428E0656E439796F562F7DAD52C32E7947D2E21DA79C8D69A8BEFB3EF73B")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000013EB0D678D9D2575D304611B9CFDA5644295AA7EAA8"))
      .build();

    assertThat(destroy.transactionFlags()).isEqualTo(destroy.flags());
    assertThat(destroy.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void builderFromCopiesFlagsCorrectly() {
    MpTokenIssuanceDestroy original = MpTokenIssuanceDestroy.builder()
      .account(Address.of("rH3piYVvQk1tygbJe8Nrqko4aeMgHxaHda"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(322))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("EDD1DD428E0656E439796F562F7DAD52C32E7947D2E21DA79C8D69A8BEFB3EF73B")
      )
      .mpTokenIssuanceId(MpTokenIssuanceId.of("0000013EB0D678D9D2575D304611B9CFDA5644295AA7EAA8"))
      .flags(TransactionFlags.FULLY_CANONICAL_SIG)
      .build();

    MpTokenIssuanceDestroy copied = MpTokenIssuanceDestroy.builder().from(original).build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}

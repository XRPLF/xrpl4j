package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.SponsorshipFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class MetaSponsorshipObjectTest extends AbstractJsonTest {

  @Test
  void testMetaSponsorshipObjectWithAllFields() throws JsonProcessingException, JSONException {
    MetaSponsorshipObject metaSponsorshipObject = ImmutableMetaSponsorshipObject.builder()
      .flags(SponsorshipFlags.REQUIRE_SIGN_FOR_FEE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .remainingOwnerCount(UnsignedInteger.valueOf(5))
      .ownerNode("0000000000000000")
      .sponseeNode("0000000000000001")
      .previousTxnId(Hash256.of("7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB"))
      .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(82357607)))
      .build();

    String json = "{" +
      "  \"Flags\": 65536," +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"," +
      "  \"FeeAmount\": \"1000000\"," +
      "  \"MaxFee\": \"100\"," +
      "  \"RemainingOwnerCount\": 5," +
      "  \"OwnerNode\": \"0000000000000000\"," +
      "  \"SponseeNode\": \"0000000000000001\"," +
      "  \"PreviousTxnID\": \"7E5F3FB60E1177F8AF8A9EAC7982F27FA5494FDEA871B23B4B149939A5A7A7BB\"," +
      "  \"PreviousTxnLgrSeq\": 82357607" +
      "}";

    assertCanSerializeAndDeserialize(metaSponsorshipObject, json, MetaSponsorshipObject.class);
  }

  @Test
  void testMetaSponsorshipObjectWithMinimalFields() throws JsonProcessingException, JSONException {
    MetaSponsorshipObject metaSponsorshipObject = ImmutableMetaSponsorshipObject.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .build();

    String json = "{" +
      "  \"Owner\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\"," +
      "  \"Sponsee\": \"rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY\"" +
      "}";

    assertCanSerializeAndDeserialize(metaSponsorshipObject, json, MetaSponsorshipObject.class);
  }
}

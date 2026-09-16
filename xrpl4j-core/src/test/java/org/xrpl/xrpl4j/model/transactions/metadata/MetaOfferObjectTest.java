package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;
import java.util.List;

class MetaOfferObjectTest extends AbstractJsonTest {

  @Test
  public void testMetaOfferObject() throws JsonProcessingException, JSONException {
    List<MetaBookWrapper> additionalBookList = Collections.singletonList(
      ImmutableMetaBookWrapper.builder()
        .book(ImmutableMetaBook.builder()
          .bookDirectory(Hash256.of("107D855B8675C299A93F5DFF1BF11D5ECF1E76AF14575F455B038D7EA4C68000"))
          .bookNode("0")
          .build()
        ).build()
    );

    MetaOfferObject object = ImmutableMetaOfferObject.builder()
      .account(Address.of("rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt"))
      .bookDirectory(Hash256.of("ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E2446000"))
      .bookNode("0000000000000000")
      .flags(OfferFlags.of(262144))
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF"))
      .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(14524914)))
      .sequence(UnsignedInteger.valueOf(866))
      .takerGets(IssuedCurrencyAmount.builder()
        .currency("XAG")
        .issuer(Address.of("r9Dr5xwkeLegBeXq6ujinjSBLQzQ1zQGjH"))
        .value("37")
        .build())
      .takerPays(XrpCurrencyAmount.ofDrops(79550000000L))
      .domainId(Hash256.of("A6F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1798"))
      .expiration(UnsignedInteger.valueOf(1749214498))
      .additionalBooks(additionalBookList)
      .build();

    String json = "{" +
      "  \"Account\": \"rBqb89MRQJnMPq8wTwEbtz4kvxrEDfcYvt\"," +
      "  \"BookDirectory\": \"ACC27DE91DBA86FC509069EAF4BC511D73128B780F2E54BF5E07A369E2446000\"," +
      "  \"BookNode\": \"0000000000000000\"," +
      "  \"Flags\": 262144," +
      "  \"OwnerNode\": \"0000000000000000\"," +
      "  \"PreviousTxnID\": \"F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF\"," +
      "  \"PreviousTxnLgrSeq\": 14524914," +
      "  \"Sequence\": 866," +
      "  \"TakerGets\": {" +
      "    \"currency\": \"XAG\"," +
      "    \"issuer\": \"r9Dr5xwkeLegBeXq6ujinjSBLQzQ1zQGjH\"," +
      "    \"value\": \"37\"" +
      "  }," +
      "  \"TakerPays\": \"79550000000\"," +
      "  \"DomainID\": \"A6F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1798\"," +
      "  \"Expiration\": 1749214498," +
      "  \"AdditionalBooks\": [" +
      "    {" +
      "      \"Book\": {" +
      "        \"BookDirectory\": \"107D855B8675C299A93F5DFF1BF11D5ECF1E76AF14575F455B038D7EA4C68000\"," +
      "        \"BookNode\": \"0\"" +
      "      }" +
      "    }" +
      "  ]" +
      "}";

    assertCanSerializeAndDeserialize(object, json, MetaOfferObject.class);
  }

}
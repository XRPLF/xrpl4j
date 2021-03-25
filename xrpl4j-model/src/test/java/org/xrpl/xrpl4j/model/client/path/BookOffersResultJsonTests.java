package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

public class BookOffersResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    BookOffersResult result = BookOffersResult.builder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedLong.valueOf(7035305)))
      .offers(
        Lists.newArrayList(
          OfferObject.builder()
            .account(Address.of("rM3X3QSr8icjTGpaF52dozhbT2BZSXJQYM"))
            .bookDirectory(Hash256.of("7E5F614417C2D0A7CEFEB73C4AA773ED5B078DE2B5771F6D55055E4C405218EB"))
            .bookNode("0000000000000000")
            .flags(Flags.OfferFlags.of(0))
            .ownerNode("0000000000000AE0")
            .previousTransactionId(Hash256.of("6956221794397C25A53647182E5C78A439766D600724074C99D78982E37599F1"))
            .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7022646))
            .sequence(UnsignedInteger.valueOf(264542))
            .takerGets(IssuedCurrencyAmount.builder()
              .currency("EUR")
              .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
              .value("17.90363633316433")
              .build())
            .takerPays(
              IssuedCurrencyAmount.builder()
                .currency("USD")
                .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
                .value("27.05340557506234")
                .build()
            )
            .index(Hash256.of("96A9104BF3137131FF8310B9174F3B37170E2144C813CA2A1695DF2C5677E811"))
            .build()
        )
      )
      .build();

    String json = "{\n" +
      "    \"ledger_current_index\": 7035305,\n" +
      "    \"offers\": [\n" +
      "      {\n" +
      "        \"Account\": \"rM3X3QSr8icjTGpaF52dozhbT2BZSXJQYM\",\n" +
      "        \"BookDirectory\": \"7E5F614417C2D0A7CEFEB73C4AA773ED5B078DE2B5771F6D55055E4C405218EB\",\n" +
      "        \"BookNode\": \"0000000000000000\",\n" +
      "        \"Flags\": 0,\n" +
      "        \"LedgerEntryType\": \"Offer\",\n" +
      "        \"OwnerNode\": \"0000000000000AE0\",\n" +
      "        \"PreviousTxnID\": \"6956221794397C25A53647182E5C78A439766D600724074C99D78982E37599F1\",\n" +
      "        \"PreviousTxnLgrSeq\": 7022646,\n" +
      "        \"Sequence\": 264542,\n" +
      "        \"TakerGets\": {\n" +
      "          \"currency\": \"EUR\",\n" +
      "          \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "          \"value\": \"17.90363633316433\"\n" +
      "        },\n" +
      "        \"TakerPays\": {\n" +
      "          \"currency\": \"USD\",\n" +
      "          \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "          \"value\": \"27.05340557506234\"\n" +
      "        },\n" +
      "        \"index\": \"96A9104BF3137131FF8310B9174F3B37170E2144C813CA2A1695DF2C5677E811\"\n" +
      "      }" +
      "    ]\n" +
      "  }";

    assertCanSerializeAndDeserialize(result, json);
  }
}

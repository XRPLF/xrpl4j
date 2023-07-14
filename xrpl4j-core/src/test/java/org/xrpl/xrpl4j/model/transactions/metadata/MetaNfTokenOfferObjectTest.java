package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class MetaNfTokenOfferObjectTest extends AbstractJsonTest {

  @Test
  public void testJsonWithXrpAmount() throws JsonProcessingException, JSONException {
    MetaNfTokenOfferObject object = ImmutableMetaNfTokenOfferObject.builder()
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .owner(Address.of("rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu"))
      .previousTransactionId(Hash256.of("78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF"))
      .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(39480038)))
      .nfTokenId(NfTokenId.of("00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD"))
      .ownerNode("2")
      .flags(NfTokenOfferFlags.BUY_TOKEN)
      .build();

    String json = "{\n" +
      "          \"Amount\": \"10000\",\n" +
      "          \"Flags\": 1,\n" +
      "          \"NFTokenID\": \"00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD\",\n" +
      "          \"NFTokenOfferNode\": \"0\",\n" +
      "          \"Owner\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu\",\n" +
      "          \"OwnerNode\": \"2\",\n" +
      "          \"PreviousTxnID\": \"78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF\",\n" +
      "          \"PreviousTxnLgrSeq\": 39480038\n" +
      "        }";

    assertCanSerializeAndDeserialize(object, json, MetaNfTokenOfferObject.class);
  }

  @Test
  public void testJsonWithIssuedCurrencyAmount() throws JsonProcessingException, JSONException {
    MetaNfTokenOfferObject object = ImmutableMetaNfTokenOfferObject.builder()
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("534F4C4F00000000000000000000000000000000")
          .issuer(Address.of("rHZwvHEs56GCmHupwjA4RY7oPA3EoAJWuN"))
          .value("0.4")
          .build()
      )
      .owner(Address.of("rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu"))
      .previousTransactionId(Hash256.of("78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF"))
      .previousTransactionLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(39480038)))
      .nfTokenId(NfTokenId.of("00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD"))
      .ownerNode("2")
      .flags(NfTokenOfferFlags.BUY_TOKEN)
      .build();

    String json = "{\n" +
      "          \"Amount\": {\n" +
      "            \"currency\": \"534F4C4F00000000000000000000000000000000\",\n" +
      "            \"issuer\": \"rHZwvHEs56GCmHupwjA4RY7oPA3EoAJWuN\",\n" +
      "            \"value\": \"0.4\"\n" +
      "          },\n" +
      "          \"Flags\": 1,\n" +
      "          \"NFTokenID\": \"00080BB86F12FFF50C3C44827709AA868A910613902F810FA11F9798000000FD\",\n" +
      "          \"NFTokenOfferNode\": \"0\",\n" +
      "          \"Owner\": \"rB3JmRd5m292YjCsCr65tc8dwZz2WN7HQu\",\n" +
      "          \"OwnerNode\": \"2\",\n" +
      "          \"PreviousTxnID\": \"78D3B7A4B07BFC1F5D7EBD9844B25209F3D5885F347EBA0868FEF2672A91F9DF\",\n" +
      "          \"PreviousTxnLgrSeq\": 39480038\n" +
      "        }";

    assertCanSerializeAndDeserialize(object, json, MetaNfTokenOfferObject.class);
  }

}
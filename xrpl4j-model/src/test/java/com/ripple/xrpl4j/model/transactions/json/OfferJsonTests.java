package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.OfferCancel;
import com.ripple.xrpl4j.model.transactions.OfferCreate;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class OfferJsonTests extends AbstractJsonTest {

  @Test
  public void testOfferCancelJson() throws JsonProcessingException, JSONException {
    OfferCancel offerCancel = OfferCancel.builder()
        .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
        .sequence(UnsignedInteger.valueOf(12))
        .offerSequence(UnsignedInteger.valueOf(13))
        .fee(XrpCurrencyAmount.ofDrops(14))
        .build();

    String json = "{\n" +
        "    \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
        "    \"TransactionType\": \"OfferCancel\",\n" +
        "    \"Sequence\": 12,\n" +
        "    \"OfferSequence\": 13,\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"Fee\": \"14\"\n" +
        "}";

    assertCanSerializeAndDeserialize(offerCancel, json);
  }

  @Test
  public void testOfferCreateJson() throws JsonProcessingException, JSONException {
    OfferCreate offerCreate = OfferCreate.builder()
        .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(12))
        .offerSequence(UnsignedInteger.valueOf(13))
        .takerPays(XrpCurrencyAmount.ofDrops(14))
        .takerGets(XrpCurrencyAmount.ofDrops(15))
        .expiration(UnsignedInteger.valueOf(16))
        .build();

    String json = "{\n" +
        "    \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
        "    \"TransactionType\": \"OfferCreate\",\n" +
        "    \"Sequence\": 1,\n" +
        "    \"OfferSequence\": 13,\n" +
        "    \"TakerPays\": \"14\",\n" +
        "    \"TakerGets\": \"15\",\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"Fee\": \"12\",\n" +
        "    \"Expiration\": 16\n" +
        "}";

    assertCanSerializeAndDeserialize(offerCreate, json);
  }

}

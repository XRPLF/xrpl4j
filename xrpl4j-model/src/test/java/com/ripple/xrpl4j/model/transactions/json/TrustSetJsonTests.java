package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.TrustSet;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class TrustSetJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalTrustSetJson() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.of("12"))
      .flags(Flags.TrustSetFlags.builder()
        .tfClearNoRipple(true)
        .tfFullyCanonicalSig(false)
        .build())
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 262144,\n" +
      "    \"LimitAmount\": {\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"Sequence\": 12\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }

  @Test
  public void testTrustSetWithQualityJson() throws JsonProcessingException, JSONException {
    TrustSet trustSet = TrustSet.builder()
      .account(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX"))
      .fee(XrpCurrencyAmount.of("12"))
      .flags(Flags.TrustSetFlags.builder()
        .tfClearNoRipple(true)
        .tfFullyCanonicalSig(false)
        .build())
      .sequence(UnsignedInteger.valueOf(12))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc"))
        .value("100")
        .build())
      .qualityIn(UnsignedInteger.valueOf(100))
      .qualityOut(UnsignedInteger.valueOf(100))
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TrustSet\",\n" +
      "    \"Account\": \"ra5nK24KXen9AHvsdFTKHSANinZseWnPcX\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Flags\": 262144,\n" +
      "    \"LimitAmount\": {\n" +
      "      \"currency\": \"USD\",\n" +
      "      \"issuer\": \"rsP3mgGb2tcYUrxiLFiHJiQXhsziegtwBc\",\n" +
      "      \"value\": \"100\"\n" +
      "    },\n" +
      "    \"Sequence\": 12,\n" +
      "    \"QualityIn\": 100,\n" +
      "    \"QualityOut\": 100\n" +
      "}";

    assertCanSerializeAndDeserialize(trustSet, json);
  }
}

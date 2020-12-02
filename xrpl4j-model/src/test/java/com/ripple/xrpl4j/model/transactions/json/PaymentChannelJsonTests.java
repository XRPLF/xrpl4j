package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.PaymentChannelClaim;
import com.ripple.xrpl4j.model.transactions.PaymentChannelCreate;
import com.ripple.xrpl4j.model.transactions.PaymentChannelFund;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class PaymentChannelJsonTests extends AbstractJsonTest {

  @Test
  public void testPaymentChannelCreateJson() throws JsonProcessingException, JSONException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .sourceTag(UnsignedInteger.valueOf(11747))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .amount(XrpCurrencyAmount.ofDrops(10000))
        .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
        .destinationTag(UnsignedInteger.valueOf(23480))
        .settleDelay(UnsignedInteger.valueOf(86400))
        .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
        .cancelAfter(UnsignedLong.valueOf(533171558))
        .build();

    String json = "{\n" +
        "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "    \"Fee\": \"10\",\n" +
        "    \"Sequence\": 1,\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"TransactionType\": \"PaymentChannelCreate\",\n" +
        "    \"Amount\": \"10000\",\n" +
        "    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "    \"SettleDelay\": 86400,\n" +
        "    \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\",\n" +
        "    \"CancelAfter\": 533171558,\n" +
        "    \"DestinationTag\": 23480,\n" +
        "    \"SourceTag\": 11747\n" +
        "}";

    assertCanSerializeAndDeserialize(create, json);
  }

  @Test
  public void testPaymentChannelClaimJson() throws JsonProcessingException, JSONException {
    PaymentChannelClaim claim = PaymentChannelClaim.builder()
        .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .flags(Flags.PaymentChannelClaimFlags.builder().tfClose(true).build())
        .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
        .balance(XrpCurrencyAmount.ofDrops(1000000))
        .amount(XrpCurrencyAmount.ofDrops(1000000))
        .signature("30440220718D264EF05CAED7C781FF6DE298DCAC68D002562C9BF3A07C1E721B420C0DAB02203A5A4779E" +
            "F4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B")
        .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
        .build();

    String json = "{\n" +
        "  \"Account\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
        "  \"Fee\": \"10\",\n" +
        "  \"Sequence\": 1,\n" +
        "  \"Flags\": 2147614720,\n" +
        "  \"TransactionType\": \"PaymentChannelClaim\",\n" +
        "  \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\",\n" +
        "  \"Balance\": \"1000000\",\n" +
        "  \"Amount\": \"1000000\",\n" +
        "  \"Signature\": \"30440220718D264EF05CAED7C781FF6DE298DCAC68D002562C9BF3A07C1E721B420C0DAB02203A5A4" +
        "779EF4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B\",\n" +
        "  \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\"\n" +
        "}";
    System.out.println(ObjectMapperFactory.create().writerWithDefaultPrettyPrinter().writeValueAsString(claim));
    assertCanSerializeAndDeserialize(claim, json);
  }

  @Test
  public void testPaymentChannelFundJson() throws JsonProcessingException, JSONException {
    PaymentChannelFund fund = PaymentChannelFund.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .channel(Hash256.of("C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198"))
        .amount(XrpCurrencyAmount.ofDrops(200000))
        .expiration(UnsignedLong.valueOf(543171558))
        .build();

    String json = "{\n" +
        "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "    \"Fee\": \"10\",\n" +
        "    \"Sequence\": 1,\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"TransactionType\": \"PaymentChannelFund\",\n" +
        "    \"Channel\": \"C1AE6DDDEEC05CF2978C0BAD6FE302948E9533691DC749DCDD3B9E5992CA6198\",\n" +
        "    \"Amount\": \"200000\",\n" +
        "    \"Expiration\": 543171558\n" +
        "}";

    assertCanSerializeAndDeserialize(fund, json);
  }
}

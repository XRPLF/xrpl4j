package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.EscrowCancel;
import com.ripple.xrpl4j.model.transactions.EscrowCreate;
import com.ripple.xrpl4j.model.transactions.EscrowFinish;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class EscrowJsonTests extends AbstractJsonTest {

  @Test
  public void testEscrowCreateJson() throws JsonProcessingException, JSONException {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.of("12"))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.of("10000"))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .cancelAfter(UnsignedInteger.valueOf(533257958))
      .finishAfter(UnsignedInteger.valueOf(533171558))
      .condition("A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100")
      .sourceTag(UnsignedInteger.valueOf(11747))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCreate\",\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"CancelAfter\": 533257958,\n" +
      "    \"FinishAfter\": 533171558,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"DestinationTag\": 23480,\n" +
      "    \"SourceTag\": 11747,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowCreate, json);
  }

  @Test
  public void testEscrowCancelJson() throws JsonProcessingException, JSONException {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.of("12"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowCancel\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";
    assertCanSerializeAndDeserialize(escrowCancel, json);
  }

  @Test
  public void testEscrowFinishJson() throws JsonProcessingException, JSONException {
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.of("12"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .offerSequence(UnsignedInteger.valueOf(7))
      .condition("A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100")
      .fulfillment("A0028000")
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"TransactionType\": \"EscrowFinish\",\n" +
      "    \"Owner\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"OfferSequence\": 7,\n" +
      "    \"Condition\": \"A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100\",\n" +
      "    \"Fulfillment\": \"A0028000\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(escrowFinish, json);
  }
}

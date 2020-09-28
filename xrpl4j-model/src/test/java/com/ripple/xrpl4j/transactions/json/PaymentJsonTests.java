package com.ripple.xrpl4j.transactions.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.transactions.Address;
import com.ripple.xrpl4j.transactions.Payment;
import com.ripple.xrpl4j.transactions.XrpCurrencyAmount;
import org.junit.Before;
import org.junit.Test;

public class PaymentJsonTests {

  ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  @Test
  public void testJson() throws JsonProcessingException {
    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.of("25000000"))
      .fee("10")
      .tfFullyCanonicalSig(false)
      .sequence(UnsignedInteger.valueOf(2))
      .build();

    String json = "{" +
        "\"Account\":\"r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb\"," +
        "\"Destination\":\"r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C\"," +
        "\"TransactionType\":\"Payment\"," +
        "\"Amount\":\"25000000\"," +
        "\"Fee\":\"10\"," +
        "\"Flags\":0," +
        "\"Sequence\":2" +
      "}";

    String serialized = objectMapper.writeValueAsString(payment);
    assertThat(serialized).isEqualTo(json);

    Payment deserialized = objectMapper.readValue(serialized, Payment.class);
    assertThat(deserialized).isEqualTo(payment);
  }
}

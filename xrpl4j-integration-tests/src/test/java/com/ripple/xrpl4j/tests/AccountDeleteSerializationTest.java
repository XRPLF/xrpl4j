package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.AccountDelete;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.junit.jupiter.api.Test;

public class AccountDeleteSerializationTest {

  private ObjectMapper objectMapper = ObjectMapperFactory.create();

  private XrplBinaryCodec binaryCodec = new XrplBinaryCodec();

  @Test
  public void serializeAccountDelete() throws JsonProcessingException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.of("5000000"))
      .sequence(UnsignedInteger.valueOf(2470665))
      .flags(Flags.of(2147483648L))
      .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
      .destinationTag(UnsignedInteger.valueOf(13))
      .build();

    String json = objectMapper.writeValueAsString(accountDelete);
    String binary = binaryCodec.encode(json);
    String expectedBinary = "1200152280000000240025B3092E0000000D6840000000004C4B4081140596915CFDEEE3A695B3EFD6BDA9AC788A368B7B8314F667B0CA50CC7709A220B0561B85E53A48461FA8";
    assertThat(binary).isEqualTo(expectedBinary);
  }

}

package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.junit.jupiter.api.Test;

public class AccountSetSerializationTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();
  private final XrplBinaryCodec binaryCodec = new XrplBinaryCodec();

  @Test
  public void serializeAccountSetTransaction() throws JsonProcessingException {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rpP2GdsQwenNnFPefbXFgiTvEgJWQpq8Rw"))
      .fee(XrpCurrencyAmount.of("10"))
      .flags(Flags.UNSET)
      .sequence(UnsignedInteger.valueOf(10598))
      .build();

    String accountSetJson = objectMapper.writeValueAsString(accountSet);
    String accountSetBinary = binaryCodec.encode(accountSetJson);
    String expectedBinary = "1200032200000000240000296668400000000000000A81140F3D0C7D2CFAB2EC8295451F0B3CA038E8E9CDCD";
    assertThat(accountSetBinary).isEqualTo(expectedBinary);
  }
}

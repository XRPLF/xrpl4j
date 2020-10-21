package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Payment;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.junit.jupiter.api.Test;

public class PaymentSerializationTest {

  private ObjectMapper objectMapper = ObjectMapperFactory.create();

  private XrplBinaryCodec binaryCodec = new XrplBinaryCodec();

  @Test
  void serializeXrpPayment() throws JsonProcessingException {
    Address source = Address.builder()
      .value("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK")
      .build();

    Address destination = Address.builder()
      .value("rrrrrrrrrrrrrrrrrrrrBZbvji")
      .build();

    Payment payment = Payment.builder()
      .account(source)
      .destination(destination)
      .sourceTag(UnsignedInteger.valueOf(1))
      .destinationTag(UnsignedInteger.valueOf(2))
      .amount(XrpCurrencyAmount.of("12345"))
      .fee(XrpCurrencyAmount.of("789"))
      .sequence(UnsignedInteger.valueOf(56565656))
      .build();

    String paymentJson = objectMapper.writeValueAsString(payment);

    String paymentBinary = binaryCodec.encode(paymentJson);

    String expectedBinary = "1200002280000000230000000124035F1F982E00000002614000000000003039684000000000000" +
      "3158114EE39E6D05CFD6A90DAB700A1D70149ECEE29DFEC83140000000000000000000000000000000000000001";
    assertThat(paymentBinary).isEqualTo(expectedBinary);
  }

  @Test
  void serializeIssuedCurrencyPayment() throws JsonProcessingException {
    Address source = Address.builder()
      .value("r45dBj4S3VvMMYXxr9vHX4Z4Ma6ifPMCkK")
      .build();

    Address destination = Address.builder()
      .value("rrrrrrrrrrrrrrrrrrrrBZbvji")
      .build();

    Address issuer = Address.builder()
      .value("rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw")
      .build();

    CurrencyAmount amount = IssuedCurrencyAmount.builder()
      .currency("USD")
      .issuer(issuer)
      .value("1234567890123456")
      .build();

    Payment payment = Payment.builder()
      .flags(Flags.Payment.builder().partialPayment(true).build())
      .account(source)
      .destination(destination)
      .sourceTag(UnsignedInteger.valueOf(1))
      .destinationTag(UnsignedInteger.valueOf(2))
      .amount(amount)
      .fee(XrpCurrencyAmount.of("789"))
      .sequence(UnsignedInteger.valueOf(1))
      .build();

    String paymentJson = objectMapper.writeValueAsString(payment);

    String paymentBinary = binaryCodec.encode(paymentJson);

    String expectedBinary = "1200002280020000230000000124000000012E0000000261D84462D53C8ABAC0000000000000000000000000" +
      "55534400000000008B1CE810C13D6F337DAC85863B3D70265A24DF446840000000000003158114EE39E6D05CFD6A90DAB700A1D7" +
      "0149ECEE29DFEC83140000000000000000000000000000000000000001";
    assertThat(paymentBinary).isEqualTo(expectedBinary);
  }

}

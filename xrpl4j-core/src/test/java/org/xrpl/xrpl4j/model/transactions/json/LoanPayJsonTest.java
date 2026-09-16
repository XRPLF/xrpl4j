package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.LoanPayFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.LoanPay;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link LoanPay} JSON serialization.
 */
public class LoanPayJsonTest extends AbstractJsonTest {

  @Test
  public void testLoanPayJsonWithAllFields()
    throws JsonProcessingException, JSONException {

    LoanPay loanPay = LoanPay.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(197))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanPayFlags.of(
        LoanPayFlags.LOAN_OVERPAYMENT.getValue()
      ))
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("5000")
          .build()
      )
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":197," +
      "\"Flags\":65536," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"Amount\":{" +
      "\"currency\":\"USD\"," +
      "\"issuer\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"value\":\"5000\"" +
      "}," +
      "\"TransactionType\":\"LoanPay\"" +
      "}";

    assertCanSerializeAndDeserialize(loanPay, json);
  }

  @Test
  public void testLoanPayJsonWithRequiredFieldsOnly()
    throws JsonProcessingException, JSONException {

    LoanPay loanPay = LoanPay.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(197))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .amount(XrpCurrencyAmount.ofDrops(5000))
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":197," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"Amount\":\"5000\"," +
      "\"TransactionType\":\"LoanPay\"" +
      "}";

    assertCanSerializeAndDeserialize(loanPay, json);
  }

  @Test
  public void testLoanPayJsonWithFullPaymentFlag()
    throws JsonProcessingException, JSONException {

    LoanPay loanPay = LoanPay.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(197))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanPayFlags.of(
        LoanPayFlags.LOAN_FULL_PAYMENT.getValue()
      ))
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("50000")
          .build()
      )
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":197," +
      "\"Flags\":131072," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"Amount\":{" +
      "\"currency\":\"USD\"," +
      "\"issuer\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"value\":\"50000\"" +
      "}," +
      "\"TransactionType\":\"LoanPay\"" +
      "}";

    assertCanSerializeAndDeserialize(loanPay, json);
  }

  @Test
  public void testLoanPayJsonWithLatePaymentFlag()
    throws JsonProcessingException, JSONException {

    LoanPay loanPay = LoanPay.builder()
      .account(Address.of("rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8"))
      .fee(XrpCurrencyAmount.ofDrops(15))
      .sequence(UnsignedInteger.valueOf(197))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey(
          "ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD"
        )
      )
      .flags(LoanPayFlags.of(
        LoanPayFlags.LOAN_LATE_PAYMENT.getValue()
      ))
      .loanId(Hash256.of(
        "A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2"
      ))
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("5000")
          .build()
      )
      .build();

    String json = "{" +
      "\"Account\":\"rU1Cm8GymH5U1WuTcmMTUZ5XjwJbanQoA8\"," +
      "\"Fee\":\"15\"," +
      "\"Sequence\":197," +
      "\"Flags\":262144," +
      "\"SigningPubKey\":" +
      "\"ED1F863E4E0957C6965B7B0563D56C77E4F68D571E8026251834F0ADBB0411D6FD\"," +
      "\"LoanID\":" +
      "\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2\"," +
      "\"Amount\":{" +
      "\"currency\":\"USD\"," +
      "\"issuer\":\"rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd\"," +
      "\"value\":\"5000\"" +
      "}," +
      "\"TransactionType\":\"LoanPay\"" +
      "}";

    assertCanSerializeAndDeserialize(loanPay, json);
  }
}

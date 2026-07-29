package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class LoanPayFlagsTest extends AbstractFlagsTest {

  @Test
  void testEmptyFlags() {
    LoanPayFlags flags = LoanPayFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfLoanOverpayment()).isFalse();
    assertThat(flags.tfLoanFullPayment()).isFalse();
    assertThat(flags.tfLoanLatePayment()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testLoanOverpaymentFlag() {
    LoanPayFlags flags = LoanPayFlags.LOAN_OVERPAYMENT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanOverpayment()).isTrue();
    assertThat(flags.tfLoanFullPayment()).isFalse();
    assertThat(flags.tfLoanLatePayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testLoanFullPaymentFlag() {
    LoanPayFlags flags = LoanPayFlags.LOAN_FULL_PAYMENT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanOverpayment()).isFalse();
    assertThat(flags.tfLoanFullPayment()).isTrue();
    assertThat(flags.tfLoanLatePayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(131072L);
  }

  @Test
  void testLoanLatePaymentFlag() {
    LoanPayFlags flags = LoanPayFlags.LOAN_LATE_PAYMENT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanOverpayment()).isFalse();
    assertThat(flags.tfLoanFullPayment()).isFalse();
    assertThat(flags.tfLoanLatePayment()).isTrue();
    assertThat(flags.getValue()).isEqualTo(262144L);
  }

  @Test
  void testBuilderWithOverpayment() {
    LoanPayFlags flags = LoanPayFlags.builder()
      .tfLoanOverpayment(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanOverpayment()).isTrue();
    assertThat(flags.tfLoanFullPayment()).isFalse();
    assertThat(flags.tfLoanLatePayment()).isFalse();
  }

  @Test
  void testBuilderWithFullPayment() {
    LoanPayFlags flags = LoanPayFlags.builder()
      .tfLoanFullPayment(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanOverpayment()).isFalse();
    assertThat(flags.tfLoanFullPayment()).isTrue();
    assertThat(flags.tfLoanLatePayment()).isFalse();
  }

  @Test
  void testBuilderWithLatePayment() {
    LoanPayFlags flags = LoanPayFlags.builder()
      .tfLoanLatePayment(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanOverpayment()).isFalse();
    assertThat(flags.tfLoanFullPayment()).isFalse();
    assertThat(flags.tfLoanLatePayment()).isTrue();
  }

  @Test
  void testOfWithValue() {
    LoanPayFlags flags = LoanPayFlags.of(0x00040000L);
    assertThat(flags.tfLoanLatePayment()).isTrue();
    assertThat(flags.getValue()).isEqualTo(262144L);
  }

  @Test
  void testLoanOverpaymentJson()
    throws JSONException, JsonProcessingException {
    LoanPayFlags flags = LoanPayFlags.builder()
      .tfLoanOverpayment(true)
      .build();

    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testLoanFullPaymentJson()
    throws JSONException, JsonProcessingException {
    LoanPayFlags flags = LoanPayFlags.builder()
      .tfLoanFullPayment(true)
      .build();

    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testLoanLatePaymentJson()
    throws JSONException, JsonProcessingException {
    LoanPayFlags flags = LoanPayFlags.builder()
      .tfLoanLatePayment(true)
      .build();

    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson()
    throws JSONException, JsonProcessingException {
    LoanPayFlags flags = LoanPayFlags.empty();
    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = "{}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }
}

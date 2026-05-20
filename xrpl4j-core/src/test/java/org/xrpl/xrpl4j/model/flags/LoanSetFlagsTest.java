package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class LoanSetFlagsTest extends AbstractFlagsTest {

  @Test
  void testEmptyFlags() {
    LoanSetFlags flags = LoanSetFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfLoanOverpayment()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testLoanOverpaymentFlag() {
    LoanSetFlags flags = LoanSetFlags.LOAN_OVERPAYMENT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanOverpayment()).isTrue();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testBuilderWithOverpayment() {
    LoanSetFlags flags = LoanSetFlags.builder()
      .tfLoanOverpayment(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanOverpayment()).isTrue();
  }

  @Test
  void testBuilderWithoutOverpayment() {
    LoanSetFlags flags = LoanSetFlags.builder()
      .tfLoanOverpayment(false)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanOverpayment()).isFalse();
  }

  @Test
  void testOfWithValue() {
    LoanSetFlags flags = LoanSetFlags.of(0x00010000L);
    assertThat(flags.tfLoanOverpayment()).isTrue();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testLoanOverpaymentJson()
    throws JSONException, JsonProcessingException {
    LoanSetFlags flags = LoanSetFlags.builder()
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
  void testEmptyJson()
    throws JSONException, JsonProcessingException {
    LoanSetFlags flags = LoanSetFlags.empty();
    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = "{}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }
}

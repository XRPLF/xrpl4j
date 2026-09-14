package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class LoanFlagsTest extends AbstractFlagsTest {

  @Test
  void testUnsetFlags() {
    LoanFlags flags = LoanFlags.UNSET;

    assertThat(flags.lsfLoanDefault()).isFalse();
    assertThat(flags.lsfLoanImpaired()).isFalse();
    assertThat(flags.lsfLoanOverpayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testLoanDefaultFlag() {
    LoanFlags flags = LoanFlags.LOAN_DEFAULT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfLoanDefault()).isTrue();
    assertThat(flags.lsfLoanImpaired()).isFalse();
    assertThat(flags.lsfLoanOverpayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testLoanImpairedFlag() {
    LoanFlags flags = LoanFlags.LOAN_IMPAIRED;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfLoanDefault()).isFalse();
    assertThat(flags.lsfLoanImpaired()).isTrue();
    assertThat(flags.lsfLoanOverpayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(131072L);
  }

  @Test
  void testLoanOverpaymentFlag() {
    LoanFlags flags = LoanFlags.LOAN_OVERPAYMENT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.lsfLoanDefault()).isFalse();
    assertThat(flags.lsfLoanImpaired()).isFalse();
    assertThat(flags.lsfLoanOverpayment()).isTrue();
    assertThat(flags.getValue()).isEqualTo(262144L);
  }

  @Test
  void testOfWithCombinedFlags() {
    LoanFlags flags = LoanFlags.of(
      0x00010000 | 0x00020000
    );
    assertThat(flags.lsfLoanDefault()).isTrue();
    assertThat(flags.lsfLoanImpaired()).isTrue();
    assertThat(flags.lsfLoanOverpayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(196608L);
  }

  @Test
  void testOfWithImpairedValue() {
    LoanFlags flags = LoanFlags.of(131072);
    assertThat(flags.lsfLoanDefault()).isFalse();
    assertThat(flags.lsfLoanImpaired()).isTrue();
    assertThat(flags.lsfLoanOverpayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(131072L);
  }

  @Test
  void testOfWithZero() {
    LoanFlags flags = LoanFlags.of(0);
    assertThat(flags.lsfLoanDefault()).isFalse();
    assertThat(flags.lsfLoanImpaired()).isFalse();
    assertThat(flags.lsfLoanOverpayment()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testLoanImpairedJson()
    throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(LoanFlags.LOAN_IMPAIRED);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", LoanFlags.LOAN_IMPAIRED.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testUnsetJson()
    throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(LoanFlags.UNSET);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", LoanFlags.UNSET.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testLoanDefaultJson()
    throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(LoanFlags.LOAN_DEFAULT);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", LoanFlags.LOAN_DEFAULT.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testLoanOverpaymentJson()
    throws JSONException, JsonProcessingException {
    FlagsWrapper wrapper = FlagsWrapper.of(LoanFlags.LOAN_OVERPAYMENT);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", LoanFlags.LOAN_OVERPAYMENT.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }
}

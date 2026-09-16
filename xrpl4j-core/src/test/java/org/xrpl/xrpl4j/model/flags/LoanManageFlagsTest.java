package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class LoanManageFlagsTest extends AbstractFlagsTest {

  @Test
  void testEmptyFlags() {
    LoanManageFlags flags = LoanManageFlags.empty();
    assertThat(flags.isEmpty()).isTrue();

    assertThat(flags.tfLoanDefault()).isFalse();
    assertThat(flags.tfLoanImpair()).isFalse();
    assertThat(flags.tfLoanUnimpair()).isFalse();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  void testLoanDefaultFlag() {
    LoanManageFlags flags = LoanManageFlags.LOAN_DEFAULT;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanDefault()).isTrue();
    assertThat(flags.tfLoanImpair()).isFalse();
    assertThat(flags.tfLoanUnimpair()).isFalse();
    assertThat(flags.getValue()).isEqualTo(65536L);
  }

  @Test
  void testLoanImpairFlag() {
    LoanManageFlags flags = LoanManageFlags.LOAN_IMPAIR;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanDefault()).isFalse();
    assertThat(flags.tfLoanImpair()).isTrue();
    assertThat(flags.tfLoanUnimpair()).isFalse();
    assertThat(flags.getValue()).isEqualTo(131072L);
  }

  @Test
  void testLoanUnimpairFlag() {
    LoanManageFlags flags = LoanManageFlags.LOAN_UNIMPAIR;
    assertThat(flags.isEmpty()).isFalse();

    assertThat(flags.tfLoanDefault()).isFalse();
    assertThat(flags.tfLoanImpair()).isFalse();
    assertThat(flags.tfLoanUnimpair()).isTrue();
    assertThat(flags.getValue()).isEqualTo(262144L);
  }

  @Test
  void testBuilderWithDefault() {
    LoanManageFlags flags = LoanManageFlags.builder()
      .tfLoanDefault(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanDefault()).isTrue();
    assertThat(flags.tfLoanImpair()).isFalse();
    assertThat(flags.tfLoanUnimpair()).isFalse();
  }

  @Test
  void testBuilderWithImpair() {
    LoanManageFlags flags = LoanManageFlags.builder()
      .tfLoanImpair(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanDefault()).isFalse();
    assertThat(flags.tfLoanImpair()).isTrue();
    assertThat(flags.tfLoanUnimpair()).isFalse();
  }

  @Test
  void testBuilderWithUnimpair() {
    LoanManageFlags flags = LoanManageFlags.builder()
      .tfLoanUnimpair(true)
      .build();

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfLoanDefault()).isFalse();
    assertThat(flags.tfLoanImpair()).isFalse();
    assertThat(flags.tfLoanUnimpair()).isTrue();
  }

  @Test
  void testOfWithValue() {
    LoanManageFlags flags = LoanManageFlags.of(0x00020000L);
    assertThat(flags.tfLoanImpair()).isTrue();
    assertThat(flags.getValue()).isEqualTo(131072L);
  }

  @Test
  void testLoanDefaultJson()
    throws JSONException, JsonProcessingException {
    LoanManageFlags flags = LoanManageFlags.builder()
      .tfLoanDefault(true)
      .build();

    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testLoanImpairJson()
    throws JSONException, JsonProcessingException {
    LoanManageFlags flags = LoanManageFlags.builder()
      .tfLoanImpair(true)
      .build();

    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = String.format("{" +
      "  \"flags\": %s" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testLoanUnimpairJson()
    throws JSONException, JsonProcessingException {
    LoanManageFlags flags = LoanManageFlags.builder()
      .tfLoanUnimpair(true)
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
    LoanManageFlags flags = LoanManageFlags.empty();
    TransactionFlagsWrapper wrapper =
      TransactionFlagsWrapper.of(flags);
    String json = "{}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }
}

package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;

import java.math.BigDecimal;

/**
 * Unit tests for {@link Amount}.
 */
class AmountTest extends AbstractJsonTest {

  // -------------------------
  // value()
  // -------------------------

  @Test
  void valueReturnsIntegerString() {
    Amount amount = Amount.of("1000000");
    assertThat(amount.value()).isEqualTo("1000000");
  }

  @Test
  void valueReturnsNegativeString() {
    Amount amount = Amount.of("-1000000");
    assertThat(amount.value()).isEqualTo("-1000000");
  }

  @Test
  void valueReturnsZeroString() {
    Amount amount = Amount.of("0");
    assertThat(amount.value()).isEqualTo("0");
  }

  @Test
  void valueReturnsScientificNotationString() {
    Amount amount = Amount.of("1.23e11");
    assertThat(amount.value()).isEqualTo("1.23e11");
  }

  // -------------------------
  // isNegative()
  // -------------------------

  @Test
  void isNegativeReturnsFalseForPositiveInteger() {
    assertThat(Amount.of("1000000").isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsTrueForNegativeInteger() {
    assertThat(Amount.of("-1000000").isNegative()).isTrue();
  }

  @Test
  void isNegativeReturnsFalseForZero() {
    assertThat(Amount.of("0").isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsFalseForPositiveScientificNotation() {
    assertThat(Amount.of("1.23e11").isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsTrueForNegativeScientificNotation() {
    assertThat(Amount.of("-1.23e11").isNegative()).isTrue();
  }

  @Test
  void isNegativeReturnsFalseForPositiveDecimal() {
    assertThat(Amount.of("1.5").isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsTrueForNegativeDecimal() {
    assertThat(Amount.of("-1.5").isNegative()).isTrue();
  }

  // -------------------------
  // bigDecimalValue()
  // -------------------------

  @Test
  void bigDecimalValueFromPositiveInteger() {
    assertThat(Amount.of("1").bigDecimalValue()).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void bigDecimalValueFromNegativeInteger() {
    assertThat(Amount.of("-1").bigDecimalValue()).isEqualByComparingTo(BigDecimal.ONE.negate());
  }

  @Test
  void bigDecimalValueFromZero() {
    assertThat(Amount.of("0").bigDecimalValue()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void bigDecimalValueFromPositiveDecimal() {
    assertThat(Amount.of("1.5").bigDecimalValue()).isEqualByComparingTo(new BigDecimal("1.5"));
  }

  @Test
  void bigDecimalValueFromNegativeDecimal() {
    assertThat(Amount.of("-1.5").bigDecimalValue()).isEqualByComparingTo(new BigDecimal("-1.5"));
  }

  @Test
  void bigDecimalValueFromPositiveScientificNotation() {
    assertThat(Amount.of("1.23e11").bigDecimalValue()).isEqualByComparingTo(new BigDecimal("1.23e11"));
  }

  @Test
  void bigDecimalValueFromNegativeScientificNotation() {
    assertThat(Amount.of("-1.23e11").bigDecimalValue()).isEqualByComparingTo(new BigDecimal("-1.23e11"));
  }

  @Test
  void bigDecimalValueInvalidStringThrows() {
    assertThrows(NumberFormatException.class, () -> Amount.of("not-a-number").bigDecimalValue());
  }

  // -------------------------
  // toCurrencyAmount() - null guard
  // -------------------------

  @Test
  void toCurrencyAmountThrowsOnNullIssue() {
    assertThrows(NullPointerException.class, () -> Amount.of("1000000").toCurrencyAmount(null));
  }

  // -------------------------
  // toCurrencyAmount() - XRP issue
  // -------------------------

  @Test
  void toCurrencyAmountWithXrpIssuePositive() {
    CurrencyAmount result = Amount.of("1000000").toCurrencyAmount(Issue.XRP);

    assertThat(result).isInstanceOf(XrpCurrencyAmount.class);
    XrpCurrencyAmount xrp = (XrpCurrencyAmount) result;
    assertThat(xrp.value()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
    assertThat(xrp.isNegative()).isFalse();
  }

  @Test
  void toCurrencyAmountWithXrpIssueZero() {
    CurrencyAmount result = Amount.of("0").toCurrencyAmount(Issue.XRP);

    assertThat(result).isInstanceOf(XrpCurrencyAmount.class);
    XrpCurrencyAmount xrp = (XrpCurrencyAmount) result;
    assertThat(xrp.value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(xrp.isNegative()).isFalse();
  }

  @Test
  void toCurrencyAmountWithXrpIssueNegative() {
    CurrencyAmount result = Amount.of("-1000000").toCurrencyAmount(Issue.XRP);

    assertThat(result).isInstanceOf(XrpCurrencyAmount.class);
    XrpCurrencyAmount xrp = (XrpCurrencyAmount) result;
    assertThat(xrp.value()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
    assertThat(xrp.isNegative()).isTrue();
  }

  // -------------------------
  // toCurrencyAmount() - IOU issue
  // -------------------------

  @Test
  void toCurrencyAmountWithIouIssuePositive() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS"))
      .build();
    CurrencyAmount result = Amount.of("100").toCurrencyAmount(iouIssue);

    assertThat(result).isInstanceOf(IssuedCurrencyAmount.class);
    IssuedCurrencyAmount iou = (IssuedCurrencyAmount) result;
    assertThat(iou.value()).isEqualTo("100");
    assertThat(iou.currency()).isEqualTo("USD");
    assertThat(iou.issuer()).isEqualTo(Address.of("rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS"));
    assertThat(iou.isNegative()).isFalse();
  }

  @Test
  void toCurrencyAmountWithIouIssueNegative() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("USD")
      .issuer(Address.of("rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS"))
      .build();
    CurrencyAmount result = Amount.of("-100").toCurrencyAmount(iouIssue);

    assertThat(result).isInstanceOf(IssuedCurrencyAmount.class);
    IssuedCurrencyAmount iou = (IssuedCurrencyAmount) result;
    assertThat(iou.value()).isEqualTo("-100");
    assertThat(iou.isNegative()).isTrue();
  }

  @Test
  void toCurrencyAmountWithIouIssueScientificNotation() {
    IouIssue iouIssue = IouIssue.builder()
      .currency("EUR")
      .issuer(Address.of("rP9JR5JTEqaVYbXHtiqR5YvBeoWQeMBipS"))
      .build();
    CurrencyAmount result = Amount.of("1.23e11").toCurrencyAmount(iouIssue);

    assertThat(result).isInstanceOf(IssuedCurrencyAmount.class);
    IssuedCurrencyAmount iou = (IssuedCurrencyAmount) result;
    assertThat(iou.value()).isEqualTo("1.23e11");
    assertThat(iou.currency()).isEqualTo("EUR");
  }

  // -------------------------
  // toCurrencyAmount() - MPT issue
  // -------------------------

  @Test
  void toCurrencyAmountWithMptIssuePositive() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9");
    MptIssue mptIssue = MptIssue.builder().mptIssuanceId(mptId).build();
    CurrencyAmount result = Amount.of("500").toCurrencyAmount(mptIssue);

    assertThat(result).isInstanceOf(MptCurrencyAmount.class);
    MptCurrencyAmount mpt = (MptCurrencyAmount) result;
    assertThat(mpt.value()).isEqualTo("500");
    assertThat(mpt.mptIssuanceId()).isEqualTo(mptId);
    assertThat(mpt.isNegative()).isFalse();
  }

  @Test
  void toCurrencyAmountWithMptIssueNegative() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000001A407AF5856CFF3379945D823561023E8E5CED9C9");
    MptIssue mptIssue = MptIssue.builder().mptIssuanceId(mptId).build();
    CurrencyAmount result = Amount.of("-500").toCurrencyAmount(mptIssue);

    assertThat(result).isInstanceOf(MptCurrencyAmount.class);
    MptCurrencyAmount mpt = (MptCurrencyAmount) result;
    assertThat(mpt.value()).isEqualTo("-500");
    assertThat(mpt.isNegative()).isTrue();
  }

  // -------------------------
  // Amount.of() - static factory method on the interface
  // -------------------------

  @Test
  void ofReturnsNonNullAmount() {
    assertThat(Amount.of("42")).isNotNull();
  }

  @Test
  void ofProducesImmutableAmountInstance() {
    assertThat(Amount.of("42")).isInstanceOf(ImmutableAmount.class);
  }

  @Test
  void ofSetsValueCorrectly() {
    assertThat(Amount.of("9999").value()).isEqualTo("9999");
  }

  @Test
  void ofWithNullValueThrows() {
    assertThrows(NullPointerException.class, () -> Amount.of(null));
  }

  // -------------------------
  // JSON serialization
  // -------------------------

  @Test
  void testJsonSerializationAndDeserialization() throws JSONException, JsonProcessingException {
    AmountWrapper wrapper = AmountWrapper.builder()
      .amount(Amount.of("1000000"))
      .build();
    assertCanSerializeAndDeserialize(wrapper, "{\"amount\": \"1000000\"}", AmountWrapper.class);
  }

  @Test
  void testJsonSerializationNegativeValue() throws JSONException, JsonProcessingException {
    AmountWrapper wrapper = AmountWrapper.builder()
      .amount(Amount.of("-1000000"))
      .build();
    assertCanSerializeAndDeserialize(wrapper, "{\"amount\": \"-1000000\"}", AmountWrapper.class);
  }

  @Test
  void testJsonSerializationScientificNotation() throws JSONException, JsonProcessingException {
    AmountWrapper wrapper = AmountWrapper.builder()
      .amount(Amount.of("1.23e11"))
      .build();
    assertCanSerializeAndDeserialize(wrapper, "{\"amount\":\"1.23e11\"}", AmountWrapper.class);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAmountWrapper.class)
  @JsonDeserialize(as = ImmutableAmountWrapper.class)
  interface AmountWrapper {

    /**
     * Construct a {@code CurrencyAmountWrapper} builder.
     *
     * @return An {@link ImmutableAmountWrapper.Builder}.
     */
    static ImmutableAmountWrapper.Builder builder() {
      return ImmutableAmountWrapper.builder();
    }

    Amount amount();
  }
}

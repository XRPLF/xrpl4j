package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
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
    Amount amount = ImmutableAmount.builder().value("1000000").build();
    assertThat(amount.value()).isEqualTo("1000000");
  }

  @Test
  void valueReturnsNegativeString() {
    Amount amount = ImmutableAmount.builder().value("-1000000").build();
    assertThat(amount.value()).isEqualTo("-1000000");
  }

  @Test
  void valueReturnsZeroString() {
    Amount amount = ImmutableAmount.builder().value("0").build();
    assertThat(amount.value()).isEqualTo("0");
  }

  @Test
  void valueReturnsScientificNotationString() {
    Amount amount = ImmutableAmount.builder().value("1.23e11").build();
    assertThat(amount.value()).isEqualTo("1.23e11");
  }

  // -------------------------
  // isNegative()
  // -------------------------

  @Test
  void isNegativeReturnsFalseForPositiveInteger() {
    Amount amount = ImmutableAmount.builder().value("1000000").build();
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsTrueForNegativeInteger() {
    Amount amount = ImmutableAmount.builder().value("-1000000").build();
    assertThat(amount.isNegative()).isTrue();
  }

  @Test
  void isNegativeReturnsFalseForZero() {
    Amount amount = ImmutableAmount.builder().value("0").build();
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsFalseForPositiveScientificNotation() {
    Amount amount = ImmutableAmount.builder().value("1.23e11").build();
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsTrueForNegativeScientificNotation() {
    Amount amount = ImmutableAmount.builder().value("-1.23e11").build();
    assertThat(amount.isNegative()).isTrue();
  }

  @Test
  void isNegativeReturnsFalseForPositiveDecimal() {
    Amount amount = ImmutableAmount.builder().value("1.5").build();
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void isNegativeReturnsTrueForNegativeDecimal() {
    Amount amount = ImmutableAmount.builder().value("-1.5").build();
    assertThat(amount.isNegative()).isTrue();
  }

  // -------------------------
  // bigDecimalValue()
  // -------------------------

  @Test
  void bigDecimalValueFromPositiveInteger() {
    Amount amount = ImmutableAmount.builder().value("1").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void bigDecimalValueFromNegativeInteger() {
    Amount amount = ImmutableAmount.builder().value("-1").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(BigDecimal.ONE.negate());
  }

  @Test
  void bigDecimalValueFromZero() {
    Amount amount = ImmutableAmount.builder().value("0").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void bigDecimalValueFromPositiveDecimal() {
    Amount amount = ImmutableAmount.builder().value("1.5").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("1.5"));
  }

  @Test
  void bigDecimalValueFromNegativeDecimal() {
    Amount amount = ImmutableAmount.builder().value("-1.5").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("-1.5"));
  }

  @Test
  void bigDecimalValueFromPositiveScientificNotation() {
    Amount amount = ImmutableAmount.builder().value("1.23e11").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("1.23e11"));
  }

  @Test
  void bigDecimalValueFromNegativeScientificNotation() {
    Amount amount = ImmutableAmount.builder().value("-1.23e11").build();
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("-1.23e11"));
  }

  @Test
  void bigDecimalValueInvalidStringThrows() {
    Amount amount = ImmutableAmount.builder().value("not-a-number").build();
    assertThrows(NumberFormatException.class, amount::bigDecimalValue);
  }

  // -------------------------
  // toCurrencyAmount() - null guard
  // -------------------------

  @Test
  void toCurrencyAmountThrowsOnNullIssue() {
    Amount amount = ImmutableAmount.builder().value("1000000").build();
    assertThrows(NullPointerException.class, () -> amount.toCurrencyAmount(null));
  }

  // -------------------------
  // toCurrencyAmount() - XRP issue
  // -------------------------

  @Test
  void toCurrencyAmountWithXrpIssuePositive() {
    Amount amount = ImmutableAmount.builder().value("1000000").build();
    CurrencyAmount result = amount.toCurrencyAmount(Issue.XRP);

    assertThat(result).isInstanceOf(XrpCurrencyAmount.class);
    XrpCurrencyAmount xrp = (XrpCurrencyAmount) result;
    assertThat(xrp.value()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
    assertThat(xrp.isNegative()).isFalse();
  }

  @Test
  void toCurrencyAmountWithXrpIssueZero() {
    Amount amount = ImmutableAmount.builder().value("0").build();
    CurrencyAmount result = amount.toCurrencyAmount(Issue.XRP);

    assertThat(result).isInstanceOf(XrpCurrencyAmount.class);
    XrpCurrencyAmount xrp = (XrpCurrencyAmount) result;
    assertThat(xrp.value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(xrp.isNegative()).isFalse();
  }

  @Test
  void toCurrencyAmountWithXrpIssueNegative() {
    Amount amount = ImmutableAmount.builder().value("-1000000").build();
    CurrencyAmount result = amount.toCurrencyAmount(Issue.XRP);

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
    Amount amount = ImmutableAmount.builder().value("100").build();
    CurrencyAmount result = amount.toCurrencyAmount(iouIssue);

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
    Amount amount = ImmutableAmount.builder().value("-100").build();
    CurrencyAmount result = amount.toCurrencyAmount(iouIssue);

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
    Amount amount = ImmutableAmount.builder().value("1.23e11").build();
    CurrencyAmount result = amount.toCurrencyAmount(iouIssue);

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
    Amount amount = ImmutableAmount.builder().value("500").build();
    CurrencyAmount result = amount.toCurrencyAmount(mptIssue);

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
    Amount amount = ImmutableAmount.builder().value("-500").build();
    CurrencyAmount result = amount.toCurrencyAmount(mptIssue);

    assertThat(result).isInstanceOf(MptCurrencyAmount.class);
    MptCurrencyAmount mpt = (MptCurrencyAmount) result;
    assertThat(mpt.value()).isEqualTo("-500");
    assertThat(mpt.isNegative()).isTrue();
  }

  // -------------------------
  // JSON serialization
  // -------------------------

  @Test
  void testJsonSerializationAndDeserialization() throws JSONException, JsonProcessingException {
    Amount amount = ImmutableAmount.builder().value("1000000").build();
    assertCanSerializeAndDeserialize(amount, "1000000", Amount.class);
  }

  @Test
  void testJsonSerializationNegativeValue() throws JSONException, JsonProcessingException {
    Amount amount = ImmutableAmount.builder().value("-1000000").build();
    assertCanSerializeAndDeserialize(amount, "{\"value\":\"-1000000\"}", Amount.class);
  }

  @Test
  void testJsonSerializationScientificNotation() throws JSONException, JsonProcessingException {
    Amount amount = ImmutableAmount.builder().value("1.23e11").build();
    assertCanSerializeAndDeserialize(amount, "{\"value\":\"1.23e11\"}", Amount.class);
  }
}

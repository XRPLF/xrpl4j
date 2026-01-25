package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class XChainModifyBridgeFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfClearAccountCreateAmount,
    boolean tfInnerBatchTxn
  ) {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.builder()
      .tfClearAccountCreateAmount(tfClearAccountCreateAmount)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfClearAccountCreateAmount, tfInnerBatchTxn));
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeriveIndividualFlagsFromFlags(
    boolean tfClearAccountCreateAmount,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = getExpectedFlags(tfClearAccountCreateAmount, tfInnerBatchTxn);
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfClearAccountCreateAmount()).isEqualTo(tfClearAccountCreateAmount);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testEmptyFlags() {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.empty();
    assertThat(flags.isEmpty()).isTrue();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfClearAccountCreateAmount()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfClearAccountCreateAmount,
    boolean tfInnerBatchTxn
  ) throws JSONException, JsonProcessingException {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.builder()
      .tfClearAccountCreateAmount(tfClearAccountCreateAmount)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  @Test
  void testEmptyJson() throws JSONException, JsonProcessingException {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.empty();
    TransactionFlagsWrapper wrapper = TransactionFlagsWrapper.of(flags);
    String json = "{\n" +
      "}";

    assertCanSerializeAndDeserialize(wrapper, json);
  }

  private long getExpectedFlags(
    boolean tfClearAccountCreateAmount,
    boolean tfInnerBatchTxn
  ) {
    return (XChainModifyBridgeFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfClearAccountCreateAmount ? XChainModifyBridgeFlags.CLEAR_ACCOUNT_CREATE_AMOUNT.getValue() : 0L) |
      (tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN.getValue() : 0L);
  }
}

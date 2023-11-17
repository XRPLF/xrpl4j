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
    return getBooleanCombinations(1);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfClearAccountCreateAmount
  ) {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.builder()
      .tfClearAccountCreateAmount(tfClearAccountCreateAmount)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfClearAccountCreateAmount));
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeriveIndividualFlagsFromFlags(
    boolean tfClearAccountCreateAmount
  ) {
    long expectedFlags = getExpectedFlags(tfClearAccountCreateAmount);
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);

    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfClearAccountCreateAmount()).isEqualTo(tfClearAccountCreateAmount);
  }

  @Test
  void testEmptyFlags() {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.empty();
    assertThat(flags.isEmpty()).isTrue();
    assertThat(flags.tfFullyCanonicalSig()).isFalse();
    assertThat(flags.tfClearAccountCreateAmount()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean tfClearAccountCreateAmount
  ) throws JSONException, JsonProcessingException {
    XChainModifyBridgeFlags flags = XChainModifyBridgeFlags.builder()
      .tfClearAccountCreateAmount(tfClearAccountCreateAmount)
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
    boolean tfClearAccountCreateAmount
  ) {
    return (XChainModifyBridgeFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfClearAccountCreateAmount? XChainModifyBridgeFlags.CLEAR_ACCOUNT_CREATE_AMOUNT.getValue() : 0L);
  }
}
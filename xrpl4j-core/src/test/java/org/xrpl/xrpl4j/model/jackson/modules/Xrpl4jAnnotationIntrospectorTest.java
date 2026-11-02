package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.example.consumer.ConsumerNamedType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfo;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Unit tests for {@link Xrpl4jAnnotationIntrospector}, installed by {@link Xrpl4jModule}. The introspector gives
 * xrpl4j types the serialization inclusion and unknown-property leniency they need, without changing those behaviors
 * for any other type handled by the same mapper.
 */
class Xrpl4jAnnotationIntrospectorTest {

  /**
   * A mapper with Jackson's defaults plus the xrpl4j module. No mapper-wide inclusion or leniency is configured.
   */
  private final ObjectMapper objectMapper = JsonMapper.builder()
    .addModule(new Jdk8Module())
    .addModule(new Xrpl4jModule())
    .build();

  @Test
  void xrpl4jTypeOmitsEmptyOptionals() throws JsonProcessingException {
    Memo memo = Memo.builder().memoData("ABC").build();

    assertThat(objectMapper.writeValueAsString(memo)).isEqualTo("{\"MemoData\":\"ABC\"}");
  }

  @Test
  void xrpl4jTypeIgnoresUnknownProperties() throws JsonProcessingException {
    Memo memo = objectMapper.readValue("{\"MemoData\":\"ABC\",\"AddedByFutureAmendment\":1}", Memo.class);

    assertThat(memo).isEqualTo(Memo.builder().memoData("ABC").build());
  }

  @Test
  void xrpl4jTypeMatchesPropertiesCaseInsensitively() throws JsonProcessingException {
    Memo memo = objectMapper.readValue("{\"memodata\":\"ABC\",\"MEMOTYPE\":\"DEF\"}", Memo.class);

    assertThat(memo).isEqualTo(Memo.builder().memoData("ABC").memoType("DEF").build());
  }

  @Test
  void xrpl4jTypeWritesBigDecimalInPlainNotation() throws JsonProcessingException {
    ServerInfo.ValidatedLedger validatedLedger = ServerInfo.ValidatedLedger.builder()
      .hash(Hash256.of(Strings.repeat("A", 64)))
      .reserveBaseXrp(XrpCurrencyAmount.ofDrops(10))
      .reserveIncXrp(XrpCurrencyAmount.ofDrops(2))
      .sequence(LedgerIndex.of(UnsignedInteger.ONE))
      .baseFeeXrp(new BigDecimal("1E-5"))
      .build();

    assertThat(objectMapper.writeValueAsString(validatedLedger)).contains("\"base_fee_xrp\":0.00001");
  }

  @Test
  void xrpl4jTypeWritesPlainAndOptionalBigDecimalInPlainNotation() throws JsonProcessingException {
    DecimalWrapper wrapper = ImmutableDecimalWrapper.builder()
      .plain(new BigDecimal("1E-7"))
      .optional(new BigDecimal("1.5E+2"))
      .build();

    assertThat(objectMapper.writeValueAsString(wrapper)).isEqualTo("{\"plain\":0.0000001,\"optional\":150}");
  }

  @Test
  void xrpl4jTypeBigDecimalRoundTrips() throws JsonProcessingException {
    DecimalWrapper wrapper = ImmutableDecimalWrapper.builder().plain(new BigDecimal("1E-7")).build();

    String json = objectMapper.writeValueAsString(wrapper);
    DecimalWrapper roundTripped = objectMapper.readValue(json, DecimalWrapper.class);

    assertThat(roundTripped.plain()).isEqualByComparingTo(wrapper.plain());
    assertThat(roundTripped.optional()).isEmpty();
  }

  /**
   * Tree conversion goes through a {@link com.fasterxml.jackson.databind.util.TokenBuffer}, where the serializer
   * must keep the typed value so the tree holds a {@link DecimalNode} rather than a string, and the tree converts
   * back to the original value.
   */
  @Test
  void xrpl4jTypeBigDecimalTreeConversionKeepsDecimalNode() throws JsonProcessingException {
    DecimalWrapper wrapper = ImmutableDecimalWrapper.builder().plain(new BigDecimal("1E-7")).build();

    JsonNode tree = objectMapper.valueToTree(wrapper);

    assertThat(tree.get("plain")).isInstanceOf(DecimalNode.class);
    assertThat(tree.get("plain").decimalValue()).isEqualByComparingTo(new BigDecimal("1E-7"));
    assertThat(objectMapper.treeToValue(tree, DecimalWrapper.class)).isEqualTo(wrapper);
  }

  @Test
  void otherTypeKeepsNullProperties() throws JsonProcessingException {
    ConsumerNamedType consumerType = new ConsumerNamedType();

    assertThat(objectMapper.writeValueAsString(consumerType)).isEqualTo("{\"name\":null}");
  }

  @Test
  void otherTypeFailsOnUnknownProperties() {
    assertThatThrownBy(() -> objectMapper.readValue("{\"name\":\"x\",\"typo\":1}", ConsumerNamedType.class))
      .isInstanceOf(UnrecognizedPropertyException.class);
  }

  /**
   * An explicit class-level {@link JsonInclude} on an xrpl4j type takes precedence over the introspector's
   * {@code NON_EMPTY} default: the empty {@link Optional} is written as {@code null} rather than omitted.
   */
  @Test
  void explicitJsonIncludeOnXrpl4jTypeWins() throws JsonProcessingException {
    AlwaysIncluded value = ImmutableAlwaysIncluded.builder().build();

    assertThat(objectMapper.writeValueAsString(value)).isEqualTo("{\"optional\":null}");
  }

  /**
   * An explicit class-level {@link JsonIgnoreProperties} on an xrpl4j type takes precedence over the introspector's
   * {@code ignoreUnknown = true} default.
   */
  @Test
  void explicitJsonIgnorePropertiesOnXrpl4jTypeWins() {
    assertThatThrownBy(() -> objectMapper.readValue("{\"optional\":null,\"typo\":1}", AlwaysIncluded.class))
      .isInstanceOf(UnrecognizedPropertyException.class);
  }

  /**
   * An explicit {@link JsonSerialize} on a {@link BigDecimal} property of an xrpl4j type takes precedence over the
   * introspector's plain-notation serializer.
   */
  @Test
  void explicitSerializerOnXrpl4jBigDecimalPropertyWins() throws JsonProcessingException {
    StringDecimal value = ImmutableStringDecimal.builder().plain(new BigDecimal("1E-7")).build();

    assertThat(objectMapper.writeValueAsString(value)).isEqualTo("{\"plain\":\"1E-7\"}");
  }

  /**
   * An xrpl4j-package type (by virtue of being nested in this test) covering both {@link BigDecimal} shapes used in
   * the model.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableDecimalWrapper.class)
  @JsonDeserialize(as = ImmutableDecimalWrapper.class)
  interface DecimalWrapper {

    BigDecimal plain();

    Optional<BigDecimal> optional();
  }

  /**
   * An xrpl4j-package type whose explicit class-level annotations contradict the introspector's defaults.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableAlwaysIncluded.class)
  @JsonDeserialize(as = ImmutableAlwaysIncluded.class)
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @JsonIgnoreProperties(ignoreUnknown = false)
  interface AlwaysIncluded {

    Optional<String> optional();
  }

  /**
   * An xrpl4j-package type whose {@link BigDecimal} property binds its own serializer.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableStringDecimal.class)
  @JsonDeserialize(as = ImmutableStringDecimal.class)
  interface StringDecimal {

    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal plain();
  }
}

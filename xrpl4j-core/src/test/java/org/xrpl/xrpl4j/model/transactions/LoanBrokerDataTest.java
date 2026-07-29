package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.Locale;

/**
 * Unit tests for {@link LoanBrokerData}.
 */
class LoanBrokerDataTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testOfPlainText() {
    LoanBrokerData data = LoanBrokerData.ofPlainText("Hello");
    String expectedValue = "48656C6C6F";

    assertThat(data.value()).isEqualTo(expectedValue);
    assertThat(data.toString()).isEqualTo(expectedValue);
  }

  @Test
  void testEqualityAndHashCode() {
    String input = "AA";

    assertThat(LoanBrokerData.of(input))
      .isEqualTo(LoanBrokerData.of(input.toLowerCase(Locale.ENGLISH)));

    assertThat(LoanBrokerData.ofPlainText(input))
      .isNotEqualTo(LoanBrokerData.ofPlainText(input.toLowerCase(Locale.ENGLISH)));

    assertThat(LoanBrokerData.of(input).hashCode())
      .isEqualTo(LoanBrokerData.of(input.toLowerCase(Locale.ENGLISH)).hashCode());

    assertThat(LoanBrokerData.of(input).equals(null)).isFalse();
  }

  @Test
  void testLength() {
    // Exceeds 256 bytes via plaintext (257 chars * 2 hex chars each = 514 > 512)
    assertThatThrownBy(() -> LoanBrokerData.ofPlainText(Strings.repeat("A", 257)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerData must be <= 256 bytes or <= 512 hex characters.");

    // Exceeds 512 hex characters directly
    assertThatThrownBy(() -> LoanBrokerData.of(Strings.repeat("A", 514)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerData must be <= 256 bytes or <= 512 hex characters.");

    // Empty via ofPlainText
    assertThatThrownBy(() -> LoanBrokerData.ofPlainText(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerData must not be empty.");

    // Empty via of
    assertThatThrownBy(() -> LoanBrokerData.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerData must not be empty.");

    // Max plaintext (256 chars = 512 hex chars)
    assertDoesNotThrow(() -> LoanBrokerData.ofPlainText(Strings.repeat("Z", 256)));

    // Max hex (512 hex chars)
    assertDoesNotThrow(() -> LoanBrokerData.of(Strings.repeat("F", 512)));
  }

  @Test
  void testHexEncoding() {
    // Odd-length hex string
    assertThatThrownBy(() -> LoanBrokerData.of("A"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerData must be encoded in hexadecimal.");

    // Non-hex characters
    assertThatThrownBy(() -> LoanBrokerData.of("ZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanBrokerData must be encoded in hexadecimal.");

    // Valid full-length hex
    assertDoesNotThrow(() -> LoanBrokerData.of("AABBCCDD"));
  }

  @Test
  void testOfPlainTextWithNull() {
    assertThrows(NullPointerException.class, () -> LoanBrokerData.ofPlainText(null));
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    LoanBrokerData data = LoanBrokerData.of("010203");
    LoanBrokerDataWrapper wrapper = ImmutableLoanBrokerDataWrapper.builder()
      .value(data)
      .build();
    assertSerializesAndDeserializes(wrapper, "{\"value\":\"010203\"}");
  }

  private void assertSerializesAndDeserializes(LoanBrokerDataWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    LoanBrokerDataWrapper deserialized = objectMapper.readValue(serialized, LoanBrokerDataWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLoanBrokerDataWrapper.class)
  @JsonDeserialize(as = ImmutableLoanBrokerDataWrapper.class)
  interface LoanBrokerDataWrapper {

    LoanBrokerData value();
  }
}

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
 * Unit tests for {@link LoanData}.
 */
class LoanDataTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testOfPlainText() {
    LoanData data = LoanData.ofPlainText("Hello");
    String expectedValue = "48656C6C6F";

    assertThat(data.value()).isEqualTo(expectedValue);
    assertThat(data.toString()).isEqualTo(expectedValue);
  }

  @Test
  void testEqualityAndHashCode() {
    String input = "AA";

    assertThat(LoanData.of(input))
      .isEqualTo(LoanData.of(input.toLowerCase(Locale.ENGLISH)));

    assertThat(LoanData.ofPlainText(input))
      .isNotEqualTo(LoanData.ofPlainText(input.toLowerCase(Locale.ENGLISH)));

    assertThat(LoanData.of(input).hashCode())
      .isEqualTo(LoanData.of(input.toLowerCase(Locale.ENGLISH)).hashCode());

    assertThat(LoanData.of(input).equals(null)).isFalse();
  }

  @Test
  void testLength() {
    // Exceeds 256 bytes via plaintext
    assertThatThrownBy(() -> LoanData.ofPlainText(Strings.repeat("A", 257)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanData must be <= 256 bytes or <= 512 hex characters.");

    // Exceeds 512 hex characters directly
    assertThatThrownBy(() -> LoanData.of(Strings.repeat("A", 514)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanData must be <= 256 bytes or <= 512 hex characters.");

    // Empty via ofPlainText
    assertThatThrownBy(() -> LoanData.ofPlainText(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanData must not be empty.");

    // Empty via of
    assertThatThrownBy(() -> LoanData.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanData must not be empty.");

    // Max plaintext (256 chars = 512 hex chars)
    assertDoesNotThrow(() -> LoanData.ofPlainText(Strings.repeat("Z", 256)));

    // Max hex (512 hex chars)
    assertDoesNotThrow(() -> LoanData.of(Strings.repeat("F", 512)));
  }

  @Test
  void testHexEncoding() {
    // Odd-length hex string
    assertThatThrownBy(() -> LoanData.of("A"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanData must be encoded in hexadecimal.");

    // Non-hex characters
    assertThatThrownBy(() -> LoanData.of("ZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("LoanData must be encoded in hexadecimal.");

    // Valid full-length hex
    assertDoesNotThrow(() -> LoanData.of("AABBCCDD"));
  }

  @Test
  void testOfPlainTextWithNull() {
    assertThrows(NullPointerException.class, () -> LoanData.ofPlainText(null));
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    LoanData data = LoanData.of("AABBCC");
    LoanDataWrapper wrapper = ImmutableLoanDataWrapper.builder()
      .value(data)
      .build();
    assertSerializesAndDeserializes(wrapper, "{\"value\":\"AABBCC\"}");
  }

  private void assertSerializesAndDeserializes(LoanDataWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    LoanDataWrapper deserialized = objectMapper.readValue(serialized, LoanDataWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLoanDataWrapper.class)
  @JsonDeserialize(as = ImmutableLoanDataWrapper.class)
  interface LoanDataWrapper {

    LoanData value();
  }
}

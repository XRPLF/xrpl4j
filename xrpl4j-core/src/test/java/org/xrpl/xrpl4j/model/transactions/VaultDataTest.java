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
 * Unit tests for {@link VaultData}.
 */
public class VaultDataTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void testVaultDataPlainText() {
    VaultData vaultData = VaultData.ofPlainText("Hello");
    String expectedValue = "48656C6C6F";

    assertThat(vaultData.value()).isEqualTo(expectedValue);
    assertThat(vaultData.toString()).isEqualTo(expectedValue);
  }

  @Test
  public void testVaultDataEqualityAndHashCode() {
    String input = "AA";

    assertThat(VaultData.of(input))
      .isEqualTo(VaultData.of(input.toLowerCase(Locale.ENGLISH)));

    assertThat(VaultData.ofPlainText(input))
      .isNotEqualTo(VaultData.ofPlainText(input.toLowerCase(Locale.ENGLISH)));

    assertThat(VaultData.of(input).hashCode())
      .isEqualTo(VaultData.of(input.toLowerCase(Locale.ENGLISH)).hashCode());

    assertThat(VaultData.of(input)).isNotEqualTo("not a VaultData");
    assertThat(VaultData.of(input)).isNotEqualTo(null);
  }

  @Test
  public void testVaultDataLength() {
    // 256 bytes = 512 hex chars. Exceeding should fail.
    assertThatThrownBy(() -> VaultData.of(Strings.repeat("A", 514)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultData must be <= 256 bytes or <= 512 hex characters.");

    // Plain text that exceeds 256 bytes when hex-encoded
    assertThatThrownBy(() -> VaultData.ofPlainText(Strings.repeat("A", 257)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultData must be <= 256 bytes or <= 512 hex characters.");

    assertThatThrownBy(() -> VaultData.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultData must not be empty.");

    assertThatThrownBy(() -> VaultData.ofPlainText(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultData must not be empty.");

    // Exactly 256 bytes = 512 hex chars should succeed
    assertDoesNotThrow(() -> VaultData.of(Strings.repeat("AB", 256)));

    // Exactly 256 plaintext chars should succeed
    assertDoesNotThrow(() -> VaultData.ofPlainText(Strings.repeat("Z", 256)));
  }

  @Test
  public void testVaultDataHexEncoding() {
    assertThatThrownBy(() -> VaultData.of("A"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultData must be encoded in hexadecimal.");

    assertThatThrownBy(() -> VaultData.of("ZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("VaultData must be encoded in hexadecimal.");

    assertDoesNotThrow(() -> VaultData.of("AABB"));
  }

  @Test
  public void testOfPlainTextWithNull() {
    assertThrows(NullPointerException.class, () -> VaultData.ofPlainText(null));
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException, JSONException {
    VaultData vaultData = VaultData.of("48656C6C6F");
    VaultDataWrapper wrapper = ImmutableVaultDataWrapper.builder()
      .value(vaultData)
      .build();
    assertSerializesAndDeserializes(wrapper, "{\"value\":\"48656C6C6F\"}");
  }

  private void assertSerializesAndDeserializes(VaultDataWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    VaultDataWrapper deserialized = objectMapper.readValue(serialized, VaultDataWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableVaultDataWrapper.class)
  @JsonDeserialize(as = ImmutableVaultDataWrapper.class)
  interface VaultDataWrapper {

    VaultData value();
  }

}

package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
 * Unit tests for {@link CredentialUri}.
 */
public class CredentialUriTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void testCredentialUriPlainText() {
    CredentialUri credentialUri = CredentialUri.ofPlainText("https://sample-vc.pdf");
    String expectedValue = "68747470733A2F2F73616D706C652D76632E706466";

    assertEquals(expectedValue, credentialUri.value());
    assertEquals(expectedValue, credentialUri.toString());
  }

  @Test
  public void testCredentialUriEqualityAndHashCode() {
    String input = "AA";

    assertThat(CredentialUri.of(input))
      .isEqualTo(CredentialUri.of(input.toLowerCase(Locale.ENGLISH)));

    assertThat(CredentialUri.ofPlainText(input))
      .isNotEqualTo(CredentialUri.ofPlainText(input.toLowerCase(Locale.ENGLISH)));

    assertThat(CredentialUri.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(null);

    assertThat(CredentialUri.of(input).hashCode())
      .isEqualTo(CredentialUri.of(input.toLowerCase(Locale.ENGLISH)).hashCode());

    assertThat(CredentialUri.of(input).equals(null)).isFalse();
  }

  @Test
  public void testCredentialUriLength() {
    assertThatThrownBy(() -> CredentialUri.ofPlainText(Strings.repeat("Z", 257)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialUri must be <= 256 characters or <= 512 hex characters.hex characters.");

    assertThatThrownBy(() -> CredentialUri.of(Strings.repeat("A", 514)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialUri must be <= 256 characters or <= 512 hex characters.hex characters.");

    assertThatThrownBy(() -> CredentialUri.ofPlainText(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialUri must not be empty.");

    assertThatThrownBy(() -> CredentialUri.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialUri must not be empty.");

    assertDoesNotThrow(() -> CredentialUri.ofPlainText(Strings.repeat("Z", 256)));

    assertDoesNotThrow(() -> CredentialUri.of(Strings.repeat("F", 512)));
  }

  @Test
  public void testCredentialUriHexEncoding() {
    assertThatThrownBy(() -> CredentialUri.of("A"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialUri must be encoded in hexadecimal.");

    assertThatThrownBy(() -> CredentialUri.of("ZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialUri must be encoded in hexadecimal.");

    assertDoesNotThrow(() ->
      CredentialUri.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"));
  }


  @Test
  public void testOfPlainTextWithNull() {
    assertThatThrownBy(() -> CredentialUri.ofPlainText(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException, JSONException {
    CredentialUri credentialUri = CredentialUri.ofPlainText("https://sample-vc.pdf");
    CredentialUriWrapper wrapper = ImmutableCredentialUriWrapper.builder()
      .value(credentialUri)
      .build();

    assertSerializesAndDeserializes(wrapper, "{\"value\":\"68747470733A2F2F73616D706C652D76632E706466\"}");
  }

  private void assertSerializesAndDeserializes(
    CredentialUriWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    CredentialUriWrapper deserialized = objectMapper.readValue(serialized, CredentialUriWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCredentialUriWrapper.class)
  @JsonDeserialize(as = ImmutableCredentialUriWrapper.class)
  interface CredentialUriWrapper {
    CredentialUri value();
  }

}

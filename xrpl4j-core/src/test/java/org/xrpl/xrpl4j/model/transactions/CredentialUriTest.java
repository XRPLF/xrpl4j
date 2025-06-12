package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Unit tests for {@link CredentialUri}.
 */
public class CredentialUriTest {

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

}

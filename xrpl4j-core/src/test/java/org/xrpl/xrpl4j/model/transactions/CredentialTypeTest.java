package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Unit tests for {@link CredentialType}.
 */
public class CredentialTypeTest {

  @Test
  public void testCredentialTypePlainText() {
    CredentialType credentialType = CredentialType.ofPlainText("voting card");
    String expectedValue = "766F74696E672063617264";

    assertEquals(expectedValue, credentialType.value());
    assertEquals(expectedValue, credentialType.toString());
  }

  @Test
  public void testCredentialTypeEqualityAndHashCode() {
    String input = "AA";

    assertThat(CredentialType.of(input))
      .isEqualTo(CredentialType.of(input.toLowerCase(Locale.ENGLISH)));

    assertThat(CredentialType.ofPlainText(input))
      .isNotEqualTo(CredentialType.ofPlainText(input.toLowerCase(Locale.ENGLISH)));

    assertThat(CredentialType.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(null);

    assertThat(CredentialType.of(input).hashCode())
      .isEqualTo(CredentialType.of(input.toLowerCase(Locale.ENGLISH)).hashCode());

    assertThat(CredentialType.of(input).equals(null)).isFalse();
  }

  @Test
  public void testCredentialTypeLength() {
    assertThatThrownBy(() -> CredentialType.ofPlainText(Strings.repeat("A", 65)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialType must be <= 64 characters or <= 128 hex characters.");

    assertThatThrownBy(() -> CredentialType.of(Strings.repeat("A", 130)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialType must be <= 64 characters or <= 128 hex characters.");

    assertThatThrownBy(() -> CredentialType.ofPlainText(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialType must not be empty.");

    assertThatThrownBy(() -> CredentialType.of(""))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialType must not be empty.");

    assertDoesNotThrow(() -> CredentialType.ofPlainText(Strings.repeat("Z", 64)));

    assertDoesNotThrow(() -> CredentialType.of(Strings.repeat("F", 128)));
  }

  @Test
  public void testCredentialTypeHexEncoding() {
    assertThatThrownBy(() -> CredentialType.of("A"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialType must be encoded in hexadecimal.");

    assertThatThrownBy(() -> CredentialType.of("ZZ"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialType must be encoded in hexadecimal.");

    assertDoesNotThrow(() ->
      CredentialType.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"));
  }

}

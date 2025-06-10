package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CredentialUri}.
 */
public class CredentialUriTests {

  @Test
  public void testCredentialUriPlainText() {
    CredentialUri credentialUri = CredentialUri.ofPlainText("https://sample-vc.pdf");
    String expectedValue = "68747470733A2F2F73616D706C652D76632E706466";

    assertEquals(expectedValue, credentialUri.value());
    assertEquals(expectedValue, credentialUri.toString());
  }

  @Test
  public void testCredentialUriEqualityAndHashCode() {
    assertThat(CredentialUri.of("AA"))
      .isEqualTo(CredentialUri.of("aa"));

    assertThat(CredentialUri.ofPlainText("AA"))
      .isNotEqualTo(CredentialUri.ofPlainText("aa"));

    assertThat(CredentialUri.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(null);

    assertThat(CredentialUri.of("AA").hashCode())
      .isEqualTo(CredentialUri.of("aa").hashCode());

    assertThat(CredentialUri.of("AA").equals(null)).isFalse();
  }
}

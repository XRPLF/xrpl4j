package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class CredentialTypeTests {

  @Test
  public void testCredentialTypePlainText() {
    CredentialType credentialType = CredentialType.ofPlainText("voting card");
    assertEquals("766F74696E672063617264", credentialType.value());
  }

  @Test
  public void testCredentialTypeEqualityAndHashCode() {
    assertThat(CredentialType.of("AA"))
      .isEqualTo(CredentialType.of("aa"));

    assertThat(CredentialType.ofPlainText("AA"))
      .isNotEqualTo(CredentialType.ofPlainText("aa"));

    assertThat(CredentialUri.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(null);

    assertThat(CredentialType.of("AA").hashCode())
      .isEqualTo(CredentialType.of("aa").hashCode());
  }

}

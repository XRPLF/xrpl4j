package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PermissionedDomainTests {

  @Test
  public void testMoreThan10AcceptedCredentials() {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 11)
      .mapToObj(i ->
        CredentialWrapper.builder()
          .credential(Credential.builder()
            .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
            .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
            .build())
          .build())
      .collect(Collectors.toList());

    assertThatThrownBy(() -> PermissionedDomainSet.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .acceptedCredentials(acceptedCredentials)
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("AcceptedCredentials shouldn't be empty and must have less than or equal to 10 credentials.");
  }

  @Test
  public void testEmptyAcceptedCredentials() {
    assertThatThrownBy(() -> PermissionedDomainSet.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .acceptedCredentials(Collections.emptyList())
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("AcceptedCredentials shouldn't be empty and must have less than or equal to 10 credentials.");
  }

  @Test
  public void testDuplicateAcceptedCredentials() {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 10)
      .mapToObj(i ->
        CredentialWrapper.builder()
          .credential(Credential.builder()
            .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
            .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
            .build())
          .build())
      .collect(Collectors.toList());

    acceptedCredentials.set(0, acceptedCredentials.get(1));

    assertThatThrownBy(() -> PermissionedDomainSet.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .acceptedCredentials(acceptedCredentials)
      .build())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("AcceptedCredentials should have unique credentials.");
  }
}
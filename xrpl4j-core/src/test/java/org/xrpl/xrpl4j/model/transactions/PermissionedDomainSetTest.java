package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link PermissionedDomainSet}.
 */
public class PermissionedDomainSetTest {

  @Test
  public void testPermissionedDomainSet() {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 10)
      .mapToObj(i -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build())
        .build())
      .collect(Collectors.toList());

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .acceptedCredentials(acceptedCredentials)
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .build();

    assertThat(permissionedDomainSet.transactionType()).isEqualTo(TransactionType.PERMISSIONED_DOMAIN_SET);
    assertThat(permissionedDomainSet.account()).isEqualTo(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"));
    assertThat(permissionedDomainSet.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(10));
    assertThat(permissionedDomainSet.acceptedCredentials()).isEqualTo(acceptedCredentials);
    assertThat(permissionedDomainSet.domainId()).isPresent().get().isEqualTo(
      Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"));
  }

  @Test
  public void testMoreThan10AcceptedCredentials() {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 11)
      .mapToObj(i -> CredentialWrapper.builder()
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
      .mapToObj(i -> CredentialWrapper.builder()
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

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 3)
      .mapToObj(i -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build())
        .build())
      .collect(Collectors.toList());

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .acceptedCredentials(acceptedCredentials)
      .build();

    assertThat(permissionedDomainSet.transactionFlags()).isEqualTo(permissionedDomainSet.flags());
    assertThat(permissionedDomainSet.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    List<CredentialWrapper> acceptedCredentials = IntStream.range(0, 3)
      .mapToObj(i -> CredentialWrapper.builder()
        .credential(Credential.builder()
          .issuer(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
          .credentialType(CredentialType.ofPlainText("Driver licence - " + i))
          .build())
        .build())
      .collect(Collectors.toList());

    PermissionedDomainSet original = PermissionedDomainSet.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .acceptedCredentials(acceptedCredentials)
      .build();

    PermissionedDomainSet copied = PermissionedDomainSet.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}

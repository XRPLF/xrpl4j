package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PermissionedDomainDeleteTest {

  @Test
  public void testPermissionedDomainDelete() {
    PermissionedDomainDelete permissionedDomainDelete = PermissionedDomainDelete.builder()
      .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .build();

    assertThat(permissionedDomainDelete.transactionType()).isEqualTo(TransactionType.PERMISSIONED_DOMAIN_DELETE);
    assertThat(permissionedDomainDelete.account()).isEqualTo(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"));
    assertThat(permissionedDomainDelete.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(10));
    assertThat(permissionedDomainDelete.domainId()).isEqualTo(
      Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"));
  }
}
package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class PermissionedDomainLedgerEntryParamsTest {

  @Test
  public void testPermissionedDomainLedgerEntryParams() {
    PermissionedDomainLedgerEntryParams param = PermissionedDomainLedgerEntryParams.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .seq(UnsignedInteger.ONE)
      .build();

    assertThat(param.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(param.seq()).isEqualTo(UnsignedInteger.ONE);
  }
}
package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class VaultLedgerEntryParamsTest {

  @Test
  public void testVaultLedgerEntryParams() {
    VaultLedgerEntryParams vaultLedgerEntryParams = VaultLedgerEntryParams.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j"))
      .seq(UnsignedInteger.valueOf(123))
      .build();

    assertThat(vaultLedgerEntryParams.owner()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j"));
    assertThat(vaultLedgerEntryParams.seq()).isEqualTo(UnsignedInteger.valueOf(123));
  }
}


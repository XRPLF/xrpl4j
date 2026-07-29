package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class LoanLedgerEntryParamsTest {

  @Test
  public void testLoanLedgerEntryParams() {
    LoanLedgerEntryParams params = LoanLedgerEntryParams.builder()
      .loanBrokerId(
        Hash256.of("A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2")
      )
      .loanSeq(UnsignedInteger.valueOf(456))
      .build();

    assertThat(params.loanBrokerId()).isEqualTo(
      Hash256.of("A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4E5F6A1B2")
    );
    assertThat(params.loanSeq()).isEqualTo(UnsignedInteger.valueOf(456));
  }
}

package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

public class CredentialLedgerEntryParamsTest {

  @Test
  public void testCredentialLedgerEntryParams() {
    CredentialLedgerEntryParams credentialLedgerEntryParams = CredentialLedgerEntryParams.builder()
      .credentialType(CredentialType.ofPlainText("driver licence"))
      .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
      .subject(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1oa"))
      .build();

    assertThat(credentialLedgerEntryParams.credentialType()).isEqualTo(CredentialType.ofPlainText("driver licence"));
    assertThat(credentialLedgerEntryParams.issuer()).isEqualTo(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"));
    assertThat(credentialLedgerEntryParams.subject()).isEqualTo(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1oa"));
  }
}

package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CredentialType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DepositPreAuthLedgerEntryParamsTest {

  @Test
  public void testDepositPreAuthParams() {
    List<DepositPreAuthCredential> credentials = Collections.singletonList(
      DepositPreAuthCredential
        .builder()
        .credentialType(CredentialType.of("6D795F63726564656E7469616C"))
        .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
        .build()
    );

    DepositPreAuthLedgerEntryParams depositPreAuthLedgerEntryParams = DepositPreAuthLedgerEntryParams.builder()
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .authorizedCredentials(credentials)
      .build();

    assertThat(depositPreAuthLedgerEntryParams.authorizedCredentials()).isEqualTo(credentials);
    assertThat(depositPreAuthLedgerEntryParams.owner()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(depositPreAuthLedgerEntryParams.authorized()).isNotPresent();
  }

  @Test
  public void testDepositPreAuthParamsMoreThanEightAuthorizedCredentials() {
    List<DepositPreAuthCredential> moreThanEight = IntStream.range(0, 9)
      .mapToObj(i ->
        DepositPreAuthCredential.builder()
          .issuer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .credentialType(
            CredentialType.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i)
          )
          .build()
      ).collect(Collectors.toList());

    assertThatThrownBy(() -> DepositPreAuthLedgerEntryParams.builder()
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .authorizedCredentials(moreThanEight)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("authorizedCredentials should have less than or equal to 8 items.");
  }

  @Test
  public void testDepositPreAuthParamsDuplicateAuthorizedCredentials() {
    List<DepositPreAuthCredential> randomCredentials = IntStream.range(0, 8)
      .mapToObj(i ->
        DepositPreAuthCredential.builder()
          .issuer(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
          .credentialType(
            CredentialType.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i)
          )
          .build()
      ).collect(Collectors.toList());

    randomCredentials.set(0, randomCredentials.get(1));

    assertThatThrownBy(() -> DepositPreAuthLedgerEntryParams.builder()
      .owner(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .authorizedCredentials(randomCredentials)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("authorizedCredentials should have unique values.");
  }
}

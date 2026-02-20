package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CredentialAccept}.
 */
public class CredentialAcceptTest {

  @Test
  public void testCredentialAccept() {
    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .issuer(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    assertThat(credentialAccept.transactionType()).isEqualTo(TransactionType.CREDENTIAL_ACCEPT);
    assertThat(credentialAccept.account()).isEqualTo(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"));
    assertThat(credentialAccept.issuer()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(credentialAccept.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(10));
    assertThat(credentialAccept.credentialType()).isEqualTo(CredentialType.of("AFDD"));
  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .issuer(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    assertThat(credentialAccept.transactionFlags()).isEqualTo(credentialAccept.flags());
    assertThat(credentialAccept.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    CredentialAccept original = CredentialAccept.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .issuer(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    CredentialAccept copied = CredentialAccept.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}

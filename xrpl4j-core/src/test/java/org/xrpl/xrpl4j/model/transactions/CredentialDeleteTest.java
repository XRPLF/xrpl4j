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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CredentialDelete}.
 */
public class CredentialDeleteTest {

  @Test
  public void testCredentialDelete() {
    CredentialDelete credentialDelete = CredentialDelete.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    assertThat(credentialDelete.transactionType()).isEqualTo(TransactionType.CREDENTIAL_DELETE);
    assertThat(credentialDelete.account()).isEqualTo(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"));
    
    assertThat(credentialDelete.issuer()).isPresent().get()
      .isEqualTo(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"));

    assertThat(credentialDelete.subject()).isPresent().get()
      .isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));

    assertThat(credentialDelete.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(10));
    assertThat(credentialDelete.credentialType()).isEqualTo(CredentialType.of("AFDD"));
  }

  @Test
  public void testSubjectOrIssuerPresence() {
    assertThatThrownBy(() -> CredentialDelete.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Either Subject or Issuer must be present.");
  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    CredentialDelete credentialDelete = CredentialDelete.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    assertThat(credentialDelete.transactionFlags()).isEqualTo(credentialDelete.flags());
    assertThat(credentialDelete.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    CredentialDelete original = CredentialDelete.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    CredentialDelete copied = CredentialDelete.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }

}

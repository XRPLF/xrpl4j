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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CredentialCreate}.
 */
public class CredentialCreateTest {

  @Test
  public void testCredentialCreate() {
    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .uri(CredentialUri.ofPlainText("http://sample-url.com"))
      .expiration(UnsignedLong.valueOf(200))
      .build();

    assertThat(credentialCreate.transactionType()).isEqualTo(TransactionType.CREDENTIAL_CREATE);
    assertThat(credentialCreate.account()).isEqualTo(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"));
    assertThat(credentialCreate.subject()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(credentialCreate.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(10));
    assertThat(credentialCreate.credentialType()).isEqualTo(CredentialType.of("AFDD"));
    assertThat(credentialCreate.expiration()).isPresent().get().isEqualTo(UnsignedLong.valueOf(200));
    assertThat(credentialCreate.uri()).isPresent().get().isEqualTo(
      CredentialUri.of("687474703A2F2F73616D706C652D75726C2E636F6D"));

  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    assertThat(credentialCreate.transactionFlags()).isEqualTo(credentialCreate.flags());
    assertThat(credentialCreate.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    CredentialCreate original = CredentialCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build();

    CredentialCreate copied = CredentialCreate.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}

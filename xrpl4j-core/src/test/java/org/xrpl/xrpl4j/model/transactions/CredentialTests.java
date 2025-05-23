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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link CredentialCreate}, {@link CredentialDelete}.
 */
public class CredentialTests {

  @Test
  public void testTooLongCredentialType() {
    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("A1B2C3D4E5F60718293A4B5C6D7E8F90123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0F"))
      .build(),
"CredentialType must be <= 128 characters.");
  }

  @Test
  public void testEmptyCredentialType() {
    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of(""))
      .uri(CredentialUri.of("ABCD"))
      .build(),
    "CredentialType must not be empty.");
  }

  @Test
  public void testCredentialCreateWithoutOptional() {
    CredentialCreate.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build();
  }

  @Test
  public void testCredentialDelete() {
    assertThrows(
      IllegalStateException.class,
      () -> CredentialDelete.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .credentialType(CredentialType.of("AFDD"))
      .build(),
    "Either Subject or Issuer must be present.");
  }
}

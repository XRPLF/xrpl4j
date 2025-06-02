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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for {@link CredentialCreate}, {@link CredentialDelete}.
 */
public class CredentialTest {

  @Test
  public void testTooLongCredentialTypeUri() {
    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .credentialType(CredentialType.ofPlainText(Strings.repeat("A", 130)))
        .build(),
      "CredentialType must be <= 128 characters.");

    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .credentialType(CredentialType.of("A1"))
        .uri(CredentialUri.ofPlainText(Strings.repeat("a", 513)))
        .build(),
      "CredentialUri must be <= 512 characters.");
  }

  @Test
  public void testEmptyCredentialTypeUri() {
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

    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .credentialType(CredentialType.of("AB"))
        .uri(CredentialUri.of(""))
        .build(),
      "CredentialUri must not be empty.");
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

  @Test
  public void testCredentialTypeUriNotHexEncoded() {
    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .credentialType(CredentialType.of("ZZ"))
        .build(),
      "CredentialType must be encoded in hexadecimal.");

    assertThrows(
      IllegalArgumentException.class,
      () -> CredentialCreate.builder()
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .subject(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .credentialType(CredentialType.ofPlainText("driver license"))
        .uri(CredentialUri.of("ZZ"))
        .build(),
      "CredentialUri must be encoded in hexadecimal.");
  }
}

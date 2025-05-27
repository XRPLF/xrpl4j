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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DepositPreAuthTests {

  @Test
  public void depositPreAuthWithAuthorize() {
    Address authorize = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .authorize(authorize)
      .build();

    assertThat(depositPreAuth.authorize()).isNotEmpty().get().isEqualTo(authorize);
    assertThat(depositPreAuth.unauthorize()).isEmpty();
  }

  @Test
  public void depositPreAuthWithUnauthorize() {
    Address unauthorize = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(2))
      .unauthorize(unauthorize)
      .build();

    assertThat(depositPreAuth.unauthorize()).isNotEmpty().get().isEqualTo(unauthorize);
    assertThat(depositPreAuth.authorize()).isEmpty();
  }

  @Test
  public void depositPreAuthWithoutAuthorizeOrUnauthorizeThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .build(),
      "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both."
    );
  }

  @Test
  public void depositPreAuthWithAuthorizeAndUnauthorizeThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .unauthorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .build(),
      "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both."
    );
  }

  @Test
  public void moreThanEightUnauthorizeCredentialsOrAuthorizeCredentials() {
    List<CredentialWrapper> moreThanEight = IntStream.range(0, 9).mapToObj(i -> CredentialWrapper.builder()
      .credential(Credential.builder()
        .issuer(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt" + i))
        .credentialType(CredentialType.of("A" + i))
        .build()).build()
    ).collect(Collectors.toList());

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .unauthorizeCredentials(moreThanEight)
        .build(),
      "UnauthorizeCredentials should not have more than 8 credentials."
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorizeCredentials(moreThanEight)
        .build(),
      "AuthorizeCredentials should not have more than 8 credentials."
    );
  }

  @Test
  public void emptyUnauthorizeCredentialsOrAuthorizeCredentials() {
    List<CredentialWrapper> empty = new ArrayList<>();

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .unauthorizeCredentials(empty)
        .build(),
      "UnauthorizeCredentials list should not be empty."
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorizeCredentials(empty)
        .build(),
      "AuthorizeCredentials list should not be empty."
    );
  }

  @Test
  public void duplicateUnauthorizeCredentialsOrAuthorizeCredentials() {
    List<CredentialWrapper> duplicateCreds = IntStream.range(0, 8).mapToObj(i -> CredentialWrapper.builder()
      .credential(Credential.builder()
        .issuer(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt"))
        .credentialType(CredentialType.of(BaseEncoding.base16().encode("A".getBytes())))
        .build()).build()
    ).collect(Collectors.toList());

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .unauthorizeCredentials(duplicateCreds)
        .build(),
      "UnauthorizeCredentials list should not be empty."
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorizeCredentials(duplicateCreds)
        .build(),
      "AuthorizeCredentials list should not be empty."
    );
  }
}

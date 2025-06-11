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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link AccountDelete}.
 */
public class AccountDeleteTest {

  @Test
  public void simpleAccountDelete() {
    List<Hash256> credentialIds = IntStream.range(0, 8)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destination(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .destinationTag(UnsignedInteger.valueOf(4))
      .credentialIds(credentialIds)
      .build();

    assertThat(accountDelete.transactionType()).isEqualTo(TransactionType.ACCOUNT_DELETE);
    assertThat(accountDelete.account()).isEqualTo(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"));
    assertThat(accountDelete.destination()).isEqualTo(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"));
    assertThat(accountDelete.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(accountDelete.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(12));
    assertThat(accountDelete.destinationTag()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(4));
    assertThat(accountDelete.credentialIds()).isEqualTo(credentialIds);
  }

  @Test
  public void testMoreThanEightCredentialIds() {
    List<Hash256> moreThanEight = IntStream.range(0, 9)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    assertThatThrownBy(() -> AccountDelete.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destination(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .destinationTag(UnsignedInteger.valueOf(4))
      .credentialIds(moreThanEight)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialIDs should have less than or equal to 8 items.");
  }

  @Test
  public void testDuplicateCredentialIds() {
    List<Hash256> randomIds = IntStream.range(0, 8)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    randomIds.set(1, randomIds.get(0));

    assertThatThrownBy(() -> AccountDelete.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destination(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .destinationTag(UnsignedInteger.valueOf(4))
      .credentialIds(randomIds)
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialIDs should have unique values.");
  }
}

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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

public class CheckTests {

  @Test
  public void checkCashWithAmount() {
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(100);
    Hash256 checkId = Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334");
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(checkId)
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .amount(amount)
      .build();

    assertThat(checkCash.amount()).isPresent().get().isEqualTo(amount);
    assertThat(checkCash.deliverMin()).isEmpty();
    assertThat(checkCash.checkId()).isEqualTo(checkId);
  }

  @Test
  public void checkCashWithDeliverMin() {
    XrpCurrencyAmount deliverMin = XrpCurrencyAmount.ofDrops(100);
    Hash256 checkId = Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334");
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(checkId)
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .deliverMin(deliverMin)
      .build();

    assertThat(checkCash.deliverMin()).isPresent().get().isEqualTo(deliverMin);
    assertThat(checkCash.amount()).isEmpty();
    assertThat(checkCash.checkId()).isEqualTo(checkId);
  }

  @Test
  public void checkCashWithoutAmountOrDeliverMinThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> CheckCash.builder()
        .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(12))
        .build(),
      "The CheckCash transaction must include either amount or deliverMin, but not both."
    );
  }

  @Test
  public void checkCashWithAmountAndDeliverMinThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> CheckCash.builder()
        .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(12))
        .amount(XrpCurrencyAmount.ofDrops(100))
        .deliverMin(XrpCurrencyAmount.ofDrops(100))
        .build(),
      "The CheckCash transaction must include either amount or deliverMin, but not both."
    );
  }

  // CheckCancel transactionFlags tests
  @Test
  public void checkCancelTransactionFlagsReturnsEmptyFlags() {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .build();

    assertThat(checkCancel.transactionFlags()).isEqualTo(checkCancel.flags());
    assertThat(checkCancel.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void checkCancelBuilderFromCopiesFlagsCorrectly() {
    CheckCancel original = CheckCancel.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .build();

    CheckCancel copied = CheckCancel.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }

  // CheckCash transactionFlags tests
  @Test
  public void checkCashTransactionFlagsReturnsEmptyFlags() {
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .build();

    assertThat(checkCash.transactionFlags()).isEqualTo(checkCash.flags());
    assertThat(checkCash.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void checkCashBuilderFromCopiesFlagsCorrectly() {
    CheckCash original = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .build();

    CheckCash copied = CheckCash.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }

  // CheckCreate transactionFlags tests
  @Test
  public void checkCreateTransactionFlagsReturnsEmptyFlags() {
    CheckCreate checkCreate = CheckCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destination(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .sendMax(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .build();

    assertThat(checkCreate.transactionFlags()).isEqualTo(checkCreate.flags());
    assertThat(checkCreate.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void checkCreateBuilderFromCopiesFlagsCorrectly() {
    CheckCreate original = CheckCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destination(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .sendMax(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .build();

    CheckCreate copied = CheckCreate.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}

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
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EscrowCreate}.
 */
public class EscrowCreateTest {


  @Test
  public void testWithNeitherCancelNorFinishNorFinishFunction() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build(),
      "EscrowCreate must have at least one of CancelAfter, FinishAfter, or FinishFunction."
    );
  }

  @Test
  public void testWithCancelAfterOnly() {
    EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .build();
  }

  @Test
  public void testWithFinishAfterOnly() {
    EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .finishAfter(UnsignedLong.valueOf(533171558))
      .build();
  }

  @Test
  public void testCancelBeforeFinish() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .cancelAfter(UnsignedLong.ONE)
        .finishAfter(UnsignedLong.valueOf(2L))
        .build(),
      "If both CancelAfter and FinishAfter are specified, the FinishAfter time must be before the CancelAfter time."
    );
  }

  @Test
  public void testCancelAfterFinish() {
    EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .cancelAfter(UnsignedLong.valueOf(2L))
      .finishAfter(UnsignedLong.ONE)
      .build();
  }

  @Test
  public void testCancelEqualsFinish() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .cancelAfter(UnsignedLong.ONE)
        .finishAfter(UnsignedLong.ONE)
        .build(),
      "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both."
    );
  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .build();

    assertThat(escrowCreate.transactionFlags()).isEqualTo(escrowCreate.flags());
    assertThat(escrowCreate.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    EscrowCreate original = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .build();

    EscrowCreate copied = EscrowCreate.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }

  @Test
  public void testSmartEscrowWithFinishFunction() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .finishFunction(FinishFunction.of("0061736D01000000"))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .build();

    assertThat(escrowCreate.finishFunction()).isNotEmpty();
    assertThat(escrowCreate.finishFunction().get().value()).isEqualTo("0061736D01000000");
    assertThat(escrowCreate.cancelAfter()).isNotEmpty();
  }

  @Test
  public void testSmartEscrowWithFinishFunctionAndData() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .finishFunction(FinishFunction.of("0061736D01000000"))
      .data(EscrowData.of("DEADBEEF"))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .build();

    assertThat(escrowCreate.finishFunction()).isNotEmpty();
    assertThat(escrowCreate.data()).isNotEmpty();
    assertThat(escrowCreate.data().get().value()).isEqualTo("DEADBEEF");
  }

  @Test
  public void testSmartEscrowFinishFunctionWithoutCancelAfter() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .amount(XrpCurrencyAmount.ofDrops(1000))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .finishFunction(FinishFunction.of("0061736D01000000"))
        .build(),
      "If FinishFunction is present, CancelAfter must also be present."
    );
  }

  @Test
  public void testSmartEscrowWithFinishFunctionOnly() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .finishFunction(FinishFunction.of("0061736D01000000"))
      .cancelAfter(UnsignedLong.valueOf(533257958))
      .build();

    assertThat(escrowCreate.finishFunction()).isNotEmpty();
    assertThat(escrowCreate.finishAfter()).isEmpty();
    assertThat(escrowCreate.condition()).isEmpty();
  }

}

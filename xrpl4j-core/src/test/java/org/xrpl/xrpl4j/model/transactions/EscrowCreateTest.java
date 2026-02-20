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
  public void testWithNeitherCancelNorFinish() {
    EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
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
      .build();

    EscrowCreate copied = EscrowCreate.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }

}

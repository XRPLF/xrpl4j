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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Memo}.
 */
@SuppressWarnings( {"OptionalGetWithoutIsPresent", "ResultOfMethodCallIgnored"})
class MemoTest {

  @Test
  void testEmptyMemo() {
    final Memo emptyMemo = Memo.builder().build();
    assertThat(emptyMemo.memoData()).isEmpty();
    assertThat(emptyMemo.memoType()).isEmpty();
    assertThat(emptyMemo.memoFormat()).isEmpty();
  }

  @Test
  void testSingleHexCharMemo() {
    final Memo emptyMemo = Memo.builder()
      .memoData("A")
      .build();
    assertThat(emptyMemo.memoData()).isNotEmpty();
    assertThat(emptyMemo.memoData().get()).isEqualTo("A");
    assertThat(emptyMemo.memoType()).isEmpty();
    assertThat(emptyMemo.memoFormat()).isEmpty();
  }

  @Test
  void lowercaseHexChars() {
    final Memo emptyMemo = Memo.builder()
      .memoData("abcdef1234")
      .memoType("abcdef1234")
      .memoFormat("abcdef1234")
      .build();
    assertThat(emptyMemo.memoData()).isNotEmpty();
    assertThat(emptyMemo.memoData().get()).isEqualTo("abcdef1234");
    assertThat(emptyMemo.memoType()).isNotEmpty();
    assertThat(emptyMemo.memoType().get()).isEqualTo("abcdef1234");
    assertThat(emptyMemo.memoFormat()).isNotEmpty();
    assertThat(emptyMemo.memoFormat().get()).isEqualTo("abcdef1234");
  }

  @Test
  void uppercaseHexChars() {
    final Memo emptyMemo = Memo.builder()
      .memoData("ABCDEF1234")
      .memoType("ABCDEF1234")
      .memoFormat("ABCDEF1234")
      .build();
    assertThat(emptyMemo.memoData()).isNotEmpty();
    assertThat(emptyMemo.memoData().get()).isEqualTo("ABCDEF1234");
    assertThat(emptyMemo.memoType()).isNotEmpty();
    assertThat(emptyMemo.memoType().get()).isEqualTo("ABCDEF1234");
    assertThat(emptyMemo.memoFormat()).isNotEmpty();
    assertThat(emptyMemo.memoFormat().get()).isEqualTo("ABCDEF1234");
  }

  @Test
  void testWithPlaintextSingleChar() {
    Memo memo = Memo.withPlaintext("Z").build();
    assertThat(memo.memoData()).isNotEmpty();
    assertThat(memo.memoData().get()).isEqualTo("5A");
    assertThat(memo.memoType()).isEmpty();
    assertThat(memo.memoFormat()).isNotEmpty();
    assertThat(memo.memoFormat().get()).isEqualTo("746578742F706C61696E");
  }

  @Test
  void testMemoWithNonHexFields() {
    IllegalStateException thrownException = Assertions.assertThrows(IllegalStateException.class, () -> Memo.builder()
      .memoData("Hello World")
      .memoType("0123456789ABCDEF")
      .memoFormat("0123456789ABCDEF")
      .build());
    assertThat(thrownException.getMessage()).isEqualTo("MemoData must be a hex-encoded string");

    thrownException = Assertions.assertThrows(IllegalStateException.class, () -> Memo.builder()
      .memoData("0123456789ABCDEF")
      .memoType("Hello World")
      .memoFormat("0123456789ABCDEF")
      .build());
    assertThat(thrownException.getMessage()).isEqualTo("MemoType must be a hex-encoded string");

    thrownException = Assertions.assertThrows(IllegalStateException.class, () -> Memo.builder()
      .memoData("0123456789ABCDEF")
      .memoType("0123456789ABCDEF")
      .memoFormat("Hello World")
      .build());
    assertThat(thrownException.getMessage()).isEqualTo("MemoFormat must be a hex-encoded string");
  }

  @Test
  void testMemoWithAllHexFields() {
    assertThat(Memo.builder()
      .memoData("0123456789ABCDEF")
      .memoType("0123456789ABCDEF")
      .memoFormat("0123456789ABCDEF")
      .build()).isNotNull();
  }

  @Test
  void testBuildFromPlain() {
    final Memo plainTextMemo = Memo.withPlaintext("Hello World").build();
    assertThat(plainTextMemo.memoType()).isEmpty();
    assertThat(plainTextMemo.memoFormat().get()).isEqualTo("746578742F706C61696E");
    assertThat(plainTextMemo.memoData().get()).isEqualTo("48656C6C6F20576F726C64");
  }
}

package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Memo}.
 */
class MemoTest {

  @Test
  void testEmptyMemo() {
    final Memo emptyMemo = Memo.builder().build();
    assertThat(emptyMemo.memoData()).isEmpty();
    assertThat(emptyMemo.memoType()).isEmpty();
    assertThat(emptyMemo.memoFormat()).isEmpty();
  }

  @Test
  void testMemoWithNonHexFields() {
    IllegalStateException thrownException = Assertions.assertThrows(IllegalStateException.class, () -> {
      Memo.builder()
        .memoData("Hello World")
        .memoType("0123456789ABCDEF")
        .memoFormat("0123456789ABCDEF")
        .build();
    });
    assertThat(thrownException.getMessage()).isEqualTo("MemoData must be a hex-encoded string");

    thrownException = Assertions.assertThrows(IllegalStateException.class, () -> {
      Memo.builder()
        .memoData("0123456789ABCDEF")
        .memoType("Hello World")
        .memoFormat("0123456789ABCDEF")
        .build();
    });
    assertThat(thrownException.getMessage()).isEqualTo("MemoType must be a hex-encoded string");

    thrownException = Assertions.assertThrows(IllegalStateException.class, () -> {
      Memo.builder()
        .memoData("0123456789ABCDEF")
        .memoType("0123456789ABCDEF")
        .memoFormat("Hello World")
        .build();
    });
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
    final Memo plainTextMemo = Memo.withPlainText("Hello World").build();
    assertThat(plainTextMemo.memoType()).isEmpty();
    assertThat(plainTextMemo.memoFormat().get()).isEqualTo("746578742F706C61696E");
    assertThat(plainTextMemo.memoData().get()).isEqualTo("48656C6C6F20576F726C64");
  }
}
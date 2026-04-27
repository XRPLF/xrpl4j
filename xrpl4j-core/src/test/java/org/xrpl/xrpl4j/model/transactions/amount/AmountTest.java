package org.xrpl.xrpl4j.model.transactions.amount;

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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link Amount} — {@link Amount#isNegative()}, {@link Amount#handle}, and {@link Amount#map}.
 *
 * <p>Because {@link Amount} is an interface, each test exercises it through one of its three
 * concrete subtypes ({@link XrpAmount}, {@link IouAmount}, {@link MptAmount}).
 */
class AmountTest {

  // -------------------------------------------------------------------------
  // isNegative()
  // -------------------------------------------------------------------------

  @Test
  void isNegativeFalseForPositiveXrp() {
    assertThat((Amount) XrpAmount.ofDrops(1_000L)).satisfies(a -> assertThat(a.isNegative()).isFalse());
  }

  @Test
  void isNegativeTrueForNegativeXrp() {
    assertThat((Amount) XrpAmount.ofDrops(-1_000L)).satisfies(a -> assertThat(a.isNegative()).isTrue());
  }

  @Test
  void isNegativeFalseForPositiveIou() {
    assertThat((Amount) IouAmount.of("100.50")).satisfies(a -> assertThat(a.isNegative()).isFalse());
  }

  @Test
  void isNegativeTrueForNegativeIou() {
    assertThat((Amount) IouAmount.of("-100.50")).satisfies(a -> assertThat(a.isNegative()).isTrue());
  }

  @Test
  void isNegativeFalseForPositiveMpt() {
    assertThat((Amount) MptAmount.of(UnsignedLong.valueOf(1_000L))).satisfies(a -> assertThat(a.isNegative()).isFalse());
  }

  @Test
  void isNegativeTrueForNegativeMpt() {
    assertThat((Amount) MptAmount.of(UnsignedLong.valueOf(1_000L), true))
      .satisfies(a -> assertThat(a.isNegative()).isTrue());
  }

  // -------------------------------------------------------------------------
  // handle() — correct branch dispatched
  // -------------------------------------------------------------------------

  @Test
  void handleDispatchesToXrpHandler() {
    AtomicBoolean xrpCalled = new AtomicBoolean(false);
    XrpAmount.ofDrops(1_000L).handle(
      xrp -> xrpCalled.set(true),
      mpt -> { throw new AssertionError("mpt handler called for XrpAmount"); },
      iou -> { throw new AssertionError("iou handler called for XrpAmount"); }
    );
    assertThat(xrpCalled).isTrue();
  }

  @Test
  void handleDispatchesToMptHandler() {
    AtomicBoolean mptCalled = new AtomicBoolean(false);
    MptAmount.of(UnsignedLong.valueOf(1_000L)).handle(
      xrp -> { throw new AssertionError("xrp handler called for MptAmount"); },
      mpt -> mptCalled.set(true),
      iou -> { throw new AssertionError("iou handler called for MptAmount"); }
    );
    assertThat(mptCalled).isTrue();
  }

  @Test
  void handleDispatchesToIouHandler() {
    AtomicBoolean iouCalled = new AtomicBoolean(false);
    IouAmount.of("100.50").handle(
      xrp -> { throw new AssertionError("xrp handler called for IouAmount"); },
      mpt -> { throw new AssertionError("mpt handler called for IouAmount"); },
      iou -> iouCalled.set(true)
    );
    assertThat(iouCalled).isTrue();
  }

  @Test
  void handlePassesCorrectValueToXrpHandler() {
    XrpAmount xrp = XrpAmount.ofDrops(42_000L);
    xrp.handle(
      received -> assertThat(received).isEqualTo(xrp),
      mpt -> { },
      iou -> { }
    );
  }

  @Test
  void handlePassesCorrectValueToMptHandler() {
    MptAmount mpt = MptAmount.of(UnsignedLong.valueOf(99L));
    mpt.handle(
      xrp -> { },
      received -> assertThat(received).isEqualTo(mpt),
      iou -> { }
    );
  }

  @Test
  void handlePassesCorrectValueToIouHandler() {
    IouAmount iou = IouAmount.of("7.77");
    iou.handle(
      xrp -> { },
      mpt -> { },
      received -> assertThat(received).isEqualTo(iou)
    );
  }

  @Test
  void handleNullXrpHandlerThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).handle(null, mpt -> { }, iou -> { }))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void handleNullMptHandlerThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).handle(xrp -> { }, null, iou -> { }))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void handleNullIouHandlerThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).handle(xrp -> { }, mpt -> { }, null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // map() — correct branch dispatched and return value propagated
  // -------------------------------------------------------------------------

  @Test
  void mapDispatchesToXrpMapper() {
    String result = XrpAmount.ofDrops(1_000L).map(
      xrp -> "xrp",
      mpt -> { throw new AssertionError("mpt mapper called for XrpAmount"); },
      iou -> { throw new AssertionError("iou mapper called for XrpAmount"); }
    );
    assertThat(result).isEqualTo("xrp");
  }

  @Test
  void mapDispatchesToMptMapper() {
    String result = MptAmount.of(UnsignedLong.valueOf(1_000L)).map(
      xrp -> { throw new AssertionError("xrp mapper called for MptAmount"); },
      mpt -> "mpt",
      iou -> { throw new AssertionError("iou mapper called for MptAmount"); }
    );
    assertThat(result).isEqualTo("mpt");
  }

  @Test
  void mapDispatchesToIouMapper() {
    String result = IouAmount.of("100.50").map(
      xrp -> { throw new AssertionError("xrp mapper called for IouAmount"); },
      mpt -> { throw new AssertionError("mpt mapper called for IouAmount"); },
      iou -> "iou"
    );
    assertThat(result).isEqualTo("iou");
  }

  @Test
  void mapReturnsValueFromXrpMapper() {
    XrpAmount xrp = XrpAmount.ofDrops(1_000_000L);
    Long drops = xrp.map(
      x -> x.unsignedLongValue().longValue(),
      mpt -> -1L,
      iou -> -1L
    );
    assertThat(drops).isEqualTo(1_000_000L);
  }

  @Test
  void mapNullXrpMapperThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).map(null, mpt -> "mpt", iou -> "iou"))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void mapNullMptMapperThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).map(xrp -> "xrp", null, iou -> "iou"))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void mapNullIouMapperThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).map(xrp -> "xrp", mpt -> "mpt", null))
      .isInstanceOf(NullPointerException.class);
  }
}

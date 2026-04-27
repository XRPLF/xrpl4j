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
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link TokenAmount} — {@link TokenAmount#handle} and {@link TokenAmount#map}.
 *
 * <p>Because {@link TokenAmount} is an interface, each test exercises it through one of its three
 * concrete subtypes ({@link XrpTokenAmount}, {@link IouTokenAmount}, {@link MptTokenAmount}).
 */
class TokenAmountTest {

  private static final String ISSUER = "rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV";
  private static final String ISSUANCE_ID = "00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41";

  private XrpTokenAmount xrp() {
    return XrpTokenAmount.ofDrops(1_000_000L);
  }

  private IouTokenAmount iou() {
    return IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
  }

  private MptTokenAmount mpt() {
    return MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
  }

  // -------------------------------------------------------------------------
  // handle() — correct branch dispatched
  // -------------------------------------------------------------------------

  @Test
  void handleDispatchesToXrpHandler() {
    AtomicBoolean xrpCalled = new AtomicBoolean(false);
    xrp().handle(
      xrp -> xrpCalled.set(true),
      iou -> {
        throw new AssertionError("iou handler called for XrpTokenAmount");
      },
      mpt -> {
        throw new AssertionError("mpt handler called for XrpTokenAmount");
      }
    );
    assertThat(xrpCalled).isTrue();
  }

  @Test
  void handleDispatchesToIouHandler() {
    AtomicBoolean iouCalled = new AtomicBoolean(false);
    iou().handle(
      xrp -> {
        throw new AssertionError("xrp handler called for IouTokenAmount");
      },
      iou -> iouCalled.set(true),
      mpt -> {
        throw new AssertionError("mpt handler called for IouTokenAmount");
      }
    );
    assertThat(iouCalled).isTrue();
  }

  @Test
  void handleDispatchesToMptHandler() {
    AtomicBoolean mptCalled = new AtomicBoolean(false);
    mpt().handle(
      xrp -> {
        throw new AssertionError("xrp handler called for MptTokenAmount");
      },
      iou -> {
        throw new AssertionError("iou handler called for MptTokenAmount");
      },
      mpt -> mptCalled.set(true)
    );
    assertThat(mptCalled).isTrue();
  }

  @Test
  void handlePassesCorrectXrpInstance() {
    XrpTokenAmount expected = xrp();
    expected.handle(
      received -> assertThat(received).isEqualTo(expected),
      iou -> {
      },
      mpt -> {
      }
    );
  }

  @Test
  void handlePassesCorrectIouInstance() {
    IouTokenAmount expected = iou();
    expected.handle(
      xrp -> {
      },
      received -> assertThat(received).isEqualTo(expected),
      mpt -> {
      }
    );
  }

  @Test
  void handlePassesCorrectMptInstance() {
    MptTokenAmount expected = mpt();
    expected.handle(
      xrp -> {
      },
      iou -> {
      },
      received -> assertThat(received).isEqualTo(expected)
    );
  }

  @Test
  void handleNullXrpHandlerThrows() {
    assertThatThrownBy(() -> xrp().handle(null, iou -> {
    }, mpt -> {
    }))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void handleNullIouHandlerThrows() {
    assertThatThrownBy(() -> xrp().handle(xrp -> {
    }, null, mpt -> {
    }))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void handleNullMptHandlerThrows() {
    assertThatThrownBy(() -> xrp().handle(xrp -> {
    }, iou -> {
    }, null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // map() — correct branch and return value
  // -------------------------------------------------------------------------

  @Test
  void mapDispatchesToXrpMapper() {
    assertThat(xrp().map(xrp -> "xrp", iou -> "iou", mpt -> "mpt").toString()).isEqualTo("xrp");
  }

  @Test
  void mapDispatchesToIouMapper() {
    assertThat(iou().map(xrp -> "xrp", iou -> "iou", mpt -> "mpt").toString()).isEqualTo("iou");
  }

  @Test
  void mapDispatchesToMptMapper() {
    assertThat(mpt().map(xrp -> "xrp", iou -> "iou", mpt -> "mpt").toString()).isEqualTo("mpt");
  }

  @Test
  void mapReturnsValueFromXrpMapper() {
    Long drops = xrp().map(
      x -> x.amount().unsignedLongValue().longValue(),
      iou -> -1L,
      mpt -> -1L
    );
    assertThat(drops).isEqualTo(1_000_000L);
  }

  @Test
  void mapReturnsValueFromIouMapper() {
    String currency = iou().map(
      xrp -> "wrong",
      i -> i.currency(),
      mpt -> "wrong"
    );
    assertThat(currency).isEqualTo("USD");
  }

  @Test
  void mapReturnsValueFromMptMapper() {
    MpTokenIssuanceId id = mpt().map(
      xrp -> null,
      iou -> null,
      m -> m.mptIssuanceId()
    );
    assertThat(id).isEqualTo(MpTokenIssuanceId.of(ISSUANCE_ID));
  }

  @Test
  void mapNullXrpMapperThrows() {
    assertThatThrownBy(() -> xrp().map(null, iou -> "iou", mpt -> "mpt"))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void mapNullIouMapperThrows() {
    assertThatThrownBy(() -> xrp().map(xrp -> "xrp", null, mpt -> "mpt"))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void mapNullMptMapperThrows() {
    assertThatThrownBy(() -> xrp().map(xrp -> "xrp", iou -> "iou", null))
      .isInstanceOf(NullPointerException.class);
  }
}

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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for {@link Permission} interface methods.
 */
public class PermissionTest {

  @Test
  void testHandleWithTransactionTypePermission() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);
    AtomicBoolean transactionTypeHandlerCalled = new AtomicBoolean(false);
    AtomicBoolean granularHandlerCalled = new AtomicBoolean(false);
    AtomicBoolean unknownHandlerCalled = new AtomicBoolean(false);

    permission.handle(
      txPermission -> {
        transactionTypeHandlerCalled.set(true);
        assertThat(txPermission.transactionType()).isEqualTo(TransactionType.PAYMENT);
      },
      granularPermission -> granularHandlerCalled.set(true),
      unknownPermission -> unknownHandlerCalled.set(true)
    );

    assertThat(transactionTypeHandlerCalled.get()).isTrue();
    assertThat(granularHandlerCalled.get()).isFalse();
    assertThat(unknownHandlerCalled.get()).isFalse();
  }

  @Test
  void testHandleWithGranularPermissionValue() {
    Permission permission = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);
    AtomicBoolean transactionTypeHandlerCalled = new AtomicBoolean(false);
    AtomicBoolean granularHandlerCalled = new AtomicBoolean(false);
    AtomicBoolean unknownHandlerCalled = new AtomicBoolean(false);

    permission.handle(
      txPermission -> transactionTypeHandlerCalled.set(true),
      granularPermission -> {
        granularHandlerCalled.set(true);
        assertThat(granularPermission.granularPermission()).isEqualTo(GranularPermission.TRUSTLINE_AUTHORIZE);
      },
      unknownPermission -> unknownHandlerCalled.set(true)
    );

    assertThat(transactionTypeHandlerCalled.get()).isFalse();
    assertThat(granularHandlerCalled.get()).isTrue();
    assertThat(unknownHandlerCalled.get()).isFalse();
  }

  @Test
  void testHandleWithUnknownPermission() {
    Permission permission = UnknownPermission.of("SomeFutureTransactionType");
    AtomicBoolean transactionTypeHandlerCalled = new AtomicBoolean(false);
    AtomicBoolean granularHandlerCalled = new AtomicBoolean(false);
    AtomicBoolean unknownHandlerCalled = new AtomicBoolean(false);

    permission.handle(
      txPermission -> transactionTypeHandlerCalled.set(true),
      granularPermission -> granularHandlerCalled.set(true),
      unknownPermission -> {
        unknownHandlerCalled.set(true);
        assertThat(unknownPermission.value()).isEqualTo("SomeFutureTransactionType");
      }
    );

    assertThat(transactionTypeHandlerCalled.get()).isFalse();
    assertThat(granularHandlerCalled.get()).isFalse();
    assertThat(unknownHandlerCalled.get()).isTrue();
  }

  @Test
  void testHandleWithNullTransactionTypeHandlerThrowsException() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThrows(NullPointerException.class, () ->
      permission.handle(null, granularPermission -> { }, unknownPermission -> { })
    );
  }

  @Test
  void testHandleWithNullGranularHandlerThrowsException() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThrows(NullPointerException.class, () ->
      permission.handle(txPermission -> { }, null, unknownPermission -> { })
    );
  }

  @Test
  void testHandleWithNullUnknownHandlerThrowsException() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThrows(NullPointerException.class, () ->
      permission.handle(txPermission -> { }, granularPermission -> { }, null)
    );
  }

  @Test
  void testMapWithTransactionTypePermission() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    String result = permission.map(
      txPermission -> "TransactionType: " + txPermission.transactionType().value(),
      granularPermission -> "GranularPermission: " + granularPermission.granularPermission().value(),
      unknownPermission -> "UnknownPermission: " + unknownPermission.value()
    );

    assertThat(result).isEqualTo("TransactionType: Payment");
  }

  @Test
  void testMapWithGranularPermissionValue() {
    Permission permission = GranularPermissionValue.of(GranularPermission.PAYMENT_MINT);

    String result = permission.map(
      txPermission -> "TransactionType: " + txPermission.transactionType().value(),
      granularPermission -> "GranularPermission: " + granularPermission.granularPermission().value(),
      unknownPermission -> "UnknownPermission: " + unknownPermission.value()
    );

    assertThat(result).isEqualTo("GranularPermission: PaymentMint");
  }

  @Test
  void testMapWithUnknownPermission() {
    Permission permission = UnknownPermission.of("SomeFutureTransactionType");

    String result = permission.map(
      txPermission -> "TransactionType: " + txPermission.transactionType().value(),
      granularPermission -> "GranularPermission: " + granularPermission.granularPermission().value(),
      unknownPermission -> "UnknownPermission: " + unknownPermission.value()
    );

    assertThat(result).isEqualTo("UnknownPermission: SomeFutureTransactionType");
  }

  @Test
  void testMapWithNullTransactionTypeMapperThrowsException() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThrows(NullPointerException.class, () ->
      permission.map(null, granularPermission -> "test", unknownPermission -> "test")
    );
  }

  @Test
  void testMapWithNullGranularMapperThrowsException() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThrows(NullPointerException.class, () ->
      permission.map(txPermission -> "test", null, unknownPermission -> "test")
    );
  }

  @Test
  void testMapWithNullUnknownMapperThrowsException() {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThrows(NullPointerException.class, () ->
      permission.map(txPermission -> "test", granularPermission -> "test", null)
    );
  }

  @Test
  void testValueMethodForTransactionTypePermission() {
    Permission permission = TransactionTypePermission.of(TransactionType.ESCROW_CREATE);

    String value = permission.map(
      txPermission -> txPermission.value(),
      granularPermission -> granularPermission.value(),
      unknownPermission -> unknownPermission.value()
    );
    assertThat(value).isEqualTo("EscrowCreate");
  }

  @Test
  void testValueMethodForGranularPermissionValue() {
    Permission permission = GranularPermissionValue.of(GranularPermission.TRUSTLINE_FREEZE);

    String value = permission.map(
      txPermission -> txPermission.value(),
      granularPermission -> granularPermission.value(),
      unknownPermission -> unknownPermission.value()
    );
    assertThat(value).isEqualTo("TrustlineFreeze");
  }

  @Test
  void testValueMethodForUnknownPermission() {
    Permission permission = UnknownPermission.of("SomeFutureTransactionType");

    String value = permission.map(
      txPermission -> txPermission.value(),
      granularPermission -> granularPermission.value(),
      unknownPermission -> unknownPermission.value()
    );
    assertThat(value).isEqualTo("SomeFutureTransactionType");
  }
}

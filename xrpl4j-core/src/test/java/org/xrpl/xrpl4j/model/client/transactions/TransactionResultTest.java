package org.xrpl.xrpl4j.model.client.transactions;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

class TransactionResultTest {

  @Test
  void testLedgerIndexSafeWithLedgerIndex() {
    TransactionResult<Payment> paymentResult = TransactionResult.<Payment>builder()
      .transaction(mock(Payment.class))
      .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
      .closeDate(UnsignedLong.valueOf(666212460))
      .metadata(mock(TransactionMetadata.class))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(paymentResult.ledgerIndex()).isNotEmpty().get().isEqualTo(paymentResult.ledgerIndexSafe());
  }

  @Test
  void testLedgerIndexSafeWithoutLedgerIndex() {
    TransactionResult<Payment> paymentResult = TransactionResult.<Payment>builder()
      .transaction(mock(Payment.class))
      .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
      .closeDate(UnsignedLong.valueOf(666212460))
      .metadata(mock(TransactionMetadata.class))
      .build();

    assertThat(paymentResult.ledgerIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      paymentResult::ledgerIndexSafe
    );
  }

  @Test
  void testCloseTimeHuman() {
    TransactionResult<Payment> paymentResult = TransactionResult.<Payment>builder()
      .transaction(mock(Payment.class))
      .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
      .closeDate(UnsignedLong.valueOf(666212460))
      .metadata(mock(TransactionMetadata.class))
      .build();

    assertThat(paymentResult.closeDateHuman()).hasValue(
      ZonedDateTime.of(LocalDateTime.of(2021, 2, 9, 19, 1, 0), ZoneId.of("UTC"))
    );
  }
}

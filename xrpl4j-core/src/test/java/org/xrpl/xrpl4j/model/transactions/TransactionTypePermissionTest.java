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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link TransactionTypePermission}.
 */
public class TransactionTypePermissionTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testBuilderWithTransactionType() {
    TransactionTypePermission permission = TransactionTypePermission.builder()
      .transactionType(TransactionType.PAYMENT)
      .build();

    assertThat(permission.transactionType()).isEqualTo(TransactionType.PAYMENT);
    assertThat(permission.value()).isEqualTo("Payment");
  }

  @Test
  void testOfFactoryMethod() {
    TransactionTypePermission permission = TransactionTypePermission.of(TransactionType.TRUST_SET);

    assertThat(permission.transactionType()).isEqualTo(TransactionType.TRUST_SET);
    assertThat(permission.value()).isEqualTo("TrustSet");
  }

  @ParameterizedTest
  @EnumSource(value = TransactionType.class, names = {"PAYMENT", "TRUST_SET", "OFFER_CREATE", "ESCROW_CREATE"})
  void testValueDerivedFromTransactionType(TransactionType transactionType) {
    TransactionTypePermission permission = TransactionTypePermission.of(transactionType);

    assertThat(permission.value()).isEqualTo(transactionType.value());
  }

  @Test
  void testEquality() {
    TransactionTypePermission permission1 = TransactionTypePermission.of(TransactionType.PAYMENT);
    TransactionTypePermission permission2 = TransactionTypePermission.of(TransactionType.PAYMENT);
    TransactionTypePermission permission3 = TransactionTypePermission.of(TransactionType.TRUST_SET);

    assertThat(permission1).isEqualTo(permission2);
    assertThat(permission1).isNotEqualTo(permission3);
    assertThat(permission1.hashCode()).isEqualTo(permission2.hashCode());
  }

  @Test
  void testToString() {
    TransactionTypePermission permission = TransactionTypePermission.of(TransactionType.PAYMENT);
    String toString = permission.toString();

    assertThat(toString).contains("TransactionTypePermission");
    assertThat(toString).contains("PAYMENT");
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException {
    TransactionTypePermission permission = TransactionTypePermission.of(TransactionType.PAYMENT);
    String json = objectMapper.writeValueAsString(permission);

    assertThat(json).isEqualTo("\"Payment\"");
  }

  @Test
  void testJsonDeserialization() throws JsonProcessingException {
    String json = "\"Payment\"";
    Permission permission = objectMapper.readValue(json, Permission.class);

    assertThat(permission).isInstanceOf(TransactionTypePermission.class);
    TransactionTypePermission txPermission = (TransactionTypePermission) permission;
    assertThat(txPermission.transactionType()).isEqualTo(TransactionType.PAYMENT);
    assertThat(txPermission.value()).isEqualTo("Payment");
  }

  @Test
  void testJsonRoundTrip() throws JsonProcessingException {
    TransactionTypePermission original = TransactionTypePermission.of(TransactionType.ESCROW_FINISH);
    String json = objectMapper.writeValueAsString(original);
    Permission deserialized = objectMapper.readValue(json, Permission.class);

    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void testImplementsPermissionInterface() {
    TransactionTypePermission permission = TransactionTypePermission.of(TransactionType.PAYMENT);

    assertThat(permission).isInstanceOf(Permission.class);
  }

  @Test
  void testWithUnknownTransactionType() {
    TransactionTypePermission permission = TransactionTypePermission.of(TransactionType.UNKNOWN);

    assertThat(permission.transactionType()).isEqualTo(TransactionType.UNKNOWN);
    assertThat(permission.value()).isEqualTo("Unknown");
  }
}


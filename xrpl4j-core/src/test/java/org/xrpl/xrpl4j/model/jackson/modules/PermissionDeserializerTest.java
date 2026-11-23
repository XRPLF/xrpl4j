package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.GranularPermissionValue;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TransactionTypePermission;

import java.io.IOException;

/**
 * Unit tests for {@link PermissionDeserializer}.
 */
class PermissionDeserializerTest {

  private PermissionDeserializer deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new PermissionDeserializer();
  }

  @Test
  void testDeserializeTransactionTypePermission() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.getValueAsString()).thenReturn("Payment");
    
    Permission permission = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    
    assertThat(permission).isInstanceOf(TransactionTypePermission.class);
    TransactionTypePermission txPermission = (TransactionTypePermission) permission;
    assertThat(txPermission.transactionType()).isEqualTo(TransactionType.PAYMENT);
    assertThat(txPermission.value()).isEqualTo("Payment");
  }

  @Test
  void testDeserializeGranularPermissionValue() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.getValueAsString()).thenReturn("TrustlineAuthorize");
    
    Permission permission = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    
    assertThat(permission).isInstanceOf(GranularPermissionValue.class);
    GranularPermissionValue granularPermission = (GranularPermissionValue) permission;
    assertThat(granularPermission.granularPermission()).isEqualTo(GranularPermission.TRUSTLINE_AUTHORIZE);
    assertThat(granularPermission.value()).isEqualTo("TrustlineAuthorize");
  }

  @Test
  void testDeserializeMultipleTransactionTypes() throws IOException {
    String[] transactionTypes = {"TrustSet", "OfferCreate", "EscrowFinish", "CheckCash"};

    for (String txType : transactionTypes) {
      JsonParser mockJsonParser = mock(JsonParser.class);
      when(mockJsonParser.getValueAsString()).thenReturn(txType);

      Permission permission = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));

      assertThat(permission).isInstanceOf(TransactionTypePermission.class);
      String value = permission.map(
        txPermission -> txPermission.value(),
        granularPermission -> granularPermission.value()
      );
      assertThat(value).isEqualTo(txType);
    }
  }

  @Test
  void testDeserializeMultipleGranularPermissions() throws IOException {
    String[] granularPermissions = {"PaymentMint", "TrustlineFreeze", "AccountDomainSet"};

    for (String granularPerm : granularPermissions) {
      JsonParser mockJsonParser = mock(JsonParser.class);
      when(mockJsonParser.getValueAsString()).thenReturn(granularPerm);

      Permission permission = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));

      assertThat(permission).isInstanceOf(GranularPermissionValue.class);
      String value = permission.map(
        txPermission -> txPermission.value(),
        granularPermission -> granularPermission.value()
      );
      assertThat(value).isEqualTo(granularPerm);
    }
  }

  @Test
  void testDeserializeInvalidPermissionThrowsException() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.getValueAsString()).thenReturn("InvalidPermission");
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
      deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class))
    );
    
    assertThat(exception.getMessage()).contains("Unknown permission value: InvalidPermission");
  }

  @Test
  void testDeserializeEmptyStringThrowsException() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.getValueAsString()).thenReturn("");
    
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
      deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class))
    );
    
    assertThat(exception.getMessage()).contains("Unknown permission value");
  }

  @Test
  void testDeserializeCaseInsensitiveTransactionType() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.getValueAsString()).thenReturn("payment");
    
    Permission permission = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    
    assertThat(permission).isInstanceOf(TransactionTypePermission.class);
    TransactionTypePermission txPermission = (TransactionTypePermission) permission;
    assertThat(txPermission.transactionType()).isEqualTo(TransactionType.PAYMENT);
  }

  @Test
  void testDeserializeCaseInsensitiveGranularPermission() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);
    when(mockJsonParser.getValueAsString()).thenReturn("trustlineauthorize");
    
    Permission permission = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    
    assertThat(permission).isInstanceOf(GranularPermissionValue.class);
    GranularPermissionValue granularPermission = (GranularPermissionValue) permission;
    assertThat(granularPermission.granularPermission()).isEqualTo(GranularPermission.TRUSTLINE_AUTHORIZE);
  }
}

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;
import org.xrpl.xrpl4j.model.transactions.GranularPermissionValue;
import org.xrpl.xrpl4j.model.transactions.Permission;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TransactionTypePermission;

import java.io.IOException;

/**
 * Unit tests for {@link PermissionSerializer}.
 */
class PermissionSerializerTest {

  private PermissionSerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new PermissionSerializer();
  }

  @Test
  void testSerializeTransactionTypePermission() throws IOException {
    Permission permission = TransactionTypePermission.of(TransactionType.PAYMENT);
    JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
    
    serializer.serialize(permission, mockJsonGenerator, mock(SerializerProvider.class));
    
    verify(mockJsonGenerator).writeString("Payment");
  }

  @Test
  void testSerializeGranularPermissionValue() throws IOException {
    Permission permission = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);
    JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
    
    serializer.serialize(permission, mockJsonGenerator, mock(SerializerProvider.class));
    
    verify(mockJsonGenerator).writeString("TrustlineAuthorize");
  }

  @Test
  void testSerializeMultipleTransactionTypes() throws IOException {
    TransactionType[] transactionTypes = {
      TransactionType.TRUST_SET,
      TransactionType.OFFER_CREATE,
      TransactionType.ESCROW_FINISH,
      TransactionType.CHECK_CASH
    };
    
    for (TransactionType txType : transactionTypes) {
      Permission permission = TransactionTypePermission.of(txType);
      JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
      
      serializer.serialize(permission, mockJsonGenerator, mock(SerializerProvider.class));
      
      verify(mockJsonGenerator).writeString(txType.value());
    }
  }

  @Test
  void testSerializeMultipleGranularPermissions() throws IOException {
    GranularPermission[] granularPermissions = {
      GranularPermission.PAYMENT_MINT,
      GranularPermission.TRUSTLINE_FREEZE,
      GranularPermission.ACCOUNT_DOMAIN_SET
    };
    
    for (GranularPermission granularPerm : granularPermissions) {
      Permission permission = GranularPermissionValue.of(granularPerm);
      JsonGenerator mockJsonGenerator = mock(JsonGenerator.class);
      
      serializer.serialize(permission, mockJsonGenerator, mock(SerializerProvider.class));
      
      verify(mockJsonGenerator).writeString(granularPerm.value());
    }
  }

  @Test
  void testSerializeUsesValueMethod() throws IOException {
    // Test that the serializer uses the value() method from the Permission interface
    Permission txPermission = TransactionTypePermission.of(TransactionType.ESCROW_CREATE);
    Permission granularPermission = GranularPermissionValue.of(GranularPermission.PAYMENT_MINT);
    
    JsonGenerator mockJsonGenerator1 = mock(JsonGenerator.class);
    JsonGenerator mockJsonGenerator2 = mock(JsonGenerator.class);
    
    serializer.serialize(txPermission, mockJsonGenerator1, mock(SerializerProvider.class));
    serializer.serialize(granularPermission, mockJsonGenerator2, mock(SerializerProvider.class));
    
    verify(mockJsonGenerator1).writeString("EscrowCreate");
    verify(mockJsonGenerator2).writeString("PaymentMint");
  }
}

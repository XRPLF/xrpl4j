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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.util.Optional;

/**
 * Unit tests for {@link GranularPermission}.
 */
public class GranularPermissionTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @ParameterizedTest
  @EnumSource
  void testForValueWithValidValues(GranularPermission permission) {
    String value = permission.value();
    Optional<GranularPermission> result = GranularPermission.forValue(value);
    
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(permission);
  }

  @ParameterizedTest
  @EnumSource
  void testForValueIsCaseInsensitive(GranularPermission permission) {
    String lowerCase = permission.value().toLowerCase();
    String upperCase = permission.value().toUpperCase();
    
    assertThat(GranularPermission.forValue(lowerCase)).isPresent().hasValue(permission);
    assertThat(GranularPermission.forValue(upperCase)).isPresent().hasValue(permission);
  }

  @Test
  void testForValueWithInvalidValue() {
    assertThat(GranularPermission.forValue("InvalidPermission")).isEmpty();
    assertThat(GranularPermission.forValue("Payment")).isEmpty();
    assertThat(GranularPermission.forValue("")).isEmpty();
  }

  @Test
  void testForValueWithNullThrowsException() {
    assertThrows(NullPointerException.class, () -> GranularPermission.forValue(null));
  }

  @ParameterizedTest
  @EnumSource
  void testNumericValueIsGreaterThan65536(GranularPermission permission) {
    // All granular permissions must have numeric values > 65536 (UINT16_MAX)
    // to avoid conflicts with transaction type permission values
    assertThat(permission.numericValue()).isGreaterThan(65536);
  }

  @ParameterizedTest
  @EnumSource
  void testNumericValuesAreUnique(GranularPermission permission) {
    // Ensure each permission has a unique numeric value
    long count = java.util.Arrays.stream(GranularPermission.values())
      .filter(p -> p.numericValue() == permission.numericValue())
      .count();
    
    assertThat(count).isEqualTo(1);
  }

  @Test
  void testSpecificPermissionValues() {
    assertThat(GranularPermission.TRUSTLINE_AUTHORIZE.value()).isEqualTo("TrustlineAuthorize");
    assertThat(GranularPermission.TRUSTLINE_AUTHORIZE.numericValue()).isEqualTo(65537);
    
    assertThat(GranularPermission.TRUSTLINE_FREEZE.value()).isEqualTo("TrustlineFreeze");
    assertThat(GranularPermission.TRUSTLINE_FREEZE.numericValue()).isEqualTo(65538);
    
    assertThat(GranularPermission.TRUSTLINE_UNFREEZE.value()).isEqualTo("TrustlineUnfreeze");
    assertThat(GranularPermission.TRUSTLINE_UNFREEZE.numericValue()).isEqualTo(65539);
    
    assertThat(GranularPermission.ACCOUNT_DOMAIN_SET.value()).isEqualTo("AccountDomainSet");
    assertThat(GranularPermission.ACCOUNT_DOMAIN_SET.numericValue()).isEqualTo(65540);
    
    assertThat(GranularPermission.ACCOUNT_EMAIL_HASH_SET.value()).isEqualTo("AccountEmailHashSet");
    assertThat(GranularPermission.ACCOUNT_EMAIL_HASH_SET.numericValue()).isEqualTo(65541);
    
    assertThat(GranularPermission.ACCOUNT_MESSAGE_KEY_SET.value()).isEqualTo("AccountMessageKeySet");
    assertThat(GranularPermission.ACCOUNT_MESSAGE_KEY_SET.numericValue()).isEqualTo(65542);
    
    assertThat(GranularPermission.ACCOUNT_TRANSFER_RATE_SET.value()).isEqualTo("AccountTransferRateSet");
    assertThat(GranularPermission.ACCOUNT_TRANSFER_RATE_SET.numericValue()).isEqualTo(65543);
    
    assertThat(GranularPermission.ACCOUNT_TICK_SIZE_SET.value()).isEqualTo("AccountTickSizeSet");
    assertThat(GranularPermission.ACCOUNT_TICK_SIZE_SET.numericValue()).isEqualTo(65544);
    
    assertThat(GranularPermission.PAYMENT_MINT.value()).isEqualTo("PaymentMint");
    assertThat(GranularPermission.PAYMENT_MINT.numericValue()).isEqualTo(65545);
    
    assertThat(GranularPermission.PAYMENT_BURN.value()).isEqualTo("PaymentBurn");
    assertThat(GranularPermission.PAYMENT_BURN.numericValue()).isEqualTo(65546);
    
    assertThat(GranularPermission.MPTOKEN_ISSUANCE_LOCK.value()).isEqualTo("MPTokenIssuanceLock");
    assertThat(GranularPermission.MPTOKEN_ISSUANCE_LOCK.numericValue()).isEqualTo(65547);
    
    assertThat(GranularPermission.MPTOKEN_ISSUANCE_UNLOCK.value()).isEqualTo("MPTokenIssuanceUnlock");
    assertThat(GranularPermission.MPTOKEN_ISSUANCE_UNLOCK.numericValue()).isEqualTo(65548);
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    GranularPermission permission = GranularPermission.TRUSTLINE_AUTHORIZE;
    String json = objectMapper.writeValueAsString(permission);
    
    assertThat(json).isEqualTo("\"TrustlineAuthorize\"");
  }

  @Test
  void testJsonDeserialization() throws JsonProcessingException {
    String json = "\"TrustlineFreeze\"";
    GranularPermission permission = objectMapper.readValue(json, GranularPermission.class);
    
    assertThat(permission).isEqualTo(GranularPermission.TRUSTLINE_FREEZE);
  }
}


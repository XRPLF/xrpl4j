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
 * Unit tests for {@link GranularPermissionValue}.
 */
public class GranularPermissionValueTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testBuilderWithGranularPermission() {
    GranularPermissionValue permission = GranularPermissionValue.builder()
      .granularPermission(GranularPermission.TRUSTLINE_AUTHORIZE)
      .build();

    assertThat(permission.granularPermission()).isEqualTo(GranularPermission.TRUSTLINE_AUTHORIZE);
    assertThat(permission.value()).isEqualTo("TrustlineAuthorize");
  }

  @Test
  void testOfFactoryMethod() {
    GranularPermissionValue permission = GranularPermissionValue.of(GranularPermission.PAYMENT_MINT);

    assertThat(permission.granularPermission()).isEqualTo(GranularPermission.PAYMENT_MINT);
    assertThat(permission.value()).isEqualTo("PaymentMint");
  }

  @ParameterizedTest
  @EnumSource(GranularPermission.class)
  void testValueDerivedFromGranularPermission(GranularPermission granularPermission) {
    GranularPermissionValue permission = GranularPermissionValue.of(granularPermission);

    assertThat(permission.value()).isEqualTo(granularPermission.value());
  }

  @Test
  void testEquality() {
    GranularPermissionValue permission1 = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);
    GranularPermissionValue permission2 = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);
    GranularPermissionValue permission3 = GranularPermissionValue.of(GranularPermission.PAYMENT_MINT);

    assertThat(permission1).isEqualTo(permission2);
    assertThat(permission1).isNotEqualTo(permission3);
    assertThat(permission1.hashCode()).isEqualTo(permission2.hashCode());
  }

  @Test
  void testToString() {
    GranularPermissionValue permission = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);
    String toString = permission.toString();

    assertThat(toString).contains("GranularPermissionValue");
    assertThat(toString).contains("TRUSTLINE_AUTHORIZE");
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException {
    GranularPermissionValue permission = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);
    String json = objectMapper.writeValueAsString(permission);

    assertThat(json).isEqualTo("\"TrustlineAuthorize\"");
  }

  @Test
  void testJsonDeserialization() throws JsonProcessingException {
    String json = "\"TrustlineAuthorize\"";
    Permission permission = objectMapper.readValue(json, Permission.class);

    assertThat(permission).isInstanceOf(GranularPermissionValue.class);
    GranularPermissionValue granularPermission = (GranularPermissionValue) permission;
    assertThat(granularPermission.granularPermission()).isEqualTo(GranularPermission.TRUSTLINE_AUTHORIZE);
    assertThat(granularPermission.value()).isEqualTo("TrustlineAuthorize");
  }

  @Test
  void testJsonRoundTrip() throws JsonProcessingException {
    GranularPermissionValue original = GranularPermissionValue.of(GranularPermission.PAYMENT_MINT);
    String json = objectMapper.writeValueAsString(original);
    Permission deserialized = objectMapper.readValue(json, Permission.class);

    assertThat(deserialized).isEqualTo(original);
  }

  @Test
  void testImplementsPermissionInterface() {
    GranularPermissionValue permission = GranularPermissionValue.of(GranularPermission.TRUSTLINE_AUTHORIZE);

    assertThat(permission).isInstanceOf(Permission.class);
  }
}


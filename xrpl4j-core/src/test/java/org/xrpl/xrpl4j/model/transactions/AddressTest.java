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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.api.AssertionsForClassTypes;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link Address}.
 */
public class AddressTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();
  private final Address ADDRESS = Seed.ed25519Seed().deriveKeyPair().publicKey().deriveAddress();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> Address.of(null));
  }

  @Test
  void testEquality() {
    AssertionsForClassTypes.assertThat(ADDRESS).isEqualTo(ADDRESS);
    AssertionsForClassTypes.assertThat(ADDRESS).isNotEqualTo(new Object());
    AssertionsForClassTypes.assertThat(ADDRESS.equals(null)).isFalse();
  }

  @Test
  void testToString() {
    Address address = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    assertThat(address.toString()).isEqualTo("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
  }

  @Test
  void testHashCode() {
    Address address1 = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    Address address2 = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
  }

  @Test
  public void addressWithBadPrefix() {
    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("c9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
      "Invalid Address: Bad Prefix"
    );
  }

  @Test
  public void addressOfIncorrectLength() {
    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("r9cZA1mLK5R"),
      "Classic Addresses must be (25,35) characters long inclusive."
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> Address.of("rAJYB9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"),
      "Classic Addresses must be (25,35) characters long inclusive."
    );
  }

  @Test
  void testJsonSerialization() throws JsonProcessingException, JSONException {
    Address address = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    AddressWrapper wrapper = ImmutableAddressWrapper.builder()
      .address(address)
      .build();

    String json = "{\"address\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(AddressWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    AddressWrapper deserialized = objectMapper.readValue(serialized, AddressWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAddressWrapper.class)
  @JsonDeserialize(as = ImmutableAddressWrapper.class)
  interface AddressWrapper {
    Address address();
  }
}

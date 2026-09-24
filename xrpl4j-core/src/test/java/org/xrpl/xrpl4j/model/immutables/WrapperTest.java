package org.xrpl.xrpl4j.model.immutables;

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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NetworkId;

/**
 * Unit tests for {@link Wrapper}.
 */
class WrapperTest {

  private static final Hash256 HASH_256_MAX = Hash256.of(
    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
  private static final Hash256 HASH_256_ZERO = Hash256.of(
    "0000000000000000000000000000000000000000000000000000000000000000");

  private static final NetworkId NETWORK_ID = NetworkId.of(1000);

  @Test
  void testValue() {
    assertThat(HASH_256_MAX.value()).isEqualTo("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
    assertThat(NETWORK_ID.value()).isEqualTo(UnsignedInteger.valueOf(1000));
  }

  @Test
  void testCompareTo() {
    // Test with Hash256 (String wrapper)
    Hash256 hash1 = HASH_256_ZERO;
    Hash256 hash2 = HASH_256_MAX;
    Hash256 hash3 = HASH_256_ZERO;

    // hash1 < hash2
    assertThat(hash1.compareTo(hash2)).isLessThan(0);
    // hash2 > hash1
    assertThat(hash2.compareTo(hash1)).isGreaterThan(0);
    // hash1 == hash3
    assertThat(hash1.compareTo(hash3)).isEqualTo(0);

    // Test with NetworkId (UnsignedInteger wrapper)
    NetworkId networkId1 = NetworkId.of(UnsignedInteger.valueOf(100));
    NetworkId networkId2 = NetworkId.of(UnsignedInteger.valueOf(200));
    NetworkId networkId3 = NetworkId.of(UnsignedInteger.valueOf(100));

    // networkId1 < networkId2
    assertThat(networkId1.compareTo(networkId2)).isLessThan(0);
    // networkId2 > networkId1
    assertThat(networkId2.compareTo(networkId1)).isGreaterThan(0);
    // networkId1 == networkId3
    assertThat(networkId1.compareTo(networkId3)).isEqualTo(0);
  }

  @Test
  void testEqualsWithNull() {
    assertThat(HASH_256_MAX.equals(null)).isFalse();
  }

  @Test
  void testEqualsWithSameObject() {
    assertThat(HASH_256_MAX.equals(HASH_256_MAX)).isTrue();
  }

  @Test
  void testEqualsWithDifferentType() {
    assertThat(HASH_256_MAX.equals("not a wrapper")).isFalse();
    assertThat(HASH_256_MAX.equals(123)).isFalse();
  }

  @Test
  void testEqualsWithDifferentWrapperType() {
    NetworkId networkId = NetworkId.of(UnsignedInteger.valueOf(1000));

    // Different wrapper types should not be equal even if values might be related
    assertThat(HASH_256_MAX.equals(networkId)).isFalse();
  }

  @Test
  void testEqualsWithDifferentValues() {
    Hash256 hash1 = HASH_256_ZERO;
    Hash256 hash2 = HASH_256_MAX;
    assertThat(hash1.equals(hash2)).isFalse();

    NetworkId networkId1 = NetworkId.of(UnsignedInteger.valueOf(100));
    NetworkId networkId2 = NetworkId.of(UnsignedInteger.valueOf(200));
    assertThat(networkId1.equals(networkId2)).isFalse();
  }

  @Test
  void testEqualsWithSameValues() {
    Hash256 hash1 = HASH_256_MAX;
    Hash256 hash2 = HASH_256_MAX;
    assertThat(hash1.equals(hash2)).isTrue();

    NetworkId networkId1 = NetworkId.of(UnsignedInteger.valueOf(1000));
    NetworkId networkId2 = NetworkId.of(UnsignedInteger.valueOf(1000));
    assertThat(networkId1.equals(networkId2)).isTrue();
  }

  @Test
  void testHashCode() {
    // Equal objects should have equal hashCodes
    Hash256 hash1 = HASH_256_MAX;
    Hash256 hash2 = HASH_256_MAX;
    assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());

    NetworkId networkId1 = NetworkId.of(UnsignedInteger.valueOf(1000));
    NetworkId networkId2 = NetworkId.of(UnsignedInteger.valueOf(1000));
    assertThat(networkId1.hashCode()).isEqualTo(networkId2.hashCode());

    // Different objects should (generally) have different hashCodes
    Hash256 hash3 = HASH_256_ZERO;
    assertThat(hash1.hashCode()).isNotEqualTo(hash3.hashCode());

    NetworkId networkId3 = NetworkId.of(UnsignedInteger.valueOf(2000));
    assertThat(networkId1.hashCode()).isNotEqualTo(networkId3.hashCode());
  }

  @Test
  void testToString() {
    // Hash256 overrides toString() to return just the value
    assertThat(HASH_256_MAX.toString()).isEqualTo("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

    // NetworkId overrides toString() to return the value's toString
    assertThat(NETWORK_ID.toString()).isEqualTo("1000");
  }
}

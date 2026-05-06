package org.xrpl.xrpl4j.model.transactions;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link WithdrawalPolicy}.
 */
public class WithdrawalPolicyTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testGetValue() {
    assertThat(WithdrawalPolicy.FIRST_COME_FIRST_SERVE.getValue()).isEqualTo(1);
  }

  @Test
  void testForValue() {
    assertThat(WithdrawalPolicy.forValue(1)).isEqualTo(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);
  }

  @Test
  void testForValueWithInvalidValue() {
    assertThatThrownBy(() -> WithdrawalPolicy.forValue(999))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("No matching WithdrawalPolicy for value 999");
  }

  @Test
  void testJsonSerializationInWrapper() throws JsonProcessingException, JSONException {
    // Test WithdrawalPolicy serialization within a wrapper object
    WithdrawalPolicyWrapper wrapper = WithdrawalPolicyWrapper.of(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);
    String json = "{\"policy\":1}";
    
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    
    WithdrawalPolicyWrapper deserialized = objectMapper.readValue(serialized, WithdrawalPolicyWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Test
  void testEquality() {
    WithdrawalPolicy policy1 = WithdrawalPolicy.FIRST_COME_FIRST_SERVE;
    WithdrawalPolicy policy2 = WithdrawalPolicy.forValue(1);

    assertThat(policy1).isEqualTo(policy2);
    assertThat(policy1.hashCode()).isEqualTo(policy2.hashCode());
  }

  /**
   * Test wrapper class for JSON serialization testing.
   */
  @Value.Immutable
  @JsonSerialize(as = ImmutableWithdrawalPolicyWrapper.class)
  @JsonDeserialize(as = ImmutableWithdrawalPolicyWrapper.class)
  interface WithdrawalPolicyWrapper {
    static WithdrawalPolicyWrapper of(WithdrawalPolicy policy) {
      return ImmutableWithdrawalPolicyWrapper.builder().policy(policy).build();
    }

    WithdrawalPolicy policy();
  }
}


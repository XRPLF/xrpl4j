package org.xrpl.xrpl4j.model.flags;

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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.params.provider.Arguments;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AbstractFlagsTest extends AbstractJsonTest {

  protected static Stream<Arguments> getBooleanCombinations(int flagCount) {
    // Every combination of 4 booleans
    List<Object[]> params = new ArrayList<>();
    for (int i = 0; i < Math.pow(2, flagCount); i++) {
      String bin = Integer.toBinaryString(i);
      while (bin.length() < flagCount) {
        bin = "0" + bin;
      }

      char[] chars = bin.toCharArray();
      Boolean[] booleans = new Boolean[flagCount];
      for (int j = 0; j < chars.length; j++) {
        booleans[j] = chars[j] == '0';
      }

      params.add(booleans);
    }

    return params.stream().map(Arguments::of);
  }

  protected void assertCanSerializeAndDeserialize(FlagsWrapper object, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(object);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    FlagsWrapper deserialized = objectMapper.readValue(serialized, FlagsWrapper.class);
    assertThat(deserialized).isEqualTo(object);
  }

  protected void assertCanSerializeAndDeserialize(TransactionFlagsWrapper object, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(object);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    TransactionFlagsWrapper deserialized = objectMapper.readValue(serialized, TransactionFlagsWrapper.class);
    assertThat(deserialized).isEqualTo(object);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableFlagsWrapper.class)
  @JsonDeserialize(as = ImmutableFlagsWrapper.class)
  interface FlagsWrapper {

    static FlagsWrapper of(Flags flags) {
      return ImmutableFlagsWrapper.builder().flags(flags).build();
    }

    Flags flags();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableTransactionFlagsWrapper.class)
  @JsonDeserialize(as = ImmutableTransactionFlagsWrapper.class)
  interface TransactionFlagsWrapper {

    static TransactionFlagsWrapper of(TransactionFlags flags) {
      return ImmutableTransactionFlagsWrapper.builder().flags(flags).build();
    }

    @Value.Default
    default TransactionFlags flags() {
      return TransactionFlags.EMPTY;
    }
  }
}

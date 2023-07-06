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
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class SignerListObjectFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(1);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(boolean lsfOneOwnerCount) {
    long expectedFlags = lsfOneOwnerCount ? SignerListFlags.ONE_OWNER_COUNT.getValue() : 0L;
    SignerListFlags flags = SignerListFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfOneOwnerCount()).isEqualTo(lsfOneOwnerCount);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testJson(
    boolean lsfOneOwnerCount
  ) throws JSONException, JsonProcessingException {
    long expectedFlags = lsfOneOwnerCount ? SignerListFlags.ONE_OWNER_COUNT.getValue() : 0L;
    SignerListFlags flags = SignerListFlags.of(expectedFlags);

    FlagsWrapper flagsWrapper = FlagsWrapper.of(flags);

    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(flagsWrapper, json);
  }
}

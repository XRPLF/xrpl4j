package org.xrpl.xrpl4j.model.client.common;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class LedgerIndexShortcutTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testConstants() {
    final LedgerIndexShortcut validated = LedgerIndexShortcut.VALIDATED;
    assertThat(validated.getValue()).isEqualTo("validated");
    assertThat(validated.toString()).isEqualTo("validated");
    assertThat(validated).isEqualTo(validated);
    assertThat(validated).isEqualTo(LedgerIndexShortcut.VALIDATED);
    assertThat(validated).isNotEqualTo(LedgerIndexShortcut.CURRENT);
    assertThat(validated).isNotEqualTo("foo");

    final LedgerIndexShortcut current = LedgerIndexShortcut.CURRENT;
    assertThat(current.getValue()).isEqualTo("current");
    assertThat(current.toString()).isEqualTo("current");
    assertThat(current).isEqualTo(current);
    assertThat(current).isEqualTo(LedgerIndexShortcut.CURRENT);
    assertThat(current).isNotEqualTo(LedgerIndexShortcut.VALIDATED);
    assertThat(current).isNotEqualTo("foo");

    final LedgerIndexShortcut closed = LedgerIndexShortcut.CLOSED;
    assertThat(closed.getValue()).isEqualTo("closed");
    assertThat(closed.toString()).isEqualTo("closed");
    assertThat(closed).isEqualTo(closed);
    assertThat(closed).isEqualTo(LedgerIndexShortcut.CLOSED);
    assertThat(closed).isNotEqualTo(LedgerIndexShortcut.CURRENT);
    assertThat(closed).isNotEqualTo("foo");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    LedgerIndexShortcutWrapper validated = LedgerIndexShortcutWrapper.of(LedgerIndexShortcut.VALIDATED);
    LedgerIndexShortcutWrapper current = LedgerIndexShortcutWrapper.of(LedgerIndexShortcut.CURRENT);
    LedgerIndexShortcutWrapper closed = LedgerIndexShortcutWrapper.of(LedgerIndexShortcut.CLOSED);

    assertSerializesAndDeserializes(validated, "{\"ledgerIndexShortcut\": \"validated\"}");
    assertSerializesAndDeserializes(current, "{\"ledgerIndexShortcut\": \"current\"}");
    assertSerializesAndDeserializes(closed, "{\"ledgerIndexShortcut\": \"closed\"}");
  }

  private void assertSerializesAndDeserializes(
    LedgerIndexShortcutWrapper wrapper,
    String expectedJson
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(expectedJson, serialized, JSONCompareMode.STRICT);

    LedgerIndexShortcutWrapper deserialized = objectMapper.readValue(serialized, LedgerIndexShortcutWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerIndexShortcutWrapper.class)
  @JsonDeserialize(as = ImmutableLedgerIndexShortcutWrapper.class)
  interface LedgerIndexShortcutWrapper {

    static LedgerIndexShortcutWrapper of(LedgerIndexShortcut ledgerIndexShortcut) {
      return ImmutableLedgerIndexShortcutWrapper.builder().ledgerIndexShortcut(ledgerIndexShortcut).build();
    }

    LedgerIndexShortcut ledgerIndexShortcut();

  }
}

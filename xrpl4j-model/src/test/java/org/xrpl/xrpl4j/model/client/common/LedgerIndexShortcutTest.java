package org.xrpl.xrpl4j.model.client.common;

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
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class LedgerIndexShortcutTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

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

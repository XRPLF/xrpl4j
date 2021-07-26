package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class OldLedgerIndexDeserializerTest extends AbstractLedgerIndexTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void deserializeCharacterLedgerIndex() throws JsonProcessingException {
    final LedgerIndex current = LedgerIndex.CURRENT;
    final LedgerIndex validated = LedgerIndex.VALIDATED;
    final LedgerIndex closed = LedgerIndex.CLOSED;

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      LedgerIndex.of("foo");
    });

    final LedgerIndex currentDeserialized = objectMapper.readValue("\"" + current.value() + "\"", LedgerIndex.class);
    assertThat(currentDeserialized).isEqualTo(current);

    final LedgerIndex validatedDeserialized = objectMapper
      .readValue("\"" + validated.value() + "\"", LedgerIndex.class);
    assertThat(validatedDeserialized).isEqualTo(validated);

    final LedgerIndex closedDeserialized = objectMapper.readValue("\"" + closed.value() + "\"", LedgerIndex.class);
    assertThat(closedDeserialized).isEqualTo(closed);
  }

  @Test
  public void deserializeNumericalLedgerIndex() throws JsonProcessingException {
    final LedgerIndex ledgerIndex = LedgerIndex.of("1");

    final LedgerIndex deserialized = objectMapper.readValue("\"1\"", LedgerIndex.class);
    assertThat(deserialized).isEqualTo(ledgerIndex);

    LedgerIndexContainer container = LedgerIndexContainer.of(ledgerIndex);
    final LedgerIndexContainer deserializedContainer = objectMapper
      .readValue("{\"ledgerIndex\": 1}", LedgerIndexContainer.class);
    assertThat(deserializedContainer).isEqualTo(container);
  }
}

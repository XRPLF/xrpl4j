package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class LedgerIndexSerializerTest extends AbstractLedgerIndexTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  public void serializeCharacterLedgerIndex() throws JsonProcessingException {
    final LedgerIndex current = LedgerIndex.CURRENT;
    final LedgerIndex validated = LedgerIndex.VALIDATED;
    final LedgerIndex closed = LedgerIndex.CLOSED;

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      LedgerIndex.of("foo");
    });

    final String currentSerialized = objectMapper.writeValueAsString(current);
    assertThat(currentSerialized).isEqualTo("\"" + current.value() + "\"");

    final String validatedSerialized = objectMapper.writeValueAsString(validated);
    assertThat(validatedSerialized).isEqualTo("\"" + validated.value() + "\"");

    final String closedSerialized = objectMapper.writeValueAsString(closed);
    assertThat(closedSerialized).isEqualTo("\"" + closed.value() + "\"");
  }

  @Test
  void serializeNumericalLedgerIndex() throws JsonProcessingException {
    final LedgerIndexContainer fromUnsignedLong = LedgerIndexContainer.of(LedgerIndex.of(UnsignedLong.ONE));
    final LedgerIndexContainer fromString = LedgerIndexContainer.of(LedgerIndex.of("1"));

    final String serializedFromUnsignedLong = objectMapper.writeValueAsString(fromUnsignedLong);
    assertThat(serializedFromUnsignedLong).isEqualTo("{\"ledgerIndex\":1}");

    final String serializedFromString = objectMapper.writeValueAsString(fromString);
    assertThat(serializedFromString).isEqualTo("{\"ledgerIndex\":1}");
  }
}

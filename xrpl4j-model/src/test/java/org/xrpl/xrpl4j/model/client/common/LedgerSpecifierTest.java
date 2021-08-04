package org.xrpl.xrpl4j.model.client.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class LedgerSpecifierTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();
  public static final Hash256 LEDGER_HASH = Hash256.of(Strings.repeat("0", 64));

  @Test
  void specifyOneSpecifier() {
    assertDoesNotThrow(
      () -> LedgerSpecifier.ledgerHash(LEDGER_HASH)
    );

    assertDoesNotThrow(
      () -> LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
    );

    assertDoesNotThrow(
      () -> LedgerSpecifier.VALIDATED
    );
  }

  @Test
  void specifyMoreThanOneThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> ImmutableLedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> ImmutableLedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> ImmutableLedgerSpecifier.builder()
        .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );

    assertThrows(
      IllegalArgumentException.class,
      () -> ImmutableLedgerSpecifier.builder()
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
        .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
        .build()
    );
  }

  @Test
  void specifyNoneThrows() {
    assertThrows(
      IllegalArgumentException.class,
      () -> ImmutableLedgerSpecifier.builder().build()
    );
  }

  @Test
  void specifyUsingUtilityConstructors() {
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerHash(LEDGER_HASH));
    assertDoesNotThrow(() -> LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE)));
    assertDoesNotThrow(() -> LedgerSpecifier.CURRENT);
  }

  @Test
  void handlesAllCorrectly() {
    List<LedgerSpecifier> ledgerSpecifiers = Lists.newArrayList(
      LedgerSpecifier.ledgerHash(LEDGER_HASH),
      LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE)),
      LedgerSpecifier.VALIDATED
    );

    ledgerSpecifiers.forEach(this::assertHandlesCorrectly);
  }

  @Test
  void handleThrowsWithNullHandlers() {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(LEDGER_HASH);
    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        null,
        $ -> {
        },
        $ -> {
        }
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        $ -> {
        },
        null,
        $ -> {
        }
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.handle(
        $ -> {
        },
        $ -> {
        },
        null
      )
    );
  }

  @Test
  void mapsAllCorrectly() {
    List<LedgerSpecifier> ledgerSpecifiers = Lists.newArrayList(
      LedgerSpecifier.ledgerHash(LEDGER_HASH),
      LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE)),
      LedgerSpecifier.VALIDATED
    );

    ledgerSpecifiers.forEach(
      specifier -> {
        final String mapped = specifier.map(
          ledgerHash -> "ledgerHash",
          ledgerIndex -> "ledgerIndex",
          ledgerIndexShortcut -> "ledgerIndexShortcut"
        );

        assertThat(mapped).isNotNull();

        if (specifier.ledgerHash().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerHash");
        } else if (specifier.ledgerIndex().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerIndex");
        } else if (specifier.ledgerIndexShortcut().isPresent()) {
          assertThat(mapped).isEqualTo("ledgerIndexShortcut");
        }
      }
    );
  }

  @Test
  void mapThrowsWithNullMappers() {
    final LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(LEDGER_HASH);
    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        null,
        $ -> "",
        $ -> ""
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        $ -> "",
        null,
        $ -> ""
      )
    );

    assertThrows(
      NullPointerException.class,
      () -> ledgerSpecifier.map(
        $ -> "",
        $ -> "",
        null
      )
    );
  }

  @Test
  void testLedgerHashJson() throws JsonProcessingException, JSONException {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerHash(LEDGER_HASH);
    LedgerSpecifierWrapper wrapper = LedgerSpecifierWrapper.of(ledgerSpecifier);
    final String serialized = objectMapper.writeValueAsString(wrapper);
    String json = "{\"ledger_hash\": \"" + LEDGER_HASH + "\"}";
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);

    final LedgerSpecifierWrapper deserialized = objectMapper.readValue(json, LedgerSpecifierWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Test
  void testLedgerIndexJson() throws JsonProcessingException, JSONException {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE));
    LedgerSpecifierWrapper wrapper = LedgerSpecifierWrapper.of(ledgerSpecifier);
    final String serialized = objectMapper.writeValueAsString(wrapper);
    String json = "{\"ledger_index\": 1}";
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    final LedgerSpecifierWrapper deserialized = objectMapper.readValue(json, LedgerSpecifierWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Test
  void testLedgerIndexShortcutJson() throws JsonProcessingException, JSONException {
    LedgerSpecifier ledgerSpecifier = LedgerSpecifier.VALIDATED;
    LedgerSpecifierWrapper wrapper = LedgerSpecifierWrapper.of(ledgerSpecifier);
    final String serialized = objectMapper.writeValueAsString(wrapper);
    String json = "{\"ledger_index\": \"validated\"}";
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    final LedgerSpecifierWrapper deserialized = objectMapper.readValue(json, LedgerSpecifierWrapper.class);
    assertThat(deserialized).isEqualTo(wrapper);
  }

  private void assertHandlesCorrectly(LedgerSpecifier ledgerSpecifier) {
    AtomicBoolean ledgerHashHandled = new AtomicBoolean(false);
    AtomicBoolean ledgerIndexHandled = new AtomicBoolean(false);
    AtomicBoolean ledgerIndexShortcutHandled = new AtomicBoolean(false);

    ledgerSpecifier.handle(
      ledgerHash -> ledgerHashHandled.set(true),
      ledgerIndex -> ledgerIndexHandled.set(true),
      ledgerIndexShortcut -> ledgerIndexShortcutHandled.set(true)
    );

    if (ledgerSpecifier.ledgerHash().isPresent()) {
      assertThat(ledgerHashHandled).isTrue();
    } else {
      assertThat(ledgerHashHandled).isFalse();
    }

    if (ledgerSpecifier.ledgerIndex().isPresent()) {
      assertThat(ledgerIndexHandled).isTrue();
    } else {
      assertThat(ledgerIndexHandled).isFalse();
    }

    if (ledgerSpecifier.ledgerIndexShortcut().isPresent()) {
      assertThat(ledgerIndexShortcutHandled).isTrue();
    } else {
      assertThat(ledgerIndexShortcutHandled).isFalse();
    }
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerSpecifierWrapper.class)
  @JsonDeserialize(as = ImmutableLedgerSpecifierWrapper.class)
  interface LedgerSpecifierWrapper {

    static LedgerSpecifierWrapper of(LedgerSpecifier ledgerSpecifier) {
      return ImmutableLedgerSpecifierWrapper.builder().ledgerSpecifier(ledgerSpecifier).build();
    }

    @JsonUnwrapped
    LedgerSpecifier ledgerSpecifier();
  }
}

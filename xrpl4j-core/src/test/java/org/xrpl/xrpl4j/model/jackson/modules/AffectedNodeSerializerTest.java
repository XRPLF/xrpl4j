package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableCreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableDeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableMetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableModifiedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Unit tests for {@link AffectedNodeSerializer}, verifying that each {@link AffectedNode} subtype is written with
 * the {@code CreatedNode}/{@code ModifiedNode}/{@code DeletedNode} wrapper that xrpld emits and that
 * {@link AffectedNodeDeserializer} expects.
 */
class AffectedNodeSerializerTest extends AbstractJsonTest {

  private static final Hash256 LEDGER_INDEX = Hash256.of(Strings.repeat("A", 64));
  private static final Hash256 PREVIOUS_TXN_ID = Hash256.of(Strings.repeat("B", 64));

  private static final MetaAccountRootObject ACCOUNT_ROOT = ImmutableMetaAccountRootObject.builder()
    .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
    .balance(XrpCurrencyAmount.of(UnsignedLong.valueOf(1000)))
    .sequence(UnsignedInteger.ONE)
    .build();

  private static final String ACCOUNT_ROOT_JSON =
    "{\"Account\":\"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\"Balance\":\"1000\",\"Sequence\":1}";

  @Test
  void serializeCreatedNode() throws JsonProcessingException, JSONException {
    CreatedNode<MetaAccountRootObject> node = ImmutableCreatedNode.<MetaAccountRootObject>builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(LEDGER_INDEX)
      .newFields(ACCOUNT_ROOT)
      .build();

    String expected = "{\"CreatedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX.value() + "\"," +
      "\"NewFields\":" + ACCOUNT_ROOT_JSON +
      "}}";

    assertCanSerializeAndDeserialize(node, expected);
  }

  @Test
  void serializeModifiedNode() throws JsonProcessingException, JSONException {
    ModifiedNode<MetaAccountRootObject> node = ImmutableModifiedNode.<MetaAccountRootObject>builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(LEDGER_INDEX)
      .finalFields(ACCOUNT_ROOT)
      .previousFields(ImmutableMetaAccountRootObject.builder().sequence(UnsignedInteger.ZERO).build())
      .previousTransactionId(PREVIOUS_TXN_ID)
      .previousTransactionsLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(42)))
      .build();

    String expected = "{\"ModifiedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX.value() + "\"," +
      "\"FinalFields\":" + ACCOUNT_ROOT_JSON + "," +
      "\"PreviousFields\":{\"Sequence\":0}," +
      "\"PreviousTxnID\":\"" + PREVIOUS_TXN_ID.value() + "\"," +
      "\"PreviousTxnLgrSeq\":42" +
      "}}";

    assertCanSerializeAndDeserialize(node, expected);
  }

  @Test
  void serializeDeletedNode() throws JsonProcessingException, JSONException {
    DeletedNode<MetaAccountRootObject> node = ImmutableDeletedNode.<MetaAccountRootObject>builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(LEDGER_INDEX)
      .finalFields(ACCOUNT_ROOT)
      .build();

    String expected = "{\"DeletedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX.value() + "\"," +
      "\"FinalFields\":" + ACCOUNT_ROOT_JSON +
      "}}";

    assertCanSerializeAndDeserialize(node, expected);
  }

  /**
   * When polymorphic type handling is enabled for a property, Jackson calls
   * {@link AffectedNodeSerializer#serializeWithType} instead of {@code serialize}. The wrapper key already identifies
   * the concrete node type, so no Jackson type id is written and the output is identical to the plain case.
   */
  @Test
  void serializeWithTypeInfoWritesWrapperWithoutTypeId() throws JsonProcessingException, JSONException {
    CreatedNode<MetaAccountRootObject> node = ImmutableCreatedNode.<MetaAccountRootObject>builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(LEDGER_INDEX)
      .newFields(ACCOUNT_ROOT)
      .build();

    String expected = "{\"node\":{\"CreatedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX.value() + "\"," +
      "\"NewFields\":" + ACCOUNT_ROOT_JSON +
      "}}}";

    String serialized = objectMapper.writeValueAsString(new PolymorphicHolder(node));
    JSONAssert.assertEquals(expected, serialized, JSONCompareMode.STRICT);
  }

  /**
   * With a delegate that is neither {@code ResolvableSerializer} nor {@link ContextualSerializer}, the wrapper has
   * nothing to forward to: {@code resolve} is a no-op and {@code createContextual} returns the wrapper itself.
   */
  @Test
  void resolveAndCreateContextualWithPlainDelegate() throws JsonMappingException {
    AffectedNodeSerializer serializer = new AffectedNodeSerializer(markerSerializer("plain"));
    SerializerProvider provider = objectMapper.getSerializerProvider();

    serializer.resolve(provider);

    assertThat(serializer.createContextual(provider, null)).isSameAs(serializer);
  }

  /**
   * When the delegate's {@code createContextual} hands back a different serializer, the wrapper must re-wrap that
   * replacement so the node is still written inside its wrapper key.
   */
  @Test
  void createContextualRewrapsReplacementDelegate() throws IOException {
    JsonSerializer<Object> replacement = markerSerializer("replaced");
    AffectedNodeSerializer serializer = new AffectedNodeSerializer(new ReplacingSerializer(replacement));
    SerializerProvider provider = objectMapper.getSerializerProvider();

    JsonSerializer<?> contextual = serializer.createContextual(provider, null);

    assertThat(contextual).isInstanceOf(AffectedNodeSerializer.class);
    assertThat(contextual).isNotSameAs(serializer);

    CreatedNode<MetaAccountRootObject> node = ImmutableCreatedNode.<MetaAccountRootObject>builder()
      .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
      .ledgerIndex(LEDGER_INDEX)
      .newFields(ACCOUNT_ROOT)
      .build();
    StringWriter writer = new StringWriter();
    try (JsonGenerator gen = objectMapper.createGenerator(writer)) {
      ((AffectedNodeSerializer) contextual).serialize(node, gen, provider);
    }
    assertThat(writer.toString()).isEqualTo("{\"CreatedNode\":\"replaced\"}");
  }

  private void assertCanSerializeAndDeserialize(AffectedNode node, String expectedJson)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(node);
    JSONAssert.assertEquals(expectedJson, serialized, JSONCompareMode.STRICT);

    AffectedNode deserialized = objectMapper.readValue(serialized, AffectedNode.class);
    assertThat(deserialized).isEqualTo(node);
  }

  /**
   * A stand-in delegate that writes a fixed marker string instead of the node's fields, so tests can tell which
   * delegate the wrapper invoked.
   */
  private static JsonSerializer<Object> markerSerializer(final String marker) {
    return new StdSerializer<Object>(Object.class) {
      @Override
      public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider provider)
        throws IOException {
        gen.writeString(marker);
      }
    };
  }

  /**
   * A {@link ContextualSerializer} delegate whose {@code createContextual} always returns a replacement serializer.
   */
  private static final class ReplacingSerializer extends StdSerializer<Object> implements ContextualSerializer {

    private final JsonSerializer<Object> replacement;

    private ReplacingSerializer(final JsonSerializer<Object> replacement) {
      super(Object.class);
      this.replacement = replacement;
    }

    @Override
    public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider provider)
      throws IOException {
      gen.writeString("original");
    }

    @Override
    public JsonSerializer<?> createContextual(final SerializerProvider provider, final BeanProperty property) {
      return replacement;
    }
  }

  /**
   * Holder whose {@code node} property has polymorphic type handling enabled, forcing Jackson to route through
   * {@link AffectedNodeSerializer#serializeWithType}.
   */
  private static final class PolymorphicHolder {

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty("node")
    private final AffectedNode node;

    private PolymorphicHolder(final AffectedNode node) {
      this.node = node;
    }
  }
}

package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
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
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableMetaUnknownObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ImmutableModifiedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaUnknownObject;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

/**
 * Unit tests for {@link AffectedNodeDeserializer}.
 */
class AffectedNodeDeserializerTest extends AbstractJsonTest {

  private static final String LEDGER_INDEX = Strings.repeat("A", 64);
  private static final String PREVIOUS_TXN_ID = Strings.repeat("B", 64);

  private static final String ACCOUNT_ROOT_JSON =
    "{\"Account\":\"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\"Balance\":\"1000\",\"Sequence\":1}";

  private static final MetaAccountRootObject ACCOUNT_ROOT = ImmutableMetaAccountRootObject.builder()
    .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
    .balance(XrpCurrencyAmount.of(UnsignedLong.valueOf(1000)))
    .sequence(UnsignedInteger.ONE)
    .build();

  @Test
  void deserializeCreatedNode() throws JsonProcessingException {
    String json = "{\"CreatedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX + "\"," +
      "\"NewFields\":" + ACCOUNT_ROOT_JSON +
      "}}";

    AffectedNode node = objectMapper.readValue(json, AffectedNode.class);

    assertThat(node).isInstanceOf(CreatedNode.class);
    assertThat(node).isEqualTo(
      ImmutableCreatedNode.<MetaAccountRootObject>builder()
        .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
        .ledgerIndex(Hash256.of(LEDGER_INDEX))
        .newFields(ACCOUNT_ROOT)
        .build()
    );
    assertThat(((CreatedNode<?>) node).newFields()).isInstanceOf(MetaAccountRootObject.class);
  }

  @Test
  void deserializeModifiedNode() throws JsonProcessingException {
    String json = "{\"ModifiedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX + "\"," +
      "\"FinalFields\":" + ACCOUNT_ROOT_JSON + "," +
      "\"PreviousFields\":{\"Sequence\":0}," +
      "\"PreviousTxnID\":\"" + PREVIOUS_TXN_ID + "\"," +
      "\"PreviousTxnLgrSeq\":42" +
      "}}";

    AffectedNode node = objectMapper.readValue(json, AffectedNode.class);

    assertThat(node).isInstanceOf(ModifiedNode.class);
    assertThat(node).isEqualTo(
      ImmutableModifiedNode.<MetaAccountRootObject>builder()
        .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
        .ledgerIndex(Hash256.of(LEDGER_INDEX))
        .finalFields(ACCOUNT_ROOT)
        .previousFields(ImmutableMetaAccountRootObject.builder().sequence(UnsignedInteger.ZERO).build())
        .previousTransactionId(Hash256.of(PREVIOUS_TXN_ID))
        .previousTransactionsLedgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(42)))
        .build()
    );
  }

  @Test
  void deserializeDeletedNode() throws JsonProcessingException {
    String json = "{\"DeletedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX + "\"," +
      "\"FinalFields\":" + ACCOUNT_ROOT_JSON +
      "}}";

    AffectedNode node = objectMapper.readValue(json, AffectedNode.class);

    assertThat(node).isInstanceOf(DeletedNode.class);
    assertThat(node).isEqualTo(
      ImmutableDeletedNode.<MetaAccountRootObject>builder()
        .ledgerEntryType(MetaLedgerEntryType.ACCOUNT_ROOT)
        .ledgerIndex(Hash256.of(LEDGER_INDEX))
        .finalFields(ACCOUNT_ROOT)
        .build()
    );
  }

  @Test
  void deserializeUnknownLedgerEntryTypeAsMetaUnknownObject() throws JsonProcessingException {
    String json = "{\"ModifiedNode\":{" +
      "\"LedgerEntryType\":\"SomeFutureLedgerEntry\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX + "\"," +
      "\"FinalFields\":{\"Foo\":\"bar\"}" +
      "}}";

    AffectedNode node = objectMapper.readValue(json, AffectedNode.class);

    assertThat(node).isInstanceOf(ModifiedNode.class);
    assertThat(node.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.of("SomeFutureLedgerEntry"));
    assertThat(((ModifiedNode<?>) node).finalFields().get()).isInstanceOf(MetaUnknownObject.class);
    assertThat(((ModifiedNode<?>) node).finalFields().get()).isEqualTo(ImmutableMetaUnknownObject.builder().build());
  }

  @Test
  void deserializeUnrecognizedNodeType() {
    String json = "{\"RenamedNode\":{" +
      "\"LedgerEntryType\":\"AccountRoot\"," +
      "\"LedgerIndex\":\"" + LEDGER_INDEX + "\"" +
      "}}";

    assertThatThrownBy(() -> objectMapper.readValue(json, AffectedNode.class))
      .isInstanceOf(JsonMappingException.class)
      .hasMessageContaining("Unrecognized AffectedNode type RenamedNode");
  }

  @Test
  void deserializeMissingLedgerEntryType() {
    String json = "{\"CreatedNode\":{\"LedgerIndex\":\"" + LEDGER_INDEX + "\"}}";

    assertThatThrownBy(() -> objectMapper.readValue(json, AffectedNode.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("missing required field LedgerEntryType");
  }

  @Test
  void deserializeNonObjectNodeValue() {
    String json = "{\"CreatedNode\":\"not-an-object\"}";

    assertThatThrownBy(() -> objectMapper.readValue(json, AffectedNode.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("CreatedNode must be a JSON object");
  }

  @Test
  void deserializeEmptyWrapper() {
    String json = "{}";

    assertThatThrownBy(() -> objectMapper.readValue(json, AffectedNode.class))
      .isInstanceOf(MismatchedInputException.class)
      .hasMessageContaining("exactly one field");
  }
}

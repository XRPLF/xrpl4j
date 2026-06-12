package org.xrpl.xrpl4j.model.jackson.modules;

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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaAccountRootObject;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerEntryType;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

class AffectedNodeDeserializerTest {

  private static final String LEDGER_INDEX = "1E7E658C2D3DF91EFAE5A12573284AD6F526B8F64DD12F013C6F889EF45BEA97";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  // Happy-path deserialization

  @Test
  void deserializesCreatedNode() throws Exception {
    String json = "{\"CreatedNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX +
      "\",\"NewFields\":{}}}";
    AffectedNode result = objectMapper.readValue(json, AffectedNode.class);

    assertThat(result).isInstanceOf(CreatedNode.class);
    CreatedNode<?> node = (CreatedNode<?>) result;
    assertThat(node.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT);
    assertThat(node.newFields()).isInstanceOf(MetaAccountRootObject.class);
  }

  @Test
  void deserializesModifiedNode() throws Exception {
    String json = "{\"ModifiedNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX + "\"}}";
    AffectedNode result = objectMapper.readValue(json, AffectedNode.class);

    assertThat(result).isInstanceOf(ModifiedNode.class);
    assertThat(result.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT);
  }

  @Test
  void deserializesDeletedNode() throws Exception {
    String json = "{\"DeletedNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX +
      "\",\"FinalFields\":{}}}";
    AffectedNode result = objectMapper.readValue(json, AffectedNode.class);

    assertThat(result).isInstanceOf(DeletedNode.class);
    assertThat(result.ledgerEntryType()).isEqualTo(MetaLedgerEntryType.ACCOUNT_ROOT);
  }

  // Malformed input — all return null (warn and skip)

  @Test
  void returnsNullOnMissingLedgerEntryType() throws Exception {
    String json = "{\"CreatedNode\":{\"SomeOtherField\":\"value\"}}";
    AffectedNode result = objectMapper.readValue(json, AffectedNode.class);
    assertThat(result).isNull();
  }

  @Test
  void returnsNullOnNullLedgerEntryType() throws Exception {
    String json = "{\"CreatedNode\":{\"LedgerEntryType\":null}}";
    AffectedNode result = objectMapper.readValue(json, AffectedNode.class);
    assertThat(result).isNull();
  }

  @Test
  void returnsNullOnNullNodeValue() throws Exception {
    String json = "{\"CreatedNode\":null}";
    AffectedNode result = objectMapper.readValue(json, AffectedNode.class);
    assertThat(result).isNull();
  }

  @Test
  void returnsNullOnEmptyObject() throws Exception {
    AffectedNode result = objectMapper.readValue("{}", AffectedNode.class);
    assertThat(result).isNull();
  }

  // Unrecognized node type — throws

  @Test
  void throwsOnUnrecognizedNodeType() {
    String json = "{\"WeirdNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX + "\"}}";
    assertThatThrownBy(() -> objectMapper.readValue(json, AffectedNode.class))
      .isInstanceOf(JsonMappingException.class)
      .hasMessageContaining("Unrecognized AffectedNode type WeirdNode");
  }
}

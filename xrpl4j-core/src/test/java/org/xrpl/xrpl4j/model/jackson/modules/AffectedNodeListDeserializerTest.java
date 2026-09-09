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
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.metadata.DeletedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.ModifiedNode;

class AffectedNodeListDeserializerTest {

  private static final String LEDGER_INDEX = "1E7E658C2D3DF91EFAE5A12573284AD6F526B8F64DD12F013C6F889EF45BEA97";
  private static final String LEDGER_INDEX_2 = "220DDA7164F3F41F3C5223FA3125D4CD368EBB4FB954B5FBFFB6D1EA6DACDD5E";

  private static final String VALID_MODIFIED_NODE =
    "{\"ModifiedNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX + "\"}}";
  private static final String VALID_DELETED_NODE =
    "{\"DeletedNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX_2 +
      "\",\"FinalFields\":{}}}";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = ObjectMapperFactory.create();
  }

  private TransactionMetadata deserialize(String affectedNodesJson) throws Exception {
    String json = "{\"AffectedNodes\":" + affectedNodesJson +
      ",\"TransactionIndex\":0,\"TransactionResult\":\"tesSUCCESS\"}";
    return objectMapper.readValue(json, TransactionMetadata.class);
  }

  @Test
  void deserializesValidNodes() throws Exception {
    TransactionMetadata result = deserialize("[" + VALID_MODIFIED_NODE + "," + VALID_DELETED_NODE + "]");

    assertThat(result.affectedNodes()).hasSize(2);
    assertThat(result.affectedNodes().get(0)).isInstanceOf(ModifiedNode.class);
    assertThat(result.affectedNodes().get(1)).isInstanceOf(DeletedNode.class);
  }

  @Test
  void skipsMissingLedgerEntryType() throws Exception {
    String malformed = "{\"ModifiedNode\":{\"SomeOtherField\":\"value\"}}";
    TransactionMetadata result = deserialize("[" + VALID_MODIFIED_NODE + "," + malformed + "]");

    assertThat(result.affectedNodes()).hasSize(1);
    assertThat(result.affectedNodes().get(0)).isInstanceOf(ModifiedNode.class);
  }

  @Test
  void skipsNullLedgerEntryType() throws Exception {
    String malformed = "{\"DeletedNode\":{\"LedgerEntryType\":null}}";
    TransactionMetadata result = deserialize("[" + VALID_MODIFIED_NODE + "," + malformed + "]");

    assertThat(result.affectedNodes()).hasSize(1);
    assertThat(result.affectedNodes().get(0)).isInstanceOf(ModifiedNode.class);
  }

  @Test
  void skipsEmptyObjects() throws Exception {
    TransactionMetadata result = deserialize("[{}," + VALID_MODIFIED_NODE + "]");

    assertThat(result.affectedNodes()).hasSize(1);
    assertThat(result.affectedNodes().get(0)).isInstanceOf(ModifiedNode.class);
  }

  @Test
  void returnsEmptyListForEmptyArray() throws Exception {
    TransactionMetadata result = deserialize("[]");

    assertThat(result.affectedNodes()).isEmpty();
  }

  @Test
  void throwsOnUnrecognizedNodeType() {
    String unrecognized =
      "{\"WeirdNode\":{\"LedgerEntryType\":\"AccountRoot\",\"LedgerIndex\":\"" + LEDGER_INDEX + "\"}}";
    assertThatThrownBy(() -> deserialize("[" + unrecognized + "]"))
      .isInstanceOf(JsonMappingException.class)
      .hasMessageContaining("Unrecognized AffectedNode type WeirdNode");
  }
}

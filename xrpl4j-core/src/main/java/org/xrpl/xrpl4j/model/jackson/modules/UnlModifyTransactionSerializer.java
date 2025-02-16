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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.UnlModify;
import org.xrpl.xrpl4j.model.transactions.metadata.CreatedNode;

import java.io.IOException;
import java.util.Map;

// TOOD: Delete if unused.
/**
 * Custom deserializer for {@link Transaction}s, which deserializes to a specific {@link Transaction} type based on the
 * TransactionType JSON field.
 */
public class UnlModifyTransactionSerializer extends StdSerializer<UnlModify> {

  /**
   * No-args constructor.
   */
  protected UnlModifyTransactionSerializer() {
    super(UnlModify.class);
  }

  @Override
  public void serialize(UnlModify value, JsonGenerator gen, SerializerProvider provider) throws IOException {

//    ObjectMapper codec = (ObjectMapper) jsonParser.getCodec();
//    JsonNode jsonNode = jsonParser.readValueAsTree();
//    Map.Entry<String, JsonNode> nodeFieldAndValue = jsonNode.fields().next();
//    String affectedNodeType = nodeFieldAndValue.getKey();

//    codec.getTypeFactory().constructParametricType(CreatedNode.class, ledgerObjectClass)


//    ObjectMapper objectMapper = (ObjectMapper) gen.getCodec();
//    String json = objectMapper.writeValueAsString(value);

//    provider.defaultSerializeValue()


//    if (UnlModify.class.isAssignableFrom(value.getClass())) {
//      gen.writeStartObject();

//    if (gen.getCurrentValue() == null
//    gen.getCurrentValue().toString().length() == 1
//    ) {
      // 1. Delegate to Jackson for the existing fields:
//      provider.defaultSerializeValue(value, gen); // Delegate to Jackson
//    }

//      ObjectMapperFactory.create().writeValue(gen, value);

    // Serialize the existing fields (delegate to Jackson's default if possible)
//      gen.writeObject((UnlModify) value); // This will handle the standard Transaction fields

    // Add the extra "Account" field here because by marking it @Derived in the Immutable, this field does not show
    // up during serialization.
//    gen.writeFieldName("Account");
//    gen.writeString(value.account().value());

//      gen.writeEndObject();
//    } else {
//       Rely on Jackson's automatic serialization
//      gen.writeObject(value);
//    }
  }
}

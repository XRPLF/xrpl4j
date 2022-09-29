package org.xrpl.xrpl4j.crypto.signing;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.io.IOException;

/**
 * Represents a digital signature for a transaction that can be submitted to the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(using = Signature.SignatureSerializer.class)
@JsonDeserialize(using = Signature.SignatureDeserializer.class)
public interface Signature {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableSignature.Builder}.
   */
  static ImmutableSignature.Builder builder() {
    return ImmutableSignature.builder();
  }

  /**
   * The bytes of this signature.
   *
   * @return A {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Accessor for this signature as a base16-encoded (HEX) string.
   *
   * @return A {@link String}.
   */
  @Derived
  default String base16Value() {
    return BaseEncoding.base16().encode(value().toByteArray());
  }

  /**
   * Custom Jackson serializer for {@link Signature}es.
   */
  class SignatureSerializer extends JsonSerializer<Signature> {
    @Override
    public void serialize(Signature signature, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeString(signature.base16Value());
    }
  }

  /**
   * Deserializes signature string value to an object of type {@link Signature}.
   */
  class SignatureDeserializer extends JsonDeserializer<Signature> {

    @Override
    public Signature deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
      JsonNode node = jsonParser.getCodec().readTree(jsonParser);
      return Signature.builder().value(UnsignedByteArray.fromHex(node.asText())).build();
    }
  }
}

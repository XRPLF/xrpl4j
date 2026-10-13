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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.transactions.metadata.AffectedNode;
import org.xrpl.xrpl4j.model.transactions.metadata.MetaLedgerObject;

import java.io.IOException;
import java.util.Objects;

/**
 * Custom Jackson serializer for {@link AffectedNode}s. xrpld emits each affected node wrapped in a single-key object
 * whose key identifies the node type (e.g., {@code {"CreatedNode": {...}}}), and {@link AffectedNodeDeserializer}
 * expects that same shape. This serializer writes the wrapper and delegates the inner object to the standard bean
 * serializer that Jackson built for the concrete node class.
 *
 * <p>Instances are installed by {@link AffectedNodeSerializerModifier} rather than registered directly, because a
 * serializer registered for {@link AffectedNode} would otherwise be found again when delegating and recurse.</p>
 */
public class AffectedNodeSerializer extends StdSerializer<AffectedNode>
  implements ContextualSerializer, ResolvableSerializer {

  private final JsonSerializer<Object> delegate;

  /**
   * Required-args constructor.
   *
   * @param delegate The {@link JsonSerializer} that writes the unwrapped node fields.
   */
  public AffectedNodeSerializer(final JsonSerializer<Object> delegate) {
    super(AffectedNode.class);
    this.delegate = Objects.requireNonNull(delegate);
  }

  @Override
  public void serialize(
    final AffectedNode node,
    final JsonGenerator gen,
    final SerializerProvider provider
  ) throws IOException {
    gen.writeStartObject();
    gen.writeFieldName(wrapperFieldName(node));
    delegate.serialize(node, gen, provider);
    gen.writeEndObject();
  }

  @Override
  public void resolve(final SerializerProvider provider) throws JsonMappingException {
    if (delegate instanceof ResolvableSerializer) {
      ((ResolvableSerializer) delegate).resolve(provider);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public JsonSerializer<?> createContextual(
    final SerializerProvider provider,
    final BeanProperty property
  ) throws JsonMappingException {
    if (delegate instanceof ContextualSerializer) {
      JsonSerializer<?> contextual = ((ContextualSerializer) delegate).createContextual(provider, property);
      if (contextual != delegate) {
        return new AffectedNodeSerializer((JsonSerializer<Object>) contextual);
      }
    }
    return this;
  }

  private static String wrapperFieldName(final AffectedNode node) {
    return node.<MetaLedgerObject, String>map(
      createdNode -> "CreatedNode",
      modifiedNode -> "ModifiedNode",
      deletedNode -> "DeletedNode"
    );
  }
}

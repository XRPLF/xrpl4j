package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
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

  /**
   * Called instead of {@link #serialize} when Jackson has polymorphic type handling enabled for the value being
   * written, e.g. via {@code @JsonTypeInfo} on a property or {@code ObjectMapper.activateDefaultTyping(...)}.
   *
   * <p>The inherited implementation throws because this serializer does not write a Jackson type id. That is
   * deliberate: the {@code CreatedNode}/{@code ModifiedNode}/{@code DeletedNode} wrapper key written by
   * {@link #serialize} already identifies the concrete node type on the wire, and {@link AffectedNodeDeserializer}
   * dispatches on that key rather than on a Jackson type id. So the {@link TypeSerializer} is intentionally ignored
   * here, which keeps the output identical to xrpld's format even when a caller has enabled default typing on their
   * {@link com.fasterxml.jackson.databind.ObjectMapper}.</p>
   *
   * <p>Writing a Jackson type id here instead would not help: with {@code JsonTypeInfo.Id.CLASS} the id would name
   * the generated {@code Immutable*} class, and on read Jackson would hand the wrapped object to that class's bean
   * deserializer, which expects the unwrapped node fields and fails.
   * {@link AffectedNodeDeserializer#deserializeWithType} is the symmetric counterpart: it ignores the type
   * deserializer and dispatches on the wrapper key.</p>
   */
  @Override
  public void serializeWithType(
    final AffectedNode node,
    final JsonGenerator gen,
    final SerializerProvider provider,
    final TypeSerializer typeSer
  ) throws IOException {
    serialize(node, gen, provider);
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

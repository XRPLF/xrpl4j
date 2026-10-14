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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

import java.math.BigDecimal;

/**
 * An {@link com.fasterxml.jackson.databind.AnnotationIntrospector} that supplies, for every class under
 * {@code org.xrpl.xrpl4j}, the Jackson behaviors xrpl4j's JSON bindings rely on:
 *
 * <ul>
 *   <li>{@link JsonInclude.Include#NON_EMPTY} property inclusion, so that absent {@link java.util.Optional}s, empty
 *   collections and empty strings are omitted from output, as xrpld expects;</li>
 *   <li>{@code ignoreUnknown = true}, so that fields added to xrpld responses by future amendments do not break
 *   deserialization;</li>
 *   <li>case-insensitive property matching on input, matching the previous mapper-wide
 *   {@link com.fasterxml.jackson.databind.MapperFeature#ACCEPT_CASE_INSENSITIVE_PROPERTIES};</li>
 *   <li>plain-notation rendering of {@link BigDecimal} properties (directly or as the content of an
 *   {@link java.util.Optional} or collection), matching the previous mapper-wide
 *   {@link com.fasterxml.jackson.core.JsonGenerator.Feature#WRITE_BIGDECIMAL_AS_PLAIN}.</li>
 * </ul>
 *
 * <p>Scoping these behaviors by package, rather than configuring them on the {@link
 * com.fasterxml.jackson.databind.ObjectMapper}, means a mapper that handles both xrpl4j types and a consumer's own
 * types keeps Jackson's defaults for the consumer's types. Explicit {@link JsonInclude} or {@link
 * JsonIgnoreProperties}, {@link JsonFormat} or serializer annotations on an xrpl4j class take precedence over the
 * values supplied here.</p>
 */
public class Xrpl4jAnnotationIntrospector extends NopAnnotationIntrospector {

  static final String PACKAGE_PREFIX = "org.xrpl.xrpl4j.";

  private static final JsonInclude.Value INCLUSION = JsonInclude.Value.construct(
    JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY
  );

  private static final JsonIgnoreProperties.Value IGNORE_UNKNOWN = JsonIgnoreProperties.Value.empty()
    .withIgnoreUnknown();

  private static final JsonFormat.Value CASE_INSENSITIVE = JsonFormat.Value.empty()
    .withFeature(JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

  @Override
  public JsonInclude.Value findPropertyInclusion(final Annotated annotated) {
    return isXrpl4jClass(annotated) ? INCLUSION : null;
  }

  @Override
  public JsonIgnoreProperties.Value findPropertyIgnoralByName(
    final MapperConfig<?> config,
    final Annotated annotated
  ) {
    return isXrpl4jClass(annotated) ? IGNORE_UNKNOWN : null;
  }

  @Override
  public JsonFormat.Value findFormat(final Annotated annotated) {
    return isXrpl4jClass(annotated) ? CASE_INSENSITIVE : null;
  }

  @Override
  public Object findSerializer(final Annotated annotated) {
    return isXrpl4jBigDecimalMember(annotated, false) ? BigDecimalPlainSerializer.class : null;
  }

  @Override
  public Object findContentSerializer(final Annotated annotated) {
    return isXrpl4jBigDecimalMember(annotated, true) ? BigDecimalPlainSerializer.class : null;
  }

  private static boolean isXrpl4jClass(final Annotated annotated) {
    return annotated instanceof AnnotatedClass &&
      isXrpl4jClass(((AnnotatedClass) annotated).getAnnotated());
  }

  private static boolean isXrpl4jClass(final Class<?> clazz) {
    return clazz != null && clazz.getName().startsWith(PACKAGE_PREFIX);
  }

  /**
   * Whether {@code annotated} is a property of an xrpl4j class whose type (or, if {@code content} is true, whose
   * content type) is {@link BigDecimal}.
   */
  private static boolean isXrpl4jBigDecimalMember(final Annotated annotated, final boolean content) {
    if (!(annotated instanceof AnnotatedMember)) {
      return false;
    }
    AnnotatedMember member = (AnnotatedMember) annotated;
    if (!isXrpl4jClass(member.getDeclaringClass())) {
      return false;
    }
    JavaType type = content ? member.getType().getContentType() : member.getType();
    return type != null && type.hasRawClass(BigDecimal.class);
  }
}

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * An {@link com.fasterxml.jackson.databind.AnnotationIntrospector} that supplies, for every class under
 * {@code org.xrpl.xrpl4j}, the Jackson behaviors xrpl4j's JSON bindings rely on:
 *
 * <ul>
 *   <li>{@link JsonInclude.Include#NON_EMPTY} property inclusion, so that absent {@link java.util.Optional}s, empty
 *   collections and empty strings are omitted from output, as xrpld expects;</li>
 *   <li>{@code ignoreUnknown = true}, so that fields added to xrpld responses by future amendments do not break
 *   deserialization.</li>
 * </ul>
 *
 * <p>Scoping these behaviors by package, rather than configuring them on the {@link
 * com.fasterxml.jackson.databind.ObjectMapper}, means a mapper that handles both xrpl4j types and a consumer's own
 * types keeps Jackson's defaults for the consumer's types. Explicit {@link JsonInclude} or {@link
 * JsonIgnoreProperties} annotations on an xrpl4j class take precedence over the values supplied here.</p>
 */
public class Xrpl4jAnnotationIntrospector extends NopAnnotationIntrospector {

  static final String PACKAGE_PREFIX = "org.xrpl.xrpl4j.";

  private static final JsonInclude.Value INCLUSION = JsonInclude.Value.construct(
    JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY
  );

  private static final JsonIgnoreProperties.Value IGNORE_UNKNOWN = JsonIgnoreProperties.Value.empty()
    .withIgnoreUnknown();

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

  private static boolean isXrpl4jClass(final Annotated annotated) {
    return annotated instanceof AnnotatedClass &&
      ((AnnotatedClass) annotated).getAnnotated().getName().startsWith(PACKAGE_PREFIX);
  }
}

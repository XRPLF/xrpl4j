package org.xrpl.xrpl4j.model.jackson;

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ripple.cryptoconditions.jackson.CryptoConditionsModule;
import com.ripple.cryptoconditions.jackson.Encoding;
import org.xrpl.xrpl4j.model.jackson.modules.Xrpl4jModule;

/**
 * A factory for constructing instances of {@link ObjectMapper} for all xrpl4j-model components.
 */
public class ObjectMapperFactory {

  /**
   * Construct an {@link ObjectMapper} that can be used to serialize and deserialize JSON.
   *
   * @return An {@link ObjectMapper}.
   */
  public static ObjectMapper create() {

    return JsonMapper.builder()
      .addModule(new Jdk8Module())
      .addModule(new GuavaModule())
      .addModule(new CryptoConditionsModule(Encoding.HEX))
      // Developer Note: The ordering here is important. The JavaTimeModule must be added before the Xrpl4jModule so
      // that the ZonedDateTimeSerializer is not overridden by the JavaTimeModule.
      .addModule(new JavaTimeModule())
      .addModule(new Xrpl4jModule())
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
      .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      // Developer NOTE: Despite `serializationInclusion` being deprecated, we continue using it to for maximum
      // compatibility with software using xrpl4j that uses older versions of Jackson. While newer versions of
      // Jackson will define a more granular way to configure this behavior that we should actually employ at some point
      // (e.g., see https://github.com/FasterXML/jackson-databind/issues/2899), we purposefully don't change
      // this behavior for now to ensure maximum compatibility.
      .serializationInclusion(JsonInclude.Include.NON_EMPTY)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
      .build();
  }
}

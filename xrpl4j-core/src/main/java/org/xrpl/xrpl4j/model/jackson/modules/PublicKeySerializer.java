package org.xrpl.xrpl4j.model.jackson.modules;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.io.IOException;
import java.util.Objects;

/**
 * A custom Jackson serializer that serializes a {@link PublicKey} to a hex-string.
 */
public class PublicKeySerializer extends StdSerializer<PublicKey> {

  /**
   * No-args Constructor.
   */
  public PublicKeySerializer() {
    super(PublicKey.class, false);
  }

  @Override
  public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(this.valueToString(value));
  }

  private String valueToString(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);
    return publicKey.base16Value();
  }
}

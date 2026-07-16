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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link ConfidentialMptSendProof}s, which writes the proof as an uppercase hex string.
 */
public class ConfidentialMptSendProofSerializer extends StdScalarSerializer<ConfidentialMptSendProof> {

  /**
   * No-args constructor.
   */
  public ConfidentialMptSendProofSerializer() {
    super(ConfidentialMptSendProof.class, false);
  }

  @Override
  public void serialize(ConfidentialMptSendProof proof, JsonGenerator gen, SerializerProvider provider)
    throws IOException {
    gen.writeString(proof.hexValue());
  }
}

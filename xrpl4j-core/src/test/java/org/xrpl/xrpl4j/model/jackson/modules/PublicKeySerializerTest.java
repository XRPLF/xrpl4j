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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PUBLIC_KEY_HEX;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.io.IOException;

/**
 * Unit tests for {@link PublicKeySerializer}.
 */
class PublicKeySerializerTest {


  private PublicKeySerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new PublicKeySerializer();
  }

  @Test
  void testSerialize() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    PublicKey expected = PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX);

    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString(ED_PUBLIC_KEY_HEX);
  }

  @Test
  void testSerializeOfMultiSignPubKey() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    PublicKey expected = PublicKey.MULTI_SIGN_PUBLIC_KEY;

    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString(PublicKey.MULTI_SIGN_PUBLIC_KEY.base16Value());
  }
}

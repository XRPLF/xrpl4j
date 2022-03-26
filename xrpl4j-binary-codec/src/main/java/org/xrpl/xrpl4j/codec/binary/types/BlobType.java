package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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

import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL Blob type.
 */
public class BlobType extends SerializedType<BlobType> {

  public BlobType() {
    this(UnsignedByteArray.empty());
  }

  public BlobType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public BlobType fromParser(BinaryParser parser, int lengthHint) {
    return new BlobType(parser.read(lengthHint));
  }

  @Override
  public BlobType fromJson(JsonNode node) {
    return new BlobType(UnsignedByteArray.fromHex(node.asText()));
  }

}

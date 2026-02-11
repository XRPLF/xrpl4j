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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.binary.definitions.DefinitionsService;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInstance;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL UInt32 type.
 */
public class UInt32Type extends UIntType<UInt32Type> {

  public UInt32Type() {
    this(UnsignedLong.ZERO);
  }

  public UInt32Type(UnsignedLong value) {
    super(value, 32);
  }

  @Override
  public UInt32Type fromParser(BinaryParser parser) {
    return new UInt32Type(parser.readUInt32());
  }

  @Override
  public UInt32Type fromJson(JsonNode value) {
    return new UInt32Type(UnsignedLong.valueOf(value.asText()));
  }

  @Override
  public UInt32Type fromJson(JsonNode value, FieldInstance fieldInstance) throws JsonProcessingException {
    // Special handling for PermissionValue field - convert string permission names to UInt32 values
    if (fieldInstance != null && "PermissionValue".equals(fieldInstance.name()) && value.isTextual()) {
      String textValue = value.asText();

      // Check if it's already a numeric value (from mapSpecializedValues)
      try {
        int numericValue = Integer.parseInt(textValue);
        return new UInt32Type(UnsignedLong.valueOf(numericValue));
      } catch (NumberFormatException e) {
        // It's a permission name, look it up in the mapping
        Integer permissionValue = DefinitionsService.getInstance()
          .mapFieldSpecialization("PermissionValue", textValue)
          .orElseThrow(() -> new IllegalArgumentException("Unknown permission value: " + textValue));
        return new UInt32Type(UnsignedLong.valueOf(permissionValue));
      }
    }
    return fromJson(value);
  }

  @Override
  public JsonNode toJson() {
    return new LongNode(UnsignedLong.valueOf(toHex(), 16).longValue());
  }

  @Override
  public JsonNode toJson(FieldInstance fieldInstance) {
    // Special handling for PermissionValue field - convert UInt32 values back to string permission names
    if (fieldInstance != null && "PermissionValue".equals(fieldInstance.name())) {
      UnsignedLong value = UnsignedLong.valueOf(toHex(), 16);
      String intValueStr = String.valueOf(value.intValue());

      // Look up the permission name from the reverse mapping
      return DefinitionsService.getInstance()
        .mapFieldRawValueToSpecialization("PermissionValue", intValueStr)
        .<JsonNode>map(TextNode::new)
        .orElseGet(this::toJson);
    }
    return toJson();
  }
}

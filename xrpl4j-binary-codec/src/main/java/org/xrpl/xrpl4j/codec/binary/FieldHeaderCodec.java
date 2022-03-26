package org.xrpl.xrpl4j.codec.binary;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.ByteUtils;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.binary.definitions.Definitions;
import org.xrpl.xrpl4j.codec.binary.definitions.DefinitionsProvider;
import org.xrpl.xrpl4j.codec.binary.definitions.FieldInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A codec for instances of {@link FieldHeader}.
 */
public class FieldHeaderCodec {

  private static final FieldHeaderCodec INSTANCE = new FieldHeaderCodec(DefinitionsProvider.getInstance().get(),
    BinaryCodecObjectMapperFactory.getObjectMapper());

  private final Definitions definitions;

  private final Map<String, FieldInfo> fieldMetadataMap;

  private final Map<FieldHeader, String> fieldIdNameMap;

  private final Map<String, Integer> typeOrdinalMap;

  /**
   * Required-args Constructor.
   *
   * @param definitions A {@link Definitions}.
   * @param mapper      An {@link ObjectMapper}.
   */
  public FieldHeaderCodec(Definitions definitions, ObjectMapper mapper) {
    this.definitions = definitions;
    this.fieldMetadataMap = new HashMap<>();
    this.fieldIdNameMap = new HashMap<>();
    this.typeOrdinalMap = definitions.types();
    this.definitions.fields().forEach(field -> {
      try {
        String fieldName = field.get(0).textValue();
        FieldInfo metadata = mapper.readValue(field.get(1).toString(), FieldInfo.class);
        FieldHeader fieldHeader = FieldHeader.builder().fieldCode(metadata.nth())
          .typeCode(typeOrdinalMap.get(metadata.type()))
          .build();
        fieldMetadataMap.put(fieldName, metadata);
        fieldIdNameMap.put(fieldHeader, fieldName);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("invalid json", e);
      }
    });
  }

  public static FieldHeaderCodec getInstance() {
    return INSTANCE;
  }

  public String encode(String fieldName) {
    return encode(getFieldId(fieldName));
  }

  protected String encode(FieldHeader fieldHeader) {
    List<UnsignedByte> segments = new ArrayList<>();
    int typeCode = fieldHeader.typeCode();
    int fieldCode = fieldHeader.fieldCode();
    if (typeCode < 16) {
      if (fieldCode < 16) {
        // single byte case where high bits contain type code, low bits contain field code
        segments.add(UnsignedByte.of((byte) typeCode, (byte) fieldCode));
      } else {
        // 2 byte case where first byte contains type code + filler, second byte contains field code
        segments.add(UnsignedByte.of((byte) typeCode, (byte) 0));
        segments.add(UnsignedByte.of(fieldCode));
      }
    } else {
      if (fieldCode < 16) {
        // 2 byte case where first byte contains filler+field code, second byte contains typeCode
        segments.add(UnsignedByte.of((byte) 0, (byte) fieldCode));
        segments.add(UnsignedByte.of(typeCode));
      } else {
        // 3 byte case where first byte is filler, 2nd byte is type code, third byte is field code
        segments.add(UnsignedByte.of((byte) 0));
        segments.add(UnsignedByte.of(typeCode));
        segments.add(UnsignedByte.of(fieldCode));
      }
    }
    return ByteUtils.toHex(segments);
  }

  public String decode(String hex) {
    FieldHeader fieldHeader = decodeFieldId(hex);
    return fieldIdNameMap.get(fieldHeader);
  }

  protected FieldHeader decodeFieldId(String hex) {
    Preconditions.checkNotNull(hex, "hex cannot be null");
    Preconditions.checkArgument(hex.length() >= 2, "hex must be at least 2 characters");
    List<UnsignedByte> segments = ByteUtils.parse(hex);
    Preconditions.checkArgument(segments.size() <= 3, "hex value is too large");
    if (segments.size() == 1) {
      UnsignedByte first = segments.get(0);
      return FieldHeader.builder()
        .typeCode(first.getHighBits())
        .fieldCode(first.getLowBits())
        .build();
    }
    if (segments.size() == 2) {
      UnsignedByte first = segments.get(0);
      UnsignedByte second = segments.get(1);
      if (first.getHighBits() == 0) {
        return FieldHeader.builder()
          .fieldCode(first.getLowBits())
          .typeCode(second.asInt())
          .build();
      } else {
        return FieldHeader.builder()
          .typeCode(first.getHighBits())
          .fieldCode(second.asInt())
          .build();
      }
    }
    return FieldHeader.builder()
      .typeCode(segments.get(1).asInt())
      .fieldCode(segments.get(2).asInt())
      .build();
  }

  protected FieldHeader getFieldId(String fieldName) {
    FieldInfo metadata = fieldMetadataMap.get(fieldName);
    Preconditions.checkNotNull(metadata, fieldName + " is not a valid field name");
    Integer typeCode = typeOrdinalMap.get(metadata.type());
    Preconditions.checkNotNull(typeCode, typeCode + " is not a valid type");
    return FieldHeader.builder().typeCode(typeCode).fieldCode(metadata.nth()).build();
  }

}


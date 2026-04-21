package org.xrpl.xrpl4j.confidential.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: mpt-crypto
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

import com.sun.jna.Structure;

/**
 * JNA mapping for the C struct {@code mpt_issuance_id}, passed by value.
 *
 * <pre>
 * typedef struct {
 *     uint8_t bytes[24];
 * } mpt_issuance_id;
 * </pre>
 */
@Structure.FieldOrder({"bytes"})
public class MptIssuanceId extends Structure implements Structure.ByValue {

  /** The 24-byte MPTokenIssuance ID. */
  public byte[] bytes = new byte[24];

  /** Default constructor required by JNA. */
  public MptIssuanceId() {
  }
}

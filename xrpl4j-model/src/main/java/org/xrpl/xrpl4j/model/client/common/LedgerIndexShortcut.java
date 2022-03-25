package org.xrpl.xrpl4j.model.client.common;

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

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * A String wrapping class that defines three static constants representing shortcut values which can be used to specify
 * a ledger index in a rippled API request.
 */
public class LedgerIndexShortcut {

  /**
   * Request information about a rippled server's current working version of the ledger.
   */
  public static final LedgerIndexShortcut CURRENT = new LedgerIndexShortcut("current");

  /**
   * Request information about for the most recent ledger that has been validated by consensus.
   */
  public static final LedgerIndexShortcut VALIDATED = new LedgerIndexShortcut("validated");

  /**
   * Request information about the most recent ledger that has been closed for modifications and proposed for
   * validation.
   */
  public static final LedgerIndexShortcut CLOSED = new LedgerIndexShortcut("closed");

  @JsonValue
  private final String value;

  private LedgerIndexShortcut(String value) {
    this.value = value;
  }

  /**
   * Accessor for the value of this shortcut.
   *
   * @return {@link String}
   */
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return getValue();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof LedgerIndexShortcut)) {
      return false;
    }

    LedgerIndexShortcut that = (LedgerIndexShortcut) other;

    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}

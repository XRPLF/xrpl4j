package org.xrpl.xrpl4j.model.ledger;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Represents the XRP asset on the ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableXrpIssue.class)
@JsonDeserialize(as = ImmutableXrpIssue.class)
public interface XrpIssue extends Issue {

  /**
   * The singleton XrpIssue instance.
   */
  XrpIssue XRP = ImmutableXrpIssue.builder().build();

  /**
   * The currency code, which is always "XRP".
   *
   * @return The {@link String} "XRP".
   */
  @Value.Derived
  default String currency() {
    return "XRP";
  }

}

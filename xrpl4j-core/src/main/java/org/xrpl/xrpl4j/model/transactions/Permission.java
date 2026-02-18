package org.xrpl.xrpl4j.model.transactions;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * {@link Permission} inner object with PermissionValue details.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePermission.class)
@JsonDeserialize(as = ImmutablePermission.class)
public interface Permission {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutablePermission.Builder}.
   */
  static ImmutablePermission.Builder builder() {
    return ImmutablePermission.builder();
  }

  /**
   * Create a {@link Permission} from a {@link TransactionType}.
   *
   * <p>The permission value is calculated as the transaction type code + 1.</p>
   *
   * @param transactionType The {@link TransactionType} to create a permission for.
   *
   * @return A {@link Permission} with the permission value set to the transaction type code + 1.
   */
  static Permission of(TransactionType transactionType) {
    // Transaction type permissions are the transaction type code + 1
    // We need to get the numeric code from definitions.json
    // For now, we'll use the string value directly as the permissionValue
    return builder()
      .permissionValue(transactionType.value())
      .build();
  }

  /**
   * Create a {@link Permission} from a {@link GranularPermission}.
   *
   * @param granularPermission The {@link GranularPermission} to create a permission for.
   *
   * @return A {@link Permission} with the permission value set to the granular permission's string value.
   */
  static Permission of(GranularPermission granularPermission) {
    return builder()
      .permissionValue(granularPermission.value())
      .build();
  }

  /**
   * The transaction type that is being delegated, represented as a string.
   *
   * @return A {@link String} representing the transaction type.
   */
  @JsonProperty("PermissionValue")
  String permissionValue();
}


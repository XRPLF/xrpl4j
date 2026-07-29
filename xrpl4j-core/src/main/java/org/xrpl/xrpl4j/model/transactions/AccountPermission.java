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
import com.google.common.annotations.Beta;
import org.immutables.value.Value;

/**
 * {@link AccountPermission} inner object with PermissionValue details.
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountPermission.class)
@JsonDeserialize(as = ImmutableAccountPermission.class)
@Beta
public interface AccountPermission {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountPermission.Builder}.
   */
  static ImmutableAccountPermission.Builder builder() {
    return ImmutableAccountPermission.builder();
  }

  /**
   * Create a {@link AccountPermission} from a {@link TransactionType}.
   *
   * @param transactionType The {@link TransactionType} to create a permission for.
   *
   * @return A {@link AccountPermission} with the permission value set to the transaction type.
   */
  static AccountPermission of(TransactionType transactionType) {
    return builder()
      .permissionValue(TransactionTypePermission.of(transactionType))
      .build();
  }

  /**
   * Create a {@link AccountPermission} from a {@link GranularPermission}.
   *
   * @param granularPermission The {@link GranularPermission} to create a permission for.
   *
   * @return A {@link AccountPermission} with the permission value set to the granular permission.
   */
  static AccountPermission of(GranularPermission granularPermission) {
    return builder()
      .permissionValue(GranularPermissionValue.of(granularPermission))
      .build();
  }

  /**
   * Create a {@link AccountPermission} from a {@link Permission}.
   *
   * @param permission The {@link Permission} to create an account permission for.
   *
   * @return A {@link AccountPermission} with the specified permission value.
   */
  static AccountPermission of(Permission permission) {
    return builder()
      .permissionValue(permission)
      .build();
  }

  /**
   * The permission value that is being delegated.
   *
   * @return A {@link Permission} representing either a transaction type or granular permission.
   */
  @JsonProperty("PermissionValue")
  Permission permissionValue();
}



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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link DelegateSet} transaction allows an account to delegate a set of permissions to another account.
 * This enables the authorized account to send certain types of transactions on behalf of the delegating account.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDelegateSet.class)
@JsonDeserialize(as = ImmutableDelegateSet.class)
public interface DelegateSet extends Transaction {

  /**
   * The maximum number of permissions that can be delegated in a single transaction.
   */
  int PERMISSIONS_MAX_LENGTH = 10;

  /**
   * Set of transaction types that cannot be delegated.
   */
  Set<String> NON_DELEGABLE_TRANSACTIONS = Sets.newHashSet(
    TransactionType.ACCOUNT_SET.value(),
    TransactionType.SET_REGULAR_KEY.value(),
    TransactionType.SIGNER_LIST_SET.value(),
    TransactionType.DELEGATE_SET.value(),
    TransactionType.ACCOUNT_DELETE.value(),
    TransactionType.BATCH.value(),
    // Pseudo transactions below:
    TransactionType.ENABLE_AMENDMENT.value(),
    TransactionType.SET_FEE.value(),
    TransactionType.UNL_MODIFY.value()
  );

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableDelegateSet.Builder}.
   */
  static ImmutableDelegateSet.Builder builder() {
    return ImmutableDelegateSet.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link DelegateSet}, which only allows the {@code tfFullyCanonicalSig}
   * flag, which is deprecated.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The XRP Ledger {@link Address} of the account to authorize.
   *
   * @return An {@link Address} of the account to authorize.
   */
  @JsonProperty("Authorize")
  Address authorize();

  /**
   * The transaction permissions (represented by transaction type strings) that the account has been granted.
   *
   * @return A {@link List} of {@link PermissionWrapper}s.
   */
  @JsonProperty("Permissions")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  List<PermissionWrapper> permissions();

  /**
   * Validate that the Authorize field is not the same as the Account field.
   */
  @Value.Check
  default void validateAuthorizeNotSameAsAccount() {
    Preconditions.checkArgument(
      !authorize().equals(account()),
      "DelegateSet: Authorize and Account must be different."
    );
  }

  /**
   * Validate that the Permissions array has at most PERMISSIONS_MAX_LENGTH elements.
   */
  @Value.Check
  default void validatePermissionsLength() {
    Preconditions.checkArgument(
      permissions().size() <= PERMISSIONS_MAX_LENGTH,
      String.format("DelegateSet: Permissions array length cannot be greater than %d.", PERMISSIONS_MAX_LENGTH)
    );
  }

  /**
   * Validate that the Permissions array does not contain duplicate values.
   */
  @Value.Check
  default void validateNoDuplicatePermissions() {
    Set<String> permissionValues = new HashSet<>();
    for (PermissionWrapper wrapper : permissions()) {
      String permissionValue = wrapper.permission().permissionValue();
      Preconditions.checkArgument(
        permissionValues.add(permissionValue),
        "DelegateSet: Permissions array cannot contain duplicate values"
      );
    }
  }

  /**
   * Validate that the Permissions array does not contain non-delegatable transaction types.
   */
  @Value.Check
  default void validateNoNonDelegatableTransactions() {
    for (PermissionWrapper wrapper : permissions()) {
      String permissionValue = wrapper.permission().permissionValue();
      Preconditions.checkArgument(
        !NON_DELEGABLE_TRANSACTIONS.contains(permissionValue),
        String.format("DelegateSet: PermissionValue contains a non-delegatable transaction %s", permissionValue)
      );
    }
  }

  /**
   * Validate that each permission value corresponds to either a valid TransactionType or GranularPermission.
   */
  @Value.Check
  default void validatePermissionValuesAreValid() {
    for (PermissionWrapper wrapper : permissions()) {
      String permissionValue = wrapper.permission().permissionValue();

      // Check if it's a valid TransactionType
      TransactionType transactionType = TransactionType.forValue(permissionValue);
      boolean isValidTransactionType = transactionType != TransactionType.UNKNOWN;

      // Check if it's a valid GranularPermission
      GranularPermission granularPermission = GranularPermission.forValue(permissionValue);
      boolean isValidGranularPermission = granularPermission != null;

      Preconditions.checkArgument(
        isValidTransactionType || isValidGranularPermission,
        String.format(
          "DelegateSet: PermissionValue '%s' is not a valid TransactionType or GranularPermission",
          permissionValue
        )
      );
    }
  }
}

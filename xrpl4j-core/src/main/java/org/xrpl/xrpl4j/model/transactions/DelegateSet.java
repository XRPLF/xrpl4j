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
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link DelegateSet} transaction allows an account to delegate a set of permissions to another account.
 * This enables the authorized account to send certain types of transactions on behalf of the delegating account.
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDelegateSet.class)
@JsonDeserialize(as = ImmutableDelegateSet.class)
@Beta
public interface DelegateSet extends Transaction {

  /**
   * The maximum number of permissions that can be delegated in a single transaction.
   */
  int PERMISSIONS_MAX_LENGTH = 10;

  /**
   * Set of transaction types that cannot be delegated.
   */
  Set<TransactionType> NON_DELEGABLE_TRANSACTIONS = ImmutableSet.of(
    TransactionType.ACCOUNT_SET,
    TransactionType.SET_REGULAR_KEY,
    TransactionType.SIGNER_LIST_SET,
    TransactionType.DELEGATE_SET,
    TransactionType.ACCOUNT_DELETE,
    TransactionType.BATCH,
    // Pseudo transactions below:
    TransactionType.ENABLE_AMENDMENT,
    TransactionType.SET_FEE,
    TransactionType.UNL_MODIFY
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
   * Set of {@link TransactionFlags}s for this {@link DelegateSet}. Note that {@link DelegateSet} transactions
   * do not support any transaction-specific flags, only universal transaction flags that apply to all transaction
   * types.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return {@link TransactionFlags#EMPTY} by default, or {@link TransactionFlags} with universal flags set when
   *   used in specific contexts.
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
   * @return A {@link List} of {@link AccountPermissionWrapper}s.
   */
  @JsonProperty("Permissions")
  @JsonInclude(JsonInclude.Include.ALWAYS)
  List<AccountPermissionWrapper> permissions();

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
    for (AccountPermissionWrapper wrapper : permissions()) {
      String permissionValue = wrapper.permission().permissionValue().map(
        transactionTypePermission -> transactionTypePermission.value(),
        granularPermissionValue -> granularPermissionValue.value(),
        unknownPermission -> unknownPermission.value()
      );
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
    for (AccountPermissionWrapper wrapper : permissions()) {
      Permission permission = wrapper.permission().permissionValue();
      // Only check transaction type permissions for non-delegatable transactions
      permission.handle(
        transactionTypePermission -> {
          boolean isNonDelegatable = NON_DELEGABLE_TRANSACTIONS.contains(transactionTypePermission.transactionType());
          Preconditions.checkArgument(
            !isNonDelegatable,
            String.format("DelegateSet: PermissionValue contains a non-delegatable transaction %s",
              transactionTypePermission.value())
          );
        },
        granularPermissionValue -> {
          // Granular permissions are always delegatable, no validation needed
        },
        unknownPermission -> {
          // Unknown permissions cannot be validated locally; xrpld is the authority on whether they are delegatable.
        }
      );
    }
  }

  /**
   * Validate that each {@link TransactionTypePermission} wraps a real {@link TransactionType} rather than
   * {@link TransactionType#UNKNOWN}. Permission values that xrpl4j does not recognize at all are deserialized as
   * {@link UnknownPermission} and are deliberately allowed through, so that a {@link DelegateSet} read from the ledger
   * remains parseable even when it references a transaction type newer than this version of xrpl4j.
   */
  @Value.Check
  default void validatePermissionValuesAreValid() {
    for (AccountPermissionWrapper wrapper : permissions()) {
      Permission permission = wrapper.permission().permissionValue();

      permission.handle(
        transactionTypePermission -> {
          Preconditions.checkArgument(
            transactionTypePermission.transactionType() != TransactionType.UNKNOWN,
            String.format("DelegateSet: PermissionValue %s is not a valid TransactionType",
              transactionTypePermission.value())
          );
        },
        granularPermissionValue -> {
          // GranularPermissionValue is always valid if it exists
        },
        unknownPermission -> {
          // Unrecognized names are preserved verbatim (see Javadoc above). However, the binary codec encodes a
          // PermissionValue it cannot map by name as a raw integer, so a purely numeric "unknown" value would encode
          // as some real permission code while sidestepping the duplicate and non-delegatable checks above, which
          // operate on names. xrpld always reports permissions by name, so a numeric value is never legitimate here.
          // Deliberately use the same parser the codec uses (UInt32Type#fromJson) so the two can never disagree.
          Preconditions.checkArgument(
            !isCodecParseableAsInteger(unknownPermission.value()),
            String.format("DelegateSet: PermissionValue %s must be a permission name, not a numeric code",
              unknownPermission.value())
          );
        }
      );
    }
  }

  /**
   * Determine whether the binary codec would treat {@code value} as a raw integer when encoding a
   * {@code PermissionValue}. This intentionally mirrors {@code UInt32Type#fromJson}, which parses via
   * {@link UnsignedLong#valueOf(String)}, rather than re-implementing "is numeric" with a regex: Java's {@code \d}
   * is ASCII-only whereas {@link Long#parseUnsignedLong} accepts any Unicode digit, and the two must never disagree.
   *
   * @param value A {@link String} permission value.
   *
   * @return {@code true} if the codec would encode {@code value} as an integer.
   */
  static boolean isCodecParseableAsInteger(final String value) {
    try {
      UnsignedLong.valueOf(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}

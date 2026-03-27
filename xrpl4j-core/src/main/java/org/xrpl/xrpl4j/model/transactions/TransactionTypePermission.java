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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;

/**
 * A {@link Permission} that represents a standard transaction type permission.
 * Transaction type permissions allow delegation of entire transaction types.
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTransactionTypePermission.class)
@JsonDeserialize(as = ImmutableTransactionTypePermission.class)
@Beta
public interface TransactionTypePermission extends Permission {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTransactionTypePermission.Builder}.
   */
  static ImmutableTransactionTypePermission.Builder builder() {
    return ImmutableTransactionTypePermission.builder();
  }

  /**
   * Create a {@link TransactionTypePermission} from a {@link TransactionType}.
   *
   * @param transactionType The {@link TransactionType} to create a permission for.
   *
   * @return A {@link TransactionTypePermission} wrapping the transaction type.
   */
  static TransactionTypePermission of(TransactionType transactionType) {
    return builder()
      .transactionType(transactionType)
      .build();
  }

  /**
   * The transaction type that this permission represents.
   *
   * @return A {@link TransactionType}.
   */
  TransactionType transactionType();

  /**
   * Get the string value of this permission for JSON serialization.
   *
   * @return The string value of the transaction type.
   */
  @Value.Derived
  default String value() {
    return transactionType().value();
  }
}


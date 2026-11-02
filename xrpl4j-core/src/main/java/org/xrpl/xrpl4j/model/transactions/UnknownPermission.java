package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2025 XRPL Foundation and its contributors
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
 * A {@link Permission} whose value is not recognized by this version of xrpl4j, i.e. it is neither a known
 * {@link TransactionType} nor a known {@link GranularPermission}. This typically happens when xrpld adds a new
 * transaction type (or granular permission) before xrpl4j has been updated to model it.
 *
 * <p>Rather than failing to deserialize an entire {@link DelegateSet} transaction (or its metadata), unrecognized
 * values are preserved verbatim in this type so that they survive a JSON round-trip, mirroring how
 * {@link UnknownTransaction} handles unrecognized transaction types.</p>
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableUnknownPermission.class)
@JsonDeserialize(as = ImmutableUnknownPermission.class)
@Beta
public interface UnknownPermission extends Permission {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableUnknownPermission.Builder}.
   */
  static ImmutableUnknownPermission.Builder builder() {
    return ImmutableUnknownPermission.builder();
  }

  /**
   * Create an {@link UnknownPermission} from a raw permission value.
   *
   * @param value The raw {@code PermissionValue} string, exactly as it appeared in JSON.
   *
   * @return An {@link UnknownPermission} wrapping the raw value.
   */
  static UnknownPermission of(String value) {
    return builder()
      .value(value)
      .build();
  }

  /**
   * The raw, unrecognized permission value, exactly as it appeared in JSON.
   *
   * @return A {@link String}.
   */
  String value();
}

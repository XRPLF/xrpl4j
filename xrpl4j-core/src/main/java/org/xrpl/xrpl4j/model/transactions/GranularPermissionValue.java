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
 * A {@link Permission} that represents a granular permission value.
 * Granular permissions allow delegation of specific portions of transactions,
 * rather than entire transaction types.
 *
 * <p>This class will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableGranularPermissionValue.class)
@JsonDeserialize(as = ImmutableGranularPermissionValue.class)
@Beta
public interface GranularPermissionValue extends Permission {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableGranularPermissionValue.Builder}.
   */
  static ImmutableGranularPermissionValue.Builder builder() {
    return ImmutableGranularPermissionValue.builder();
  }

  /**
   * Create a {@link GranularPermissionValue} from a {@link GranularPermission}.
   *
   * @param granularPermission The {@link GranularPermission} to create a permission value for.
   *
   * @return A {@link GranularPermissionValue} wrapping the granular permission.
   */
  static GranularPermissionValue of(GranularPermission granularPermission) {
    return builder()
      .granularPermission(granularPermission)
      .build();
  }

  /**
   * The granular permission that this permission value represents.
   *
   * @return A {@link GranularPermission}.
   */
  GranularPermission granularPermission();

  /**
   * Get the string value of this permission for JSON serialization.
   *
   * @return The string value of the granular permission.
   */
  @Value.Derived
  default String value() {
    return granularPermission().value();
  }
}


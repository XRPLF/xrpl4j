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
import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.jackson.modules.PermissionDeserializer;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A marker interface for permission values in the XRP Ledger. Permissions can be either
 * {@link TransactionTypePermission} (for standard transaction types) or {@link GranularPermissionValue}
 * (for granular permissions that control specific portions of transactions).
 *
 * <p>This interface will be marked {@link Beta} until the featurePermissionDelegation amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Beta
@JsonDeserialize(using = PermissionDeserializer.class)
public interface Permission {

  /**
   * Handle this {@link Permission} depending on its actual polymorphic subtype.
   *
   * @param transactionTypePermissionHandler A {@link Consumer} that is called if this instance is of type
   *                                         {@link TransactionTypePermission}.
   * @param granularPermissionValueHandler   A {@link Consumer} that is called if this instance is of type
   *                                         {@link GranularPermissionValue}.
   */
  default void handle(
    final Consumer<TransactionTypePermission> transactionTypePermissionHandler,
    final Consumer<GranularPermissionValue> granularPermissionValueHandler
  ) {
    Objects.requireNonNull(transactionTypePermissionHandler);
    Objects.requireNonNull(granularPermissionValueHandler);

    if (TransactionTypePermission.class.isAssignableFrom(this.getClass())) {
      transactionTypePermissionHandler.accept((TransactionTypePermission) this);
    } else if (GranularPermissionValue.class.isAssignableFrom(this.getClass())) {
      granularPermissionValueHandler.accept((GranularPermissionValue) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Permission Type: %s", this.getClass()));
    }
  }

  /**
   * Map this {@link Permission} to an instance of {@link R}, depending on its actual polymorphic subtype.
   *
   * @param transactionTypePermissionMapper A {@link Function} that is called if this instance is of type
   *                                        {@link TransactionTypePermission}.
   * @param granularPermissionValueMapper   A {@link Function} that is called if this instance is of type
   *                                        {@link GranularPermissionValue}.
   * @param <R>                             The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
  default <R> R map(
    final Function<TransactionTypePermission, R> transactionTypePermissionMapper,
    final Function<GranularPermissionValue, R> granularPermissionValueMapper
  ) {
    Objects.requireNonNull(transactionTypePermissionMapper);
    Objects.requireNonNull(granularPermissionValueMapper);

    if (TransactionTypePermission.class.isAssignableFrom(this.getClass())) {
      return transactionTypePermissionMapper.apply((TransactionTypePermission) this);
    } else if (GranularPermissionValue.class.isAssignableFrom(this.getClass())) {
      return granularPermissionValueMapper.apply((GranularPermissionValue) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Permission Type: %s", this.getClass()));
    }
  }
}


package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
 * Wrapper object for a {@link BatchSigner} in the {@code BatchSigners} array of a {@link Batch} transaction, to conform
 * to the XRPL transaction JSON structure.
 *
 * <p>This class will be marked {@link Beta} until the featureBatch amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBatchSignerWrapper.class)
@JsonDeserialize(as = ImmutableBatchSignerWrapper.class)
@Beta
public interface BatchSignerWrapper {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableBatchSignerWrapper.Builder}.
   */
  static ImmutableBatchSignerWrapper.Builder builder() {
    return ImmutableBatchSignerWrapper.builder();
  }

  /**
   * Construct a {@link BatchSignerWrapper} wrapping the given {@link BatchSigner}.
   *
   * @param batchSigner A {@link BatchSigner}.
   *
   * @return A {@link BatchSignerWrapper}.
   */
  static BatchSignerWrapper of(BatchSigner batchSigner) {
    return builder().batchSigner(batchSigner).build();
  }

  /**
   * The {@link BatchSigner} that this wrapper wraps.
   *
   * @return A {@link BatchSigner}.
   */
  @JsonProperty("BatchSigner")
  BatchSigner batchSigner();

}

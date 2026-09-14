package org.xrpl.xrpl4j.crypto.confidential;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import org.immutables.value.Value;

/**
 * A {@link ConfidentialMptOp} that folds a holder's confidential inbox balance into its spendable balance. Requires no
 * zero-knowledge proof — the ledger knows the exact value being moved.
 */
@Value.Immutable
public interface ConfidentialMergeInboxOp extends ConfidentialMptOp {

  /**
   * Builder for constructing {@link ConfidentialMergeInboxOp}.
   *
   * @return An {@link ImmutableConfidentialMergeInboxOp.Builder}.
   */
  static ImmutableConfidentialMergeInboxOp.Builder builder() {
    return ImmutableConfidentialMergeInboxOp.builder();
  }
}

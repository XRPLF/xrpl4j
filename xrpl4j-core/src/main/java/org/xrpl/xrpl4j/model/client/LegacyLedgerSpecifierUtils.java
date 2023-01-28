package org.xrpl.xrpl4j.model.client;

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

import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Simple utility class to help deprecate ledger index and ledger hash fields in request parameter objects.
 *
 * @deprecated This will be removed once all {@code ledgerIndex} and {@code ledgerHash} fields are removed.
 */
@Deprecated
public class LegacyLedgerSpecifierUtils {

  /**
   * Computes a {@link LedgerSpecifier} from a ledger hash and a ledger index.
   *
   * @param ledgerHash An optionally-present {@link Hash256}.
   * @param ledgerIndex A {@link LedgerIndex}.
   * @return A {@link LedgerSpecifier}.
   */
  public static LedgerSpecifier computeLedgerSpecifier(Optional<Hash256> ledgerHash, LedgerIndex ledgerIndex) {
    return ledgerHash
      .map(LedgerSpecifier::of)
      .orElseGet(() -> {
        if (ledgerIndex != null) {
          return computeLedgerSpecifierFromLedgerIndex(ledgerIndex);
        } else {
          return LedgerSpecifier.CURRENT;
        }
      });
  }

  /**
   * Computes a {@link LedgerSpecifier} that either has a {@link LedgerSpecifier#ledgerIndex()} or
   * {@link LedgerSpecifier#ledgerIndexShortcut()}.
   *
   * @param ledgerIndex A {@link LedgerIndex} potentially containing a shortcut.
   *
   * @return A {@link LedgerSpecifier} with the appropriate fields based on {@code ledgerIndex}.
   */
  public static LedgerSpecifier computeLedgerSpecifierFromLedgerIndex(LedgerIndex ledgerIndex) {
    return LedgerSpecifier.CURRENT;
  }

}

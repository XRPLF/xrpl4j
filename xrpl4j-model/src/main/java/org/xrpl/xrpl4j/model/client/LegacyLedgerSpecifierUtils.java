package org.xrpl.xrpl4j.model.client;

import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
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
      .map(LedgerSpecifier::ledgerHash)
      .orElseGet(() -> {
        if (ledgerIndex != null) {
          if (ledgerIndex.equals(LedgerIndex.VALIDATED)) {
            return LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED);
          } else if (ledgerIndex.equals(LedgerIndex.CURRENT)) {
            return LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT);
          } else if (ledgerIndex.equals(LedgerIndex.CLOSED)) {
            return LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CLOSED);
          } else {
            return LedgerSpecifier.ledgerIndex(ledgerIndex);
          }
        } else {
          return LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT);
        }
      });
  }

}

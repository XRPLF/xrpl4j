package org.xrpl.xrpl4j.model.client;

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
    if (ledgerIndex.equals(LedgerIndex.VALIDATED)) {
      return LedgerSpecifier.VALIDATED;
    } else if (ledgerIndex.equals(LedgerIndex.CURRENT)) {
      return LedgerSpecifier.CURRENT;
    } else if (ledgerIndex.equals(LedgerIndex.CLOSED)) {
      return LedgerSpecifier.CLOSED;
    } else {
      return LedgerSpecifier.of(ledgerIndex);
    }
  }

}

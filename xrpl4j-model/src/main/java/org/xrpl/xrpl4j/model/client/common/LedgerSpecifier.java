package org.xrpl.xrpl4j.model.client.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

import java.util.Optional;

/**
 * TODO: javadoc
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerSpecifier.class)
@JsonDeserialize(as = ImmutableLedgerSpecifier.class)
public interface LedgerSpecifier {

  static ImmutableLedgerSpecifier.Builder builder() {
    return ImmutableLedgerSpecifier.builder();
  }

  Optional<Hash256> ledgerHash();

  Optional<LedgerIndex> ledgerIndex();

  Optional<LedgerIndexShortcut> ledgerIndexShortcut();

  @Value.Check
  default void validateOnlyOneSpecified() {
    int numSpecified = 0;
    if (ledgerHash().isPresent()) {
      numSpecified += 1;
    }
    if (ledgerIndex().isPresent()) {
      numSpecified += 1;
    }
    if (ledgerIndexShortcut().isPresent()) {
      numSpecified += 1;
    }

    Preconditions.checkArgument(
      numSpecified == 1,
      String.format("Exactly one Ledger specifier must be provided. %s were specified", numSpecified)
    );
  }
}

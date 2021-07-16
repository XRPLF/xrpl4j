package org.xrpl.xrpl4j.model.client.specifiers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.LedgerIndex;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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

  static LedgerSpecifier ledgerHash(Hash256 ledgerHash) {
    return builder()
      .ledgerHash(ledgerHash)
      .build();
  }

  static LedgerSpecifier ledgerIndex(LedgerIndex ledgerIndex) {
    return builder()
      .ledgerIndex(ledgerIndex)
      .build();
  }

  static LedgerSpecifier ledgerIndexShortcut(LedgerIndexShortcut ledgerIndexShortcut) {
    return builder()
      .ledgerIndexShortcut(ledgerIndexShortcut)
      .build();
  }

  Optional<Hash256> ledgerHash();

  Optional<LedgerIndex> ledgerIndex();

  Optional<LedgerIndexShortcut> ledgerIndexShortcut();

  @Value.Auxiliary
  default void handle(
    final Consumer<Hash256> ledgerHashHandler,
    final Consumer<LedgerIndex> ledgerIndexHandler,
    final Consumer<LedgerIndexShortcut> ledgerIndexShortcutHandler
  ) {
    Objects.requireNonNull(ledgerHashHandler);
    Objects.requireNonNull(ledgerIndexHandler);
    Objects.requireNonNull(ledgerIndexShortcutHandler);

    ledgerHash().ifPresent(ledgerHashHandler);
    ledgerIndex().ifPresent(ledgerIndexHandler);
    ledgerIndexShortcut().ifPresent(ledgerIndexShortcutHandler);
  }

  @Value.Auxiliary
  default <R> R map(
    final Function<Hash256, R> ledgerHashMapper,
    final Function<LedgerIndex, R> ledgerIndexMapper,
    final Function<LedgerIndexShortcut, R> ledgerIndexShortcutMapper
  ) {
    Objects.requireNonNull(ledgerHashMapper);
    Objects.requireNonNull(ledgerIndexMapper);
    Objects.requireNonNull(ledgerIndexShortcutMapper);

    if (ledgerHash().isPresent()) {
      return ledgerHashMapper.apply(ledgerHash().get());
    } else if (ledgerIndex().isPresent()) {
      return ledgerIndexMapper.apply(ledgerIndex().get());
    } else if (ledgerIndexShortcut().isPresent()) {
      return ledgerIndexShortcutMapper.apply(ledgerIndexShortcut().get());
    } else {
      throw new IllegalStateException("Unsupported field.");
    }
  }

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

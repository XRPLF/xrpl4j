package org.xrpl.xrpl4j.model.client.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.jackson.modules.LedgerSpecifierDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents one of the three ways of specifying a ledger in a rippled API request.
 *
 * @see "https://xrpl.org/basic-data-types.html#specifying-ledgers"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerSpecifier.class)
@JsonDeserialize(as = ImmutableLedgerSpecifier.class, using = LedgerSpecifierDeserializer.class)
@SuppressWarnings("OverloadMethodsDeclarationOrder")
public interface LedgerSpecifier {

  /**
   * Request information about a rippled server's current working version of the ledger.
   */
  LedgerSpecifier VALIDATED = ImmutableLedgerSpecifier
    .builder()
    .ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)
    .build();

  /**
   * Request information about for the most recent ledger that has been validated by consensus.
   */
  LedgerSpecifier CURRENT = ImmutableLedgerSpecifier
    .builder()
    .ledgerIndexShortcut(LedgerIndexShortcut.CURRENT)
    .build();

  /**
   * Request information about the most recent ledger that has been closed for modifications and proposed for
   * validation.
   */
  LedgerSpecifier CLOSED = ImmutableLedgerSpecifier
    .builder()
    .ledgerIndexShortcut(LedgerIndexShortcut.CLOSED)
    .build();

  /**
   * Construct a {@link LedgerSpecifier} with a ledger hash.
   *
   * @param ledgerHash A {@link Hash256} containing the ledger hash of the desired ledger.
   *
   * @return A {@link LedgerSpecifier} containing {@code ledgerHash}.
   */
  static LedgerSpecifier ledgerHash(Hash256 ledgerHash) {
    return ImmutableLedgerSpecifier.builder()
      .ledgerHash(ledgerHash)
      .build();
  }

  /**
   * Construct a {@link LedgerSpecifier} with a numerical ledger index.
   *
   * @param ledgerIndex The {@link LedgerIndex} of the desired ledger.
   *
   * @return A {@link LedgerSpecifier} containing {@code ledgerIndex}.
   */
  static LedgerSpecifier ledgerIndex(LedgerIndex ledgerIndex) {
    return ImmutableLedgerSpecifier.builder()
      .ledgerIndex(ledgerIndex)
      .build();
  }

  /**
   * A 20-byte hex string for the ledger version to use.
   *
   * @return An optionally-present {@link Hash256}.
   */
  Optional<Hash256> ledgerHash();

  /**
   * The ledger index of the ledger to use.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  Optional<LedgerIndex> ledgerIndex();

  /**
   * A shortcut word specifying the ledger to use.
   *
   * @return An optionally-present {@link LedgerIndexShortcut}.
   */
  Optional<LedgerIndexShortcut> ledgerIndexShortcut();

  /**
   * Handle this {@link LedgerSpecifier} depending on which specifier is present.
   *
   * @param ledgerHashHandler          A {@link Consumer} that is called if {@link #ledgerHash()} is present.
   * @param ledgerIndexHandler         A {@link Consumer} that is called if {@link #ledgerIndex()} is present.
   * @param ledgerIndexShortcutHandler A {@link Consumer} that is called if {@link #ledgerIndexShortcut()}
   *                                   is present.
   */
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

  /**
   * Map this {@link LedgerSpecifier} to an instance of {@link R}, depending on which specifier is present.
   *
   * @param ledgerHashMapper          A {@link Function} that is called if {@link #ledgerHash()} is present.
   * @param ledgerIndexMapper         A {@link Function} that is called if {@link #ledgerIndex()} is present.
   * @param ledgerIndexShortcutMapper A {@link Function} that is called if {@link #ledgerIndexShortcut()}
   *                                  is present.
   * @param <R>                       The type of object to return after mapping.
   *
   * @return A {@link R} that is constructed by the appropriate mapper function.
   */
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

  /**
   * Validates that only one of the three fields in a {@link LedgerSpecifier} is present.
   */
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

package org.xrpl.xrpl4j.model.transactions.amount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.jackson.modules.MptAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.MptAmountSerializer;

import java.util.Objects;

/**
 * An {@link Amount} representing a quantity of a Multi-Purpose Token (MPT).
 *
 * <p>MPT amounts are non-negative integers on the ledger (max 2^63 - 1), but the
 * RPC may return a leading {@code -} when reporting changes in metadata. Construct via the typed static factories:
 * <pre>{@code
 *   MptAmount amount = MptAmount.of(UnsignedLong.valueOf(1_000L));
 *   MptAmount negative = MptAmount.of(UnsignedLong.valueOf(500L), true);  // e.g. from metadata
 * }</pre>
 *
 * <p>The {@link #value()} returns a decimal-integer string, optionally prefixed with
 * {@code -}. For example {@code "1000"} or {@code "-500"}.</p>
 *
 * <p>This type is {@link Beta} while the XLS-65 Single Asset Vault amendment is
 * pending mainnet activation.</p>
 */
@Immutable
@Beta
@JsonSerialize(using = MptAmountSerializer.class)
@JsonDeserialize(using = MptAmountDeserializer.class)
public interface MptAmount extends Amount {

  /**
   * Construct a non-negative {@link MptAmount}.
   *
   * @param value The MPT quantity. Must not be null.
   *
   * @return An {@link MptAmount}.
   */
  static MptAmount of(final UnsignedLong value) {
    Objects.requireNonNull(value, "value must not be null");
    return ImmutableMptAmount.builder()
      .value(value.toString())
      .build();
  }

  /**
   * Construct an {@link MptAmount} with an explicit sign, e.g. when constructing a value sourced from transaction
   * metadata where negative amounts can appear.
   *
   * @param magnitude  The absolute MPT quantity. Must not be null.
   * @param isNegative {@code true} to prefix the value with {@code -}.
   *
   * @return An {@link MptAmount}.
   */
  static MptAmount of(final UnsignedLong magnitude, final boolean isNegative) {
    Objects.requireNonNull(magnitude, "magnitude must not be null");
    return ImmutableMptAmount.builder()
      .value(isNegative ? "-" + magnitude : magnitude.toString())
      .build();
  }

  @Override
  @Derived
  @Auxiliary
  @JsonIgnore
  default boolean isNegative() {
    return Amount.super.isNegative();
  }

  /**
   * The absolute magnitude of this MPT amount as an {@link UnsignedLong} (sign stripped).
   *
   * @return An {@link UnsignedLong}.
   */
  @Auxiliary
  @JsonIgnore
  default UnsignedLong unsignedLongValue() {
    return isNegative() ? UnsignedLong.valueOf(value().substring(1)) : UnsignedLong.valueOf(value());
  }
}

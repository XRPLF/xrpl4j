package org.xrpl.xrpl4j.model.transactions.amount;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.jackson.modules.IouAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.IouAmountSerializer;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An {@link Amount} representing a quantity of an Issued Currency (IOU).
 *
 * <p>IOU values are arbitrary-precision decimals (up to 16 significant digits on
 * the ledger) and may include scientific notation as returned by the XRPL RPC. Construct via the typed static
 * factories:
 * <pre>{@code
 *   IouAmount amount = IouAmount.of(new BigDecimal("100.50"));
 *   IouAmount amount = IouAmount.of("1.23e10");   // scientific notation preserved
 * }</pre>
 *
 * <p>The {@link #value()} returns the string exactly as supplied (or as
 * {@link BigDecimal#toPlainString()} when constructed from a {@link BigDecimal}). This preserves round-trip fidelity
 * with RPC responses.</p>
 *
 * <p>This type is {@link Beta} while the XLS-65 Single Asset Vault amendment is
 * pending mainnet activation.</p>
 */
@Immutable
@Beta
@JsonSerialize(using = IouAmountSerializer.class)
@JsonDeserialize(using = IouAmountDeserializer.class)
public interface IouAmount extends Amount {

  /**
   * Construct an {@link IouAmount} from a {@link BigDecimal}.
   *
   * <p>The value is stored as a plain decimal string (no scientific notation) via
   * {@link BigDecimal#toPlainString()}.</p>
   *
   * @param value The IOU quantity. Must not be null.
   *
   * @return An {@link IouAmount}.
   */
  static IouAmount of(final BigDecimal value) {
    Objects.requireNonNull(value, "value must not be null");
    return ImmutableIouAmount.builder()
      .value(value.toPlainString())
      .build();
  }

  /**
   * Construct an {@link IouAmount} from a pre-formatted numeric string.
   *
   * <p>Scientific notation (e.g. {@code "1.23e10"}) is accepted and stored verbatim,
   * preserving the exact representation returned by the RPC. The string is validated as a parseable number at
   * construction time.</p>
   *
   * @param value A non-null, non-empty numeric string.
   *
   * @return An {@link IouAmount}.
   *
   * @throws NumberFormatException if {@code value} is not a valid decimal number.
   */
  static IouAmount of(final String value) {
    Objects.requireNonNull(value, "value must not be null");
    // Validate — will throw NumberFormatException for non-numeric input.
    new BigDecimal(value);
    return ImmutableIouAmount.builder()
      .value(value)
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
   * Returns the value as a {@link BigDecimal} for arithmetic or comparison purposes.
   *
   * @return A {@link BigDecimal} representing this IOU amount.
   */
  @Auxiliary
  @JsonIgnore
  default BigDecimal bigDecimalValue() {
    return new BigDecimal(value());
  }
}

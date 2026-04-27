package org.xrpl.xrpl4j.model.transactions.amount;

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A polymorphic amount type representing a numeric value without currency-type metadata.
 *
 * <p>Unlike {@link TokenAmount}, which bundles the currency type (XRP, IOU with currency+issuer, MPT with issuance
 * ID) together with the quantity, {@link Amount} carries only the scalar numeric value. This is the representation used
 * by Single Asset Vault (XLS-65) fields such as {@code AssetsTotal}, {@code AssetsAvailable}, {@code AssetsMaximum},
 * and {@code LossUnrealized}, whose internal type on the ledger is {@code NUMBER} — asset-type-agnostic.</p>
 *
 * <p>All three subtypes expose their quantity via {@link #value()}, which returns a
 * {@link String} for wire-format fidelity. Type safety is provided at <em>construction</em> time through typed static
 * factories on each subtype:</p>
 * <ul>
 *   <li>{@link XrpAmount#ofDrops(long)} / {@link XrpAmount#ofDrops(UnsignedLong)}</li>
 *   <li>{@link MptAmount#of(UnsignedLong)}</li>
 *   <li>{@link IouAmount#of(BigDecimal)} / {@link IouAmount#of(String)}</li>
 * </ul>
 *
 * <p><strong>JSON note:</strong> All three subtypes serialize to a plain JSON string
 * (the {@link #value()}). Because the raw JSON carries no type tag, deserialization
 * requires knowledge of the vault's {@code Asset} type at the call-site; see the
 * per-subtype Jackson modules for details (not yet wired up in this prototype).</p>
 *
 * <p>This type is {@link Beta} while the XLS-65 Single Asset Vault amendment is
 * pending mainnet activation.</p>
 */
public interface Amount {

  /**
   * The wire-format string representation of this amount. For {@link XrpAmount} and {@link MptAmount} this is a decimal
   * integer string (optionally prefixed with {@code -}); for {@link IouAmount} it may additionally contain a decimal
   * point or scientific-notation exponent.
   *
   * @return A non-null {@link String}.
   */
  String value();

  /**
   * Whether this amount is negative.
   *
   * @return {@code true} if the drop count is negative; {@code false} otherwise.
   */
  default boolean isNegative() {
    return value().startsWith("-");
  }

  /**
   * Dispatches to the appropriate handler based on the concrete subtype of this amount.
   *
   * @param xrpAmountHandler A {@link Consumer} invoked when this is an {@link XrpAmount}.
   * @param mptAmountHandler A {@link Consumer} invoked when this is an {@link MptAmount}.
   * @param iouAmountHandler A {@link Consumer} invoked when this is an {@link IouAmount}.
   */
  default void handle(final Consumer<XrpAmount> xrpAmountHandler, final Consumer<MptAmount> mptAmountHandler,
    final Consumer<IouAmount> iouAmountHandler) {
    Objects.requireNonNull(xrpAmountHandler);
    Objects.requireNonNull(mptAmountHandler);
    Objects.requireNonNull(iouAmountHandler);

    if (XrpAmount.class.isAssignableFrom(this.getClass())) {
      xrpAmountHandler.accept((XrpAmount) this);
    } else if (MptAmount.class.isAssignableFrom(this.getClass())) {
      mptAmountHandler.accept((MptAmount) this);
    } else if (IouAmount.class.isAssignableFrom(this.getClass())) {
      iouAmountHandler.accept((IouAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Amount type: %s", this.getClass()));
    }
  }

  /**
   * Maps this amount to a value of type {@link R} based on its concrete subtype.
   *
   * @param xrpAmountMapper A {@link Function} applied when this is an {@link XrpAmount}.
   * @param mptAmountMapper A {@link Function} applied when this is an {@link MptAmount}.
   * @param iouAmountMapper A {@link Function} applied when this is an {@link IouAmount}.
   * @param <R>             The return type.
   *
   * @return The result of the matching mapper function.
   */
  default <R> R map(final Function<XrpAmount, R> xrpAmountMapper, final Function<MptAmount, R> mptAmountMapper,
    final Function<IouAmount, R> iouAmountMapper) {
    Objects.requireNonNull(xrpAmountMapper);
    Objects.requireNonNull(mptAmountMapper);
    Objects.requireNonNull(iouAmountMapper);

    if (XrpAmount.class.isAssignableFrom(this.getClass())) {
      return xrpAmountMapper.apply((XrpAmount) this);
    } else if (MptAmount.class.isAssignableFrom(this.getClass())) {
      return mptAmountMapper.apply((MptAmount) this);
    } else if (IouAmount.class.isAssignableFrom(this.getClass())) {
      return iouAmountMapper.apply((IouAmount) this);
    } else {
      throw new IllegalStateException(String.format("Unsupported Amount type: %s", this.getClass()));
    }
  }
}

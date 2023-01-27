package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.ledger.OfferObject;

/**
 * A set of static {@link Flags} which can be set on {@link OfferObject}s.
 */
public class OfferFlags extends Flags {

  /**
   * Constant {@link OfferFlags} for the {@code lsfPassive} flag.
   */
  protected static final OfferFlags PASSIVE = new OfferFlags(0x00010000);

  /**
   * Constant {@link OfferFlags} for the {@code lsfSell} flag.
   */
  protected static final OfferFlags SELL = new OfferFlags(0x00020000);

  private OfferFlags(long value) {
    super(value);
  }

  /**
   * Construct {@link OfferFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link OfferFlags}.
   *
   * @return New {@link OfferFlags}.
   */
  public static OfferFlags of(long value) {
    return new OfferFlags(value);
  }

  /**
   * The object was placed as a passive offer. This has no effect on the object in the ledger.
   *
   * @return {@code true} if {@code lsfPassive} is set, otherwise {@code false}.
   */
  public boolean lsfPassive() {
    return this.isSet(PASSIVE);
  }

  /**
   * The object was placed as a sell offer. This has no effect on the object in the ledger (because tfSell only
   * matters if you get a better rate than you asked for, which cannot happen after the object enters the ledger).
   *
   * @return {@code true} if {@code lsfSell} is set, otherwise {@code false}.
   */
  public boolean lsfSell() {
    return this.isSet(SELL);
  }
}

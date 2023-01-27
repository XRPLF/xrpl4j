package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;

/**
 * A set of static {@link Flags} which can be set on {@link NfTokenOfferObject}s.
 */
public class NfTokenOfferFlags extends Flags {

  /**
   * Constant {@link NfTokenOfferFlags} for the {@code lsfBuyToken} flag.
   */
  public static final NfTokenOfferFlags BUY_TOKEN = new NfTokenOfferFlags(0x00000001);

  /**
   * Constant {@link NfTokenOfferFlags} for the {@code lsfAuthorized} flag.
   */
  public static final NfTokenOfferFlags AUTHORIZED = new NfTokenOfferFlags(0x00000002);


  private NfTokenOfferFlags(long value) {
    super(value);
  }

  /**
   * Construct {@link NfTokenOfferFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link NfTokenOfferFlags}.
   *
   * @return New {@link NfTokenOfferFlags}.
   */
  public static NfTokenOfferFlags of(long value) {
    return new NfTokenOfferFlags(value);
  }

  /**
   * Indicates the offer is a buy offer.
   *
   * @return {@code true} if {@code lsfBuyToken} is set, otherwise {@code false}.
   */
  public boolean lsfBuyToken() {
    return this.isSet(BUY_TOKEN);
  }

  /**
   * Indicates the offer has been approved by the issuer.
   *
   * @return {@code true} if {@code lsfAuthorized} is set, otherwise {@code false}.
   */
  public boolean lsfAuthorized() {
    return this.isSet(AUTHORIZED);
  }
}

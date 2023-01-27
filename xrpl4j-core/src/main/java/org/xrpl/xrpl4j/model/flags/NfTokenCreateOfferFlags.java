package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;

/**
 * A set of static {@link Flags} which can be set on {@link NfTokenCreateOffer}s.
 */
public class NfTokenCreateOfferFlags extends TransactionFlags {

  /**
   * Constant {@link NfTokenCreateOfferFlags} for the {@code tfSellNFToken} flag.
   */
  public static final NfTokenCreateOfferFlags SELL_NFTOKEN = new NfTokenCreateOfferFlags(0x00000001);

  private NfTokenCreateOfferFlags(long value) {
    super(value);
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static NfTokenCreateOfferFlags of(boolean tfFullyCanonicalSig, boolean tfSellToken) {
    return new NfTokenCreateOfferFlags(
      TransactionFlags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfSellToken ? SELL_NFTOKEN : UNSET
      ).getValue()
    );
  }

  /**
   * Construct {@link NfTokenCreateOfferFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link NfTokenCreateOfferFlags}.
   *
   * @return New {@link NfTokenCreateOfferFlags}.
   */
  public static NfTokenCreateOfferFlags of(long value) {
    return new NfTokenCreateOfferFlags(value);
  }

  /**
   * If set, indicates that the minted token may be burned by the issuer even
   * if the issuer does not currently hold the token. The current holder of
   * the token may always burn it.
   *
   * @return {@code true} if {@code tfBurnable} is set, otherwise {@code false}.
   */
  public boolean tfSellNfToken() {
    return this.isSet(SELL_NFTOKEN);
  }

  /**
   * A builder class for {@link NfTokenCreateOfferFlags}.
   */
  public static class Builder {
    boolean tfSellNfToken = false;

    /**
     * Set {@code tfSellToken} to the given value.
     *
     * @param tfSellNfToken A boolean value.
     *
     * @return The same {@link NfTokenMintFlags.Builder}.
     */
    public Builder tfSellToken(boolean tfSellNfToken) {
      this.tfSellNfToken = tfSellNfToken;
      return this;
    }

    /**
     * Build a new {@link NfTokenCreateOfferFlags} from the current boolean values.
     *
     * @return A new {@link NfTokenCreateOfferFlags}.
     */
    public NfTokenCreateOfferFlags build() {
      return NfTokenCreateOfferFlags.of(true, tfSellNfToken);
    }
  }
}

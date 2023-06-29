package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

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

  public NfTokenCreateOfferFlags() {
    super();
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
   * Construct an empty instance of {@link NfTokenCreateOfferFlags}. Transactions with empty flags will
   * not be serialized with a {@code Flags} field.
   *
   * @return An empty {@link NfTokenCreateOfferFlags}.
   */
  public static NfTokenCreateOfferFlags empty() {
    return new NfTokenCreateOfferFlags();
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

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

import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;

/**
 * A set of static {@link TransactionFlags} which can be set on {@link PaymentChannelClaim} transactions.
 */
public class PaymentChannelClaimFlags extends TransactionFlags {

  /**
   * Constant {@link PaymentChannelClaimFlags} for the {@code tfRenew} flag.
   */
  protected static final PaymentChannelClaimFlags RENEW = new PaymentChannelClaimFlags(0x00010000);

  /**
   * Constant {@link PaymentChannelClaimFlags} for the {@code tfClose} flag.
   */
  protected static final PaymentChannelClaimFlags CLOSE = new PaymentChannelClaimFlags(0x00020000);

  /**
   * Constant {@link PaymentChannelClaimFlags} for the {@code tfInnerBatchTxn} flag.
   */
  public static final PaymentChannelClaimFlags INNER_BATCH_TXN =
    new PaymentChannelClaimFlags(TransactionFlags.INNER_BATCH_TXN.getValue());

  private PaymentChannelClaimFlags(long value) {
    super(value);
  }

  private PaymentChannelClaimFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static PaymentChannelClaimFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfRenew,
    boolean tfClose,
    boolean tfInnerBatchTxn
  ) {
    return new PaymentChannelClaimFlags(
      TransactionFlags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfRenew ? RENEW : UNSET,
        tfClose ? CLOSE : UNSET,
        tfInnerBatchTxn ? INNER_BATCH_TXN : UNSET
      ).getValue()
    );
  }

  /**
   * Construct {@link PaymentChannelClaimFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link PaymentChannelClaimFlags}.
   *
   * @return New {@link PaymentChannelClaimFlags}.
   */
  public static PaymentChannelClaimFlags of(long value) {
    return new PaymentChannelClaimFlags(value);
  }

  /**
   * Construct an empty instance of {@link PaymentChannelClaimFlags}. Transactions with empty flags will not be
   * serialized with a {@code Flags} field.
   *
   * @return An empty {@link PaymentChannelClaimFlags}.
   */
  public static PaymentChannelClaimFlags empty() {
    return new PaymentChannelClaimFlags();
  }

  /**
   * Require a fully canonical transaction signature.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * Clear the {@link PayChannelObject#expiration()} time (different from {@link PayChannelObject#cancelAfter()} time.)
   * Only the source address of the payment channel can use this flag.
   *
   * @return {@code true} if {@code tfRenew} is set, otherwise {@code false}.
   */
  public boolean tfRenew() {
    return this.isSet(RENEW);
  }

  /**
   * Request to close the channel.
   *
   * <p>Only the {@link PayChannelObject#account()} and {@link PayChannelObject#destination()} addresses can use
   * this flag.</p>
   *
   * <p>This flag closes the channel immediately if it has no more XRP allocated to it after processing the
   * current claim, or if the {@link PayChannelObject#destination()} address uses it. If the source address uses this
   * flag when the channel still holds XRP, this schedules the channel to close after
   * {@link PayChannelObject#settleDelay()} seconds have passed. (Specifically, this sets the
   * {@link PayChannelObject#expiration()} of the channel to the close time of the previous ledger plus the channel's
   * {@link PayChannelObject#settleDelay()} time, unless the channel already has an earlier
   * {@link PayChannelObject#expiration()} time.)</p>
   *
   * <p>If the {@link PayChannelObject#destination()} address uses this flag when the channel still holds XRP,
   * any XRP that remains after processing the claim is returned to the source address.</p>
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfClose() {
    return this.isSet(CLOSE);
  }

  /**
   * Whether the {@code tfInnerBatchTxn} flag is set.
   *
   * @return {@code true} if {@code tfInnerBatchTxn} is set, otherwise {@code false}.
   */
  public boolean tfInnerBatchTxn() {
    return this.isSet(INNER_BATCH_TXN);
  }

  /**
   * A builder class for {@link PaymentChannelClaimFlags}.
   */
  public static class Builder {

    boolean tfRenew = false;
    boolean tfClose = false;
    boolean tfInnerBatchTxn = false;

    /**
     * Set {@code tfRenew} to the given value.
     *
     * @param tfRenew A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfRenew(boolean tfRenew) {
      this.tfRenew = tfRenew;
      return this;
    }

    /**
     * Set {@code tfClose} to the given value.
     *
     * @param tfClose A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfClose(boolean tfClose) {
      this.tfClose = tfClose;
      return this;
    }

    /**
     * Set {@code tfInnerBatchTxn} to the given value.
     *
     * @param tfInnerBatchTxn A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfInnerBatchTxn(boolean tfInnerBatchTxn) {
      this.tfInnerBatchTxn = tfInnerBatchTxn;
      return this;
    }

    /**
     * Build a new {@link PaymentChannelClaimFlags} from the current boolean values.
     *
     * @return A new {@link PaymentChannelClaimFlags}.
     */
    public PaymentChannelClaimFlags build() {
      return PaymentChannelClaimFlags.of(true, tfRenew, tfClose, tfInnerBatchTxn);
    }
  }
}

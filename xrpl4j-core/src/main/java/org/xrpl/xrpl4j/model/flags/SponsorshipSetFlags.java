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

import com.google.common.annotations.Beta;
import org.xrpl.xrpl4j.model.transactions.SponsorshipSet;

/**
 * A set of {@link TransactionFlags} which can be set on {@link SponsorshipSet} transactions.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
public class SponsorshipSetFlags extends TransactionFlags {

  /**
   * Constant for an unset flag.
   */
  protected static final SponsorshipSetFlags UNSET = new SponsorshipSetFlags(0);

  /**
   * Constant {@link SponsorshipSetFlags} for the {@code tfSponsorshipSetRequireSignForFee} flag.
   *
   * <p>If set on a {@link SponsorshipSet} transaction, the created/modified {@code Sponsorship} object will
   * require a signature from the sponsor for fee sponsorship.</p>
   */
  protected static final SponsorshipSetFlags REQUIRE_SIGN_FOR_FEE = new SponsorshipSetFlags(0x00010000L);

  /**
   * Constant {@link SponsorshipSetFlags} for the {@code tfSponsorshipSetRequireSignForReserve} flag.
   *
   * <p>If set on a {@link SponsorshipSet} transaction, the created/modified {@code Sponsorship} object will
   * require a signature from the sponsor for reserve sponsorship.</p>
   */
  protected static final SponsorshipSetFlags REQUIRE_SIGN_FOR_RESERVE = new SponsorshipSetFlags(0x00040000L);

  /**
   * Constant {@link SponsorshipSetFlags} for the {@code tfDeleteObject} flag.
   *
   * <p>If set, deletes the {@code Sponsorship} object. Any remaining XRP in {@code FeeAmount} is returned
   * to the sponsor's account upon deletion.</p>
   */
  protected static final SponsorshipSetFlags DELETE_OBJECT = new SponsorshipSetFlags(0x00100000L);

  /**
   * Constant {@link SponsorshipSetFlags} for the {@code tfInnerBatchTxn} flag.
   *
   * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
   */
  public static final SponsorshipSetFlags INNER_BATCH_TXN =
    new SponsorshipSetFlags(TransactionFlags.INNER_BATCH_TXN.getValue());

  private SponsorshipSetFlags(long value) {
    super(value);
  }

  private SponsorshipSetFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static SponsorshipSetFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfRequireSignForFee,
    boolean tfRequireSignForReserve,
    boolean tfDeleteObject,
    boolean tfInnerBatchTxn
  ) {
    return new SponsorshipSetFlags(
      Flags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfRequireSignForFee ? REQUIRE_SIGN_FOR_FEE : UNSET,
        tfRequireSignForReserve ? REQUIRE_SIGN_FOR_RESERVE : UNSET,
        tfDeleteObject ? DELETE_OBJECT : UNSET,
        tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN : UNSET
      ).getValue()
    );
  }

  /**
   * Construct {@link SponsorshipSetFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link SponsorshipSetFlags}.
   *
   * @return New {@link SponsorshipSetFlags}.
   */
  public static SponsorshipSetFlags of(long value) {
    return new SponsorshipSetFlags(value);
  }

  /**
   * Construct an empty instance of {@link SponsorshipSetFlags}. Transactions with empty flags will
   * not be serialized with a {@code Flags} field.
   *
   * @return An empty {@link SponsorshipSetFlags}.
   */
  public static SponsorshipSetFlags empty() {
    return new SponsorshipSetFlags();
  }

  /**
   * Require a fully canonical signature.
   *
   * @return {@code true} if {@code tfFullyCanonicalSig} is set, otherwise {@code false}.
   */
  public boolean tfFullyCanonicalSig() {
    return this.isSet(TransactionFlags.FULLY_CANONICAL_SIG);
  }

  /**
   * If set, the {@code Sponsorship} object will require a signature from the sponsor for fee sponsorship.
   *
   * @return {@code true} if {@code tfSponsorshipSetRequireSignForFee} is set, otherwise {@code false}.
   */
  public boolean tfRequireSignForFee() {
    return this.isSet(REQUIRE_SIGN_FOR_FEE);
  }

  /**
   * If set, the {@code Sponsorship} object will require a signature from the sponsor for reserve sponsorship.
   *
   * @return {@code true} if {@code tfSponsorshipSetRequireSignForReserve} is set, otherwise {@code false}.
   */
  public boolean tfRequireSignForReserve() {
    return this.isSet(REQUIRE_SIGN_FOR_RESERVE);
  }

  /**
   * If set, deletes the {@code Sponsorship} object.
   *
   * @return {@code true} if {@code tfDeleteObject} is set, otherwise {@code false}.
   */
  public boolean tfDeleteObject() {
    return this.isSet(DELETE_OBJECT);
  }

  /**
   * Indicates that this transaction is an inner transaction of a Batch transaction.
   *
   * @return {@code true} if {@code tfInnerBatchTxn} is set, otherwise {@code false}.
   *
   * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0056-batch"
   */
  public boolean tfInnerBatchTxn() {
    return this.isSet(SponsorshipSetFlags.INNER_BATCH_TXN);
  }

  /**
   * A builder class for {@link SponsorshipSetFlags}.
   */
  public static class Builder {

    private boolean tfRequireSignForFee = false;
    private boolean tfRequireSignForReserve = false;
    private boolean tfDeleteObject = false;
    private boolean tfInnerBatchTxn = false;

    /**
     * Set {@code tfRequireSignForFee} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfRequireSignForFee() {
      this.tfRequireSignForFee = true;
      return this;
    }

    /**
     * Set {@code tfRequireSignForReserve} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfRequireSignForReserve() {
      this.tfRequireSignForReserve = true;
      return this;
    }

    /**
     * Set {@code tfDeleteObject} to {@code true}.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfDeleteObject() {
      this.tfDeleteObject = true;
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
     * Build a new {@link SponsorshipSetFlags} from the current boolean values.
     *
     * @return A new {@link SponsorshipSetFlags}.
     */
    public SponsorshipSetFlags build() {
      return SponsorshipSetFlags.of(
        true,
        tfRequireSignForFee,
        tfRequireSignForReserve,
        tfDeleteObject,
        tfInnerBatchTxn
      );
    }
  }
}

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

import org.xrpl.xrpl4j.model.transactions.NfTokenMint;

/**
 * A set of static {@link TransactionFlags} which can be set on
 * {@link NfTokenMint} transactions.
 */
@SuppressWarnings("abbreviationaswordinname")
public class NfTokenMintFlags extends TransactionFlags {

  /**
   * Constant {@link NfTokenMintFlags} for the {@code tfBurnable} flag.
   */
  protected static final NfTokenMintFlags BURNABLE = new NfTokenMintFlags(0x00000001);

  /**
   * Constant {@link NfTokenMintFlags} for the {@code tfOnlyXRP} flag.
   */
  protected static final NfTokenMintFlags ONLY_XRP = new NfTokenMintFlags(0x00000002);

  /**
   * Constant {@link NfTokenMintFlags} for the {@code tfTrustLine} flag.
   */
  protected static final NfTokenMintFlags TRUSTLINE = new NfTokenMintFlags(0x00000004);

  /**
   * Constant {@link NfTokenMintFlags} for the {@code tfTransferable} flag.
   */
  protected static final NfTokenMintFlags TRANSFERABLE = new NfTokenMintFlags(0x00000008);

  /**
   * Constant {@link NfTokenMintFlags} for the {@code tfInnerBatchTxn} flag.
   */
  public static final NfTokenMintFlags INNER_BATCH_TXN =
    new NfTokenMintFlags(TransactionFlags.INNER_BATCH_TXN.getValue());

  private NfTokenMintFlags(long value) {
    super(value);
  }

  private NfTokenMintFlags() {
  }

  /**
   * Create a new {@link Builder}.
   *
   * @return A new {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  private static NfTokenMintFlags of(
    boolean tfFullyCanonicalSig,
    boolean tfBurnable,
    boolean tfOnlyXRP,
    boolean tfTrustLine,
    boolean tfTransferable,
    boolean tfInnerBatchTxn
  ) {
    return new NfTokenMintFlags(
      TransactionFlags.of(
        tfFullyCanonicalSig ? TransactionFlags.FULLY_CANONICAL_SIG : UNSET,
        tfBurnable ? BURNABLE : UNSET,
        tfOnlyXRP ? ONLY_XRP : UNSET,
        tfTrustLine ? TRUSTLINE : UNSET,
        tfTransferable ? TRANSFERABLE : UNSET,
        tfInnerBatchTxn ? INNER_BATCH_TXN : UNSET
      ).getValue()
    );
  }

  /**
   * Construct {@link NfTokenMintFlags} with a given value.
   *
   * @param value The long-number encoded flags value of this {@link NfTokenMintFlags}.
   *
   * @return New {@link NfTokenMintFlags}.
   */
  public static NfTokenMintFlags of(long value) {
    return new NfTokenMintFlags(value);
  }

  /**
   * Construct an empty instance of {@link NfTokenMintFlags}. Transactions with empty flags will
   * not be serialized with a {@code Flags} field.
   *
   * @return An empty {@link NfTokenMintFlags}.
   */
  public static NfTokenMintFlags empty() {
    return new NfTokenMintFlags();
  }

  /**
   * If set, indicates that the minted token may be burned by the issuer even
   * if the issuer does not currently hold the token. The current holder of
   * the token may always burn it.
   *
   * @return {@code true} if {@code tfBurnable} is set, otherwise {@code false}.
   */
  public boolean tfBurnable() {
    return this.isSet(BURNABLE);
  }

  /**
   * If set, indicates that the token may only be offered or sold for XRP.
   *
   * @return {@code true} if {@code tfOnlyXRP} is set, otherwise {@code false}.
   */
  public boolean tfOnlyXRP() {
    return this.isSet(ONLY_XRP);
  }

  /**
   * If set, indicates that the issuer wants a trustline to be automatically created.
   *
   * @return {@code true} if {@code tfTrustLine} is set, otherwise {@code false}.
   */
  public boolean tfTrustLine() {
    return this.isSet(TRUSTLINE);
  }

  /**
   * If set, indicates that this NfT can be transferred. This flag has no
   * effect if the token is being transferred from the issuer or to the
   * issuer.
   *
   * @return {@code true} if {@code tfTransferable} is set, otherwise {@code false}.
   */
  public boolean tfTransferable() {
    return this.isSet(TRANSFERABLE);
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
   * A builder class for {@link NfTokenMintFlags}.
   */
  public static class Builder {
    boolean tfBurnable = false;
    boolean tfOnlyXRP = false;
    boolean tfTrustLine = false;
    boolean tfTransferable = false;
    boolean tfInnerBatchTxn = false;

    /**
     * Set {@code tfBurnable} to the given value.
     *
     * @param tfBurnable A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfBurnable(boolean tfBurnable) {
      this.tfBurnable = tfBurnable;
      return this;
    }

    /**
     * Set {@code tfOnlyXRP} to the given value.
     *
     * @param tfOnlyXRP A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfOnlyXRP(boolean tfOnlyXRP) {
      this.tfOnlyXRP = tfOnlyXRP;
      return this;
    }

    /**
     * Set {@code tfTrustLine} to the given value.
     *
     * @param tfTrustLine A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfTrustLine(boolean tfTrustLine) {
      this.tfTrustLine = tfTrustLine;
      return this;
    }

    /**
     * Set {@code tfTransferable} to the given value.
     *
     * @param tfTransferable A boolean value.
     *
     * @return The same {@link Builder}.
     */
    public Builder tfTransferable(boolean tfTransferable) {
      this.tfTransferable = tfTransferable;
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
     * Build a new {@link NfTokenMintFlags} from the current boolean values.
     *
     * @return A new {@link NfTokenMintFlags}.
     */
    public NfTokenMintFlags build() {
      return NfTokenMintFlags.of(true, tfBurnable, tfOnlyXRP, tfTrustLine, tfTransferable, tfInnerBatchTxn);
    }
  }
}

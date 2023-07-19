package org.xrpl.xrpl4j.model.flags;

import org.xrpl.xrpl4j.model.transactions.AmmDeposit;

/**
 * A set of {@link TransactionFlags} that can be set on {@link AmmDeposit} transactions. Exactly
 * one flag must be set on each {@link AmmDeposit} transaction, so this class does not allow for combination
 * of multiple flags.
 *
 * <p>While most other TransactionFlags support empty flags or 0, AmmDeposit transactions must have a Flags field
 * to denote the deposit mode. Therefore, AmmDepositFlags does not support empty or unset flags.
 * </p>
 */
public class AmmDepositFlags extends TransactionFlags {

  /**
   * Constant {@link AmmDepositFlags} for the {@code tfLPToken} flag.
   */
  public static final AmmDepositFlags LP_TOKEN = new AmmDepositFlags(0x00010000);

  /**
   * Constant {@link AmmDepositFlags} for the {@code tfSingleAsset} flag.
   */
  public static final AmmDepositFlags SINGLE_ASSET = new AmmDepositFlags(0x00080000);

  /**
   * Constant {@link AmmDepositFlags} for the {@code tfTwoAsset} flag.
   */
  public static final AmmDepositFlags TWO_ASSET = new AmmDepositFlags(0x00100000);

  /**
   * Constant {@link AmmDepositFlags} for the {@code tfOneAssetLPToken} flag.
   */
  public static final AmmDepositFlags ONE_ASSET_LP_TOKEN = new AmmDepositFlags(0x00200000);

  /**
   * Constant {@link AmmDepositFlags} for the {@code tfLimitLPToken} flag.
   */
  public static final AmmDepositFlags LIMIT_LP_TOKEN = new AmmDepositFlags(0x00400000);

  private AmmDepositFlags(long value) {
    super(value);
  }

  /**
   * Whether the {@code tfLPToken} flag is set.
   *
   * @return {@code true} if {@code tfLPToken} is set, otherwise {@code false}.
   */
  public boolean tfLpToken() {
    return this.isSet(LP_TOKEN);
  }

  /**
   * Whether the {@code tfSingleAsset} flag is set.
   *
   * @return {@code true} if {@code tfSingleAsset} is set, otherwise {@code false}.
   */
  public boolean tfSingleAsset() {
    return this.isSet(SINGLE_ASSET);
  }

  /**
   * Whether the {@code tfTwoAsset} flag is set.
   *
   * @return {@code true} if {@code tfTwoAsset} is set, otherwise {@code false}.
   */
  public boolean tfTwoAsset() {
    return this.isSet(TWO_ASSET);
  }

  /**
   * Whether the {@code tfOneAssetLPToken} flag is set.
   *
   * @return {@code true} if {@code tfOneAssetLPToken} is set, otherwise {@code false}.
   */
  public boolean tfOneAssetLpToken() {
    return this.isSet(ONE_ASSET_LP_TOKEN);
  }

  /**
   * Whether the {@code tfLimitLPToken} flag is set.
   *
   * @return {@code true} if {@code tfLimitLPToken} is set, otherwise {@code false}.
   */
  public boolean tfLimitLpToken() {
    return this.isSet(LIMIT_LP_TOKEN);
  }

}
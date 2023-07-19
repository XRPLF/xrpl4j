package org.xrpl.xrpl4j.model.flags;

/**
 * A set of {@link TransactionFlags} that can be set on {@link org.xrpl.xrpl4j.model.transactions.AmmWithdraw}
 * transactions. Exactly one flag must be set on each {@link org.xrpl.xrpl4j.model.transactions.AmmWithdraw}
 * transaction, so this class does not allow for combination of multiple flags.
 *
 * <p>While most other TransactionFlags support empty flags or 0, AmmWithdraw transactions must have a Flags field
 * to denote the withdraw mode. Therefore, AmmWithdrawFlags does not support empty or unset flags.
 * </p>
 */
public class AmmWithdrawFlags extends TransactionFlags {

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfLPToken} flag.
   */
  public static final AmmWithdrawFlags LP_TOKEN = new AmmWithdrawFlags(0x00010000);

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfWithdrawAll} flag.
   */
  public static final AmmWithdrawFlags WITHDRAW_ALL = new AmmWithdrawFlags(0x00020000);

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfOneAssetWithdrawAll} flag.
   */
  public static final AmmWithdrawFlags ONE_ASSET_WITHDRAW_ALL = new AmmWithdrawFlags(0x00040000);

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfSingleAsset} flag.
   */
  public static final AmmWithdrawFlags SINGLE_ASSET = new AmmWithdrawFlags(0x00080000);

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfTwoAsset} flag.
   */
  public static final AmmWithdrawFlags TWO_ASSET = new AmmWithdrawFlags(0x00100000);

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfOneAssetLPToken} flag.
   */
  public static final AmmWithdrawFlags ONE_ASSET_LP_TOKEN = new AmmWithdrawFlags(0x00200000);

  /**
   * Constant {@link AmmWithdrawFlags} for the {@code tfLimitLPToken} flag.
   */
  public static final AmmWithdrawFlags LIMIT_LP_TOKEN = new AmmWithdrawFlags(0x00400000);

  private AmmWithdrawFlags(long value) {
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
   * Whether the {@code tfWithdrawAll} flag is set.
   *
   * @return {@code true} if {@code tfWithdrawAll} is set, otherwise {@code false}.
   */
  public boolean tfWithdrawAll() {
    return this.isSet(WITHDRAW_ALL);
  }

  /**
   * Whether the {@code tfOneAssetWithdrawAll} flag is set.
   *
   * @return {@code true} if {@code tfOneAssetWithdrawAll} is set, otherwise {@code false}.
   */
  public boolean tfOneAssetWithdrawAll() {
    return this.isSet(ONE_ASSET_WITHDRAW_ALL);
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

package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class AmmDepositFlagsTest {

  @Test
  void testFlagValues() {
    AmmDepositFlags lpToken = AmmDepositFlags.LP_TOKEN;
    assertThat(lpToken.tfLpToken()).isTrue();
    assertThat(lpToken.tfSingleAsset()).isFalse();
    assertThat(lpToken.tfTwoAsset()).isFalse();
    assertThat(lpToken.tfOneAssetLpToken()).isFalse();
    assertThat(lpToken.tfLimitLpToken()).isFalse();
    assertThat(lpToken.tfTwoAssetIfEmpty()).isFalse();
    assertThat(lpToken.tfInnerBatchTxn()).isFalse();

    AmmDepositFlags singleAsset = AmmDepositFlags.SINGLE_ASSET;
    assertThat(singleAsset.tfLpToken()).isFalse();
    assertThat(singleAsset.tfSingleAsset()).isTrue();
    assertThat(singleAsset.tfTwoAsset()).isFalse();
    assertThat(singleAsset.tfOneAssetLpToken()).isFalse();
    assertThat(singleAsset.tfLimitLpToken()).isFalse();
    assertThat(singleAsset.tfTwoAssetIfEmpty()).isFalse();
    assertThat(singleAsset.tfInnerBatchTxn()).isFalse();

    AmmDepositFlags twoAsset = AmmDepositFlags.TWO_ASSET;
    assertThat(twoAsset.tfLpToken()).isFalse();
    assertThat(twoAsset.tfSingleAsset()).isFalse();
    assertThat(twoAsset.tfTwoAsset()).isTrue();
    assertThat(twoAsset.tfOneAssetLpToken()).isFalse();
    assertThat(twoAsset.tfLimitLpToken()).isFalse();
    assertThat(twoAsset.tfTwoAssetIfEmpty()).isFalse();
    assertThat(twoAsset.tfInnerBatchTxn()).isFalse();

    AmmDepositFlags oneAssetLpToken = AmmDepositFlags.ONE_ASSET_LP_TOKEN;
    assertThat(oneAssetLpToken.tfLpToken()).isFalse();
    assertThat(oneAssetLpToken.tfSingleAsset()).isFalse();
    assertThat(oneAssetLpToken.tfTwoAsset()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetLpToken()).isTrue();
    assertThat(oneAssetLpToken.tfLimitLpToken()).isFalse();
    assertThat(oneAssetLpToken.tfTwoAssetIfEmpty()).isFalse();
    assertThat(oneAssetLpToken.tfInnerBatchTxn()).isFalse();

    AmmDepositFlags limitLpToken = AmmDepositFlags.LIMIT_LP_TOKEN;
    assertThat(limitLpToken.tfLpToken()).isFalse();
    assertThat(limitLpToken.tfSingleAsset()).isFalse();
    assertThat(limitLpToken.tfTwoAsset()).isFalse();
    assertThat(limitLpToken.tfOneAssetLpToken()).isFalse();
    assertThat(limitLpToken.tfLimitLpToken()).isTrue();
    assertThat(limitLpToken.tfTwoAssetIfEmpty()).isFalse();
    assertThat(limitLpToken.tfInnerBatchTxn()).isFalse();

    AmmDepositFlags twoAssetIfEmpty = AmmDepositFlags.TWO_ASSET_IF_EMPTY;
    assertThat(twoAssetIfEmpty.tfLpToken()).isFalse();
    assertThat(twoAssetIfEmpty.tfSingleAsset()).isFalse();
    assertThat(twoAssetIfEmpty.tfTwoAsset()).isFalse();
    assertThat(twoAssetIfEmpty.tfOneAssetLpToken()).isFalse();
    assertThat(twoAssetIfEmpty.tfLimitLpToken()).isFalse();
    assertThat(twoAssetIfEmpty.tfTwoAssetIfEmpty()).isTrue();
    assertThat(twoAssetIfEmpty.tfInnerBatchTxn()).isFalse();
  }

  @Test
  void testInnerBatchTxn() {
    AmmDepositFlags flags = AmmDepositFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfLpToken()).isFalse();
    assertThat(flags.tfSingleAsset()).isFalse();
    assertThat(flags.tfTwoAsset()).isFalse();
    assertThat(flags.tfOneAssetLpToken()).isFalse();
    assertThat(flags.tfLimitLpToken()).isFalse();
    assertThat(flags.tfTwoAssetIfEmpty()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }
}

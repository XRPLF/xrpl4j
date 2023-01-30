package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class AmmWithdrawFlagsTest {

  @Test
  void testFlagValues() {
    AmmWithdrawFlags lpToken = AmmWithdrawFlags.LP_TOKEN;
    assertThat(lpToken.tfLpToken()).isTrue();
    assertThat(lpToken.tfWithdrawAll()).isFalse();
    assertThat(lpToken.tfOneAssetWithdrawAll()).isFalse();
    assertThat(lpToken.tfSingleAsset()).isFalse();
    assertThat(lpToken.tfTwoAsset()).isFalse();
    assertThat(lpToken.tfOneAssetLpToken()).isFalse();
    assertThat(lpToken.tfLimitLpToken()).isFalse();

    AmmWithdrawFlags withdrawAll = AmmWithdrawFlags.WITHDRAW_ALL;
    assertThat(withdrawAll.tfLpToken()).isFalse();
    assertThat(withdrawAll.tfWithdrawAll()).isTrue();
    assertThat(withdrawAll.tfOneAssetWithdrawAll()).isFalse();
    assertThat(withdrawAll.tfSingleAsset()).isFalse();
    assertThat(withdrawAll.tfTwoAsset()).isFalse();
    assertThat(withdrawAll.tfOneAssetLpToken()).isFalse();
    assertThat(withdrawAll.tfLimitLpToken()).isFalse();

    AmmWithdrawFlags oneAssetWithdrawAll = AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL;
    assertThat(oneAssetWithdrawAll.tfLpToken()).isFalse();
    assertThat(oneAssetWithdrawAll.tfWithdrawAll()).isFalse();
    assertThat(oneAssetWithdrawAll.tfOneAssetWithdrawAll()).isTrue();
    assertThat(oneAssetWithdrawAll.tfSingleAsset()).isFalse();
    assertThat(oneAssetWithdrawAll.tfTwoAsset()).isFalse();
    assertThat(oneAssetWithdrawAll.tfOneAssetLpToken()).isFalse();
    assertThat(oneAssetWithdrawAll.tfLimitLpToken()).isFalse();

    AmmWithdrawFlags singleAsset = AmmWithdrawFlags.SINGLE_ASSET;
    assertThat(singleAsset.tfLpToken()).isFalse();
    assertThat(singleAsset.tfWithdrawAll()).isFalse();
    assertThat(singleAsset.tfOneAssetWithdrawAll()).isFalse();
    assertThat(singleAsset.tfSingleAsset()).isTrue();
    assertThat(singleAsset.tfTwoAsset()).isFalse();
    assertThat(singleAsset.tfOneAssetLpToken()).isFalse();
    assertThat(singleAsset.tfLimitLpToken()).isFalse();

    AmmWithdrawFlags twoAsset = AmmWithdrawFlags.TWO_ASSET;
    assertThat(twoAsset.tfLpToken()).isFalse();
    assertThat(twoAsset.tfWithdrawAll()).isFalse();
    assertThat(twoAsset.tfOneAssetWithdrawAll()).isFalse();
    assertThat(twoAsset.tfSingleAsset()).isFalse();
    assertThat(twoAsset.tfTwoAsset()).isTrue();
    assertThat(twoAsset.tfOneAssetLpToken()).isFalse();
    assertThat(twoAsset.tfLimitLpToken()).isFalse();

    AmmWithdrawFlags oneAssetLpToken = AmmWithdrawFlags.ONE_ASSET_LP_TOKEN;
    assertThat(oneAssetLpToken.tfLpToken()).isFalse();
    assertThat(oneAssetLpToken.tfWithdrawAll()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetWithdrawAll()).isFalse();
    assertThat(oneAssetLpToken.tfSingleAsset()).isFalse();
    assertThat(oneAssetLpToken.tfTwoAsset()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetLpToken()).isTrue();
    assertThat(oneAssetLpToken.tfLimitLpToken()).isFalse();

    AmmWithdrawFlags limitLpToken = AmmWithdrawFlags.LIMIT_LP_TOKEN;
    assertThat(limitLpToken.tfLpToken()).isFalse();
    assertThat(limitLpToken.tfWithdrawAll()).isFalse();
    assertThat(limitLpToken.tfOneAssetWithdrawAll()).isFalse();
    assertThat(limitLpToken.tfSingleAsset()).isFalse();
    assertThat(limitLpToken.tfTwoAsset()).isFalse();
    assertThat(limitLpToken.tfOneAssetLpToken()).isFalse();
    assertThat(limitLpToken.tfLimitLpToken()).isTrue();

  }
}

package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class AmmWithdrawFlagsTest {

  @Test
  void testFlagValues() {
    Flags.AmmWithdrawFlags lpToken = Flags.AmmWithdrawFlags.LP_TOKEN;
    assertThat(lpToken.tfLPToken()).isTrue();
    assertThat(lpToken.tfWithdrawAll()).isFalse();
    assertThat(lpToken.tfOneAssetWithdrawAll()).isFalse();
    assertThat(lpToken.tfSingleAsset()).isFalse();
    assertThat(lpToken.tfTwoAsset()).isFalse();
    assertThat(lpToken.tfOneAssetLPToken()).isFalse();
    assertThat(lpToken.tfLimitLPToken()).isFalse();

    Flags.AmmWithdrawFlags withdrawAll = Flags.AmmWithdrawFlags.WITHDRAW_ALL;
    assertThat(withdrawAll.tfLPToken()).isFalse();
    assertThat(withdrawAll.tfWithdrawAll()).isTrue();
    assertThat(withdrawAll.tfOneAssetWithdrawAll()).isFalse();
    assertThat(withdrawAll.tfSingleAsset()).isFalse();
    assertThat(withdrawAll.tfTwoAsset()).isFalse();
    assertThat(withdrawAll.tfOneAssetLPToken()).isFalse();
    assertThat(withdrawAll.tfLimitLPToken()).isFalse();

    Flags.AmmWithdrawFlags oneAssetWithdrawAll = Flags.AmmWithdrawFlags.ONE_ASSET_WITHDRAW_ALL;
    assertThat(oneAssetWithdrawAll.tfLPToken()).isFalse();
    assertThat(oneAssetWithdrawAll.tfWithdrawAll()).isFalse();
    assertThat(oneAssetWithdrawAll.tfOneAssetWithdrawAll()).isTrue();
    assertThat(oneAssetWithdrawAll.tfSingleAsset()).isFalse();
    assertThat(oneAssetWithdrawAll.tfTwoAsset()).isFalse();
    assertThat(oneAssetWithdrawAll.tfOneAssetLPToken()).isFalse();
    assertThat(oneAssetWithdrawAll.tfLimitLPToken()).isFalse();

    Flags.AmmWithdrawFlags singleAsset = Flags.AmmWithdrawFlags.SINGLE_ASSET;
    assertThat(singleAsset.tfLPToken()).isFalse();
    assertThat(singleAsset.tfWithdrawAll()).isFalse();
    assertThat(singleAsset.tfOneAssetWithdrawAll()).isFalse();
    assertThat(singleAsset.tfSingleAsset()).isTrue();
    assertThat(singleAsset.tfTwoAsset()).isFalse();
    assertThat(singleAsset.tfOneAssetLPToken()).isFalse();
    assertThat(singleAsset.tfLimitLPToken()).isFalse();

    Flags.AmmWithdrawFlags twoAsset = Flags.AmmWithdrawFlags.TWO_ASSET;
    assertThat(twoAsset.tfLPToken()).isFalse();
    assertThat(twoAsset.tfWithdrawAll()).isFalse();
    assertThat(twoAsset.tfOneAssetWithdrawAll()).isFalse();
    assertThat(twoAsset.tfSingleAsset()).isFalse();
    assertThat(twoAsset.tfTwoAsset()).isTrue();
    assertThat(twoAsset.tfOneAssetLPToken()).isFalse();
    assertThat(twoAsset.tfLimitLPToken()).isFalse();

    Flags.AmmWithdrawFlags oneAssetLpToken = Flags.AmmWithdrawFlags.ONE_ASSET_LP_TOKEN;
    assertThat(oneAssetLpToken.tfLPToken()).isFalse();
    assertThat(oneAssetLpToken.tfWithdrawAll()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetWithdrawAll()).isFalse();
    assertThat(oneAssetLpToken.tfSingleAsset()).isFalse();
    assertThat(oneAssetLpToken.tfTwoAsset()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetLPToken()).isTrue();
    assertThat(oneAssetLpToken.tfLimitLPToken()).isFalse();

    Flags.AmmWithdrawFlags limitLpToken = Flags.AmmWithdrawFlags.LIMIT_LP_TOKEN;
    assertThat(limitLpToken.tfLPToken()).isFalse();
    assertThat(limitLpToken.tfWithdrawAll()).isFalse();
    assertThat(limitLpToken.tfOneAssetWithdrawAll()).isFalse();
    assertThat(limitLpToken.tfSingleAsset()).isFalse();
    assertThat(limitLpToken.tfTwoAsset()).isFalse();
    assertThat(limitLpToken.tfOneAssetLPToken()).isFalse();
    assertThat(limitLpToken.tfLimitLPToken()).isTrue();

  }
}

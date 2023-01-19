package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class AmmDepositFlagsTest {

  @Test
  void testFlagValues() {
    Flags.AmmDepositFlags lpToken = Flags.AmmDepositFlags.LP_TOKEN;
    assertThat(lpToken.tfLpToken()).isTrue();
    assertThat(lpToken.tfSingleAsset()).isFalse();
    assertThat(lpToken.tfTwoAsset()).isFalse();
    assertThat(lpToken.tfOneAssetLpToken()).isFalse();
    assertThat(lpToken.tfLimitLpToken()).isFalse();

    Flags.AmmDepositFlags singleAsset = Flags.AmmDepositFlags.SINGLE_ASSET;
    assertThat(singleAsset.tfLpToken()).isFalse();
    assertThat(singleAsset.tfSingleAsset()).isTrue();
    assertThat(singleAsset.tfTwoAsset()).isFalse();
    assertThat(singleAsset.tfOneAssetLpToken()).isFalse();
    assertThat(singleAsset.tfLimitLpToken()).isFalse();

    Flags.AmmDepositFlags twoAsset = Flags.AmmDepositFlags.TWO_ASSET;
    assertThat(twoAsset.tfLpToken()).isFalse();
    assertThat(twoAsset.tfSingleAsset()).isFalse();
    assertThat(twoAsset.tfTwoAsset()).isTrue();
    assertThat(twoAsset.tfOneAssetLpToken()).isFalse();
    assertThat(twoAsset.tfLimitLpToken()).isFalse();

    Flags.AmmDepositFlags oneAssetLpToken = Flags.AmmDepositFlags.ONE_ASSET_LP_TOKEN;
    assertThat(oneAssetLpToken.tfLpToken()).isFalse();
    assertThat(oneAssetLpToken.tfSingleAsset()).isFalse();
    assertThat(oneAssetLpToken.tfTwoAsset()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetLpToken()).isTrue();
    assertThat(oneAssetLpToken.tfLimitLpToken()).isFalse();

    Flags.AmmDepositFlags limitLpToken = Flags.AmmDepositFlags.LIMIT_LP_TOKEN;
    assertThat(limitLpToken.tfLpToken()).isFalse();
    assertThat(limitLpToken.tfSingleAsset()).isFalse();
    assertThat(limitLpToken.tfTwoAsset()).isFalse();
    assertThat(limitLpToken.tfOneAssetLpToken()).isFalse();
    assertThat(limitLpToken.tfLimitLpToken()).isTrue();
  }
}

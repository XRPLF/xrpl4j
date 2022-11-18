package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class AmmDepositFlagsTest {

  @Test
  void testFlagValues() {
    Flags.AmmDepositFlags lpToken = Flags.AmmDepositFlags.LP_TOKEN;
    assertThat(lpToken.tfLPToken()).isTrue();
    assertThat(lpToken.tfSingleAsset()).isFalse();
    assertThat(lpToken.tfTwoAsset()).isFalse();
    assertThat(lpToken.tfOneAssetLPToken()).isFalse();
    assertThat(lpToken.tfLimitLPToken()).isFalse();

    Flags.AmmDepositFlags singleAsset = Flags.AmmDepositFlags.SINGLE_ASSET;
    assertThat(singleAsset.tfLPToken()).isFalse();
    assertThat(singleAsset.tfSingleAsset()).isTrue();
    assertThat(singleAsset.tfTwoAsset()).isFalse();
    assertThat(singleAsset.tfOneAssetLPToken()).isFalse();
    assertThat(singleAsset.tfLimitLPToken()).isFalse();

    Flags.AmmDepositFlags twoAsset = Flags.AmmDepositFlags.TWO_ASSET;
    assertThat(twoAsset.tfLPToken()).isFalse();
    assertThat(twoAsset.tfSingleAsset()).isFalse();
    assertThat(twoAsset.tfTwoAsset()).isTrue();
    assertThat(twoAsset.tfOneAssetLPToken()).isFalse();
    assertThat(twoAsset.tfLimitLPToken()).isFalse();

    Flags.AmmDepositFlags oneAssetLpToken = Flags.AmmDepositFlags.ONE_ASSET_LP_TOKEN;
    assertThat(oneAssetLpToken.tfLPToken()).isFalse();
    assertThat(oneAssetLpToken.tfSingleAsset()).isFalse();
    assertThat(oneAssetLpToken.tfTwoAsset()).isFalse();
    assertThat(oneAssetLpToken.tfOneAssetLPToken()).isTrue();
    assertThat(oneAssetLpToken.tfLimitLPToken()).isFalse();

    Flags.AmmDepositFlags limitLpToken = Flags.AmmDepositFlags.LIMIT_LP_TOKEN;
    assertThat(limitLpToken.tfLPToken()).isFalse();
    assertThat(limitLpToken.tfSingleAsset()).isFalse();
    assertThat(limitLpToken.tfTwoAsset()).isFalse();
    assertThat(limitLpToken.tfOneAssetLPToken()).isFalse();
    assertThat(limitLpToken.tfLimitLPToken()).isTrue();
  }
}

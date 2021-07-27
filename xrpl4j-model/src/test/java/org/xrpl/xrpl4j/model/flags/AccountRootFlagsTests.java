package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AccountRootFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(9);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfDefaultRipple,
    boolean lsfDepositAuth,
    boolean lsfDisableMaster,
    boolean lsfDisallowXrp,
    boolean lsfGlobalFreeze,
    boolean lsfNoFreeze,
    boolean lsfPasswordSpent,
    boolean lsfRequireAuth,
    boolean lsfRequireDestTag
  ) {
    long expectedFlags = (lsfDefaultRipple ? Flags.AccountRootFlags.DEFAULT_RIPPLE.getValue() : 0L) |
      (lsfDepositAuth ? Flags.AccountRootFlags.DEPOSIT_AUTH.getValue() : 0L) |
      (lsfDisableMaster ? Flags.AccountRootFlags.DISABLE_MASTER.getValue() : 0L) |
      (lsfDisallowXrp ? Flags.AccountRootFlags.DISALLOW_XRP.getValue() : 0L) |
      (lsfGlobalFreeze ? Flags.AccountRootFlags.GLOBAL_FREEZE.getValue() : 0L) |
      (lsfNoFreeze ? Flags.AccountRootFlags.NO_FREEZE.getValue() : 0L) |
      (lsfPasswordSpent ? Flags.AccountRootFlags.PASSWORD_SPENT.getValue() : 0L) |
      (lsfRequireAuth ? Flags.AccountRootFlags.REQUIRE_AUTH.getValue() : 0L) |
      (lsfRequireDestTag ? Flags.AccountRootFlags.REQUIRE_DEST_TAG.getValue() : 0L);
    Flags.AccountRootFlags flags = Flags.AccountRootFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);

    assertThat(flags.lsfDefaultRipple()).isEqualTo(lsfDefaultRipple);
    assertThat(flags.lsfDepositAuth()).isEqualTo(lsfDepositAuth);
    assertThat(flags.lsfDisableMaster()).isEqualTo(lsfDisableMaster);
    assertThat(flags.lsfDisallowXrp()).isEqualTo(lsfDisallowXrp);
    assertThat(flags.lsfGlobalFreeze()).isEqualTo(lsfGlobalFreeze);
    assertThat(flags.lsfNoFreeze()).isEqualTo(lsfNoFreeze);
    assertThat(flags.lsfPasswordSpent()).isEqualTo(lsfPasswordSpent);
    assertThat(flags.lsfRequireAuth()).isEqualTo(lsfRequireAuth);
    assertThat(flags.lsfRequireDestTag()).isEqualTo(lsfRequireDestTag);
  }
}

package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class AccountRootFlagsTests extends AbstractFlagsTest {

  boolean lsfDefaultRipple;
  boolean lsfDepositAuth;
  boolean lsfDisableMaster;
  boolean lsfDisallowXrp;
  boolean lsfGlobalFreeze;
  boolean lsfNoFreeze;
  boolean lsfPasswordSpent;
  boolean lsfRequireAuth;
  boolean lsfRequireDestTag;

  long expectedFlags;

  public AccountRootFlagsTests(
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
    this.lsfDefaultRipple = lsfDefaultRipple;
    this.lsfDepositAuth = lsfDepositAuth;
    this.lsfDisableMaster = lsfDisableMaster;
    this.lsfDisallowXrp = lsfDisallowXrp;
    this.lsfGlobalFreeze = lsfGlobalFreeze;
    this.lsfNoFreeze = lsfNoFreeze;
    this.lsfPasswordSpent = lsfPasswordSpent;
    this.lsfRequireAuth = lsfRequireAuth;
    this.lsfRequireDestTag = lsfRequireDestTag;

    expectedFlags = (lsfDefaultRipple ? Flags.AccountRootFlags.DEFAULT_RIPPLE.getValue() : 0L) |
        (lsfDepositAuth ? Flags.AccountRootFlags.DEPOSIT_AUTH.getValue() : 0L) |
        (lsfDisableMaster ? Flags.AccountRootFlags.DISABLE_MASTER.getValue() : 0L) |
        (lsfDisallowXrp ? Flags.AccountRootFlags.DISALLOW_XRP.getValue() : 0L) |
        (lsfGlobalFreeze ? Flags.AccountRootFlags.GLOBAL_FREEZE.getValue() : 0L) |
        (lsfNoFreeze ? Flags.AccountRootFlags.NO_FREEZE.getValue() : 0L) |
        (lsfPasswordSpent ? Flags.AccountRootFlags.PASSWORD_SPENT.getValue() : 0L) |
        (lsfRequireAuth ? Flags.AccountRootFlags.REQUIRE_AUTH.getValue() : 0L) |
        (lsfRequireDestTag ? Flags.AccountRootFlags.REQUIRE_DEST_TAG.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(9);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
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

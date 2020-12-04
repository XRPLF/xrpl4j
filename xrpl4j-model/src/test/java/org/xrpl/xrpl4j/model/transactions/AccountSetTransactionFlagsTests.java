package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Test;

public class AccountSetTransactionFlagsTests {

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    AccountSet accountSet = AccountSet.builder()
        .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .build();

    assertThat(accountSet.flags().tfFullyCanonicalSig()).isTrue();
  }
}

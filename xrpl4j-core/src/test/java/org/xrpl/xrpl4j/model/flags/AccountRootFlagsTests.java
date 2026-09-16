package org.xrpl.xrpl4j.model.flags;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AccountRootFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(15);
  }

  @ParameterizedTest
  @MethodSource("data")
  @SuppressWarnings("AbbreviationAsWordInName")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfDefaultRipple,
    boolean lsfDepositAuth,
    boolean lsfDisableMaster,
    boolean lsfDisallowXrp,
    boolean lsfGlobalFreeze,
    boolean lsfNoFreeze,
    boolean lsfPasswordSpent,
    boolean lsfRequireAuth,
    boolean lsfRequireDestTag,
    boolean lsfDisallowIncomingNFTokenOffer,
    boolean lsfDisallowIncomingCheck,
    boolean lsfDisallowIncomingPayChan,
    boolean lsfDisallowIncomingTrustline,
    boolean lsfAllowTrustlineClawback,
    boolean lsfAllowTrustlineLocking
  ) {
    long expectedFlags = (lsfDefaultRipple ? AccountRootFlags.DEFAULT_RIPPLE.getValue() : 0L) |
      (lsfDepositAuth ? AccountRootFlags.DEPOSIT_AUTH.getValue() : 0L) |
      (lsfDisableMaster ? AccountRootFlags.DISABLE_MASTER.getValue() : 0L) |
      (lsfDisallowXrp ? AccountRootFlags.DISALLOW_XRP.getValue() : 0L) |
      (lsfGlobalFreeze ? AccountRootFlags.GLOBAL_FREEZE.getValue() : 0L) |
      (lsfNoFreeze ? AccountRootFlags.NO_FREEZE.getValue() : 0L) |
      (lsfPasswordSpent ? AccountRootFlags.PASSWORD_SPENT.getValue() : 0L) |
      (lsfRequireAuth ? AccountRootFlags.REQUIRE_AUTH.getValue() : 0L) |
      (lsfRequireDestTag ? AccountRootFlags.REQUIRE_DEST_TAG.getValue() : 0L) |
      (lsfDisallowIncomingNFTokenOffer ? AccountRootFlags.DISALLOW_INCOMING_NFT_OFFER.getValue() : 0L) |
      (lsfDisallowIncomingCheck ? AccountRootFlags.DISALLOW_INCOMING_CHECK.getValue() : 0L) |
      (lsfDisallowIncomingPayChan ? AccountRootFlags.DISALLOW_INCOMING_PAY_CHAN.getValue() : 0L) |
      (lsfDisallowIncomingTrustline ? AccountRootFlags.DISALLOW_INCOMING_TRUSTLINE.getValue() : 0L) |
      (lsfAllowTrustlineClawback ? AccountRootFlags.ALLOW_TRUSTLINE_CLAWBACK.getValue() : 0L) |
      (lsfAllowTrustlineLocking ? AccountRootFlags.ALLOW_TRUSTLINE_LOCKING.getValue() : 0L);
    Flags flagsFromFlags = AccountRootFlags.of(
      (lsfDefaultRipple ? AccountRootFlags.DEFAULT_RIPPLE : AccountRootFlags.UNSET),
      (lsfDepositAuth ? AccountRootFlags.DEPOSIT_AUTH : AccountRootFlags.UNSET),
      (lsfDisableMaster ? AccountRootFlags.DISABLE_MASTER : AccountRootFlags.UNSET),
      (lsfDisallowXrp ? AccountRootFlags.DISALLOW_XRP : AccountRootFlags.UNSET),
      (lsfGlobalFreeze ? AccountRootFlags.GLOBAL_FREEZE : AccountRootFlags.UNSET),
      (lsfNoFreeze ? AccountRootFlags.NO_FREEZE : AccountRootFlags.UNSET),
      (lsfPasswordSpent ? AccountRootFlags.PASSWORD_SPENT : AccountRootFlags.UNSET),
      (lsfRequireAuth ? AccountRootFlags.REQUIRE_AUTH : AccountRootFlags.UNSET),
      (lsfRequireDestTag ? AccountRootFlags.REQUIRE_DEST_TAG : AccountRootFlags.UNSET),
      (lsfDisallowIncomingNFTokenOffer ? AccountRootFlags.DISALLOW_INCOMING_NFT_OFFER : AccountRootFlags.UNSET),
      (lsfDisallowIncomingCheck ? AccountRootFlags.DISALLOW_INCOMING_CHECK : AccountRootFlags.UNSET),
      (lsfDisallowIncomingPayChan ? AccountRootFlags.DISALLOW_INCOMING_PAY_CHAN : AccountRootFlags.UNSET),
      (lsfDisallowIncomingTrustline ? AccountRootFlags.DISALLOW_INCOMING_TRUSTLINE : AccountRootFlags.UNSET),
      (lsfAllowTrustlineClawback ? AccountRootFlags.ALLOW_TRUSTLINE_CLAWBACK : AccountRootFlags.UNSET),
      (lsfAllowTrustlineLocking ? AccountRootFlags.ALLOW_TRUSTLINE_LOCKING : AccountRootFlags.UNSET)
    );
    assertThat(flagsFromFlags.getValue()).isEqualTo(expectedFlags);

    AccountRootFlags flagsFromLong = AccountRootFlags.of(expectedFlags);

    assertThat(flagsFromLong.getValue()).isEqualTo(expectedFlags);

    assertThat(flagsFromLong.lsfDefaultRipple()).isEqualTo(lsfDefaultRipple);
    assertThat(flagsFromLong.lsfDepositAuth()).isEqualTo(lsfDepositAuth);
    assertThat(flagsFromLong.lsfDisableMaster()).isEqualTo(lsfDisableMaster);
    assertThat(flagsFromLong.lsfDisallowXrp()).isEqualTo(lsfDisallowXrp);
    assertThat(flagsFromLong.lsfGlobalFreeze()).isEqualTo(lsfGlobalFreeze);
    assertThat(flagsFromLong.lsfNoFreeze()).isEqualTo(lsfNoFreeze);
    assertThat(flagsFromLong.lsfPasswordSpent()).isEqualTo(lsfPasswordSpent);
    assertThat(flagsFromLong.lsfRequireAuth()).isEqualTo(lsfRequireAuth);
    assertThat(flagsFromLong.lsfRequireDestTag()).isEqualTo(lsfRequireDestTag);
    assertThat(flagsFromLong.lsfDisallowIncomingNFTokenOffer()).isEqualTo(lsfDisallowIncomingNFTokenOffer);
    assertThat(flagsFromLong.lsfDisallowIncomingCheck()).isEqualTo(lsfDisallowIncomingCheck);
    assertThat(flagsFromLong.lsfDisallowIncomingPayChan()).isEqualTo(lsfDisallowIncomingPayChan);
    assertThat(flagsFromLong.lsfDisallowIncomingTrustline()).isEqualTo(lsfDisallowIncomingTrustline);
    assertThat(flagsFromLong.lsfAllowTrustLineClawback()).isEqualTo(lsfAllowTrustlineClawback);
    assertThat(flagsFromLong.lsfAllowTrustLineLocking()).isEqualTo(lsfAllowTrustlineLocking);
  }

  @ParameterizedTest
  @MethodSource("data")
  @SuppressWarnings("AbbreviationAsWordInName")
  void testJson(
    boolean lsfDefaultRipple,
    boolean lsfDepositAuth,
    boolean lsfDisableMaster,
    boolean lsfDisallowXrp,
    boolean lsfGlobalFreeze,
    boolean lsfNoFreeze,
    boolean lsfPasswordSpent,
    boolean lsfRequireAuth,
    boolean lsfRequireDestTag,
    boolean lsfDisallowIncomingNFTokenOffer,
    boolean lsfDisallowIncomingCheck,
    boolean lsfDisallowIncomingPayChan,
    boolean lsfDisallowIncomingTrustline,
    boolean lsfAllowTrustlineClawback,
    boolean lsfAllowTrustlineLocking
  ) throws JSONException, JsonProcessingException {
    Flags flags = AccountRootFlags.of(
      (lsfDefaultRipple ? AccountRootFlags.DEFAULT_RIPPLE : AccountRootFlags.UNSET),
      (lsfDepositAuth ? AccountRootFlags.DEPOSIT_AUTH : AccountRootFlags.UNSET),
      (lsfDisableMaster ? AccountRootFlags.DISABLE_MASTER : AccountRootFlags.UNSET),
      (lsfDisallowXrp ? AccountRootFlags.DISALLOW_XRP : AccountRootFlags.UNSET),
      (lsfGlobalFreeze ? AccountRootFlags.GLOBAL_FREEZE : AccountRootFlags.UNSET),
      (lsfNoFreeze ? AccountRootFlags.NO_FREEZE : AccountRootFlags.UNSET),
      (lsfPasswordSpent ? AccountRootFlags.PASSWORD_SPENT : AccountRootFlags.UNSET),
      (lsfRequireAuth ? AccountRootFlags.REQUIRE_AUTH : AccountRootFlags.UNSET),
      (lsfRequireDestTag ? AccountRootFlags.REQUIRE_DEST_TAG : AccountRootFlags.UNSET),
      (lsfDisallowIncomingNFTokenOffer ? AccountRootFlags.DISALLOW_INCOMING_NFT_OFFER : AccountRootFlags.UNSET),
      (lsfDisallowIncomingCheck ? AccountRootFlags.DISALLOW_INCOMING_CHECK : AccountRootFlags.UNSET),
      (lsfDisallowIncomingPayChan ? AccountRootFlags.DISALLOW_INCOMING_PAY_CHAN : AccountRootFlags.UNSET),
      (lsfDisallowIncomingTrustline ? AccountRootFlags.DISALLOW_INCOMING_TRUSTLINE : AccountRootFlags.UNSET),
      (lsfAllowTrustlineClawback ? AccountRootFlags.ALLOW_TRUSTLINE_CLAWBACK : AccountRootFlags.UNSET),
      (lsfAllowTrustlineLocking ? AccountRootFlags.ALLOW_TRUSTLINE_LOCKING : AccountRootFlags.UNSET)
    );

    FlagsWrapper flagsWrapper = FlagsWrapper.of(flags);

    String json = String.format("{\n" +
      "               \"flags\": %s\n" +
      "}", flags.getValue());

    assertCanSerializeAndDeserialize(flagsWrapper, json);
  }
}

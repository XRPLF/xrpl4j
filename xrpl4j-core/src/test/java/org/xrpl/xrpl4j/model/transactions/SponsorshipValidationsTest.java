package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;

/**
 * Unit tests for {@link SponsorshipValidations}.
 */
public class SponsorshipValidationsTest {

  private static final Address ACCOUNT = Address.of("rN7n3zLHyQFRnHaPG4UBpWfXwcz5saAqBN");
  private static final Address DESTINATION = Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy");
  private static final Address SPONSOR = Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe");
  private static final XrpCurrencyAmount FEE = XrpCurrencyAmount.ofDrops(12);
  private static final XrpCurrencyAmount AMOUNT = XrpCurrencyAmount.ofDrops(1000000);

  @Test
  void validateSponsorFieldsWithNoSponsorshipPasses() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .build();

    // Should not throw
    SponsorshipValidations.validateSponsorFields(payment);
    assertThat(SponsorshipValidations.isValidSponsorFields(payment)).isTrue();
  }

  @Test
  void validateSponsorFieldsWithValidSponsorshipPasses() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
      .build();

    // Should not throw
    SponsorshipValidations.validateSponsorFields(payment);
    assertThat(SponsorshipValidations.isValidSponsorFields(payment)).isTrue();
  }

  @Test
  void validateSponsorFieldsWithBothFlagsPasses() {
    long bothFlags = SponsorFlags.SPONSOR_FEE.getValue() | SponsorFlags.SPONSOR_RESERVE.getValue();
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.valueOf(bothFlags))
      .build();

    // Should not throw
    SponsorshipValidations.validateSponsorFields(payment);
    assertThat(SponsorshipValidations.isValidSponsorFields(payment)).isTrue();
  }

  @Test
  void validateSponsorFieldsWithSponsorButNoFlagsFails() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      // No sponsorFlags
      .build();

    assertThatThrownBy(() -> SponsorshipValidations.validateSponsorFields(payment))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsor field requires SponsorFlags to be set");

    assertThat(SponsorshipValidations.isValidSponsorFields(payment)).isFalse();
  }

  @Test
  void validateSponsorFieldsWithFlagsButNoSponsorFails() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      // No sponsor
      .sponsorFlags(UnsignedInteger.valueOf(SponsorFlags.SPONSOR_FEE.getValue()))
      .build();

    assertThatThrownBy(() -> SponsorshipValidations.validateSponsorFields(payment))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorFlags must not be present without Sponsor field");

    assertThat(SponsorshipValidations.isValidSponsorFields(payment)).isFalse();
  }

  @Test
  void validateSponsorFieldsWithZeroFlagsFails() {
    Payment payment = Payment.builder()
      .account(ACCOUNT)
      .destination(DESTINATION)
      .amount(AMOUNT)
      .fee(FEE)
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(PublicKey.MULTI_SIGN_PUBLIC_KEY)
      .sponsor(SPONSOR)
      .sponsorFlags(UnsignedInteger.ZERO)  // No flags set
      .build();

    assertThatThrownBy(() -> SponsorshipValidations.validateSponsorFields(payment))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorFlags must have at least one flag set");

    assertThat(SponsorshipValidations.isValidSponsorFields(payment)).isFalse();
  }
}

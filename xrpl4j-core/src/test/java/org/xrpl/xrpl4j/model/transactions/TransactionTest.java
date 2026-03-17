package org.xrpl.xrpl4j.model.transactions;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.PaymentFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Unit tests for {@link Transaction}.
 */
public class TransactionTest {

  @Test
  void typeMapContainsAllExpectedTransactionTypes() {
    // Verify typeMap contains entries for all known transaction types
    assertThat(Transaction.typeMap.get(ImmutableAccountSet.class)).isEqualTo(TransactionType.ACCOUNT_SET);
    assertThat(Transaction.typeMap.get(ImmutableAccountDelete.class)).isEqualTo(TransactionType.ACCOUNT_DELETE);
    assertThat(Transaction.typeMap.get(ImmutableCheckCancel.class)).isEqualTo(TransactionType.CHECK_CANCEL);
    assertThat(Transaction.typeMap.get(ImmutableCheckCash.class)).isEqualTo(TransactionType.CHECK_CASH);
    assertThat(Transaction.typeMap.get(ImmutableCheckCreate.class)).isEqualTo(TransactionType.CHECK_CREATE);
    assertThat(Transaction.typeMap.get(ImmutableCredentialAccept.class)).isEqualTo(TransactionType.CREDENTIAL_ACCEPT);
    assertThat(Transaction.typeMap.get(ImmutableCredentialCreate.class)).isEqualTo(TransactionType.CREDENTIAL_CREATE);
    assertThat(Transaction.typeMap.get(ImmutableCredentialDelete.class)).isEqualTo(TransactionType.CREDENTIAL_DELETE);
    assertThat(Transaction.typeMap.get(ImmutableDepositPreAuth.class)).isEqualTo(TransactionType.DEPOSIT_PRE_AUTH);
    assertThat(Transaction.typeMap.get(ImmutableEnableAmendment.class)).isEqualTo(TransactionType.ENABLE_AMENDMENT);
    assertThat(Transaction.typeMap.get(ImmutableEscrowCancel.class)).isEqualTo(TransactionType.ESCROW_CANCEL);
    assertThat(Transaction.typeMap.get(ImmutableEscrowCreate.class)).isEqualTo(TransactionType.ESCROW_CREATE);
    assertThat(Transaction.typeMap.get(ImmutableEscrowFinish.class)).isEqualTo(TransactionType.ESCROW_FINISH);
    assertThat(Transaction.typeMap.get(ImmutableNfTokenBurn.class)).isEqualTo(TransactionType.NFTOKEN_BURN);
    assertThat(Transaction.typeMap.get(ImmutableNfTokenMint.class)).isEqualTo(TransactionType.NFTOKEN_MINT);
    assertThat(Transaction.typeMap.get(ImmutableNfTokenAcceptOffer.class))
      .isEqualTo(TransactionType.NFTOKEN_ACCEPT_OFFER);
    assertThat(Transaction.typeMap.get(ImmutableNfTokenCancelOffer.class))
      .isEqualTo(TransactionType.NFTOKEN_CANCEL_OFFER);
    assertThat(Transaction.typeMap.get(ImmutableNfTokenCreateOffer.class))
      .isEqualTo(TransactionType.NFTOKEN_CREATE_OFFER);
    assertThat(Transaction.typeMap.get(ImmutableOfferCancel.class)).isEqualTo(TransactionType.OFFER_CANCEL);
    assertThat(Transaction.typeMap.get(ImmutableOfferCreate.class)).isEqualTo(TransactionType.OFFER_CREATE);
    assertThat(Transaction.typeMap.get(ImmutablePayment.class)).isEqualTo(TransactionType.PAYMENT);
    assertThat(Transaction.typeMap.get(ImmutablePaymentChannelClaim.class))
      .isEqualTo(TransactionType.PAYMENT_CHANNEL_CLAIM);
    assertThat(Transaction.typeMap.get(ImmutablePaymentChannelCreate.class))
      .isEqualTo(TransactionType.PAYMENT_CHANNEL_CREATE);
    assertThat(Transaction.typeMap.get(ImmutablePaymentChannelFund.class))
      .isEqualTo(TransactionType.PAYMENT_CHANNEL_FUND);
    assertThat(Transaction.typeMap.get(ImmutablePermissionedDomainSet.class))
      .isEqualTo(TransactionType.PERMISSIONED_DOMAIN_SET);
    assertThat(Transaction.typeMap.get(ImmutablePermissionedDomainDelete.class))
      .isEqualTo(TransactionType.PERMISSIONED_DOMAIN_DELETE);
    assertThat(Transaction.typeMap.get(ImmutableSetFee.class)).isEqualTo(TransactionType.SET_FEE);
    assertThat(Transaction.typeMap.get(ImmutableSetRegularKey.class)).isEqualTo(TransactionType.SET_REGULAR_KEY);
    assertThat(Transaction.typeMap.get(ImmutableSignerListSet.class)).isEqualTo(TransactionType.SIGNER_LIST_SET);
    assertThat(Transaction.typeMap.get(ImmutableTrustSet.class)).isEqualTo(TransactionType.TRUST_SET);
    assertThat(Transaction.typeMap.get(ImmutableTicketCreate.class)).isEqualTo(TransactionType.TICKET_CREATE);
    assertThat(Transaction.typeMap.get(ImmutableUnlModify.class)).isEqualTo(TransactionType.UNL_MODIFY);
    assertThat(Transaction.typeMap.get(ImmutableAmmBid.class)).isEqualTo(TransactionType.AMM_BID);
    assertThat(Transaction.typeMap.get(ImmutableAmmCreate.class)).isEqualTo(TransactionType.AMM_CREATE);
    assertThat(Transaction.typeMap.get(ImmutableAmmDeposit.class)).isEqualTo(TransactionType.AMM_DEPOSIT);
    assertThat(Transaction.typeMap.get(ImmutableAmmVote.class)).isEqualTo(TransactionType.AMM_VOTE);
    assertThat(Transaction.typeMap.get(ImmutableAmmWithdraw.class)).isEqualTo(TransactionType.AMM_WITHDRAW);
    assertThat(Transaction.typeMap.get(ImmutableAmmDelete.class)).isEqualTo(TransactionType.AMM_DELETE);
    assertThat(Transaction.typeMap.get(ImmutableAmmClawback.class)).isEqualTo(TransactionType.AMM_CLAWBACK);
    assertThat(Transaction.typeMap.get(ImmutableClawback.class)).isEqualTo(TransactionType.CLAWBACK);
    assertThat(Transaction.typeMap.get(ImmutableXChainAccountCreateCommit.class))
      .isEqualTo(TransactionType.XCHAIN_ACCOUNT_CREATE_COMMIT);
    assertThat(Transaction.typeMap.get(ImmutableXChainAddAccountCreateAttestation.class))
      .isEqualTo(TransactionType.XCHAIN_ADD_ACCOUNT_CREATE_ATTESTATION);
    assertThat(Transaction.typeMap.get(ImmutableXChainAddClaimAttestation.class))
      .isEqualTo(TransactionType.XCHAIN_ADD_CLAIM_ATTESTATION);
    assertThat(Transaction.typeMap.get(ImmutableXChainClaim.class)).isEqualTo(TransactionType.XCHAIN_CLAIM);
    assertThat(Transaction.typeMap.get(ImmutableXChainCommit.class)).isEqualTo(TransactionType.XCHAIN_COMMIT);
    assertThat(Transaction.typeMap.get(ImmutableXChainCreateBridge.class))
      .isEqualTo(TransactionType.XCHAIN_CREATE_BRIDGE);
    assertThat(Transaction.typeMap.get(ImmutableXChainCreateClaimId.class))
      .isEqualTo(TransactionType.XCHAIN_CREATE_CLAIM_ID);
    assertThat(Transaction.typeMap.get(ImmutableXChainModifyBridge.class))
      .isEqualTo(TransactionType.XCHAIN_MODIFY_BRIDGE);
    assertThat(Transaction.typeMap.get(ImmutableDidSet.class)).isEqualTo(TransactionType.DID_SET);
    assertThat(Transaction.typeMap.get(ImmutableDidDelete.class)).isEqualTo(TransactionType.DID_DELETE);
    assertThat(Transaction.typeMap.get(ImmutableOracleSet.class)).isEqualTo(TransactionType.ORACLE_SET);
    assertThat(Transaction.typeMap.get(ImmutableOracleDelete.class)).isEqualTo(TransactionType.ORACLE_DELETE);
    assertThat(Transaction.typeMap.get(ImmutableMpTokenAuthorize.class)).isEqualTo(TransactionType.MPT_AUTHORIZE);
    assertThat(Transaction.typeMap.get(ImmutableMpTokenIssuanceCreate.class))
      .isEqualTo(TransactionType.MPT_ISSUANCE_CREATE);
    assertThat(Transaction.typeMap.get(ImmutableMpTokenIssuanceDestroy.class))
      .isEqualTo(TransactionType.MPT_ISSUANCE_DESTROY);
    assertThat(Transaction.typeMap.get(ImmutableMpTokenIssuanceSet.class)).isEqualTo(TransactionType.MPT_ISSUANCE_SET);
    assertThat(Transaction.typeMap.get(ImmutableUnknownTransaction.class)).isEqualTo(TransactionType.UNKNOWN);
  }

  @Test
  void typeMapIsBidirectional() {
    // Verify the inverse mapping works correctly
    assertThat(Transaction.typeMap.inverse().get(TransactionType.PAYMENT)).isEqualTo(ImmutablePayment.class);
    assertThat(Transaction.typeMap.inverse().get(TransactionType.ACCOUNT_SET)).isEqualTo(ImmutableAccountSet.class);
    assertThat(Transaction.typeMap.inverse().get(TransactionType.TRUST_SET)).isEqualTo(ImmutableTrustSet.class);
  }

  @Test
  void sequenceDefaultsToZero() {
    Payment payment = Payment.builder()
      .account(Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .build();

    assertThat(payment.sequence()).isEqualTo(UnsignedInteger.ZERO);
  }

  @Test
  void signingPublicKeyDefaultsToMultiSignPublicKey() {
    Payment payment = Payment.builder()
      .account(Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .build();

    assertThat(payment.signingPublicKey()).isEqualTo(PublicKey.MULTI_SIGN_PUBLIC_KEY);
  }

  @Test
  void transactionFlagsReturnsEmptyFlagsWhenNoFlagsSet() {
    Payment payment = Payment.builder()
      .account(Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .build();

    assertThat(payment.transactionFlags()).isEqualTo(payment.flags());
    assertThat(payment.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  void transactionFlagsReturnsCorrectFlagsWhenFlagsSet() {
    PaymentFlags flags = PaymentFlags.builder().tfPartialPayment(true).build();
    Payment payment = Payment.builder()
      .account(Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .flags(flags)
      .build();

    assertThat(payment.transactionFlags()).isEqualTo(payment.flags());
    assertThat(((PaymentFlags) payment.transactionFlags()).tfPartialPayment()).isTrue();
  }

  @Test
  void transactionFlagsReturnsEmptyWhenFlagsMethodNotFound() {
    // Create a mock Transaction. Since Transaction interface doesn't declare flags(),
    // the reflection call in transactionFlags() will throw NoSuchMethodException
    Transaction mockTransaction = mock(Transaction.class);
    // Allow the default method transactionFlags() to be called
    when(mockTransaction.transactionFlags()).thenCallRealMethod();

    // When transactionFlags() is called, it should catch the NoSuchMethodException and return EMPTY
    TransactionFlags result = mockTransaction.transactionFlags();

    assertThat(result).isEqualTo(TransactionFlags.EMPTY);
  }

  @Test
  void delegateFieldCanBeSet() {
    Address delegateAddress = Address.of("rDelegate1234567890123456789012");

    Payment payment = Payment.builder()
      .account(Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .delegate(delegateAddress)
      .build();

    assertThat(payment.delegate()).isPresent();
    assertThat(payment.delegate().get()).isEqualTo(delegateAddress);
  }

  @Test
  void delegateFieldIsOptional() {
    Payment payment = Payment.builder()
      .account(Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .build();

    assertThat(payment.delegate()).isEmpty();
  }

  @Test
  void delegateCannotBeSameAsAccount() {
    Address sameAddress = Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      Payment.builder()
        .account(sameAddress)
        .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .amount(XrpCurrencyAmount.ofDrops(1000))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .delegate(sameAddress)
        .build();
    });

    assertThat(exception.getMessage()).contains("Delegate and Account must be different");
  }

  @Test
  void delegateWorksOnDifferentTransactionTypes() {
    Address delegateAddress = Address.of("rDelegate1234567890123456789012");
    Address accountAddress = Address.of("rN7n3otQDd6FczFgLdSqtcsAUxDkw6fzRH");

    // Test with TrustSet
    TrustSet trustSet = TrustSet.builder()
      .account(accountAddress)
      .fee(XrpCurrencyAmount.ofDrops(10))
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rIssuer123456789012345678901234"))
        .value("1000")
        .build())
      .delegate(delegateAddress)
      .build();

    assertThat(trustSet.delegate()).isPresent();
    assertThat(trustSet.delegate().get()).isEqualTo(delegateAddress);

    // Test with OfferCreate
    OfferCreate offerCreate = OfferCreate.builder()
      .account(accountAddress)
      .fee(XrpCurrencyAmount.ofDrops(10))
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .takerPays(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rIssuer123456789012345678901234"))
        .value("100")
        .build())
      .delegate(delegateAddress)
      .build();

    assertThat(offerCreate.delegate()).isPresent();
    assertThat(offerCreate.delegate().get()).isEqualTo(delegateAddress);
  }
}

package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Base class for MPT (Multi-Purpose Token) integration tests, holding the generic setup shared across MPT IT families:
 * issuance creation, holder authorization/lock, public-MPT payment, and MPToken lookup. Helpers submit single-signed
 * transactions, assert {@code tesSUCCESS}, and wait for validation. Confidential setup lives in
 * {@link AbstractConfidentialMptIT}.
 */
public abstract class AbstractMptIT extends AbstractIT {

  /**
   * The standard (non-confidential) recommended network fee.
   */
  protected XrpCurrencyAmount networkFee() throws JsonRpcClientErrorException {
    return FeeUtils.computeNetworkFees(xrplClient.fee()).recommendedFee();
  }

  /**
   * The current validated account sequence for {@code account}, waiting for the account to appear if necessary.
   */
  protected UnsignedInteger currentSequence(final Address account) {
    return this.scanForResult(() -> this.getValidatedAccountInfo(account)).accountData().sequence();
  }

  /**
   * Submit an already-signed transaction, assert {@code tesSUCCESS}, and wait for it to validate.
   */
  protected <T extends Transaction> void submitAndWait(final SingleSignedTransaction<T> signed, final Class<T> type)
    throws Exception {
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), type));
  }

  /**
   * Create an MPT issuance with the given capability flags.
   *
   * @param issuer The issuer account.
   * @param flags  The capability flags (e.g. transfer, clawback, lock, require-auth, confidential).
   *
   * @return The new {@link MpTokenIssuanceId}.
   */
  protected MpTokenIssuanceId createMptIssuance(final KeyPair issuer, final MpTokenIssuanceCreateFlags flags)
    throws Exception {
    final MpTokenIssuanceCreate create = MpTokenIssuanceCreate.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(currentSequence(issuer.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(issuer.publicKey())
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .flags(flags)
      .build();
    final SingleSignedTransaction<MpTokenIssuanceCreate> signed = signatureService.sign(issuer.privateKey(), create);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo(SUCCESS_STATUS);
    return this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenIssuanceCreate.class))
      .metadata().orElseThrow(() -> new RuntimeException("no issuance create metadata"))
      .mpTokenIssuanceId().orElseThrow(() -> new RuntimeException("metadata did not contain issuance id"));
  }

  /**
   * A holder opts into (authorizes itself for) an MPT issuance.
   */
  protected void authorizeHolder(final KeyPair holder, final MpTokenIssuanceId issuanceId) throws Exception {
    final MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holder.publicKey().deriveAddress())
      .sequence(currentSequence(holder.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(holder.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .build();
    final SingleSignedTransaction<MpTokenAuthorize> signed = signatureService.sign(holder.privateKey(), authorize);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenAuthorize.class));
  }

  /**
   * The issuer authorizes a holder (allow-listing for a {@code RequireAuth} issuance).
   */
  protected void authorizeHolderByIssuer(
    final KeyPair issuer, final Address holder, final MpTokenIssuanceId issuanceId
  ) throws Exception {
    submitIssuerAuthorize(issuer, holder, issuanceId, MpTokenAuthorizeFlags.empty());
  }

  /**
   * The issuer unauthorizes a holder (clears {@code lsfMPTAuthorized}).
   */
  protected void unauthorizeHolderByIssuer(
    final KeyPair issuer, final Address holder, final MpTokenIssuanceId issuanceId
  ) throws Exception {
    submitIssuerAuthorize(issuer, holder, issuanceId, MpTokenAuthorizeFlags.UNAUTHORIZE);
  }

  private void submitIssuerAuthorize(
    final KeyPair issuer, final Address holder, final MpTokenIssuanceId issuanceId, final MpTokenAuthorizeFlags flags
  ) throws Exception {
    final MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(currentSequence(issuer.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(issuer.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .holder(holder)
      .flags(flags)
      .build();
    final SingleSignedTransaction<MpTokenAuthorize> signed = signatureService.sign(issuer.privateKey(), authorize);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenAuthorize.class));
  }

  /**
   * The issuer locks an individual holder's MPToken ({@code tfMPTLock} + {@code Holder}).
   */
  protected void lockHolder(
    final KeyPair issuer, final Address holder, final MpTokenIssuanceId issuanceId
  ) throws Exception {
    final MpTokenIssuanceSet set = MpTokenIssuanceSet.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(currentSequence(issuer.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(issuer.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .holder(holder)
      .flags(MpTokenIssuanceSetFlags.LOCK)
      .build();
    final SingleSignedTransaction<MpTokenIssuanceSet> signed = signatureService.sign(issuer.privateKey(), set);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), MpTokenIssuanceSet.class));
  }

  /**
   * The issuer pays {@code amount} of public MPT to {@code destination}.
   */
  protected void payMpt(
    final KeyPair issuer, final Address destination, final MpTokenIssuanceId issuanceId, final long amount
  ) throws Exception {
    final Payment payment = Payment.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(currentSequence(issuer.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(issuer.publicKey())
      .destination(destination)
      .amount(MptCurrencyAmount.builder().mptIssuanceId(issuanceId).value(Long.toString(amount)).build())
      .build();
    final SingleSignedTransaction<Payment> signed = signatureService.sign(issuer.privateKey(), payment);
    assertThat(xrplClient.submit(signed).engineResult()).isEqualTo(SUCCESS_STATUS);
    this.scanForResult(() -> this.getValidatedTransaction(signed.hash(), Payment.class));
  }

  /**
   * Fetch a holder's {@link MpTokenObject} from the validated ledger.
   */
  protected MpTokenObject getMpToken(final Address holder, final MpTokenIssuanceId issuanceId) throws Exception {
    return xrplClient.ledgerEntry(LedgerEntryRequestParams.mpToken(
      MpTokenLedgerEntryParams.builder().account(holder).mpTokenIssuanceId(issuanceId).build(),
      LedgerSpecifier.VALIDATED
    )).node();
  }
}

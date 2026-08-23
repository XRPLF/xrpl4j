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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptClawback;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert;
import org.xrpl.xrpl4j.model.transactions.ConfidentialMptSend;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

import java.util.Collections;
import java.util.List;

/**
 * Integration tests for the non-batch Confidential MPT (XLS-0096) transaction types against a real rippled node.
 * Batch composition lives in {@link ConfidentialMptBatchIT}.
 */
@DisabledIf(
  value = "shouldNotRun",
  disabledReason = "ConfidentialMptIT only runs on a local rippled node or Devnet."
)
public class ConfidentialMptIT extends AbstractConfidentialMptIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null || System.getProperty("useClioTestnet") != null;
  }

  // ===========================================================================
  // Convert
  // ===========================================================================

  @Test
  void convertRegistersKeyAndCreditsInbox() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = setupHolder(issuance);
    payMpt(issuance.issuer, holder.address(), issuance.issuanceId, 1000);
    convert(holder, issuance, 1000, true);

    final MpTokenObject token = getMpToken(holder.address(), issuance.issuanceId);
    assertThat(token.holderEncryptionKey()).isPresent();
    assertThat(token.holderEncryptionKey().get().base16Value())
      .isEqualTo(holder.elGamal.publicKey().base16Value());
    // Converted amount lands in the inbox; it is not spendable until merged.
    assertThat(inboxBalance(holder, issuance)).isEqualTo(UnsignedLong.valueOf(1000));
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void topUpConvertOmitsHolderKey() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = holderWithBalance(issuance, 1000);
    payMpt(issuance.issuer, holder.address(), issuance.issuanceId, 500);

    // A second Convert on an already-registered holder must omit the key + proof, else rippled returns tecDUPLICATE.
    final SingleSignedTransaction<ConfidentialMptConvert> topUp = buildConvert(holder, issuance, 500, false);
    assertThat(topUp.unsignedTransaction().holderEncryptionKey()).isEmpty();
    assertThat(topUp.unsignedTransaction().zkProof()).isEmpty();
    submitAndWait(topUp, ConfidentialMptConvert.class);

    mergeInbox(holder, issuance);
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.valueOf(1500));
  }

  @Test
  void registersKeyWithZeroConvertThenFunds() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = setupHolder(issuance);

    convert(holder, issuance, 0, true);
    final MpTokenObject registered = getMpToken(holder.address(), issuance.issuanceId);
    assertThat(registered.holderEncryptionKey()).isPresent();

    payMpt(issuance.issuer, holder.address(), issuance.issuanceId, 750);
    final SingleSignedTransaction<ConfidentialMptConvert> fund = buildConvert(holder, issuance, 750, false);
    assertThat(fund.unsignedTransaction().holderEncryptionKey()).isEmpty();
    submitAndWait(fund, ConfidentialMptConvert.class);

    mergeInbox(holder, issuance);
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.valueOf(750));
  }

  // ===========================================================================
  // ConvertBack
  // ===========================================================================

  @Test
  void convertBackRevealsPublicAmount() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = holderWithBalance(issuance, 1000);
    convertBack(holder, issuance, 400);

    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.valueOf(600));
    // The revealed amount must land in the public MPT balance -- the other half of what ConvertBack does.
    assertThat(getMpToken(holder.address(), issuance.issuanceId).mptAmount())
      .isEqualTo(MpTokenNumericAmount.of(UnsignedLong.valueOf(400)));
  }

  // ===========================================================================
  // MergeInbox
  // ===========================================================================

  @Test
  void mergeInboxFoldsIntoSpendable() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = setupHolder(issuance);
    payMpt(issuance.issuer, holder.address(), issuance.issuanceId, 500);
    convert(holder, issuance, 500, true);
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.ZERO);

    mergeInbox(holder, issuance);
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.valueOf(500));
  }

  // ===========================================================================
  // Send
  // ===========================================================================

  @Test
  void sendCreditsDestinationInbox() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder sender = holderWithBalance(issuance, 1000);
    final ConfidentialHolder destination = registerHolderKey(issuance);

    send(sender, destination, issuance, 300);

    assertThat(spendable(sender, issuance)).isEqualTo(UnsignedLong.valueOf(700));
    assertThat(inboxBalance(destination, issuance)).isEqualTo(UnsignedLong.valueOf(300));
  }

  @Test
  void sendToDepositAuthDestinationWithCredentials() throws Exception {
    final CredentialType[] credentialTypes = {CredentialType.ofPlainText("confidential-mpt-kyc")};
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder sender = holderWithBalance(issuance, 1000);
    final ConfidentialHolder destination = registerHolderKey(issuance);

    // Destination only accepts deposits backed by an issuer-granted credential the sender holds.
    enableDepositAuthorization(destination.account);
    createAndAcceptCredentials(issuance.issuer, sender.account, credentialTypes);
    preAuthorizeCredentials(issuance.issuer, destination.account, credentialTypes);
    final List<Hash256> credentialIds = getCredentialObjectIds(issuance.issuer, sender.account, credentialTypes);

    submitAndWait(buildSend(sender, destination, issuance, 200, credentialIds), ConfidentialMptSend.class);

    assertThat(spendable(sender, issuance)).isEqualTo(UnsignedLong.valueOf(800));
    assertThat(inboxBalance(destination, issuance)).isEqualTo(UnsignedLong.valueOf(200));
  }

  @Test
  void rejectsSendToUnauthorizedDestination() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance(true);

    // Sender must be issuer-authorized before it can hold a balance.
    final ConfidentialHolder sender = setupHolder(issuance);
    authorizeHolderByIssuer(issuance.issuer, sender.address(), issuance.issuanceId);
    payMpt(issuance.issuer, sender.address(), issuance.issuanceId, 1000);
    convert(sender, issuance, 1000, true);
    mergeInbox(sender, issuance);

    // Destination is authorized, registers its key, then the issuer revokes authorization.
    final ConfidentialHolder destination = setupHolder(issuance);
    authorizeHolderByIssuer(issuance.issuer, destination.address(), issuance.issuanceId);
    convert(destination, issuance, 0, true);
    unauthorizeHolderByIssuer(issuance.issuer, destination.address(), issuance.issuanceId);

    final SubmitResult<ConfidentialMptSend> result =
      xrplClient.submit(buildSend(sender, destination, issuance, 100, Collections.emptyList()));
    assertThat(result.engineResult()).isEqualTo("tecNO_AUTH");
  }

  @Test
  void rejectsSendFromLockedHolder() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder sender = holderWithBalance(issuance, 1000);
    final ConfidentialHolder destination = registerHolderKey(issuance);

    lockHolder(issuance.issuer, sender.address(), issuance.issuanceId);

    final SubmitResult<ConfidentialMptSend> result =
      xrplClient.submit(buildSend(sender, destination, issuance, 100, Collections.emptyList()));
    assertThat(result.engineResult()).isEqualTo("tecLOCKED");
  }

  // ===========================================================================
  // Clawback
  // ===========================================================================

  @Test
  void issuerClawsBackFullBalance() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = holderWithBalance(issuance, 1000);
    clawback(issuance, holder);
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void rejectsClawbackWithWrongAmount() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();
    final ConfidentialHolder holder = holderWithBalance(issuance, 1000);

    // The clawback proof asserts the issuer-encrypted balance equals the stated amount; 400 != 1000 is unverifiable.
    final SubmitResult<ConfidentialMptClawback> result = xrplClient.submit(buildClawback(issuance, holder, 400));
    assertThat(result.engineResult()).isEqualTo("tecBAD_PROOF");
  }

  // ===========================================================================
  // Full lifecycle + auditor selective disclosure
  // ===========================================================================

  @Test
  void auditorTracksBalancesAcrossLifecycle() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();

    final ConfidentialHolder holder1 = holderWithBalance(issuance, 1000);
    assertThat(spendable(holder1, issuance)).isEqualTo(UnsignedLong.valueOf(1000));
    assertThat(auditorReads(holder1, issuance)).isEqualTo(UnsignedLong.valueOf(1000));

    final ConfidentialHolder holder2 = registerHolderKey(issuance);
    send(holder1, holder2, issuance, 300);
    mergeInbox(holder2, issuance);
    assertThat(spendable(holder1, issuance)).isEqualTo(UnsignedLong.valueOf(700));
    assertThat(spendable(holder2, issuance)).isEqualTo(UnsignedLong.valueOf(300));
    assertThat(auditorReads(holder1, issuance)).isEqualTo(UnsignedLong.valueOf(700));
    assertThat(auditorReads(holder2, issuance)).isEqualTo(UnsignedLong.valueOf(300));

    convertBack(holder1, issuance, 200);
    assertThat(spendable(holder1, issuance)).isEqualTo(UnsignedLong.valueOf(500));

    clawback(issuance, holder1);
    assertThat(spendable(holder1, issuance)).isEqualTo(UnsignedLong.ZERO);
  }

  // ===========================================================================
  // MpTokenIssuanceSet: enabling the confidential capability after creation
  // ===========================================================================

  /**
   * The one-way {@code tfMPTSetCanHoldConfidentialBalance} flag: an issuance created without confidential capability
   * gains it via {@link MpTokenIssuanceSet}, and the newly-enabled capability is then exercised end-to-end. This is
   * the only test that flips the capability post-create; every other test enables it at create time.
   */
  @Test
  void enablesConfidentialCapabilityPostCreate() throws Exception {
    // Create a non-confidential issuance -- tfMptCanHoldConfidentialBalance is deliberately omitted.
    final KeyPair issuer = createRandomAccountEd25519();
    final MpTokenIssuanceId issuanceId = createMptIssuance(issuer, MpTokenIssuanceCreateFlags.builder()
      .tfMptCanTransfer(true)
      .tfMptCanClawback(true)
      .tfMptCanLock(true)
      .build());
    assertThat(issuanceObject(issuanceId).flags().lsfMptCanHoldConfidentialBalance()).isFalse();

    // Flip the capability on (one-way; requires the DynamicMPT amendment, enabled in the container config).
    final MpTokenIssuanceSet enable = MpTokenIssuanceSet.builder()
      .account(issuer.publicKey().deriveAddress())
      .sequence(currentSequence(issuer.publicKey().deriveAddress()))
      .fee(networkFee())
      .signingPublicKey(issuer.publicKey())
      .mpTokenIssuanceId(issuanceId)
      .flags(MpTokenIssuanceSetFlags.SET_CAN_HOLD_CONFIDENTIAL_BALANCE)
      .build();
    submitAndWait(signatureService.sign(issuer.privateKey(), enable), MpTokenIssuanceSet.class);
    assertThat(issuanceObject(issuanceId).flags().lsfMptCanHoldConfidentialBalance()).isTrue();

    // The newly-enabled capability must actually work: register keys, fund a holder, read the balance back.
    final ConfidentialIssuance issuance = registerConfidentialKeys(issuer, issuanceId);
    final ConfidentialHolder holder = holderWithBalance(issuance, 500);
    assertThat(spendable(holder, issuance)).isEqualTo(UnsignedLong.valueOf(500));
  }

  private MpTokenIssuanceObject issuanceObject(final MpTokenIssuanceId issuanceId) throws Exception {
    return xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(issuanceId, LedgerSpecifier.VALIDATED)
    ).node();
  }

  // ===========================================================================
  // Client lookup surface for a confidential issuance
  // ===========================================================================

  /**
   * A confidential issuance must be readable through every {@code ledger_entry} shape and via {@code account_objects}.
   * The rest of this suite only ever reads an MPToken, so without this the issuance-side lookups -- including the
   * untyped-by-index path, which exercises polymorphic {@link LedgerObject} deserialization -- go untested.
   */
  @Test
  void confidentialIssuanceIsReadableThroughEveryLookupShape() throws Exception {
    final ConfidentialIssuance issuance = createConfidentialIssuance();

    final LedgerEntryResult<MpTokenIssuanceObject> byIssuanceId = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(issuance.issuanceId, LedgerSpecifier.VALIDATED)
    );
    final MpTokenIssuanceObject issuanceObject = byIssuanceId.node();

    final LedgerEntryResult<MpTokenIssuanceObject> byIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(issuanceObject.index(), MpTokenIssuanceObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(byIndex.node()).isEqualTo(issuanceObject);

    final LedgerEntryResult<LedgerObject> byIndexUntyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(issuanceObject.index(), LedgerSpecifier.VALIDATED)
    );
    assertThat(byIndexUntyped.node()).isEqualTo(issuanceObject);

    final AccountObjectsResult accountObjects = xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .type(AccountObjectsRequestParams.AccountObjectType.MPT_ISSUANCE)
        .account(issuanceObject.issuer())
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    );
    assertThat(accountObjects.accountObjects()).contains(issuanceObject);
  }
}

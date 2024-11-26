package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.LocalRippledEnvironment;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

import java.time.Duration;

/**
 * An integration test that submits AccountDelete transactions that handle a successful usage along with
 * examples of all failure cases.
 *
 * @see "https://xrpl.org/accountset.html"
 */
@DisabledIf(value = "shouldRun", disabledReason = "AccountDeleteIT only runs with local rippled nodes.")
class AccountDeleteIT extends AbstractIT {
  static boolean shouldRun() {
    return System.getProperty("useTestnet") != null ||
        System.getProperty("useDevnet") != null ||
        System.getProperty("useClioTestnet") != null;
  }

  @Test
  void testAccountDeleteItFailsWith_TooSoon() throws JsonRpcClientErrorException, JsonProcessingException {
    // create two accounts, one will be the destination in the tx
    KeyPair senderAccount = constructRandomAccount();
    KeyPair receiverAccount = constructRandomAccount();

    // get account info for the sequence number
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
    );

    // create, sign & submit AccountDelete tx
    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(accountInfo.accountData().sequence())
        .destination(receiverAccount.publicKey().deriveAddress())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );
    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get tecTOO_SOON because need to wait for ledger index to be greater than sequenceNumber + 256
    assertThat(response.engineResult()).isEqualTo("tecTOO_SOON");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }

  @Test
  void testAccountDeleteItFailsWith_DestinationIsSource() throws JsonRpcClientErrorException, JsonProcessingException {
    // create one account, will be the sender & destination in the tx
    KeyPair senderAccount = constructRandomAccount();

    // get account info for the sequence number
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
    );

    // create, sign & submit AccountDelete tx
    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(accountInfo.accountData().sequence())
        .destination(senderAccount.publicKey().deriveAddress())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );
    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get temDST_IS_SRC because sender is the same as the destination
    assertThat(response.engineResult()).isEqualTo("temDST_IS_SRC");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }

  @Test
  void testAccountDeleteItFailsWith_DestinationTagNeeded() throws JsonRpcClientErrorException, JsonProcessingException {
    // create two accounts, one will be the destination in the tx
    KeyPair senderAccount = constructRandomAccount();
    KeyPair receiverAccount = constructRandomAccount();

    // get account info for the sequence number
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverAccount.publicKey().deriveAddress())
    );

    // create, sign & submit REQUIRE_DEST AccountSet tx for receiver
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
        .account(receiverAccount.publicKey().deriveAddress())
        .fee(feeResult.drops().openLedgerFee())
        .sequence(receiverAccountInfo.accountData().sequence())
        .setFlag(AccountSet.AccountSetFlag.REQUIRE_DEST)
        .signingPublicKey(receiverAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
        receiverAccount.privateKey(), accountSet
    );
    SubmitResult<AccountSet> accountSetSubmitResult = xrplClient.submit(signedAccountSet);

    assertThat(accountSetSubmitResult.engineResult()).isEqualTo("tesSUCCESS");
    assertThat(signedAccountSet.hash()).isEqualTo(accountSetSubmitResult.transactionResult().hash());

    // confirm flag was set
    TransactionResult<AccountSet> accountSetTransactionResult = this.scanForResult(() ->
        this.getValidatedTransaction(signedAccountSet.hash(), AccountSet.class)
    );

    AccountInfoResult updatedReceiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverAccount.publicKey().deriveAddress())
    );

    assertThat(accountSetTransactionResult.transaction().setFlag().orElse(null))
        .isEqualTo(AccountSet.AccountSetFlag.REQUIRE_DEST);
    assertThat(updatedReceiverAccountInfo.accountData().flags().lsfRequireDestTag()).isTrue();

    // create, sign & submit AccountDelete tx
    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(receiverAccountInfo.accountData().sequence())
        .destination(receiverAccount.publicKey().deriveAddress())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );
    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get tecDST_TAG_NEEDED because the receiver requires the destination tag to be set
    assertThat(response.engineResult()).isEqualTo("tecDST_TAG_NEEDED");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }

  @Test
  void testAccountDeleteItFailsWith_NoPermission() throws JsonRpcClientErrorException, JsonProcessingException {
    // create two accounts, one will be the destination in the tx
    KeyPair senderAccount = constructRandomAccount();
    KeyPair receiverAccount = constructRandomAccount();

    // get account info for the sequence number
    AccountInfoResult receiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverAccount.publicKey().deriveAddress())
    );

    // create, sign & submit DEPOSIT_AUTH AccountSet tx for receiver
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
        .account(receiverAccount.publicKey().deriveAddress())
        .fee(feeResult.drops().openLedgerFee())
        .sequence(receiverAccountInfo.accountData().sequence())
        .setFlag(AccountSet.AccountSetFlag.DEPOSIT_AUTH)
        .signingPublicKey(receiverAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
        receiverAccount.privateKey(), accountSet
    );
    SubmitResult<AccountSet> accountSetSubmitResult = xrplClient.submit(signedAccountSet);

    assertThat(accountSetSubmitResult.engineResult()).isEqualTo("tesSUCCESS");
    assertThat(signedAccountSet.hash()).isEqualTo(accountSetSubmitResult.transactionResult().hash());

    // confirm flag was set
    TransactionResult<AccountSet> accountSetTransactionResult = this.scanForResult(
      () -> this.getValidatedTransaction(signedAccountSet.hash(), AccountSet.class)
    );

    AccountInfoResult updatedReceiverAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(receiverAccount.publicKey().deriveAddress())
    );

    assertThat(accountSetTransactionResult.transaction().setFlag().orElse(null))
        .isEqualTo(AccountSet.AccountSetFlag.DEPOSIT_AUTH);
    assertThat(updatedReceiverAccountInfo.accountData().flags().lsfDepositAuth()).isTrue();

    // create, sign & submit AccountDelete tx
    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(receiverAccountInfo.accountData().sequence())
        .destination(receiverAccount.publicKey().deriveAddress())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );
    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get tecNO_PERMISSION because deposit auth is enabled by receiver and sender is not authorized
    assertThat(response.engineResult()).isEqualTo("tecNO_PERMISSION");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }

  @Test
  void testAccountDeleteItFailsWith_NoDestination() throws JsonRpcClientErrorException, JsonProcessingException {
    // create one account and a random key pair that will be used for the destination
    KeyPair senderAccount = constructRandomAccount();
    KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();

    // get account info for the sequence number
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
    );

    // create, sign & submit AccountDelete tx
    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(accountInfo.accountData().sequence())
        .destination(randomKeyPair.publicKey().deriveAddress())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );
    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get tecNO_DST because destination was not a funded account on the ledger
    assertThat(response.engineResult()).isEqualTo("tecNO_DST");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }

  @Test
  @Disabled
  void testAccountDeleteItFailsWith_HasObligations() throws JsonRpcClientErrorException, JsonProcessingException {
    // create sender account
    KeyPair senderAccount = constructRandomAccount();

    // get account info for the sequence number
    AccountInfoResult accountInfo = this.scanForResult(
        () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
    );

    // create EscrowCreate tx to link an account with an object for tecHAS_OBLIGATIONS error
    EscrowCreate escrowCreate = EscrowCreate.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(200)).build())
        .amount(XrpCurrencyAmount.of(UnsignedLong.valueOf(10)))
        .sequence(accountInfo.accountData().sequence())
        .destination(senderAccount.publicKey().deriveAddress())
        .finishAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofSeconds(10))))
        .signingPublicKey(senderAccount.publicKey())
        .build();

    // sign and submit EscrowCreate tx
    SingleSignedTransaction<EscrowCreate> signedEscrowCreate = signatureService.sign(
        senderAccount.privateKey(), escrowCreate
    );
    SubmitResult<EscrowCreate> escrowCreateResult = xrplClient.submit(signedEscrowCreate);

    assertThat(escrowCreateResult.engineResult()).isEqualTo("tesSUCCESS");
    assertThat(signedEscrowCreate.hash()).isEqualTo(escrowCreateResult.transactionResult().hash());

    // accept next 256 ledgers to avoid tec_TOOSOON error case and get current ledger index
    for (int i = 0; i < 256; i++) {
      LocalRippledEnvironment localRippledEnvironment = (LocalRippledEnvironment) xrplEnvironment;
      localRippledEnvironment.acceptLedger();
    }

    LedgerResult lastLedgerResult = xrplClient.ledger(LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.CURRENT).build());

    // create receiver account, then create sign & submit AccountDelete tx
    KeyPair receiverAccount = constructRandomAccount();

    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(signedEscrowCreate.signedTransaction().sequence().plus(UnsignedInteger.ONE))
        .destination(receiverAccount.publicKey().deriveAddress())
        .lastLedgerSequence(lastLedgerResult.ledgerCurrentIndexSafe().unsignedIntegerValue())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );

    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get tecHAS_OBLIGATIONS because there are objects depending on the account that is trying to be deleted
    assertThat(response.engineResult()).isEqualTo("tecHAS_OBLIGATIONS");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }

  @Test
  @Disabled
  void testAccountDeleteIt() throws JsonRpcClientErrorException, JsonProcessingException {
    // create two accounts, one will be the destination in the tx
    KeyPair senderAccount = constructRandomAccount();
    KeyPair receiverAccount = constructRandomAccount();

    // get account info for the sequence number
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
    );

    // accept next 256 ledgers to avoid tec_TOOSOON error case and get current ledger index
    for (int i = 0; i < 256; i++) {
      LocalRippledEnvironment localRippledEnvironment = (LocalRippledEnvironment) xrplEnvironment;
      localRippledEnvironment.acceptLedger();
    }

    LedgerResult lastLedgerResult = xrplClient.ledger(LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.CURRENT).build());

    // create, sign & submit AccountDelete tx
    AccountDelete accountDelete = AccountDelete.builder()
        .account(senderAccount.publicKey().deriveAddress())
        .fee(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(2000000)).build())
        .sequence(accountInfo.accountData().sequence())
        .destination(receiverAccount.publicKey().deriveAddress())
        .lastLedgerSequence(lastLedgerResult.ledgerCurrentIndexSafe().unsignedIntegerValue())
        .signingPublicKey(senderAccount.publicKey())
        .build();

    SingleSignedTransaction<AccountDelete> signedAccountDelete = signatureService.sign(
        senderAccount.privateKey(), accountDelete
    );

    // after 256 other txs are submitted, then submit AccountDelete
    SubmitResult<AccountDelete> response = xrplClient.submit(signedAccountDelete);

    // get tesSUCCESS because we wait for sequence # + 256 is less than ledger index
    assertThat(response.engineResult()).isEqualTo("tesSUCCESS");
    assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
  }
}

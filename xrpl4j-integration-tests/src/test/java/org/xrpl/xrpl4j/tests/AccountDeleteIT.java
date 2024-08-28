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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.ServerSecret;
import org.xrpl.xrpl4j.crypto.keys.*;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcDerivedKeySignatureService;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.Finality;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.accounts.*;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.*;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

import java.security.Key;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

/**
 * An integration test that submits AccountDelete transactions that handle a successful usage along with
 * examples of all failure cases.
 *
 * @see "https://xrpl.org/accountset.html"
 */
class AccountDeleteIT extends AbstractIT {
    @Test
    void testAccountDeleteItFailsWith_tecTOO_SOON() throws JsonRpcClientErrorException, JsonProcessingException {
        // create two accounts, one will be the destination in the tx
        KeyPair senderAccount = constructRandomAccount();
        KeyPair receiverAccount = constructRandomAccount();

        // get account info the sequence number
        AccountInfoResult accountInfo = this.scanForResult(
                () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
        );

        // create, sign & submit tx
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

        // get tecTOO_SOON because sequence # is too high, need to wait for ledger index to be
        // greater than sequence number + 256
        assertThat(response.engineResult()).isEqualTo("tecTOO_SOON");
        assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
    }

    @Test
    void testAccountDeleteItFailsWith_temDST_IS_SRC() throws JsonRpcClientErrorException, JsonProcessingException {
        // create one account, will be the sender & destination in the tx
        KeyPair senderAccount = constructRandomAccount();

        // get account info the sequence number
        AccountInfoResult accountInfo = this.scanForResult(
                () -> this.getValidatedAccountInfo(senderAccount.publicKey().deriveAddress())
        );

        // create, sign & submit tx
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
    void testAccountDeleteItFailsWith_tecDST_TAG_NEEDED() throws JsonRpcClientErrorException, JsonProcessingException {
        // create two accounts, one will be the destination in the tx
        KeyPair senderAccount = constructRandomAccount();
        KeyPair receiverAccount = constructRandomAccount();

        // get account info the sequence number
        AccountInfoResult receiverAccountInfo = this.scanForResult(
                () -> this.getValidatedAccountInfo(receiverAccount.publicKey().deriveAddress())
        );

        // create, sign & submit account set tx for receiver
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

        // get destination tag
        TransactionResult<AccountSet> accountSetTransactionResult = this.scanForResult(() ->
                this.getValidatedTransaction(signedAccountSet.hash(), AccountSet.class)
        );

        AccountInfoResult updatedReceiverAccountInfo = this.scanForResult(
                () -> this.getValidatedAccountInfo(receiverAccount.publicKey().deriveAddress())
        );

        assertThat(accountSetTransactionResult.transaction().setFlag()).isNotEmpty().get().isEqualTo(AccountSet.AccountSetFlag.REQUIRE_DEST);
        assertThat(updatedReceiverAccountInfo.accountData().flags().lsfRequireDestTag()).isTrue();

        // create, sign & submit tx
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

        // get tecDST_TAG_NEEDED because the destination tag is required for the receiver
        assertThat(response.engineResult()).isEqualTo("tecDST_TAG_NEEDED");
        assertThat(signedAccountDelete.hash()).isEqualTo(response.transactionResult().hash());
    }
}

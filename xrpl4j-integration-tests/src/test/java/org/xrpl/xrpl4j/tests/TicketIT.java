package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.TicketObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.util.List;

public class TicketIT extends AbstractIT {

  @Test
  void createTicketAndUseSequenceNumber() throws JsonRpcClientErrorException {
    Wallet sourceWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );

    TicketCreate ticketCreate = TicketCreate.builder()
      .account(sourceWallet.classicAddress())
      .sequence(accountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .ticketCount(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<TicketCreate> submitResult = xrplClient.submit(sourceWallet, ticketCreate);
    assertThat(submitResult.result()).isEqualTo(SUCCESS_STATUS);

    logInfo(
      submitResult.transactionResult().transaction().transactionType(),
      submitResult.transactionResult().hash()
    );

    this.scanForResult(
      () -> this.getValidatedTransaction(
        submitResult.transactionResult().hash(),
        TicketCreate.class)
    );

    List<TicketObject> tickets = getValidatedAccountObjects(sourceWallet.classicAddress(), TicketObject.class);
    assertThat(tickets).asList().hasSize(1);

    AccountSet accountSet = AccountSet.builder()
      .account(sourceWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .ticketSequence(tickets.get(0).ticketSequence())
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(sourceWallet, accountSet);
    assertThat(accountSetResult.result()).isEqualTo(SUCCESS_STATUS);

    logInfo(
      accountSetResult.transactionResult().transaction().transactionType(),
      accountSetResult.transactionResult().hash()
    );
  }
}

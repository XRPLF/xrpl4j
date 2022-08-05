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

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

public class TransactionWithMemoIT extends AbstractIT {

  /**
   * Tests a transaction that has a memo containing only a nibble (i.e., a half byte).
   */
  @Test
  public void transactionWithMemoNibble() throws JsonRpcClientErrorException {
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .amount(amount)
      .signingPublicKey(sourceWallet.publicKey())
      .addMemos(MemoWrapper.builder()
        .memo(Memo.builder()
          .memoData("A")
          .build())
        .build())
      .build();

    SubmitResult<Payment> result = xrplClient.submit(sourceWallet, payment);
    assertThat(result.result()).isEqualTo(SUCCESS_STATUS);
    assertThat(result.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(result.transactionResult().hash());
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" +
      result.transactionResult().hash()
    );

    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult().hash(),
        Payment.class)
    );

    assertThat(validatedPayment.metadata().get().deliveredAmount()).hasValue(amount);
    assertThat(validatedPayment.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

  /**
   * Tests a transaction that has a memo containing only a nibble (i.e., a half byte).
   */
  @Test
  public void transactionWithPlaintextMemo() throws JsonRpcClientErrorException {
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.classicAddress())
    );
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .amount(amount)
      .signingPublicKey(sourceWallet.publicKey())
      .addMemos(MemoWrapper.builder()
        .memo(Memo.withPlaintext("Hello World").build())
        .build()
      )
      .build();

    SubmitResult<Payment> result = xrplClient.submit(sourceWallet, payment);
    assertThat(result.result()).isEqualTo(SUCCESS_STATUS);
    assertThat(result.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(result.transactionResult().hash());
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/" +
      result.transactionResult().hash()
    );

    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult().hash(),
        Payment.class)
    );

    assertThat(validatedPayment.metadata().get().deliveredAmount()).hasValue(amount);
    assertThat(validatedPayment.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);
  }

}

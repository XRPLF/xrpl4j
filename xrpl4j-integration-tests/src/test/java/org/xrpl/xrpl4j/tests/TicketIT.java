package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
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
      .fee(feeResult.drops().openLedgerFee())
      .ticketCount(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<TicketCreate> submitResult = xrplClient.submit(sourceWallet, ticketCreate);
    assertThat(submitResult.result()).isEqualTo(SUCCESS_STATUS);
    logger.info("TicketCreate successful: https://testnet.xrpl.org/transactions/" +
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
      .fee(feeResult.drops().openLedgerFee())
      .ticketSequence(tickets.get(0).ticketSequence())
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<AccountSet> accountSetResult = xrplClient.submit(sourceWallet, accountSet);
    assertThat(accountSetResult.result()).isEqualTo(SUCCESS_STATUS);
    logger.info("AccountSet successful: https://testnet.xrpl.org/transactions/" +
      accountSetResult.transactionResult().hash()
    );
  }
}

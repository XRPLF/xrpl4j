package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.wallet.Wallet;

public class IsFinalIT extends AbstractIT {

  Wallet wallet = createRandomAccount();

  @Test
  public void simpleIsFinalTest() throws JsonRpcClientErrorException, InterruptedException {

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    UnsignedInteger sequence = accountInfo.accountData().sequence();

    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .setFlag(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID)
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);
    assertThat(response.result()).isEqualTo("tesSUCCESS");

    assertThat(xrplClient.isFinal(response.transactionResult().hash())).isFalse();
    Thread.sleep(4000);
    assertThat(xrplClient.isFinal(response.transactionResult().hash())).isTrue();
  }
}

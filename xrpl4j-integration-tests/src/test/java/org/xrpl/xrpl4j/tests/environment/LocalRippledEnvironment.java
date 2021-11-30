package org.xrpl.xrpl4j.tests.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.math.BigDecimal;

/**
 * Environment that runs a local rippled inside docker in standalone mode.
 */
public class LocalRippledEnvironment implements XrplEnvironment {

  private static final Logger LOGGER = getLogger(LocalRippledEnvironment.class);

  private static final RippledContainer rippledContainer = new RippledContainer().start();

  @Override
  public XrplClient getXrplClient() {
    return rippledContainer.getXrplClient();
  }

  @Override
  public void fundAccount(Address classicAddress) {
    // accounts are funded from the genesis account that holds all XRP when the ledger container starts.
    try {
      sendPayment(
        RippledContainer.getMasterWallet(),
        classicAddress,
        XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1000))
      );
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException("could not fund account", e);
    }

  }

  protected AccountInfoResult getCurrentAccountInfo(Address classicAddress) {
    try {
      AccountInfoRequestParams params = AccountInfoRequestParams.of(classicAddress);
      return getXrplClient().accountInfo(params);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected void sendPayment(Wallet sourceWallet, Address destinationAddress, XrpCurrencyAmount paymentAmount)
    throws JsonRpcClientErrorException {
    FeeResult feeResult = getXrplClient().fee();
    AccountInfoResult accountInfo = this.getCurrentAccountInfo(sourceWallet.classicAddress());
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationAddress)
      .amount(paymentAmount)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<Payment> result = getXrplClient().submit(sourceWallet, payment);
    assertThat(result.engineResult()).isNotEmpty().isEqualTo("tesSUCCESS");
    LOGGER.info("Payment successful: " + rippledContainer.getBaseUri().toString() +
      result.transactionResult().transaction());
  }

}

package org.xrpl.xrpl4j.tests.environment;

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
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplAdminClient;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.admin.AcceptLedgerResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Environment that connects to a locally running rippled node in standalone mode.
 */
public class LocalRippledEnvironment implements XrplEnvironment {

  private static final Logger LOGGER = getLogger(LocalRippledEnvironment.class);

  /**
   * The URL of the locally running rippled node.
   */
  private static final HttpUrl LOCAL_RIPPLED_URL = HttpUrl.parse("http://127.0.0.1:51234/");

  /**
   * Seed for the Master/Root wallet in the rippled standalone mode.
   */
  private static final String MASTER_WALLET_SEED = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";

  private static ScheduledExecutorService ledgerAcceptor;

  private final XrplClient xrplClient;
  private final XrplAdminClient xrplAdminClient;
  private final SignatureService<PrivateKey> signatureService = new BcSignatureService();

  /**
   * No-args constructor that initializes the connection to the local rippled node.
   */
  public LocalRippledEnvironment() {
    this.xrplClient = new XrplClient(LOCAL_RIPPLED_URL);
    this.xrplAdminClient = new XrplAdminClient(LOCAL_RIPPLED_URL);
    // Start the ledger acceptor with a default interval
    this.startLedgerAcceptor(Duration.ofMillis(1000));
  }

  /**
   * Get the {@link KeyPair} of the master account.
   *
   * @return The {@link KeyPair} of the master account.
   */
  public static KeyPair getMasterKeyPair() {
    return Seed.fromBase58EncodedSecret(Base58EncodedSecret.of(MASTER_WALLET_SEED)).deriveKeyPair();
  }

  @Override
  public XrplClient getXrplClient() {
    return xrplClient;
  }

  @Override
  public void fundAccount(Address classicAddress) {
    // accounts are funded from the genesis account that holds all XRP when the ledger starts.
    try {
      sendPayment(
        getMasterKeyPair(),
        classicAddress,
        XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1000))
      );
    } catch (JsonRpcClientErrorException | JsonProcessingException e) {
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

  protected void sendPayment(KeyPair sourceKeyPair, Address destinationAddress, XrpCurrencyAmount paymentAmount)
    throws JsonRpcClientErrorException, JsonProcessingException {
    FeeResult feeResult = getXrplClient().fee();
    AccountInfoResult accountInfo = this.getCurrentAccountInfo(sourceKeyPair.publicKey().deriveAddress());
    Payment payment = Payment.builder()
      .account(sourceKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationAddress)
      .amount(paymentAmount)
      .signingPublicKey(sourceKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(sourceKeyPair.privateKey(), payment);
    SubmitResult<Payment> result = getXrplClient().submit(signedPayment);
    assertThat(result.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    LOGGER.info("Payment successful: " + LOCAL_RIPPLED_URL +
      result.transactionResult().transaction());
  }

  /**
   * Method to accept next ledger ad hoc.
   */
  @Override
  public void acceptLedger() {
    try {
      AcceptLedgerResult status = xrplAdminClient.acceptLedger();
      LOGGER.info("Accepted ledger status: {}", status);
    } catch (RuntimeException | JsonRpcClientErrorException e) {
      LOGGER.warn("Ledger accept failed", e);
    }
  }

  @Override
  public void startLedgerAcceptor(Duration acceptIntervalMillis) {
    Objects.requireNonNull(acceptIntervalMillis, "acceptIntervalMillis must not be null");
    Preconditions.checkArgument(acceptIntervalMillis.toMillis() > 0, "acceptIntervalMillis must be greater than 0");

    // Stop any existing ledger acceptor
    if (ledgerAcceptor != null && !ledgerAcceptor.isShutdown()) {
      stopLedgerAcceptor();
    }

    // rippled is run in standalone mode which means that ledgers won't automatically close. You have to manually
    // advance the ledger using the "ledger_accept" method on the admin API. To mimic the behavior of a networked
    // rippled, run a scheduled task to trigger the "ledger_accept" method.
    ledgerAcceptor = Executors.newScheduledThreadPool(1);
    ledgerAcceptor.scheduleAtFixedRate(this::acceptLedger,
      acceptIntervalMillis.toMillis(),
      acceptIntervalMillis.toMillis(),
      TimeUnit.MILLISECONDS
    );
  }

  @Override
  public void stopLedgerAcceptor() {
    if (ledgerAcceptor != null) {
      try {
        ledgerAcceptor.shutdown();
        ledgerAcceptor.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException("Unable to stop ledger acceptor", e);
      }
    }
  }
}

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

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.ComputationAllowance;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowData;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.FinishFunction;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.ImmutableEscrowCreate;
import org.xrpl.xrpl4j.model.transactions.ImmutableEscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.NetworkId;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Smart Escrow Soak Test - Continuous testing of XLS-0100 Smart Escrows.
 *
 * <p>This test is designed to run continuously to stress-test the Smart Escrow feature.
 * It creates a faucet account, then spawns multiple worker threads that continuously:
 * <ul>
 *   <li>Create Smart Escrows with various WASM functions</li>
 *   <li>Attempt to finish the escrows</li>
 *   <li>Collect metrics on success/failure rates and gas consumption</li>
 * </ul>
 *
 * <p>This test is disabled by default. To run it manually:
 * <pre>
 * mvn test -Dtest=SmartEscrowSoakTest -pl xrpl4j-integration-tests
 * </pre>
 *
 * <p>Note: This test requires the SmartEscrow amendment to be enabled on the target network.
 */
@Disabled("Manual soak test - run explicitly when needed")
public class SmartEscrowSoakTest extends AbstractIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmartEscrowSoakTest.class);

  // Configuration
  private static final int NUM_WORKER_THREADS = 20;
  private static final long FAUCET_INITIAL_BALANCE_DROPS = 500_000_000; // 500 XRP (increased for reserve requirements)
  private static final long ESCROW_AMOUNT_DROPS = 1_000; // 0.001 XRP per escrow (reduced to minimize reserve impact)
  private static final long TEST_DURATION_MINUTES = 60; // Run for 1 hour by default
  private static final long METRICS_REPORT_INTERVAL_SECONDS = 30;

  // Resilience configuration
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 100;
  private static final long MAX_BACKOFF_MS = 5000;
  private static final long SEQUENCE_REFRESH_INTERVAL_MS = 10000; // Refresh sequence every 10s

  // Network configuration
  private Optional<NetworkId> networkId = Optional.empty();

  // Metrics
  private final AtomicInteger escrowsCreated = new AtomicInteger(0);
  private final AtomicInteger expectedSuccesses = new AtomicInteger(0);
  private final AtomicInteger expectedFailures = new AtomicInteger(0);
  private final AtomicInteger unexpectedFailures = new AtomicInteger(0);
  private final AtomicInteger errors = new AtomicInteger(0);
  private final AtomicInteger sequenceErrors = new AtomicInteger(0);
  private final AtomicInteger sequenceRefreshes = new AtomicInteger(0);
  private final AtomicInteger retriedTransactions = new AtomicInteger(0);
  private final AtomicLong totalGasUsed = new AtomicLong(0);
  private final ConcurrentHashMap<String, AtomicInteger> functionExpectedSuccessCount = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicInteger> functionExpectedFailureCount = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicInteger> functionUnexpectedFailureCount = new ConcurrentHashMap<>();

  // WASM function types to test
  private enum WasmFunction {
    // Simple functions - minimal gas needed
    ALWAYS_SUCCEED("always_succeed", true, 1269),
    // Stateful counter - fails on first call, succeeds on second (simulates data counter)
    DATA_COUNTER("data_counter", true, 7500),
    CREDENTIAL_CHECK("credential_check", true, 10160),
    TIME_WINDOW("time_window", true, 1836),
    ORACLE_PRICE_CHECK("oracle_price_check", true, 1836),
    // Stress test - maximum gas for 1M iterations
    GAS_STRESS_TEST("gas_stress_test", true, 805000);

    private final String name;
    private final boolean shouldSucceed;
    private final long gasAllowance;

    WasmFunction(String name, boolean shouldSucceed, long gasAllowance) {
      this.name = name;
      this.shouldSucceed = shouldSucceed;
      this.gasAllowance = gasAllowance;
    }

    public String getName() {
      return name;
    }

    public boolean shouldSucceed() {
      return shouldSucceed;
    }

    public long getGasAllowance() {
      return gasAllowance;
    }
  }

  @Test
  public void runSmartEscrowSoakTest() throws Exception {
    printTestHeader();

    // Get network ID from server
    LOGGER.info("Fetching network ID from server...");
    ServerInfoResult serverInfo = xrplClient.serverInformation();
    networkId = serverInfo.info().networkId();
    if (networkId.isPresent()) {
      LOGGER.info("Network ID found: {}", networkId.get().value());
    } else {
      LOGGER.warn("Network ID not present in server_info - transactions may fail on sidechains/devnets");
    }

    // Load WASM functions - each function has its own WASM file
    LOGGER.info("Loading WASM functions...");
    Map<String, String> wasmHexByFunction = loadAllWasmFunctions();
    LOGGER.info("Loaded {} WASM functions", wasmHexByFunction.size());

    // Start metrics reporter
    ExecutorService metricsExecutor = Executors.newSingleThreadExecutor();
    metricsExecutor.submit(this::reportMetricsPeriodically);

    // Create dedicated accounts for each worker thread
    LOGGER.info("Creating {} dedicated accounts for worker threads...", NUM_WORKER_THREADS);
    List<KeyPair> workerAccounts = new ArrayList<>();
    for (int i = 0; i < NUM_WORKER_THREADS; i++) {
      KeyPair workerAccount = createRandomAccountEd25519();
      workerAccounts.add(workerAccount);
      LOGGER.info("Worker {} account created: {}", i, workerAccount.publicKey().deriveAddress());
    }

    // Start worker threads
    LOGGER.info("Starting {} worker threads...", NUM_WORKER_THREADS);
    ExecutorService workerExecutor = Executors.newFixedThreadPool(NUM_WORKER_THREADS);
    List<WorkerThread> workers = new ArrayList<>();

    for (int i = 0; i < NUM_WORKER_THREADS; i++) {
      WorkerThread worker = new WorkerThread(i, workerAccounts.get(i), wasmHexByFunction);
      workers.add(worker);
      workerExecutor.submit(worker);
    }

    // Run for configured duration
    LOGGER.info("Soak test running for {} minutes...", TEST_DURATION_MINUTES);
    Thread.sleep(TimeUnit.MINUTES.toMillis(TEST_DURATION_MINUTES));

    // Shutdown workers
    LOGGER.info("Shutting down workers...");
    workers.forEach(WorkerThread::stop);
    workerExecutor.shutdown();
    workerExecutor.awaitTermination(30, TimeUnit.SECONDS);

    // Shutdown metrics reporter
    metricsExecutor.shutdownNow();

    // Final metrics report
    printTestSummary();
  }

  /**
   * Worker thread that continuously creates and finishes Smart Escrows. Each worker has its own dedicated account to
   * avoid sequence number conflicts.
   */
  private class WorkerThread implements Runnable {

    private final int workerId;
    private final KeyPair workerAccount;
    private final Map<String, String> wasmHexByFunction;
    private volatile boolean running = true;
    private UnsignedInteger currentSequence = null;
    private long lastSequenceRefresh = 0;
    private int consecutiveErrors = 0;

    public WorkerThread(int workerId, KeyPair workerAccount, Map<String, String> wasmHexByFunction) {
      this.workerId = workerId;
      this.workerAccount = workerAccount;
      this.wasmHexByFunction = wasmHexByFunction;
      LOGGER.info("Worker {} using dedicated account: {}", workerId,
        workerAccount.publicKey().deriveAddress());
    }

    public void stop() {
      running = false;
    }

    /**
     * Refreshes the sequence number from the ledger. This is called periodically and when sequence errors occur.
     */
    private void refreshSequenceFromLedger() {
      try {
        Address workerAddress = workerAccount.publicKey().deriveAddress();
        AccountInfoResult accountInfo = scanForResult(
          () -> getValidatedAccountInfo(workerAddress)
        );
        currentSequence = accountInfo.accountData().sequence();
        lastSequenceRefresh = System.currentTimeMillis();
        sequenceRefreshes.incrementAndGet();
        LOGGER.info("Worker {} - Refreshed sequence from ledger: {}", workerId, currentSequence);
      } catch (Exception e) {
        LOGGER.error("Worker {} - Failed to refresh sequence from ledger", workerId, e);
        throw new RuntimeException("Failed to refresh sequence", e);
      }
    }

    /**
     * Checks if sequence should be refreshed based on time interval.
     */
    private boolean shouldRefreshSequence() {
      return (System.currentTimeMillis() - lastSequenceRefresh) > SEQUENCE_REFRESH_INTERVAL_MS;
    }

    /**
     * Applies exponential backoff when errors occur.
     */
    private void applyBackoff(int attemptNumber) {
      long backoffMs = Math.min(INITIAL_BACKOFF_MS * (1L << attemptNumber), MAX_BACKOFF_MS);
      try {
        LOGGER.info("Worker {} - Backing off for {} ms (attempt {})", workerId, backoffMs, attemptNumber);
        Thread.sleep(backoffMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    @Override
    public void run() {
      LOGGER.info("Worker {} started", workerId);

      try {
        // Initialize sequence number from ledger
        refreshSequenceFromLedger();
      } catch (Exception e) {
        LOGGER.error("Worker {} - Failed to initialize sequence", workerId, e);
        return;
      }

      while (running) {
        try {
          // Periodically refresh sequence from ledger to prevent drift
          if (shouldRefreshSequence()) {
            refreshSequenceFromLedger();
          }

          // Use worker's dedicated account as both sender and receiver
          Address workerAddress = workerAccount.publicKey().deriveAddress();

          // Select a random WASM function to test
          WasmFunction function = selectRandomFunction();

          // Get the WASM hex for this specific function
          String wasmHex = wasmHexByFunction.get(function.getName());
          if (wasmHex == null) {
            LOGGER.error("Worker {} - No WASM found for function: {}", workerId, function.getName());
            continue;
          }

          // Create Smart Escrow with retry logic
          EscrowCreationResult escrowResult = createSmartEscrowWithRetry(
            workerAccount,
            workerAddress,
            wasmHex,
            function
          );

          if (escrowResult == null) {
            LOGGER.warn("Worker {} - Failed to create escrow after retries, continuing", workerId);
            consecutiveErrors++;
            if (consecutiveErrors > 5) {
              applyBackoff(Math.min(consecutiveErrors - 5, 5));
            }
            continue;
          }

          consecutiveErrors = 0; // Reset on success

          if (escrowResult.txHash != null) {
            escrowsCreated.incrementAndGet();

            // Wait for the escrow creation to be validated in a ledger
            LOGGER.info("Worker {} - Waiting for EscrowCreate validation - hash: {}", workerId, escrowResult.txHash);
            TransactionResult<EscrowCreate> createResult = scanForResult(
              () -> getValidatedTransaction(escrowResult.txHash, EscrowCreate.class)
            );
            LOGGER.info("Worker {} - ✓ EscrowCreate validated - hash: {}, ledger: {}, result: {}",
              workerId,
              escrowResult.txHash,
              createResult.ledgerIndex().map(LedgerIndex::toString).orElse("unknown"),
              createResult.metadata().map(TransactionMetadata::transactionResult).orElse("unknown"));

            // Attempt to finish the escrow with retry logic
            EscrowFinishResult finishResult = finishSmartEscrowWithRetry(
              workerAccount,
              workerAddress,
              escrowResult.sequence,
              function
            );

            if (finishResult == null) {
              LOGGER.warn("Worker {} - Failed to finish escrow after retries", workerId);
              continue;
            }

            if (finishResult.txHash != null) {
              // Wait for the finish transaction to be validated
              LOGGER.info("Worker {} - Waiting for EscrowFinish validation - hash: {}", workerId, finishResult.txHash);
              TransactionResult<EscrowFinish> finishTxResult = scanForResult(
                () -> getValidatedTransaction(finishResult.txHash, EscrowFinish.class)
              );

              // Check the transaction result to determine success/failure
              String txResult = finishTxResult.metadata()
                .map(TransactionMetadata::transactionResult)
                .orElse("unknown");

              LOGGER.info("Worker {} - ✓ EscrowFinish validated - hash: {}, ledger: {}, result: {}",
                workerId,
                finishResult.txHash,
                finishTxResult.ledgerIndex().map(LedgerIndex::toString).orElse("unknown"),
                txResult);

              // Extract gas used from metadata (for both success and failure)
              Long gasUsed = finishTxResult.metadata()
                .flatMap(TransactionMetadata::gasUsed)
                .map(gas -> gas.value().longValue())
                .orElse(null);

              if (gasUsed != null) {
                totalGasUsed.addAndGet(gasUsed);
                LOGGER.info("Worker {} - Gas used for function {}: {} (total: {})",
                  workerId,
                  function.getName(),
                  gasUsed,
                  totalGasUsed.get());
              }

              // Categorize the result based on expected vs actual outcome
              // tesSUCCESS = WASM returned 1 (escrow finished)
              // tecWASM_REJECTED = WASM returned 0 (escrow not finished, but transaction succeeded)
              // Other tec* codes = Transaction processed but failed for other reasons
              boolean wasmReturnedSuccess = "tesSUCCESS".equals(txResult);
              boolean wasmReturnedFailure = "tecWASM_REJECTED".equals(txResult);
              boolean expectedSuccess = function.shouldSucceed();

              // Special handling for data_counter which requires 2 calls to succeed
              if ("data_counter".equals(function.getName()) && wasmReturnedFailure) {
                LOGGER.info("Worker {} - data_counter returned failure on first attempt (counter=0), retrying...",
                  workerId);
                // Retry the finish - the second call should succeed
                EscrowFinishResult retryResult = finishSmartEscrowWithRetry(
                  workerAccount,
                  workerAddress,
                  escrowResult.sequence,
                  function
                );

                if (retryResult != null && retryResult.txHash != null) {
                  TransactionResult<EscrowFinish> retryTxResult = scanForResult(
                    () -> getValidatedTransaction(retryResult.txHash, EscrowFinish.class)
                  );
                  String retryTxResultCode = retryTxResult.metadata()
                    .map(TransactionMetadata::transactionResult)
                    .orElse("unknown");

                  if ("tesSUCCESS".equals(retryTxResultCode)) {
                    expectedSuccesses.incrementAndGet();
                    functionExpectedSuccessCount.computeIfAbsent(function.getName(), k -> new AtomicInteger(0))
                      .incrementAndGet();
                    LOGGER.info("Worker {} - ✓ data_counter succeeded on second attempt (counter >= 1)", workerId);
                  } else {
                    unexpectedFailures.incrementAndGet();
                    functionUnexpectedFailureCount.computeIfAbsent(function.getName(), k -> new AtomicInteger(0))
                      .incrementAndGet();
                    LOGGER.warn("Worker {} - ✗ data_counter failed on second attempt: {}", workerId, retryTxResultCode);
                  }
                }
                continue; // Skip normal result processing
              }

              if (wasmReturnedSuccess && expectedSuccess) {
                // Expected success - WASM returned 1 as expected
                expectedSuccesses.incrementAndGet();
                functionExpectedSuccessCount.computeIfAbsent(function.getName(), k -> new AtomicInteger(0))
                  .incrementAndGet();
                LOGGER.info("Worker {} - ✓ Expected success: {} returned success, gas used: {}",
                  workerId, function.getName(), gasUsed != null ? gasUsed : "N/A");
              } else if (wasmReturnedFailure && !expectedSuccess) {
                // Expected failure - WASM returned 0 as expected
                expectedFailures.incrementAndGet();
                functionExpectedFailureCount.computeIfAbsent(function.getName(), k -> new AtomicInteger(0))
                  .incrementAndGet();
                LOGGER.info("Worker {} - ✓ Expected failure: {} returned failure as designed ({}), gas used: {}",
                  workerId, function.getName(), txResult, gasUsed != null ? gasUsed : "N/A");
              } else {
                // Unexpected result - WASM behaved differently than expected, or transaction failed for other reasons
                unexpectedFailures.incrementAndGet();
                functionUnexpectedFailureCount.computeIfAbsent(function.getName(), k -> new AtomicInteger(0))
                  .incrementAndGet();
                LOGGER.warn("Worker {} - ✗ Unexpected result: {} expected {}, got {} ({})",
                  workerId, function.getName(),
                  expectedSuccess ? "success" : "failure",
                  wasmReturnedSuccess ? "success" : (wasmReturnedFailure ? "failure" : "error"),
                  txResult);
              }
            } else {
              // Transaction submission failed
              unexpectedFailures.incrementAndGet();
              functionUnexpectedFailureCount.computeIfAbsent(function.getName(), k -> new AtomicInteger(0))
                .incrementAndGet();
              LOGGER.warn("Worker {} - ✗ Transaction submission failed for {}", workerId, function.getName());
            }
          }

        } catch (Exception e) {
          LOGGER.error("Worker {} encountered error", workerId, e);
          errors.incrementAndGet();
        }
      }

      LOGGER.info("Worker {} stopped", workerId);
    }

    /**
     * Creates a Smart Escrow with retry logic and sequence number error handling.
     */
    private EscrowCreationResult createSmartEscrowWithRetry(
      KeyPair senderKeyPair,
      Address destination,
      String wasmHex,
      WasmFunction function
    ) {
      for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
        try {
          EscrowCreationResult result = createSmartEscrow(
            senderKeyPair,
            destination,
            wasmHex,
            function,
            currentSequence
          );

          if (result != null) {
            // Only increment sequence if it was actually consumed by the ledger
            if (result.sequenceConsumed) {
              currentSequence = currentSequence.plus(UnsignedInteger.ONE);
            }
            return result;
          }

          // If result is null, retry
          if (attempt < MAX_RETRIES - 1) {
            retriedTransactions.incrementAndGet();
            applyBackoff(attempt);
          }
        } catch (Exception e) {
          LOGGER.error("Worker {} - Error creating escrow (attempt {})", workerId, attempt + 1, e);

          // Check if it's a sequence error
          if (isSequenceError(e)) {
            sequenceErrors.incrementAndGet();
            LOGGER.warn("Worker {} - Sequence error detected, refreshing from ledger", workerId);
            refreshSequenceFromLedger();
          }

          if (attempt < MAX_RETRIES - 1) {
            retriedTransactions.incrementAndGet();
            applyBackoff(attempt);
          }
        }
      }

      errors.incrementAndGet();
      return null;
    }

    /**
     * Finishes a Smart Escrow with retry logic and sequence number error handling.
     */
    private EscrowFinishResult finishSmartEscrowWithRetry(
      KeyPair finisherKeyPair,
      Address owner,
      UnsignedInteger offerSequence,
      WasmFunction function
    ) {
      for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
        try {
          EscrowFinishResult result = finishSmartEscrow(
            finisherKeyPair,
            owner,
            offerSequence,
            function,
            currentSequence
          );

          if (result != null) {
            // Only increment sequence if it was actually consumed by the ledger
            if (result.sequenceConsumed) {
              currentSequence = currentSequence.plus(UnsignedInteger.ONE);
            }
            return result;
          }

          // If result is null, retry
          if (attempt < MAX_RETRIES - 1) {
            retriedTransactions.incrementAndGet();
            applyBackoff(attempt);
          }
        } catch (Exception e) {
          LOGGER.error("Worker {} - Error finishing escrow (attempt {})", workerId, attempt + 1, e);

          // Check if it's a sequence error
          if (isSequenceError(e)) {
            sequenceErrors.incrementAndGet();
            LOGGER.warn("Worker {} - Sequence error detected, refreshing from ledger", workerId);
            refreshSequenceFromLedger();
          }

          if (attempt < MAX_RETRIES - 1) {
            retriedTransactions.incrementAndGet();
            applyBackoff(attempt);
          }
        }
      }

      errors.incrementAndGet();
      return null;
    }

    /**
     * Checks if an exception is related to sequence number errors.
     */
    private boolean isSequenceError(Exception e) {
      String message = e.getMessage();
      if (message == null) {
        return false;
      }
      return message.contains("tefPAST_SEQ") ||
        message.contains("terPRE_SEQ") ||
        message.contains("terQUEUED") ||
        message.contains("tefMAX_LEDGER") ||
        message.contains("Account not found");
    }
  }

  /**
   * Simple holder for escrow creation result.
   */
  private static class EscrowCreationResult {

    final Hash256 txHash;
    final UnsignedInteger sequence;
    final boolean sequenceConsumed;

    EscrowCreationResult(Hash256 txHash, UnsignedInteger sequence, boolean sequenceConsumed) {
      this.txHash = txHash;
      this.sequence = sequence;
      this.sequenceConsumed = sequenceConsumed;
    }
  }

  /**
   * Simple holder for escrow finish result.
   */
  private static class EscrowFinishResult {

    final Hash256 txHash;
    final boolean sequenceConsumed;

    EscrowFinishResult(Hash256 txHash, boolean sequenceConsumed) {
      this.txHash = txHash;
      this.sequenceConsumed = sequenceConsumed;
    }
  }

  /**
   * Creates a Smart Escrow with the specified WASM function.
   *
   * @return EscrowCreationResult with sequenceConsumed=true if sequence was used, false if not submitted
   */
  private EscrowCreationResult createSmartEscrow(
    KeyPair senderKeyPair,
    Address destination,
    String wasmHex,
    WasmFunction function,
    UnsignedInteger sequence
  ) {
    try {
      FeeResult feeResult = xrplClient.fee();

      // Calculate fee for Smart Escrow: base_fee * 10 + 5 drops per byte
      // Per XLS-0100 section 6.2
      long baseFee = feeResult.drops().openLedgerFee().value().longValue();
      long wasmBytes = wasmHex.length() / 2; // Hex string is 2 chars per byte
      long smartEscrowFee = (baseFee * 10) + (5 * wasmBytes);

      ImmutableEscrowCreate.Builder escrowCreateBuilder = EscrowCreate.builder()
        .account(senderKeyPair.publicKey().deriveAddress())
        .sequence(sequence)
        .fee(XrpCurrencyAmount.ofDrops(smartEscrowFee))
        .amount(XrpCurrencyAmount.ofDrops(ESCROW_AMOUNT_DROPS))
        .destination(destination)
        .finishFunction(FinishFunction.of(wasmHex))
        .data(EscrowData.of(createEscrowData(function)))
        .cancelAfter(instantToXrpTimestamp(getMinExpirationTime().plus(Duration.ofMinutes(10))))
        .signingPublicKey(senderKeyPair.publicKey())
        .addMemos(MemoWrapper.builder()
          .memo(Memo.withPlaintext("WASM: " + function.getName()).build())
          .build());

      // Add network ID if present
      networkId.ifPresent(escrowCreateBuilder::networkId);

      EscrowCreate escrowCreate = escrowCreateBuilder.build();

      LOGGER.info(
        "Submitting EscrowCreate - account: {}, sequence: {}, function: {}, amount: {} drops, fee: {} drops, wasm bytes: {}",
        escrowCreate.account(),
        escrowCreate.sequence(),
        function.getName(),
        ESCROW_AMOUNT_DROPS,
        smartEscrowFee,
        wasmBytes);

      LOGGER.debug("Creating EscrowCreate - function: {}, sequence: {}, WASM size: {} bytes, WASM hex prefix: {}",
        function.getName(), sequence, wasmBytes, wasmHex.substring(0, Math.min(40, wasmHex.length())));

      SingleSignedTransaction<EscrowCreate> signedTx = signatureService.sign(
        senderKeyPair.privateKey(),
        escrowCreate
      );

      SubmitResult<EscrowCreate> result;
      try {
        result = xrplClient.submit(signedTx);
      } catch (JsonRpcClientErrorException e) {
        // Submission failed before reaching ledger - sequence NOT consumed
        LOGGER.error("✗ EscrowCreate submission failed - function: {}, sequence: {}, error: {}",
          function.getName(), sequence, e.getMessage());

        // Check if this is a retryable error
        if (isRetryableError(e)) {
          LOGGER.info("Retryable error detected for EscrowCreate");
          throw e; // Propagate to trigger retry
        }

        return new EscrowCreationResult(null, sequence, false);
      }

      // Transaction was submitted to the ledger - check result
      String engineResult = result.engineResult();

      // Handle sequence-related errors
      if (isSequenceRelatedEngineResult(engineResult)) {
        LOGGER.warn("✗ EscrowCreate sequence error - function: {}, result: {}, sequence: {}",
          function.getName(), engineResult, escrowCreate.sequence());
        // Sequence may or may not be consumed depending on the error
        boolean consumed = !engineResult.equals("terPRE_SEQ");
        throw new RuntimeException("Sequence error: " + engineResult);
      }

      if (engineResult.equals("tesSUCCESS")) {
        LOGGER.info("✓ EscrowCreate submitted successfully - hash: {}, sequence: {}, function: {}",
          result.transactionResult().hash(),
          escrowCreate.sequence(),
          function.getName());
        return new EscrowCreationResult(result.transactionResult().hash(), escrowCreate.sequence(), true);
      } else {
        LOGGER.warn("✗ EscrowCreate failed - function: {}, result: {}, sequence: {}",
          function.getName(),
          engineResult,
          escrowCreate.sequence());
        // Sequence was consumed even though transaction failed
        return new EscrowCreationResult(null, escrowCreate.sequence(), true);
      }

    } catch (JsonRpcClientErrorException e) {
      // Re-throw to be handled by retry logic
      throw new RuntimeException(e);
    } catch (Exception e) {
      LOGGER.error("Error creating Smart Escrow - function: {}, sequence: {}",
        function.getName(), sequence, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Attempts to finish a Smart Escrow.
   *
   * @return EscrowFinishResult with sequenceConsumed=true if sequence was used
   */
  private EscrowFinishResult finishSmartEscrow(
    KeyPair finisherKeyPair,
    Address owner,
    UnsignedInteger offerSequence,
    WasmFunction function,
    UnsignedInteger sequence
  ) {
    try {

      FeeResult feeResult = xrplClient.fee();

      // Use gas allowance from the function's configuration
      UnsignedInteger gasAllowance = UnsignedInteger.valueOf(function.getGasAllowance());

      // Calculate fee for Smart Escrow finish
      // Per XLS-0100, EscrowFinish with WASM execution requires higher fee
      // Using max gas allowance to ensure we never run into insufficient fee
      long baseFee = feeResult.drops().openLedgerFee().value().longValue();
      long smartEscrowFinishFee = (baseFee * 100) + gasAllowance.longValue();

      ImmutableEscrowFinish.Builder escrowFinishBuilder = EscrowFinish.builder()
        .account(finisherKeyPair.publicKey().deriveAddress())
        .sequence(sequence)
        .fee(XrpCurrencyAmount.ofDrops(smartEscrowFinishFee))
        .owner(owner)
        .offerSequence(offerSequence)
        .computationAllowance(ComputationAllowance.of(gasAllowance))
        .signingPublicKey(finisherKeyPair.publicKey())
        .addMemos(MemoWrapper.builder()
          .memo(Memo.withPlaintext("WASM: " + function.getName()).build())
          .build());

      // Add network ID if present
      networkId.ifPresent(escrowFinishBuilder::networkId);

      EscrowFinish escrowFinish = escrowFinishBuilder.build();

      LOGGER.info(
        "Submitting EscrowFinish - account: {}, sequence: {}, function: {}, owner: {}, offerSequence: {}, fee: {} drops, gas allowance: {}",
        escrowFinish.account(),
        escrowFinish.sequence(),
        function.getName(),
        owner,
        offerSequence,
        smartEscrowFinishFee,
        gasAllowance);

      SingleSignedTransaction<EscrowFinish> signedTx = signatureService.sign(
        finisherKeyPair.privateKey(),
        escrowFinish
      );

      SubmitResult<EscrowFinish> result;
      try {
        result = xrplClient.submit(signedTx);
      } catch (JsonRpcClientErrorException e) {
        // Submission failed before reaching ledger - sequence NOT consumed
        LOGGER.error("✗ EscrowFinish submission failed - function: {}, sequence: {}, error: {}",
          function.getName(), sequence, e.getMessage());

        // Check if this is a retryable error
        if (isRetryableError(e)) {
          LOGGER.info("Retryable error detected for EscrowFinish");
          throw e; // Propagate to trigger retry
        }

        return new EscrowFinishResult(null, false);
      }

      // Transaction was submitted to the ledger - check result
      Hash256 txHash = result.transactionResult().hash();
      String engineResult = result.engineResult();

      // Handle sequence-related errors
      if (isSequenceRelatedEngineResult(engineResult)) {
        LOGGER.warn("✗ EscrowFinish sequence error - function: {}, result: {}, sequence: {}",
          function.getName(), engineResult, sequence);
        // Sequence may or may not be consumed depending on the error
        boolean consumed = !engineResult.equals("terPRE_SEQ");
        throw new RuntimeException("Sequence error: " + engineResult);
      }

      if (engineResult.equals("tesSUCCESS")) {
        LOGGER.info("✓ EscrowFinish submitted successfully - hash: {}, function: {}",
          txHash,
          function.getName());
        return new EscrowFinishResult(txHash, true);
      } else if (engineResult.equals("tecWASM_REJECTED") && !function.shouldSucceed()) {
        // Expected failure - WASM returned 0 as designed
        LOGGER.info("✓ EscrowFinish WASM rejected as expected - function: {}, result: {}, offerSequence: {}",
          function.getName(),
          engineResult,
          offerSequence);
        return new EscrowFinishResult(txHash, true);
      } else {
        LOGGER.warn("✗ EscrowFinish failed - function: {}, result: {}, offerSequence: {}",
          function.getName(),
          engineResult,
          offerSequence);
        // Sequence was consumed, return hash so we can validate and check the result
        return new EscrowFinishResult(txHash, true);
      }

    } catch (JsonRpcClientErrorException e) {
      // Re-throw to be handled by retry logic
      throw new RuntimeException(e);
    } catch (Exception e) {
      LOGGER.error("Error finishing Smart Escrow", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to repeat a string (Java 8 compatible).
   */
  private String repeat(String str, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(str);
    }
    return sb.toString();
  }

  /**
   * Prints a clean, formatted test header to the console.
   */
  private void printTestHeader() {
    System.out.println();
    System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    SMART ESCROW SOAK TEST                                  ║");
    System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
    System.out.println();
    System.out.println("📋 TEST CONFIGURATION");
    System.out.println("  ├─ Worker Threads:        " + NUM_WORKER_THREADS);
    System.out.println("  ├─ Test Duration:         " + TEST_DURATION_MINUTES + " minutes");
    System.out.println("  ├─ Escrow Amount:         " + String.format("%.4f XRP", ESCROW_AMOUNT_DROPS / 1_000_000.0));
    System.out.println("  ├─ Max Retries:           " + MAX_RETRIES);
    System.out.println("  └─ Sequence Refresh:      Every " + (SEQUENCE_REFRESH_INTERVAL_MS / 1000) + " seconds");
    System.out.println();
    System.out.println("🔧 RESILIENCE FEATURES");
    System.out.println("  ├─ Automatic sequence number refresh");
    System.out.println("  ├─ Exponential backoff retry logic");
    System.out.println("  ├─ Sequence error detection & recovery");
    System.out.println("  └─ Node overload handling");
    System.out.println();
    System.out.println("🧪 WASM FUNCTIONS UNDER TEST");
    for (WasmFunction function : WasmFunction.values()) {
      String expected = function.shouldSucceed() ? "✓ Success" : "✗ Failure";
      String note = "";
      if ("data_counter".equals(function.getName())) {
        note = " [Requires 2 calls - simulates stateful counter]";
      }
      System.out.println("  ├─ " + String.format("%-25s", function.getName()) +
        " (Expected: " + expected + ", Gas: " + function.getGasAllowance() + ")" + note);
    }
    System.out.println();
    System.out.println("⏱️  Starting test run for " + TEST_DURATION_MINUTES + " minutes...");
    System.out.println("   (Metrics will be reported every " + METRICS_REPORT_INTERVAL_SECONDS + " seconds)");
    System.out.println();
    System.out.println(repeat("─", 80));
    System.out.println();
  }

  /**
   * Prints a comprehensive, human-readable test summary to the console.
   */
  private void printTestSummary() {
    int created = escrowsCreated.get();
    int expSuccess = expectedSuccesses.get();
    int expFailure = expectedFailures.get();
    int unexpFailure = unexpectedFailures.get();
    int errorCount = errors.get();
    int seqErrors = sequenceErrors.get();
    int seqRefreshes = sequenceRefreshes.get();
    int retried = retriedTransactions.get();
    long gasUsed = totalGasUsed.get();

    int totalFinished = expSuccess + expFailure + unexpFailure;
    double correctnessRate = totalFinished > 0 ? ((expSuccess + expFailure) * 100.0) / totalFinished : 0;
    double errorRate = created > 0 ? (errorCount * 100.0) / created : 0;
    double retryRate = created > 0 ? (retried * 100.0) / created : 0;
    double avgGas = expSuccess > 0 ? gasUsed / (double) expSuccess : 0;

    System.out.println();
    System.out.println(repeat("─", 80));
    System.out.println();
    System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
    System.out.println("║                    SOAK TEST FINAL SUMMARY                                 ║");
    System.out.println("╚════════════════════════════════════════════════════════════════════════════╝");
    System.out.println();

    // Overall Statistics
    System.out.println("📊 OVERALL STATISTICS");
    System.out.println("  ├─ Total Escrows Created:     " + String.format("%,d", created));
    System.out.println("  ├─ Total Escrows Finished:    " + String.format("%,d", totalFinished));
    System.out.println("  ├─ Test Duration:             " + TEST_DURATION_MINUTES + " minutes");
    System.out.println("  └─ Throughput:                " +
      String.format("%.2f escrows/minute", created / (double) TEST_DURATION_MINUTES));
    System.out.println();

    // Success Metrics
    System.out.println("✅ SUCCESS METRICS");
    System.out.println("  ├─ Expected Successes:        " + String.format("%,d", expSuccess) +
      " (" + String.format("%.1f%%", totalFinished > 0 ? (expSuccess * 100.0) / totalFinished : 0) + ")");
    System.out.println("  ├─ Expected Failures:         " + String.format("%,d", expFailure) +
      " (" + String.format("%.1f%%", totalFinished > 0 ? (expFailure * 100.0) / totalFinished : 0) + ")");
    System.out.println("  └─ Correctness Rate:          " + String.format("%.2f%%", correctnessRate) +
      getHealthIndicator(correctnessRate, 95, 90));
    System.out.println();

    // Error Metrics
    System.out.println("⚠️  ERROR METRICS");
    System.out.println("  ├─ Unexpected Failures:       " + String.format("%,d", unexpFailure) +
      " (" + String.format("%.1f%%", totalFinished > 0 ? (unexpFailure * 100.0) / totalFinished : 0) + ")");
    System.out.println("  ├─ Transaction Errors:        " + String.format("%,d", errorCount) +
      " (" + String.format("%.2f%%", errorRate) + ")" + getHealthIndicator(100 - errorRate, 95, 90));
    System.out.println("  └─ Sequence Errors:           " + String.format("%,d", seqErrors));
    System.out.println();

    // Resilience Metrics
    System.out.println("🛡️  RESILIENCE METRICS");
    System.out.println("  ├─ Retried Transactions:      " + String.format("%,d", retried) +
      " (" + String.format("%.2f%%", retryRate) + ")");
    System.out.println("  ├─ Sequence Refreshes:        " + String.format("%,d", seqRefreshes));
    System.out.println("  └─ Recovery Success Rate:     " +
      String.format("%.2f%%", retried > 0 ? ((retried - errorCount) * 100.0) / retried : 100.0));
    System.out.println();

    // Gas Usage
    System.out.println("⛽ GAS USAGE");
    System.out.println("  ├─ Total Gas Used:            " + String.format("%,d", gasUsed));
    System.out.println("  ├─ Average Gas per Success:   " + String.format("%,.0f", avgGas));
    System.out.println("  └─ Gas Efficiency:            " +
      String.format("%.2f gas/escrow", totalFinished > 0 ? gasUsed / (double) totalFinished : 0));
    System.out.println();

    // Per-Function Breakdown
    System.out.println("📈 PER-FUNCTION BREAKDOWN");
    System.out.println();
    System.out.println("  Function                    Expected ✓   Expected ✗   Unexpected ✗   Total");
    System.out.println("  " + repeat("─", 76));

    for (WasmFunction function : WasmFunction.values()) {
      String name = function.getName();
      int funcExpSuccess = functionExpectedSuccessCount.getOrDefault(name, new AtomicInteger(0)).get();
      int funcExpFailure = functionExpectedFailureCount.getOrDefault(name, new AtomicInteger(0)).get();
      int funcUnexpFailure = functionUnexpectedFailureCount.getOrDefault(name, new AtomicInteger(0)).get();
      int funcTotal = funcExpSuccess + funcExpFailure + funcUnexpFailure;

      if (funcTotal > 0) {
        System.out.println("  " + String.format("%-25s", name) +
          String.format("%12d", funcExpSuccess) +
          String.format("%14d", funcExpFailure) +
          String.format("%16d", funcUnexpFailure) +
          String.format("%8d", funcTotal));
      }
    }
    System.out.println();

    // Overall Assessment
    System.out.println("🎯 OVERALL ASSESSMENT");
    String assessment = getOverallAssessment(correctnessRate, errorRate);
    System.out.println("  " + assessment);
    System.out.println();

    System.out.println(repeat("─", 80));
    System.out.println();
    System.out.println("✨ Soak test completed successfully!");
    System.out.println();
  }

  /**
   * Returns a health indicator emoji based on the value and thresholds.
   */
  private String getHealthIndicator(double value, double goodThreshold, double okThreshold) {
    if (value >= goodThreshold) {
      return " 🟢";
    } else if (value >= okThreshold) {
      return " 🟡";
    } else {
      return " 🔴";
    }
  }

  /**
   * Returns an overall assessment message based on test results.
   */
  private String getOverallAssessment(double correctnessRate, double errorRate) {
    if (correctnessRate >= 95 && errorRate < 5) {
      return "🟢 EXCELLENT - Test performed exceptionally well with high correctness and low errors.";
    } else if (correctnessRate >= 90 && errorRate < 10) {
      return "🟡 GOOD - Test performed well with acceptable correctness and error rates.";
    } else if (correctnessRate >= 80 && errorRate < 20) {
      return "🟠 FAIR - Test completed but showed some issues. Review error logs for details.";
    } else {
      return "🔴 NEEDS ATTENTION - Test showed significant issues. Review logs and consider:\n" +
        "     • Reducing worker threads to lower node load\n" +
        "     • Increasing retry limits and backoff times\n" +
        "     • Checking node health and network connectivity";
    }
  }

  /**
   * Checks if an error is retryable (e.g., network errors, node overload).
   */
  private boolean isRetryableError(JsonRpcClientErrorException e) {
    String message = e.getMessage();
    if (message == null) {
      return false;
    }
    // Check for common retryable errors
    return message.contains("500") ||
      message.contains("503") ||
      message.contains("too busy") ||
      message.contains("timeout") ||
      message.contains("connection") ||
      message.contains("Internal error");
  }

  /**
   * Checks if an engine result is sequence-related.
   */
  private boolean isSequenceRelatedEngineResult(String engineResult) {
    return "tefPAST_SEQ".equals(engineResult) ||
      "terPRE_SEQ".equals(engineResult) ||
      "terQUEUED".equals(engineResult) ||
      "tefMAX_LEDGER".equals(engineResult);
  }

  /**
   * Checks if an engine result is reserve-related and should trigger backoff.
   */
  private boolean isReserveRelatedEngineResult(String engineResult) {
    return "tecINSUFFICIENT_RESERVE".equals(engineResult) ||
      "tecINSUF_RESERVE_LINE".equals(engineResult) ||
      "tecINSUF_RESERVE_OFFER".equals(engineResult);
  }

  /**
   * Selects a random WASM function to test.
   */
  private WasmFunction selectRandomFunction() {
    WasmFunction[] functions = WasmFunction.values();
    return functions[(int) (Math.random() * functions.length)];
  }

  /**
   * Creates escrow data for the specified function. This is a placeholder - in a real implementation, this would encode
   * function-specific parameters.
   */
  private String createEscrowData(WasmFunction function) {
    // Simple placeholder: just encode the function name
    return BaseEncoding.base16().encode(function.getName().getBytes());
  }


  /**
   * Loads all WASM functions from resources. Each function has its own compiled WASM file.
   *
   * Testing real WASM files one at a time to see which ones work on the devnet.
   */
  private Map<String, String> loadAllWasmFunctions() throws IOException {
    Map<String, String> wasmHexByFunction = new HashMap<>();

    // List of functions to try loading real WASM for
    // Start with simple ones, add more as they work
    Set<String> useRealWasm = new HashSet<>();
    useRealWasm.add("always_succeed");
    useRealWasm.add("gas_stress_test");
    useRealWasm.add("balance_check");
    useRealWasm.add("credential_check");
    useRealWasm.add("time_window");
    useRealWasm.add("data_counter");
    useRealWasm.add("oracle_price_check");

    for (WasmFunction function : WasmFunction.values()) {
      String functionName = function.getName();

      if (useRealWasm.contains(functionName)) {
        // Try to load real compiled WASM
        String resourcePath = "/wasm/" + functionName + "_optimized.wasm";
        InputStream stream = getClass().getResourceAsStream(resourcePath);

        if (stream == null) {
          // Fall back to non-optimized version
          resourcePath = "/wasm/" + functionName + ".wasm";
          stream = getClass().getResourceAsStream(resourcePath);
        }

        if (stream != null) {
          byte[] wasmBytes = stream.readAllBytes();
          String hexEncoded = BaseEncoding.base16().encode(wasmBytes);
          LOGGER.info("Loaded real WASM for {}: {} bytes from {}", functionName, wasmBytes.length, resourcePath);
          wasmHexByFunction.put(functionName, hexEncoded);
          stream.close();
        } else {
          LOGGER.warn("Real WASM file not found for {}, using stub", functionName);
          byte[] stubBytes = createMinimalWasmStub(function.shouldSucceed() ? 1 : 0);
          wasmHexByFunction.put(functionName, BaseEncoding.base16().encode(stubBytes));
        }
      } else {
        // Use minimal stub
        LOGGER.info("Using minimal stub for function: {}", functionName);
        byte[] stubBytes = createMinimalWasmStub(function.shouldSucceed() ? 1 : 0);
        String hexEncoded = BaseEncoding.base16().encode(stubBytes);
        LOGGER.debug("Created stub for {}: {} bytes", functionName, stubBytes.length);
        wasmHexByFunction.put(functionName, hexEncoded);
      }
    }

    return wasmHexByFunction;
  }

  /**
   * Creates a minimal WASM stub for testing when no compiled WASM is available. This is a valid WASM module that
   * exports a "finish" function that returns the specified value.
   *
   * @param returnValue The value to return (0 for failure, 1 for success)
   */
  private byte[] createMinimalWasmStub(int returnValue) {
    // Minimal WASM module: (module (func (export "finish") (result i32) i32.const <returnValue>))
    // The function MUST be named "finish" per XLS-0100 specification
    return new byte[] {
      0x00, 0x61, 0x73, 0x6d, // Magic number
      0x01, 0x00, 0x00, 0x00, // Version
      0x01, 0x05, 0x01, 0x60, 0x00, 0x01, 0x7f, // Type section: function type (no params, returns i32)
      0x03, 0x02, 0x01, 0x00, // Function section: 1 function with type 0
      0x07, 0x0a, 0x01, 0x06, 0x66, 0x69, 0x6e, 0x69, 0x73, 0x68, 0x00, 0x00,
      // Export section: export function 0 as "finish"
      0x0a, 0x06, 0x01, 0x04, 0x00, 0x41, (byte) returnValue, 0x0b
      // Code section: function body (i32.const <returnValue>, end)
    };
  }

  /**
   * Reports metrics periodically.
   */
  private void reportMetricsPeriodically() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(METRICS_REPORT_INTERVAL_SECONDS));
        reportMetrics();
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  /**
   * Reports current metrics in a clean, readable format.
   */
  private void reportMetrics() {
    int created = escrowsCreated.get();
    int expSuccess = expectedSuccesses.get();
    int expFailure = expectedFailures.get();
    int unexpFailure = unexpectedFailures.get();
    int errorCount = errors.get();
    int seqErrors = sequenceErrors.get();
    int seqRefreshes = sequenceRefreshes.get();
    int retried = retriedTransactions.get();
    long gasUsed = totalGasUsed.get();

    int totalFinished = expSuccess + expFailure + unexpFailure;
    double correctnessRate = totalFinished > 0 ? ((expSuccess + expFailure) * 100.0) / totalFinished : 0;
    double errorRate = created > 0 ? (errorCount * 100.0) / created : 0;
    double retryRate = created > 0 ? (retried * 100.0) / created : 0;
    double avgGas = expSuccess > 0 ? gasUsed / (double) expSuccess : 0;

    System.out.println();
    System.out.println("📊 PROGRESS UPDATE - " + java.time.LocalDateTime.now().format(
      java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    System.out.println("  Created: " + String.format("%,5d", created) +
      " | Finished: " + String.format("%,5d", totalFinished) +
      " | Success: " + String.format("%,5d", expSuccess) +
      " | Errors: " + String.format("%,4d", errorCount) +
      " (" + String.format("%.1f%%", errorRate) + ")");
    System.out.println("  Correctness: " + String.format("%.1f%%", correctnessRate) +
      " | Retries: " + String.format("%,4d", retried) +
      " (" + String.format("%.1f%%", retryRate) + ")" +
      " | Seq Errors: " + String.format("%,3d", seqErrors) +
      " | Avg Gas: " + String.format("%,.0f", avgGas));
    System.out.println();
  }
}

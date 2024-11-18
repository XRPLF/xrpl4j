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

import static org.slf4j.LoggerFactory.getLogger;

import com.github.dockerjava.api.command.CreateContainerCmd;
import okhttp3.HttpUrl;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplAdminClient;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.client.serverinfo.ReportingModeServerInfo;
import org.xrpl.xrpl4j.model.client.serverinfo.RippledServerInfo;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service to start rippled inside docker using testcontainers.
 */
public class RippledContainer {

  // Seed for the Master/Root wallet in the rippled docker container.
  public static final String MASTER_WALLET_SEED = "snoPBrXtMeMyMHUVTgbuqAfg1SUTb";
  private static final Logger LOGGER = getLogger(RippledContainer.class);
  private static final Consumer<RippledContainer> LEDGER_ACCEPTOR = (rippledContainer) -> {
    try {
      rippledContainer.getXrplAdminClient().acceptLedger();
    } catch (RuntimeException | JsonRpcClientErrorException e) {
      LOGGER.warn("Ledger accept failed", e);
    }
  };
  private final GenericContainer<?> rippledContainer;
  private final ScheduledExecutorService ledgerAcceptor;
  private boolean started;

  /**
   * No-args constructor.
   */
  public RippledContainer() {
    rippledContainer = new GenericContainer<>("rippleci/rippled:2.2.0-rc3")
      .withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) (cmd) ->
        cmd.withEntrypoint("/opt/ripple/bin/rippled"))
      .withCommand("-a --start --conf /config/rippled.cfg")
      .withExposedPorts(5005)
      .withClasspathResourceMapping("rippled",
        "/config",
        BindMode.READ_ONLY)
      .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Application starting.*"));
    ledgerAcceptor = Executors.newScheduledThreadPool(1);
  }

  /**
   * Get the {@link KeyPair} of the master account.
   *
   * @return The {@link KeyPair} of the master account.
   */
  public static KeyPair getMasterKeyPair() {
    return Seed.fromBase58EncodedSecret(Base58EncodedSecret.of(MASTER_WALLET_SEED)).deriveKeyPair();
  }

  /**
   * Starts container with default interval (1s) for closing ledgers.
   */
  public RippledContainer start() {
    return this.start(1000);
  }

  /**
   * Start contain with given interval for closing ledgers.
   *
   * @param acceptIntervalMillis The number of milliseconds before each accept call to close the ledger.
   *
   * @return A {@link RippledContainer}.
   */
  public RippledContainer start(int acceptIntervalMillis) {
    if (started) {
      throw new IllegalStateException("container already started");
    }
    started = true;
    rippledContainer.start();

    // rippled is run in standalone mode which means that ledgers won't automatically close. You have to manually
    // advance the ledger using the "ledger_accept" method on the admin API. To mimic the behavior of a networked
    // rippled, run a scheduled task to trigger the "ledger_accept" method.
    ledgerAcceptor.scheduleAtFixedRate(() -> LEDGER_ACCEPTOR.accept(this),
      acceptIntervalMillis,
      acceptIntervalMillis,
      TimeUnit.MILLISECONDS);
    waitForLedgerTimeToSync();
    return this;
  }

  /**
   * Waits until the system time in the docker container matches the host's time.
   */
  private void waitForLedgerTimeToSync() {
    Awaitility.await()
      .pollDelay(1, TimeUnit.SECONDS)
      .atMost(10, TimeUnit.SECONDS)
      .until(() -> Duration.between(getLedgerTime().toInstant(), Instant.now()).abs().getSeconds() < 1);
  }

  private ZonedDateTime getLedgerTime() {
    try {
      return getXrplClient().serverInformation().info()
        .map(
          RippledServerInfo::time,
          clioServerInfo -> ZonedDateTime.now(),
          ReportingModeServerInfo::time
        );
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Shutdown all TestContainers.
   */
  public void shutdown() {
    assertContainerStarted();
    ledgerAcceptor.shutdownNow();
    rippledContainer.stop();
    started = false;
  }

  private void assertContainerStarted() {
    if (!started) {
      throw new IllegalStateException("container not started");
    }
  }

  /**
   * Provides an instance of an {@link XrplAdminClient} that will connect to the rippled container.
   *
   * @return A {@link XrplAdminClient}.
   */
  public XrplAdminClient getXrplAdminClient() {
    return new XrplAdminClient(this.getBaseUri());
  }

  /**
   * Provides an instance of an {@link XrplClient} that will connect to the rippled container.
   *
   * @return A {@link XrplAdminClient}.
   */
  public XrplClient getXrplClient() {
    return new XrplClient(this.getBaseUri());
  }

  private static HttpUrl getBaseUri(GenericContainer<?> rippledContainer) {
    return HttpUrl.parse("http://" + rippledContainer.getHost() + ":" + rippledContainer.getMappedPort(5005) + "/");
  }

  public HttpUrl getBaseUri() {
    assertContainerStarted();
    return getBaseUri(rippledContainer);
  }

  /**
   * Exposed method to accept next ledger ad hoc.
   */
  public void acceptLedger() {
    assertContainerStarted();
    LEDGER_ACCEPTOR.accept(this);
  }
}

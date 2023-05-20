package org.xrpl.xrpl4j.client.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.websocket.model.AccountTransactionsRequest;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration test that connects to s1.ripple.com and looks for account transactions.
 * <p>
 * TODO: Move to ITs
 */
class XrplWsClientTest {
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  
  XrplWsClient xrplWsClient;
  
  @BeforeEach
  void setUp() {
    xrplWsClient = new XrplWsClient(HttpUrl.parse("https://s1.ripple.com"));
  }
  
  @Test
  void subscribeToAccountTransactions() throws InterruptedException, JsonProcessingException {
    
    final CountDownLatch numRequestsLatch = new CountDownLatch(20);
    
    WebSocket websocket = xrplWsClient.subscribeToAccountTransactions(
      AccountTransactionsRequest.unboundedBuilder()
        .id(UnsignedInteger.ONE)
        .account(Address.of("rLNaPoKeeBjZe2qs6x52yVPZpZ8td4dc6w"))
        .limit(UnsignedInteger.valueOf(20))
        .build(),
      new WebSocketListener() {
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
          numRequestsLatch.countDown();
          LOGGER.info("Text: " + text);
        }
        
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
          numRequestsLatch.countDown();
          LOGGER.info("Bytes: " + bytes);
        }
      }
    );
    
    numRequestsLatch.await(15, TimeUnit.SECONDS);
    
  }
}
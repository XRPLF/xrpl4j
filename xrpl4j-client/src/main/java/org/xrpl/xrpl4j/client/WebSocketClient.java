package org.xrpl.xrpl4j.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import feign.codec.EncodeException;
import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Future;
import org.atmosphere.wasync.OptionsBuilder;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.WebSocketRequest;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@VisibleForTesting
public class WebSocketClient {

  private final Socket socket;
  private final ObjectMapper objectMapper;
  private final RequestBuilder request;
  Logger logger = LoggerFactory.getLogger(WebSocketClient.class);

  WebSocketClient() throws Exception {

    try {
      //AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
      //Client client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
      Client client = ClientFactory.getDefault().newClient();

      this.objectMapper = ObjectMapperFactory.create();

      request = client.newRequestBuilder()
        .method(Request.METHOD.GET)
        .uri("wss://s.altnet.rippletest.net/")
//      .encoder((Encoder<WebSocketRequest, String>) request -> {
//          try {
//            System.out.println("encoded" + objectMapper.writeValueAsString(request));
//            return objectMapper.writeValueAsString(request);
//          } catch (JsonProcessingException e) {
//            throw new EncodeException(e.getMessage(), e);
//          }
//      })
        .encoder(new Encoder<WebSocketRequest, String>() {

          @Override
          public String encode(WebSocketRequest request) {
            try {
              System.out.println("encoded: " + objectMapper.writeValueAsString(request));
              return objectMapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
              e.printStackTrace();
              throw new EncodeException(e.getMessage(), e);
            }
          }
        })
//      .decoder((Decoder<Socket, Object>) (o, T) -> {
//        try {
//          return objectMapper.readValue(o.toString(), T.getClass());
//        } catch (JsonProcessingException e) {
//          throw new EncodeException(e.getMessage(), e);
//        }
//      })
        .transport(Request.TRANSPORT.WEBSOCKET);

      OptionsBuilder builder = client.newOptionsBuilder();
      builder.requestTimeoutInSeconds(10);

      socket = client.create(builder.build());
      logger.info("WebSocket Client created.");

      socket.on(Event.MESSAGE.name(), (Function<String>) res -> {
        logger.info("Message received from server:");
        System.out.println(res);
      });


      socket.on(Event.TRANSPORT.name(), new Function<String>() {
          @Override
          public void on(String transport) {
            logger.info("Transport:" + transport);
          }
        }).on(Event.ERROR, new Function<String>() {

          @Override
          public void on(String error) {
            System.out.println("Error: " + error);
            try {
              socket.open(request.build(), 60, TimeUnit.SECONDS);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }).on(Event.CLOSE.name(), (Function<String>) t -> logger.info("Connection closed"))
        .on(Event.OPEN.name(), (Function<String>) t -> logger.info("Connection opened"))
        .open(request.build());

    } catch (Exception e) {
      throw new Exception(e);
    }

  }

  void close() {
    socket.close();
  }

  String status() {
    return socket.status().toString();
  }

  boolean isFutureDone(Future future) {
    return future.isDone();
  }

  Future send(WebSocketRequest request) {
    logger.info("Request received: " + request);

    try {
      return socket.fire(request);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
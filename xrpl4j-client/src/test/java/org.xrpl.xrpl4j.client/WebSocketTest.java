package org.xrpl.xrpl4j.client;

import org.atmosphere.wasync.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.WebSocketRequest;

import java.util.ArrayList;
import java.util.List;

class WebSocketTest {

  @Test
  public void test() throws Exception {
    WebSocketClient client = new WebSocketClient();
    String[] addresses = new String[] {"rPmPErQe4g9725pcNxJpuvKkdqTESTQ6Tu", "rfo6ATr9xQFdd9goNPCwPigbXmwWJKeS3Y",
      "rpAy69hbgYLprDFVEK59Vkjr9UhZoX8P8Y", "rBnwU3mjvvhrABDyPvApYqEhn9RUMWFgEo", "r45fqc6ro75RgfsUuBBchRPXW6s2ZnckTe",
      "r45fqc6ro75RgfsUuBBchRPXW6s2ZnckTe", "rL7c1TArabrZXS3yYXd3iMDTs6czZaoLv8", "rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe",
      "rnJf62WZaYFZgJYvsrA5jfNDoi1v1MSBog", "r96tbH8ohNuBK7j6L7p86hV782QfaFfdUo", "rMNT29AXg211DKko8bkWSxuywcKGE2SenU"};

    List<Future> futures = new ArrayList<>();

    for(int i = 0; i < addresses.length; i++){
      AccountInfoRequestParams accountInfoRequestParams = AccountInfoRequestParams.of(Address.of(addresses[i]));
      WebSocketRequest request = WebSocketRequest.builder()
        .command(XrplMethods.ACCOUNT_INFO)
        .params(accountInfoRequestParams)
        .build();

      futures.add(client.send(request));
      System.out.println(futures.get(i).isDone());
      System.out.println(i + " " + client.status());
    }
    
    Thread.sleep(200);
    client.close();
    System.out.println("closed");
  }
}
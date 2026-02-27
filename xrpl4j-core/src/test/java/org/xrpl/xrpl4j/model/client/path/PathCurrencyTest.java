package org.xrpl.xrpl4j.model.client.path;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.ledger.CurrencyIssue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link PathCurrency}.
 */
public class PathCurrencyTest extends AbstractJsonTest {

  @Test
  public void ofCurrencyOnly() {
    PathCurrency pathCurrency = PathCurrency.of("USD");
    
    assertThat(pathCurrency.issue()).isInstanceOf(CurrencyIssue.class);
    CurrencyIssue currencyIssue = (CurrencyIssue) pathCurrency.issue();
    assertThat(currencyIssue.currency()).isEqualTo("USD");
    assertThat(currencyIssue.issuer()).isEmpty();
  }

  @Test
  public void ofCurrencyAndIssuer() {
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    PathCurrency pathCurrency = PathCurrency.of("USD", issuer);
    
    assertThat(pathCurrency.issue()).isInstanceOf(CurrencyIssue.class);
    CurrencyIssue currencyIssue = (CurrencyIssue) pathCurrency.issue();
    assertThat(currencyIssue.currency()).isEqualTo("USD");
    assertThat(currencyIssue.issuer()).isPresent().get().isEqualTo(issuer);
  }

  @Test
  public void ofCurrencyIssue() {
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    CurrencyIssue currencyIssue = CurrencyIssue.builder()
      .currency("EUR")
      .issuer(issuer)
      .build();
    
    PathCurrency pathCurrency = PathCurrency.of(currencyIssue);
    
    assertThat(pathCurrency.issue()).isEqualTo(currencyIssue);
    assertThat(pathCurrency.issue()).isInstanceOf(CurrencyIssue.class);
  }

  @Test
  public void ofMptIssue() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    MptIssue mptIssue = MptIssue.of(mptId);
    
    PathCurrency pathCurrency = PathCurrency.of(mptIssue);
    
    assertThat(pathCurrency.issue()).isEqualTo(mptIssue);
    assertThat(pathCurrency.issue()).isInstanceOf(MptIssue.class);
  }

  @Test
  public void serializeCurrencyOnly() throws JsonProcessingException, JSONException {
    PathCurrency pathCurrency = PathCurrency.of("XRP");

    String json = "{\"currency\":\"XRP\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeCurrencyWithIssuer() throws JsonProcessingException, JSONException {
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    PathCurrency pathCurrency = PathCurrency.of("USD", issuer);

    String json = "{\"currency\":\"USD\",\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeMptIssue() throws JsonProcessingException, JSONException {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    PathCurrency pathCurrency = PathCurrency.of(MptIssue.of(mptId));

    String json = "{\"mpt_issuance_id\":\"00000002430427B80BD2D09D36B70B969E12801065F22308\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeHexCurrencyCode() throws JsonProcessingException, JSONException {
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    String hexCurrency = "015841551A748AD2C1F76FF6ECB0CCCD00000000";
    PathCurrency pathCurrency = PathCurrency.of(hexCurrency, issuer);

    String json = "{\"currency\":\"015841551A748AD2C1F76FF6ECB0CCCD00000000\"," +
                  "\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void builder() {
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    CurrencyIssue currencyIssue = CurrencyIssue.builder()
      .currency("GBP")
      .issuer(issuer)
      .build();
    
    PathCurrency pathCurrency = PathCurrency.builder()
      .issue(currencyIssue)
      .build();
    
    assertThat(pathCurrency.issue()).isEqualTo(currencyIssue);
  }
}


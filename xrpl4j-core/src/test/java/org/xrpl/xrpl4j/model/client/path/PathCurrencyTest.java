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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link PathCurrency}.
 */
public class PathCurrencyTest extends AbstractJsonTest {

  private static final Address ISSUER = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
  private static final String MPT_ID = "00000002430427B80BD2D09D36B70B969E12801065F22308";

  @Test
  public void ofCurrencyOnly() {
    PathCurrency pathCurrency = PathCurrency.of("XRP");

    assertThat(pathCurrency.currency()).hasValue("XRP");
    assertThat(pathCurrency.issuer()).isEmpty();
    assertThat(pathCurrency.mptIssuanceId()).isEmpty();
  }

  @Test
  public void ofCurrencyOnlyNonXrp() {
    // A bare currency code means "this currency, from any issuer" in source_currencies.
    PathCurrency pathCurrency = PathCurrency.of("USD");

    assertThat(pathCurrency.currency()).hasValue("USD");
    assertThat(pathCurrency.issuer()).isEmpty();
    assertThat(pathCurrency.mptIssuanceId()).isEmpty();
  }

  @Test
  public void ofNullThrows() {
    assertThatThrownBy(() -> PathCurrency.of((Issue) null))
      .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> PathCurrency.of((String) null))
      .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> PathCurrency.of("USD", null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void ofCurrencyAndIssuer() {
    PathCurrency pathCurrency = PathCurrency.of("USD", ISSUER);

    assertThat(pathCurrency.currency()).hasValue("USD");
    assertThat(pathCurrency.issuer()).hasValue(ISSUER);
    assertThat(pathCurrency.mptIssuanceId()).isEmpty();
  }

  @Test
  public void ofXrpIssue() {
    PathCurrency pathCurrency = PathCurrency.of(Issue.XRP);

    assertThat(pathCurrency.currency()).hasValue("XRP");
    assertThat(pathCurrency.issuer()).isEmpty();
  }

  @Test
  public void ofIouIssue() {
    IouIssue iouIssue = IouIssue.builder().currency("EUR").issuer(ISSUER).build();

    PathCurrency pathCurrency = PathCurrency.of(iouIssue);

    assertThat(pathCurrency.currency()).hasValue("EUR");
    assertThat(pathCurrency.issuer()).hasValue(ISSUER);
  }

  @Test
  public void ofMptIssue() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of(MPT_ID);

    PathCurrency pathCurrency = PathCurrency.of(MptIssue.of(mptId));

    assertThat(pathCurrency.mptIssuanceId()).hasValue(mptId);
    assertThat(pathCurrency.currency()).isEmpty();
    assertThat(pathCurrency.issuer()).isEmpty();
  }

  @Test
  public void cannotBuildWithNeitherCurrencyNorMptIssuanceId() {
    assertThatThrownBy(() -> PathCurrency.builder().build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("exactly one of currency or mptIssuanceId");
  }

  @Test
  public void cannotBuildWithBothCurrencyAndMptIssuanceId() {
    assertThatThrownBy(() -> PathCurrency.builder()
      .currency("USD")
      .mptIssuanceId(MpTokenIssuanceId.of(MPT_ID))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("exactly one of currency or mptIssuanceId");
  }

  @Test
  public void cannotBuildMptWithIssuer() {
    assertThatThrownBy(() -> PathCurrency.builder()
      .mptIssuanceId(MpTokenIssuanceId.of(MPT_ID))
      .issuer(ISSUER)
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("cannot have an issuer");
  }

  @Test
  public void serializeCurrencyOnly() throws JsonProcessingException, JSONException {
    PathCurrency pathCurrency = PathCurrency.of("XRP");

    String json = "{\"currency\":\"XRP\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeCurrencyWithoutIssuer() throws JsonProcessingException, JSONException {
    PathCurrency pathCurrency = PathCurrency.of("USD");

    String json = "{\"currency\":\"USD\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeCurrencyWithIssuer() throws JsonProcessingException, JSONException {
    PathCurrency pathCurrency = PathCurrency.of("USD", ISSUER);

    String json = "{\"currency\":\"USD\",\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeMptIssue() throws JsonProcessingException, JSONException {
    PathCurrency pathCurrency = PathCurrency.of(MptIssue.of(MpTokenIssuanceId.of(MPT_ID)));

    String json = "{\"mpt_issuance_id\":\"" + MPT_ID + "\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void serializeHexCurrencyCode() throws JsonProcessingException, JSONException {
    String hexCurrency = "015841551A748AD2C1F76FF6ECB0CCCD00000000";
    PathCurrency pathCurrency = PathCurrency.of(hexCurrency, ISSUER);

    String json = "{\"currency\":\"015841551A748AD2C1F76FF6ECB0CCCD00000000\"," +
                  "\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    assertCanSerializeAndDeserialize(pathCurrency, json, PathCurrency.class);
  }

  @Test
  public void builder() {
    PathCurrency pathCurrency = PathCurrency.builder()
      .currency("GBP")
      .issuer(ISSUER)
      .build();

    assertThat(pathCurrency.currency()).hasValue("GBP");
    assertThat(pathCurrency.issuer()).hasValue(ISSUER);
  }
}

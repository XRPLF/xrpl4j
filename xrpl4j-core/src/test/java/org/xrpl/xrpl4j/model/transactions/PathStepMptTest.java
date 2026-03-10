package org.xrpl.xrpl4j.model.transactions;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

/**
 * Unit tests for {@link PathStep} with MPT (Multi-Purpose Token) support.
 * 
 * <p>This test verifies that PathStep correctly handles mpt_issuance_id and validates
 * mutual exclusivity with account, currency, and issuer fields per the XLS-82d spec.</p>
 */
public class PathStepMptTest extends AbstractJsonTest {

  @Test
  public void mptIssuanceIdOnly() throws JsonProcessingException, JSONException {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    PathStep pathStep = PathStep.builder()
      .mptIssuanceId(mptId)
      .build();

    assertThat(pathStep.mptIssuanceId()).isPresent();
    assertThat(pathStep.mptIssuanceId().get()).isEqualTo(mptId);
    assertThat(pathStep.account()).isEmpty();
    assertThat(pathStep.currency()).isEmpty();
    assertThat(pathStep.issuer()).isEmpty();

    String json = "{\"mpt_issuance_id\":\"00000002430427B80BD2D09D36B70B969E12801065F22308\"}";
    assertCanSerializeAndDeserialize(pathStep, json, PathStep.class);
  }

  @Test
  public void mptIssuanceIdWithIssuerShouldFail() {
    // According to PathStep validation, mpt_issuance_id is mutually exclusive with issuer
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    assertThrows(IllegalArgumentException.class, () -> {
      PathStep.builder()
        .mptIssuanceId(mptId)
        .issuer(issuer)
        .build();
    });
  }

  @Test
  public void mptIssuanceIdWithCurrencyShouldFail() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");

    assertThrows(IllegalArgumentException.class, () -> {
      PathStep.builder()
        .mptIssuanceId(mptId)
        .currency("USD")
        .build();
    });
  }

  @Test
  public void mptIssuanceIdWithAccountShouldFail() {
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    Address account = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");

    assertThrows(IllegalArgumentException.class, () -> {
      PathStep.builder()
        .mptIssuanceId(mptId)
        .account(account)
        .build();
    });
  }

  /**
   * CRITICAL TEST: Verify that if ripple_path_find returns both mpt_issuance_id AND issuer,
   * the deserialization will fail due to PathStep validation.
   * 
   * <p>This test documents the potential issue: if the spec says ripple_path_find returns
   * issuer along with mpt_issuance_id, but PathStep enforces mutual exclusivity, then
   * deserialization will break.</p>
   */
  @Test
  public void deserializeMptWithIssuerShouldFail() {
    // This JSON represents what ripple_path_find might return according to spec
    String jsonWithBothFields = "{\"mpt_issuance_id\":\"00000002430427B80BD2D09D36B70B969E12801065F22308\"," +
                                "\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";

    // This should fail because PathStep validation enforces mutual exclusivity
    // Jackson wraps the IllegalArgumentException in ValueInstantiationException
    ValueInstantiationException exception = assertThrows(ValueInstantiationException.class, () -> {
      objectMapper.readValue(jsonWithBothFields, PathStep.class);
    });

    // Verify the root cause is IllegalArgumentException from PathStep validation
    assertThat(exception.getCause()).isInstanceOf(IllegalArgumentException.class);
    assertThat(exception.getCause().getMessage()).contains("mpt_issuance_id is mutually exclusive");
  }

  @Test
  public void currencyWithIssuer() throws JsonProcessingException, JSONException {
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    PathStep pathStep = PathStep.builder()
      .currency("USD")
      .issuer(issuer)
      .build();

    assertThat(pathStep.currency()).isPresent();
    assertThat(pathStep.currency().get()).isEqualTo("USD");
    assertThat(pathStep.issuer()).isPresent();
    assertThat(pathStep.issuer().get()).isEqualTo(issuer);
    assertThat(pathStep.mptIssuanceId()).isEmpty();

    String json = "{\"currency\":\"USD\",\"issuer\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";
    assertCanSerializeAndDeserialize(pathStep, json, PathStep.class);
  }

  @Test
  public void accountOnly() throws JsonProcessingException, JSONException {
    Address account = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    PathStep pathStep = PathStep.builder()
      .account(account)
      .build();

    assertThat(pathStep.account()).isPresent();
    assertThat(pathStep.account().get()).isEqualTo(account);
    assertThat(pathStep.mptIssuanceId()).isEmpty();

    String json = "{\"account\":\"rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk\"}";
    assertCanSerializeAndDeserialize(pathStep, json, PathStep.class);
  }
}


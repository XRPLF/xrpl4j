package org.xrpl.xrpl4j.model.jackson.modules;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.path.PathCurrency;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.io.IOException;

/**
 * Unit tests for {@link PathCurrencySerializer}.
 */
class PathCurrencySerializerTest {

  private PathCurrencySerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new PathCurrencySerializer();
  }

  @Test
  void testSerializeCurrencyOnly() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    PathCurrency pathCurrency = PathCurrency.of("XRP");

    serializer.serialize(pathCurrency, jsonGeneratorMock, mock(SerializerProvider.class));

    verify(jsonGeneratorMock).writeStartObject();
    verify(jsonGeneratorMock).writeStringField("currency", "XRP");
    verify(jsonGeneratorMock).writeEndObject();
  }

  @Test
  void testSerializeCurrencyWithIssuer() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    PathCurrency pathCurrency = PathCurrency.of("USD", issuer);

    serializer.serialize(pathCurrency, jsonGeneratorMock, mock(SerializerProvider.class));

    verify(jsonGeneratorMock).writeStartObject();
    verify(jsonGeneratorMock).writeStringField("currency", "USD");
    verify(jsonGeneratorMock).writeStringField("issuer", "rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    verify(jsonGeneratorMock).writeEndObject();
  }

  @Test
  void testSerializeMptIssue() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    PathCurrency pathCurrency = PathCurrency.of(MptIssue.of(mptId));

    serializer.serialize(pathCurrency, jsonGeneratorMock, mock(SerializerProvider.class));

    verify(jsonGeneratorMock).writeStartObject();
    verify(jsonGeneratorMock).writeStringField("mpt_issuance_id", "00000002430427B80BD2D09D36B70B969E12801065F22308");
    verify(jsonGeneratorMock).writeEndObject();
  }

  @Test
  void testSerializeCurrencyIssueThrowsIoException() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    Address issuer = Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMfXoKk");
    PathCurrency pathCurrency = PathCurrency.of("USD", issuer);

    // Mock IOException when writing currency field
    doThrow(new IOException("Test IOException")).when(jsonGeneratorMock).writeStringField("currency", "USD");

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> serializer.serialize(pathCurrency, jsonGeneratorMock, mock(SerializerProvider.class))
    );

    assertThat(exception.getMessage()).isEqualTo("Error serializing IouIssue");
    assertThat(exception.getCause()).isInstanceOf(IOException.class);
    assertThat(exception.getCause().getMessage()).isEqualTo("Test IOException");
  }

  @Test
  void testSerializeMptIssueThrowsIoException() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    MpTokenIssuanceId mptId = MpTokenIssuanceId.of("00000002430427B80BD2D09D36B70B969E12801065F22308");
    PathCurrency pathCurrency = PathCurrency.of(MptIssue.of(mptId));

    // Mock IOException when writing mpt_issuance_id field
    doThrow(new IOException("Test MPT IOException")).when(jsonGeneratorMock)
      .writeStringField("mpt_issuance_id", "00000002430427B80BD2D09D36B70B969E12801065F22308");

    RuntimeException exception = assertThrows(
      RuntimeException.class,
      () -> serializer.serialize(pathCurrency, jsonGeneratorMock, mock(SerializerProvider.class))
    );

    assertThat(exception.getMessage()).isEqualTo("Error serializing MptIssue");
    assertThat(exception.getCause()).isInstanceOf(IOException.class);
    assertThat(exception.getCause().getMessage()).isEqualTo("Test MPT IOException");
  }
}


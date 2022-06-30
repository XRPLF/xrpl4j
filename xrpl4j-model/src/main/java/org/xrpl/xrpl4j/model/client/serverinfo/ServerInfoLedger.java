package org.xrpl.xrpl4j.model.client.serverinfo;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Human-readable information about a rippled server being queried.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLedger.class)
@JsonDeserialize(as = ImmutableServerInfoLedger.class)
public interface ServerInfoLedger {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableServerInfoLedger.Builder}.
   */
  static ImmutableServerInfoLedger.Builder builder() {
    return ImmutableServerInfoLedger.builder();
  }

  /**
   * The time since the ledger was closed, in seconds.
   *
   * @return An {@link UnsignedInteger} representing the age, in seconds.
   */
  UnsignedInteger age();

  /**
   * Unique hash for the ledger, as hexadecimal.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  Hash256 hash();

  /**
   * Minimum amount of XRP necessary for every account to keep in reserve. Note that the server returns values in XRP,
   * whereas {@link XrpCurrencyAmount} supports both drops and XRP.
   *
   * @return An {@link XrpCurrencyAmount} representing the amount of XRP (in drops) to reserve.
   */
  @JsonProperty("reserve_base_xrp")
  @JsonSerialize(using = CurrencyAmountToXrpSerializer.class)
  @JsonDeserialize(using = XrpToCurrencyAmountDeserializer.class)
  XrpCurrencyAmount reserveBaseXrp();

  /**
   * Amount of XRP added to the account reserve for each object an account owns in the ledger. Note that the server
   * returns values in XRP, * whereas {@link XrpCurrencyAmount} supports both drops and XRP.
   *
   * @return An {@link XrpCurrencyAmount} representing the amount of XRP (in drops) added.
   */
  @JsonProperty("reserve_inc_xrp")
  @JsonSerialize(using = CurrencyAmountToXrpSerializer.class)
  @JsonDeserialize(using = XrpToCurrencyAmountDeserializer.class)
  XrpCurrencyAmount reserveIncXrp();

  /**
   * The ledger index of the ledger.
   *
   * @return A {@link LedgerIndex} indicating the sequence of the latest ledger.
   */
  @JsonProperty("seq")
  LedgerIndex sequence();

  /**
   * The base XRP cost of transaction.
   *
   * @return A {@link BigDecimal} representing base fee amount in XRP.
   */
  @JsonProperty("base_fee_xrp")
  BigDecimal baseFeeXrp();

  /**
   * Deserializes either a scientific notation or a decimal notation amount of XRP into an {@link XrpCurrencyAmount}
   * that holds drops internally.
   */
  class XrpToCurrencyAmountDeserializer extends JsonDeserializer<XrpCurrencyAmount> {

    @Override
    public XrpCurrencyAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode node = jsonParser.getCodec().readTree(jsonParser);
      return XrpCurrencyAmount.ofXrp(new BigDecimal(node.asText()));
    }
  }

  /**
   * Serializes XrpCurrencyAmount (which is by default stored in drops) into an XRP-based value as an
   * {@link BigDecimal}.
   */
  class CurrencyAmountToXrpSerializer extends StdScalarSerializer<XrpCurrencyAmount> {

    public CurrencyAmountToXrpSerializer() {
      super(XrpCurrencyAmount.class, false);
    }

    @Override
    public void serialize(XrpCurrencyAmount amount, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeNumber(amount.toXrp());
    }
  }
}

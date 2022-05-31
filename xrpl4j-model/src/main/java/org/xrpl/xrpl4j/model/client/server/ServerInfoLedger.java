package org.xrpl.xrpl4j.model.client.server;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.math.RoundingMode;

/**
 * Information about a recent ledger, as represented in {@link ServerInfoResult}s.
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
   * Minimum amount of XRP (not drops) necessary for every account to keep in reserve.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP to reserve.
   */
  @JsonIgnore
  @Value.Auxiliary
  @Deprecated
  @Value.Default
  default UnsignedInteger reserveBaseXrp() {
    return UnsignedInteger.valueOf(reserveBaseDrops().toXrp().setScale(0, RoundingMode.CEILING).toBigInteger());
  }

  @JsonProperty("reserve_base_xrp")
  @JsonSerialize(using = DecimalXrpCurrencyAmountSerializer.class)
  @JsonDeserialize(using = DecimalXrpCurrencyAmountDeserializer.class)
  XrpCurrencyAmount reserveBaseDrops();

  /**
   * Amount of XRP (not drops) added to the account reserve for each object an account owns in the ledger.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP added.
   */
  @JsonIgnore
  @Value.Auxiliary
  @Deprecated
  @Value.Default
  default UnsignedInteger reserveIncXrp() {
    return UnsignedInteger.valueOf(reserveIncDrops().toXrp().setScale(0, RoundingMode.CEILING).toBigInteger());
  }

  @JsonProperty("reserve_inc_xrp")
  @JsonSerialize(using = DecimalXrpCurrencyAmountSerializer.class)
  @JsonDeserialize(using = DecimalXrpCurrencyAmountDeserializer.class)
  XrpCurrencyAmount reserveIncDrops();

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
   * Deserializes either scientific notation or decimal notation in JSON to an XrpCurrencyAmount.
   */
  class DecimalXrpCurrencyAmountDeserializer extends JsonDeserializer<XrpCurrencyAmount> {
    @Override
    public XrpCurrencyAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
      JsonNode node = jsonParser.getCodec().readTree(jsonParser);
      return XrpCurrencyAmount.ofXrp(new BigDecimal(node.asText()));
    }
  }

  /**
   * Serializes XrpCurrencyAmount in drops to Xrp value as {@link UnsignedInteger}.
   */
  class DecimalXrpCurrencyAmountSerializer extends StdScalarSerializer<XrpCurrencyAmount> {

    public DecimalXrpCurrencyAmountSerializer() {
      super(XrpCurrencyAmount.class, false);
    }

    @Override
    public void serialize(XrpCurrencyAmount amount, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeNumber(
        XrpCurrencyAmount.ofDrops(Long.parseLong(amount.toString())).toXrp().setScale(0, RoundingMode.CEILING)
      );
    }
  }

}

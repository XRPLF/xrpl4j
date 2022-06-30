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
import org.immutables.value.Value.Default;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Information about a recent ledger, as represented in {@link ServerInfoResult}s.
 *
 * @deprecated Package org.xrpl.xrpl4j.model.client.server was deprecated hence this interface is also deprecated. Use
 *   {@link org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoLedger} instead.
 */
@Deprecated
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
   * Minimum amount of XRP (not drops) necessary for every account to keep in reserve. This value will be rounded up to
   * the nearest integer.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP to reserve.
   *
   * @deprecated This method handles only drops and not xrp values from servers of types scientific and decimal. Please
   *   use {@link #reserveBaseAsXrp()} instead.
   */
  @JsonIgnore
  @Value.Auxiliary
  @Deprecated
  @Value.Default
  default UnsignedInteger reserveBaseXrp() {
    // Given that this is a reserve amount, we always want _some_ amount of XRP, so we use CEILING here, for example, so
    // that something like `0.1` would round up to a whole number of 1 XRP. While this is a rather large assumption,
    // this method is deprecated, and will go away in a future version.
    return UnsignedInteger.valueOf(reserveIncAsXrp().toXrp().setScale(0, RoundingMode.CEILING).toBigInteger());
  }

  /**
   * Accessor for the `reserve_base_xrp` field.
   *
   * @return A {@link XrpCurrencyAmount}.
   */
  @JsonProperty("reserve_base_xrp")
  @JsonSerialize(using = CurrencyAmountToXrpSerializer.class)
  @JsonDeserialize(using = XrpToCurrencyAmountDeserializer.class)
  @Default // TODO: remove once reserveIncXrp is removed.
  default XrpCurrencyAmount reserveBaseAsXrp() {
    return XrpCurrencyAmount.ofXrp(new BigDecimal(reserveBaseXrp().bigIntegerValue()));
  }

  /**
   * Amount of XRP (not drops) added to the account reserve for each object an account owns in the ledger. This value
   * will be rounded up to the nearest integer.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP added.
   *
   * @deprecated This method handles only drops and not xrp values from servers of types scientific and decimal. Please
   *   use {@link #reserveIncAsXrp()} instead.
   */
  @JsonIgnore
  @Value.Auxiliary
  @Deprecated
  @Value.Default
  default UnsignedInteger reserveIncXrp() {
    // Given that this is a reserve amount, we always want _some_ amount of XRP, so we use CEILING here, for example, so
    // that something like `0.1` would round up to a whole number of 1 XRP. While this is a rather large assumption,
    // this method is deprecated, and will go away in a future version.
    return UnsignedInteger.valueOf(reserveIncAsXrp().toXrp().setScale(0, RoundingMode.CEILING).toBigInteger());
  }

  /**
   * Accessor for the `reserve_inc_xrp` field.
   *
   * @return A {@link XrpCurrencyAmount}.
   */
  @JsonProperty("reserve_inc_xrp")
  @JsonSerialize(using = CurrencyAmountToXrpSerializer.class)
  @JsonDeserialize(using = XrpToCurrencyAmountDeserializer.class)
  @Default // TODO: remove once reserveIncXrp is removed.
  default XrpCurrencyAmount reserveIncAsXrp() {
    return XrpCurrencyAmount.ofXrp(new BigDecimal(reserveIncXrp().bigIntegerValue()));
  }

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

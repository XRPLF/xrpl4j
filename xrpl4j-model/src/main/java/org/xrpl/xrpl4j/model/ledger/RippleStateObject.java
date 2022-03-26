package org.xrpl.xrpl4j.model.ledger;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

import java.util.Optional;

/**
 * Represents the <a href="https://xrpl.org/ripplestate.html">RippleState XRP Ledger Object</a>.
 *
 * <p>There can only be one {@link RippleStateObject} per currency for any given pair of accounts. Since no account
 * is privileged in the XRP Ledger, a {@link RippleStateObject} sorts account addresses numerically, to ensure
 * a canonical form. Whichever address is numerically lower when decoded is deemed the "low account" and the
 * other is the "high account". The net balance of the trust line is stored from the low account's perspective.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRippleStateObject.class)
@JsonDeserialize(as = ImmutableRippleStateObject.class)
public interface RippleStateObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableRippleStateObject.Builder}.
   */
  static ImmutableRippleStateObject.Builder builder() {
    return ImmutableRippleStateObject.builder();
  }

  /**
   * The type of ledger object. In this case, this is always "RippleState".
   *
   * @return Always {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#RIPPLE_STATE}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.RIPPLE_STATE;
  }

  /**
   * A set of boolean {@link Flags.RippleStateFlags} containing options
   * enabled for this object.
   *
   * @return The {@link org.xrpl.xrpl4j.model.flags.Flags.RippleStateFlags} for this object.
   */
  @JsonProperty("Flags")
  Flags.RippleStateFlags flags();

  /**
   * The balance of the trust line, from the perspective of the low account. A negative balance indicates that the
   * low account has issued currency to the high account. The issuer in this is always set to the neutral
   * value <a href="https://xrpl.org/accounts.html#special-addresses">ACCOUNT_ONE</a>.
   *
   * @return An {@link IssuedCurrencyAmount} containing the balance of this trust line.
   */
  @JsonProperty("Balance")
  IssuedCurrencyAmount balance();

  /**
   * The limit that the low account has set on the trust line. The issuer is the address of the
   * low account that set this limit.
   *
   * @return An {@link IssuedCurrencyAmount} containing the low account's limit on this trust line.
   */
  @JsonProperty("LowLimit")
  IssuedCurrencyAmount lowLimit();

  /**
   * The limit that the high account has set on the trust line. The issuer is the address of the
   * high account that set this limit.
   *
   * @return An {@link IssuedCurrencyAmount} containing the high account's limit on this trust line.
   */
  @JsonProperty("HighLimit")
  IssuedCurrencyAmount highLimit();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * (Omitted in some historical ledgers) A hint indicating which page of the low account's owner directory
   * links to this object, in case the directory consists of multiple pages.
   *
   * @return An {@link Optional} of type {@link String} containing the hint.
   */
  @JsonProperty("LowNode")
  Optional<String> lowNode();

  /**
   * (Omitted in some historical ledgers) A hint indicating which page of the high account's owner directory
   * links to this object, in case the directory consists of multiple pages.
   *
   * @return An {@link Optional} of type {@link String} containing the hint.
   */
  @JsonProperty("HighNode")
  Optional<String> highNode();

  /**
   * The inbound quality set by the low account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the low account's inbound quality.
   */
  @JsonProperty("LowQualityIn")
  Optional<UnsignedInteger> lowQualityIn();

  /**
   * The outbound quality set by the low account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the low account's outbound quality.
   */
  @JsonProperty("LowQualityOut")
  Optional<UnsignedInteger> lowQualityOut();

  /**
   * The inbound quality set by the high account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the high account's inbound quality.
   */
  @JsonProperty("HighQualityIn")
  Optional<UnsignedInteger> highQualityIn();

  /**
   * The outbound quality set by the high account, as an integer in the implied ratio
   * {@code LowQualityOut:1,000,000,000}. As a special case, the value 0 is equivalent to 1 billion, or face value.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the high account's outbound quality.
   */
  @JsonProperty("HighQualityOut")
  Optional<UnsignedInteger> highQualityOut();

  /**
   * Unique identifier for this {@link RippleStateObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();
}

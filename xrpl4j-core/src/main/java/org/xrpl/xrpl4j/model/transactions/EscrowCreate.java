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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.Condition;
import com.ripple.cryptoconditions.CryptoConditionReader;
import com.ripple.cryptoconditions.CryptoConditionWriter;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Sequester XRP until the escrow process either finishes or is canceled.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEscrowCreate.class)
@JsonDeserialize(as = ImmutableEscrowCreate.class)
public interface EscrowCreate extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableEscrowCreate.Builder}.
   */
  static ImmutableEscrowCreate.Builder builder() {
    return ImmutableEscrowCreate.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link EscrowCreate}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * Amount of XRP, in drops, to deduct from the sender's balance and escrow. Once escrowed, the XRP can either go to
   * the {@link EscrowCreate#destination()} address (after the {@link EscrowCreate#finishAfter()} time) or returned to
   * the sender (after the {@link EscrowCreate#cancelAfter()} time).
   *
   * @return An {@link XrpCurrencyAmount} representing the amount of the escrow.
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

  /**
   * Address to receive escrowed XRP.
   *
   * @return The {@link Address} of the destination account.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Arbitrary tag to further specify the destination for this escrowed payment, such as a hosted recipient at the
   * destination address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tag of the destination account.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * The time, in seconds since the Ripple Epoch, when this escrow expires.
   *
   * <p>This value is immutable - the funds can only be returned to the sender after this time.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the cancel after time.
   */
  @JsonProperty("CancelAfter")
  Optional<UnsignedLong> cancelAfter();

  /**
   * The time, in seconds since the Ripple Epoch, when the escrowed XRP can be released to the recipient.
   *
   * <p>This value is immutable - the funds cannot move until this time is reached.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the finish after time.
   */
  @JsonProperty("FinishAfter")
  Optional<UnsignedLong> finishAfter();

  /**
   * Hex value representing a PREIMAGE-SHA-256 crypto-condition. The funds can only be delivered to the recipient if
   * this condition is fulfilled.
   *
   * @return An {@link Optional} of type {@link Condition} containing the escrow condition.
   */
  @JsonIgnore
  Optional<Condition> condition();

  /**
   * The raw, hex-encoded PREIMAGE-SHA-256 crypto-condition of the escrow.
   *
   * <p>Developers should prefer setting {@link #condition()} and leaving this field empty when constructing a new
   * {@link EscrowCreate}. This field is used to serialize and deserialize the {@code "Condition"} field in JSON, the
   * XRPL will sometimes include an {@link EscrowCreate} in its ledger even if the crypto condition is malformed.
   * Without this field, xrpl4j would fail to deserialize those transactions, as {@link #condition()} is typed as a
   * {@link Condition}, which tries to decode the condition from DER.</p>
   *
   * @return An {@link Optional} {@link String} containing the hex-encoded PREIMAGE-SHA-256 condition.
   */
  @JsonProperty("Condition")
  Optional<String> conditionRawValue();

  /**
   * Normalization method to try to get {@link #condition()} and {@link #conditionRawValue()} to match.
   *
   * <p>If neither field is present, there is nothing to do.</p>
   * <p>If both fields are present, there is nothing to do, but we will check that {@link #condition()}'s
   * underlying value equals {@link #conditionRawValue()}.</p>
   * <p>If {@link #condition()} is present but {@link #conditionRawValue()} is empty, we set
   * {@link #conditionRawValue()} to the underlying value of {@link #condition()}.</p>
   * <p>If {@link #condition()} is empty and {@link #conditionRawValue()} is present, we will set
   * {@link #condition()} to the {@link Condition} representing the raw condition value, or leave
   * {@link #condition()} empty if {@link #conditionRawValue()} is a malformed {@link Condition}.</p>
   *
   * @return A normalized {@link EscrowCreate}.
   */
  @Value.Check
  default EscrowCreate normalizeCondition() {
    try {
      if (!condition().isPresent() && !conditionRawValue().isPresent()) {
        // If both are empty, nothing to do.
        return this;
      } else if (condition().isPresent() && conditionRawValue().isPresent()) {
        // Both will be present if:
        //   1. A developer set them both manually (in the builder)
        //   2. This method has already been called.

        // We should check that the condition()'s value matches the raw value.
        Preconditions.checkState(
          Arrays.equals(CryptoConditionWriter.writeCondition(condition().get()),
            BaseEncoding.base16().decode(conditionRawValue().get())),
          "condition and conditionRawValue should be equivalent if both are present."
        );
        return this;
      } else if (condition().isPresent() && !conditionRawValue().isPresent()) {
        // This can only happen if the developer only set condition() because condition() will never be set
        // after deserializing from JSON. In this case, we need to set conditionRawValue to match setFlag.
        return EscrowCreate.builder().from(this)
          .conditionRawValue(BaseEncoding.base16().encode(CryptoConditionWriter.writeCondition(condition().get())))
          .build();
      } else { // condition is empty and conditionRawValue is present
        // This can happen if:
        //  1. A developer sets conditionRawValue manually in the builder
        //  2. JSON has Condition and Jackson sets conditionRawValue

        // In this case, we should try to read conditionRawValue to a Condition. If that fails, condition()
        // will remain empty, otherwise we will set condition().
        try {
          Condition condition = CryptoConditionReader.readCondition(
            BaseEncoding.base16().decode(conditionRawValue().get().toUpperCase(Locale.US))
          );
          return EscrowCreate.builder().from(this)
            .condition(condition)
            .build();
        } catch (DerEncodingException | IllegalArgumentException e) {
          return this;
        }
      }

    } catch (DerEncodingException e) {
      // This should never happen. CryptoconditionWriter.writeCondition errantly declares that it can throw
      // a DerEncodingException, but nowhere in its implementation does it throw.
      throw new RuntimeException(e);
    }
  }

  /**
   * Validate cancelAfter, finishAfter, and condition fields.
   */
  @Value.Check
  default void check() {
    if (cancelAfter().isPresent() && finishAfter().isPresent()) {
      Preconditions.checkState(
        finishAfter().get().compareTo(cancelAfter().get()) < 0,
        "If both CancelAfter and FinishAfter are specified, the FinishAfter time must be before the CancelAfter time."
      );
    }
  }
}

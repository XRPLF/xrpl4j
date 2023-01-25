package org.xrpl.xrpl4j.model.client.channels;

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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * A payment channel claim that can be signed by the source account of a payment channel and presented to the
 * destination account. Once the destination account has this information, as well as the signature, it can submit a
 * {@link PaymentChannelClaim} to claim their XRP.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableUnsignedClaim.class)
@JsonDeserialize(as = ImmutableUnsignedClaim.class)
public interface UnsignedClaim {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableUnsignedClaim.Builder}.
   */
  static ImmutableUnsignedClaim.Builder builder() {
    return ImmutableUnsignedClaim.builder();
  }

  /**
   * The Channel ID of the channel that provides the XRP.
   *
   * @return A {@link Hash256} containing the Channel ID.
   */
  @JsonProperty("Channel")
  Hash256 channel();

  /**
   * The amount of XRP, in drops, that the signature of this claim authorizes.
   *
   * @return An {@link XrpCurrencyAmount} representing the amount of the claim.
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

}

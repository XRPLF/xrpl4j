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
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Request parameters for the "channel_verify" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableChannelVerifyRequestParams.class)
@JsonDeserialize(as = ImmutableChannelVerifyRequestParams.class)
public interface ChannelVerifyRequestParams extends XrplRequestParams {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableChannelVerifyRequestParams.Builder}.
   */
  static ImmutableChannelVerifyRequestParams.Builder builder() {
    return ImmutableChannelVerifyRequestParams.builder();
  }

  /**
   * The amount of XRP, in drops, the provided {@link #signature()} authorizes.
   *
   * @return the xrp currency amount
   */
  XrpCurrencyAmount amount();

  /**
   * The Channel ID of the channel that provides the XRP.
   *
   * @return the hash 256
   */
  @JsonProperty("channel_id")
  Hash256 channelId();

  /**
   * The public key of the channel and the key pair that was used to create the {@link #signature()}, in
   * hexadecimal or the XRP Ledger's base58 format.
   *
   * @return A {@link String} containing the public key.
   */
  @JsonProperty("public_key")
  String publicKey();

  /**
   * The signature to verify, in hexadecimal.
   *
   * @return A {@link String} containing the signature.
   */
  String signature();

}

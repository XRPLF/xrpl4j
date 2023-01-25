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
import org.xrpl.xrpl4j.model.client.XrplResult;

/**
 * The result of a "channel_verify" rippled API request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableChannelVerifyResult.class)
@JsonDeserialize(as = ImmutableChannelVerifyResult.class)
public interface ChannelVerifyResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableChannelVerifyResult.Builder}.
   */
  static ImmutableChannelVerifyResult.Builder builder() {
    return ImmutableChannelVerifyResult.builder();
  }

  /**
   * If {@code true}, the signature is valid for the stated amount, channel, and public key.
   *
   * @return {@code true} if the signature was valid, otherwise {@code false}.
   */
  @JsonProperty("signature_verified")
  boolean signatureVerified();

}

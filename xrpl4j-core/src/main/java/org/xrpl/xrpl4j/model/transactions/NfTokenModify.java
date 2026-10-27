package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * The {@link NfTokenModify} transaction is used to change the URI field of an NFT to point to a different URI in
 * order to update the supporting data for the NFT. The NFT must have been minted with the {@code tfMutable} flag
 * set.
 *
 * <p>This class will be marked {@link Beta} until the featureDynamicNFT amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/tree/master/XLS-0046-dynamic-non-fungible-tokens"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenModify.class)
@JsonDeserialize(as = ImmutableNfTokenModify.class)
@Beta
public interface NfTokenModify extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenModify.Builder}.
   */
  static ImmutableNfTokenModify.Builder builder() {
    return ImmutableNfTokenModify.builder();
  }

  /**
   * The {@link Address} of the account that submitted this transaction. This must be the issuer of the NFT, or an
   * account authorized by the issuer via the {@code NFTokenMinter} field of the issuer's {@link AccountSet}.
   *
   * @return The {@link Address} of the account modifying the NFT.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * Identifies the NFT to be modified.
   *
   * @return The {@link NfTokenId} of the NFT to be modified.
   */
  @JsonProperty("NFTokenID")
  NfTokenId nfTokenId();

  /**
   * The {@link Address} of the current owner of the NFT, if it is different from the {@link #account()}.
   *
   * @return An optionally-present {@link Address} of the NFT's owner.
   */
  @JsonProperty("Owner")
  Optional<Address> owner();

  /**
   * The new URI that points to the data and/or metadata associated with the NFT. If omitted, the NFT's existing URI
   * is removed.
   *
   * @return An optionally-present {@link NfTokenUri}.
   */
  @JsonProperty("URI")
  Optional<NfTokenUri> uri();

  /**
   * Set of {@link TransactionFlags}s for this {@link NfTokenModify}, which only allows the
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
}

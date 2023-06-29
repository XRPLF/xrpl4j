package org.xrpl.xrpl4j.model.client.accounts;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import org.xrpl.xrpl4j.model.flags.NfTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.util.Optional;

/**
 * Structure of an NFToken stored on the ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenObject.class)
@JsonDeserialize(as = ImmutableNfTokenObject.class)
public interface NfTokenObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenObject.Builder}.
   */
  static ImmutableNfTokenObject.Builder builder() {
    return ImmutableNfTokenObject.builder();
  }

  /**
   * The unique NFTokenID of the token.
   *
   * @return The unique NFTokenID of the token.
   */
  @JsonProperty("NFTokenID")
  NfTokenId nfTokenId();

  /**
   * The URI for the data of the token.
   *
   * @return The URI for the data of the token.
   */
  @JsonProperty("URI")
  Optional<NfTokenUri> uri();

  /**
   * A bit-map of boolean flags enabled for this NFToken.
   *
   * @return An {@link NfTokenFlags} for this NFToken.
   */
  @JsonProperty("Flags")
  NfTokenFlags flags();

  /**
   * The account that issued this NFToken.
   *
   * @return The {@link Address} of the issuer.
   */
  @JsonProperty("Issuer")
  Address issuer();

  /**
   * The unscrambled version of this token's taxon. Several tokens with the same taxon might represent instances of a
   * limited series.
   *
   * @return An {@link UnsignedInteger} representing the taxon.
   */
  @JsonProperty("NFTokenTaxon")
  UnsignedInteger taxon();

  /**
   * The token sequence number of this NFToken, which is unique for its issuer.
   *
   * @return An {@link UnsignedInteger} representing the sequence.
   */
  @JsonProperty("nft_serial")
  UnsignedInteger nftSerial();

  /**
   * Specifies the fee charged by the issuer for secondary sales of the Token,
   * if such sales are allowed. This field will only be present if the `lsfTransferable` flag is set.
   *
   * @return An {@link Optional} {@link TransferFee}.
   */
  @JsonProperty("TransferFee")
  Optional<TransferFee> transferFee();
}

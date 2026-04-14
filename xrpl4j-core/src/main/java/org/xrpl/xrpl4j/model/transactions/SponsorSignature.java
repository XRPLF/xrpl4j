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
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contains the signing information for a sponsor in a sponsored transaction. A sponsor can sign a transaction
 * either with a single signature (using {@link #signingPublicKey()} and {@link #transactionSignature()}) or
 * with multiple signatures (using {@link #signers()}).
 *
 * <p>This class will be marked {@link com.google.common.annotations.Beta} until the featureSponsorship
 * amendment is enabled on mainnet. Its API is subject to change.</p>
 *
 * @see "https://github.com/XRPLF/XRPL-Standards/blob/master/XLS-0068-sponsored-fees-and-reserves/README.md"
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableSponsorSignature.class)
@JsonDeserialize(as = ImmutableSponsorSignature.class)
public interface SponsorSignature {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableSponsorSignature.Builder}.
   */
  static ImmutableSponsorSignature.Builder builder() {
    return ImmutableSponsorSignature.builder();
  }

  /**
   * The public key used to create the sponsor's signature. This field is used for single-signature sponsorship.
   * If this field is present, {@link #transactionSignature()} must also be present, and {@link #signers()}
   * must not be present.
   *
   * @return An {@link Optional} {@link PublicKey} containing the sponsor's public key.
   */
  @JsonProperty("SigningPubKey")
  Optional<PublicKey> signingPublicKey();

  /**
   * The sponsor's signature for the transaction, verifiable using {@link #signingPublicKey()}.
   * This field is used for single-signature sponsorship. If this field is present,
   * {@link #signingPublicKey()} must also be present, and {@link #signers()} must not be present.
   *
   * @return An {@link Optional} {@link Signature} containing the sponsor's transaction signature.
   */
  @JsonProperty("TxnSignature")
  Optional<Signature> transactionSignature();

  /**
   * An array of {@link Signer} objects representing the sponsor's multi-signature. This field is used for
   * multi-signature sponsorship. If this field is present, {@link #signingPublicKey()} and
   * {@link #transactionSignature()} must not be present.
   *
   * @return An {@link Optional} {@link List} of {@link Signer} objects, sorted by account address.
   */
  @JsonProperty("Signers")
  Optional<List<SignerWrapper>> signers();

  /**
   * Whether the signers list has already been sorted. This is an internal flag used to prevent re-sorting during
   * object construction and is not serialized to JSON.
   *
   * @return {@code true} if signers have been sorted.
   */
  @JsonIgnore
  @Default
  default boolean sortedSigners() {
    return false;
  }

  /**
   * Validates that the SponsorSignature has exactly one signature type (either single-signature or multi-signature),
   * that the fields are consistent with the chosen signature type, and normalizes the signer order by account address
   * if multi-signing.
   *
   * @return A normalized {@link SponsorSignature}.
   * @throws IllegalStateException if validation fails.
   */
  @Value.Check
  default SponsorSignature checkAndNormalize() {
    boolean hasSingleSignature = transactionSignature().isPresent();
    boolean hasMultiSignature = signers().isPresent();

    // Must have exactly one signature type
    if (hasSingleSignature && hasMultiSignature) {
      throw new IllegalStateException("SponsorSignature must have either TxnSignature or Signers, but not both");
    }

    if (!hasSingleSignature && !hasMultiSignature) {
      throw new IllegalStateException("SponsorSignature must have either TxnSignature or Signers");
    }

    // If using single signature, SigningPubKey must be non-empty
    if (hasSingleSignature) {
      if (!signingPublicKey().isPresent() || signingPublicKey().get().equals(PublicKey.MULTI_SIGN_PUBLIC_KEY)) {
        throw new IllegalStateException("SigningPubKey must be non-empty when using TxnSignature");
      }
    }

    // If using multi-signature, SigningPubKey must be the multi-sign marker, and signers must be sorted by AccountID
    if (hasMultiSignature) {
      if (!signingPublicKey().isPresent() || !signingPublicKey().get().equals(PublicKey.MULTI_SIGN_PUBLIC_KEY)) {
        throw new IllegalStateException("SigningPubKey must be empty when using Signers");
      }
      if (!sortedSigners()) {
        return ImmutableSponsorSignature.builder()
          .from(this)
          .signers(signers().get().stream()
            .sorted(Comparator.comparing(wrapper -> new BigInteger(
              AddressCodec.getInstance().decodeAccountId(wrapper.signer().account()).hexValue(), 16
            )))
            .collect(Collectors.toList()))
          .sortedSigners(true)
          .build();
      }
    }

    return this;
  }

}


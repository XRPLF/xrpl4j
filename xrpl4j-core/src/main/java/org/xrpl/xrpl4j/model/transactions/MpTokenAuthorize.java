package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;

import java.util.Optional;

/**
 * Representation of the {@code MPTokenAuthorize} transaction.
 */
@Immutable
@JsonSerialize(as = ImmutableMpTokenAuthorize.class)
@JsonDeserialize(as = ImmutableMpTokenAuthorize.class)
public interface MpTokenAuthorize extends Transaction {

  /**
   * Construct a {@code MpTokenAuthorize} builder.
   *
   * @return An {@link ImmutableMpTokenAuthorize.Builder}.
   */
  static ImmutableMpTokenAuthorize.Builder builder() {
    return ImmutableMpTokenAuthorize.builder();
  }

  @JsonProperty("Flags")
  @Value.Default
  default MpTokenAuthorizeFlags flags() {
    return MpTokenAuthorizeFlags.empty();
  }

  /**
   * The {@link MpTokenIssuanceId} of the issuance to authorize.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * Specifies the holder's address that the issuer wants to authorize. Only used for authorization/allow-listing;
   * should not be present if submitted by the holder.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Holder")
  Optional<Address> holder();

}

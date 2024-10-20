package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;

import java.util.Optional;

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

  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  @JsonProperty("MPTokenHolder")
  Optional<Address> mpTokenHolder();

}

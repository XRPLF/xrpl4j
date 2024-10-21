package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.transactions.ImmutableMpTokenIssuanceDestroy.Builder;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMpTokenIssuanceSet.class)
@JsonDeserialize(as = ImmutableMpTokenIssuanceSet.class)
public interface MpTokenIssuanceSet extends Transaction {

  /**
   * Construct a {@code MpTokenIssuanceSet} builder.
   *
   * @return An {@link ImmutableMpTokenIssuanceSet.Builder}.
   */
  static ImmutableMpTokenIssuanceSet.Builder builder() {
    return ImmutableMpTokenIssuanceSet.builder();
  }

  @JsonProperty("Flags")
  @Value.Default
  default MpTokenIssuanceSetFlags flags() {
    return MpTokenIssuanceSetFlags.empty();
  }

  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  @JsonProperty("MPTokenHolder")
  Optional<Address> mpTokenHolder();

}

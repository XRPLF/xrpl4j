package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;

import java.util.Optional;

@Immutable
@JsonSerialize(as = ImmutableMpTokenIssuanceCreate.class)
@JsonDeserialize(as = ImmutableMpTokenIssuanceCreate.class)
public interface MpTokenIssuanceCreate extends Transaction {

  /**
   * Construct a {@code MpTokenIssuanceCreate} builder.
   *
   * @return An {@link ImmutableMpTokenIssuanceCreate.Builder}.
   */
  static ImmutableMpTokenIssuanceCreate.Builder builder() {
    return ImmutableMpTokenIssuanceCreate.builder();
  }

  /**
   * Set of {@link MpTokenIssuanceCreateFlags}s for this {@link MpTokenIssuanceCreate}.
   *
   * @return The {@link MpTokenIssuanceCreateFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default MpTokenIssuanceCreateFlags flags() {
    return MpTokenIssuanceCreateFlags.empty();
  }

  @JsonProperty("AssetScale")
  Optional<AssetScale> assetScale();

  @JsonProperty("TransferFee")
  Optional<TransferFee> transferFee();

  @JsonProperty("MaximumAmount")
  Optional<MpTokenObjectAmount> maximumAmount();

  @JsonProperty("MPTokenMetadata")
  Optional<String> mpTokenMetadata();

}

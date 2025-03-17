package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;

import java.util.Optional;

/**
 * Representation of the {@code MPTokenIssuanceCreate} transaction.
 */
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

  /**
   * An asset scale is the difference, in orders of magnitude, between a standard unit and a corresponding fractional
   * unit. More formally, the asset scale is a non-negative integer (0, 1, 2, …) such that one standard unit equals
   * 10^(-scale) of a corresponding fractional unit. If the fractional unit equals the standard unit, then the asset
   * scale is 0. Note that this value is optional, and will default to 0 if not supplied.
   *
   * @return An optionally present {@link AssetScale}.
   */
  @JsonProperty("AssetScale")
  Optional<AssetScale> assetScale();

  /**
   * The value specifies the fee to charged by the issuer for secondary sales of the Token, if such sales are allowed.
   * Valid values for this field are between 0 and 50,000 inclusive, allowing transfer rates of between 0.000% and
   * 50.000% in increments of 0.001. The default value is 0 if this field is not specified.
   *
   * <p>The field MUST NOT be present if the tfMPTCanTransfer flag is not set.
   *
   * @return An optionally present {@link TransferFee}.
   */
  @JsonProperty("TransferFee")
  Optional<TransferFee> transferFee();

  /**
   * The maximum number of this token's units that should ever be issued. This field is optional. If omitted, the
   * implementation will set this to an empty default field value, which will be interpreted at runtime as the current
   * maximum allowed value (currently 0x7FFF'FFFF'FFFF'FFFF).
   *
   * @return An optionally present {@link MpTokenObjectAmount}.
   */
  @JsonProperty("MaximumAmount")
  Optional<MpTokenObjectAmount> maximumAmount();

  /**
   * Arbitrary metadata about this issuance, in hex format. The limit for this field is 1024 bytes.
   *
   * @return An optionally present {@link MpTokenMetadata}.
   */
  @JsonProperty("MPTokenMetadata")
  Optional<MpTokenMetadata> mpTokenMetadata();

}

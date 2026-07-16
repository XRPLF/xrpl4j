package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateMutableFlags;

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
   * @return An optionally present {@link MpTokenNumericAmount}.
   */
  @JsonProperty("MaximumAmount")
  Optional<MpTokenNumericAmount> maximumAmount();

  /**
   * Arbitrary metadata about this issuance, in hex format. The limit for this field is 1024 bytes.
   *
   * @return An optionally present {@link MpTokenMetadata}.
   */
  @JsonProperty("MPTokenMetadata")
  Optional<MpTokenMetadata> mpTokenMetadata();

  /**
   * An optional set of flags declaring which fields or flags of the created {@code MPTokenIssuance} may be mutated
   * after issuance via {@code MPTokenIssuanceSet}. Requires the {@code DynamicMPT} amendment.
   *
   * <p>Bit {@code 0x00000001} is reserved and must not be set. Only bits defined in
   * {@link MpTokenIssuanceCreateMutableFlags} are valid.
   *
   * @return An optionally present {@link MpTokenIssuanceCreateMutableFlags}.
   */
  @JsonProperty("MutableFlags")
  Optional<MpTokenIssuanceCreateMutableFlags> mutableFlags();

  /**
   * The {@link Hash256} of a {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject} that restricts
   * who can hold this MPT.
   *
   * @return An optionally present {@link Hash256} representing the domain ID.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

  /**
   * Validates invariants for {@link MpTokenIssuanceCreate}.
   * <ul>
   *   <li>{@code MutableFlags}, when present, must only contain bits from the allowed set — bit {@code 0x1} is
   *       reserved for {@code lsfMPTLocked} and may not appear here.</li>
   *   <li>A non-zero {@code TransferFee} must not be combined with the {@code tfMPTCanHoldConfidentialBalance} flag.
   *   </li>
   * </ul>
   */
  @Value.Check
  default void check() {
    mutableFlags().ifPresent(mf -> Preconditions.checkState(
      (mf.getValue() & ~MpTokenIssuanceCreateMutableFlags.VALID_MASK) == 0,
      "MutableFlags contains invalid or reserved bits. " +
        "Bit 0x1 is reserved (lsfMPTLocked) and must not be set in MutableFlags."
    ));

    transferFee().ifPresent(fee -> Preconditions.checkState(
      fee.value().equals(UnsignedInteger.ZERO) || !flags().tfMptCanHoldConfidentialBalance(),
      "A non-zero TransferFee must not be set when the tfMPTCanHoldConfidentialBalance flag is set"
    ));
  }
}

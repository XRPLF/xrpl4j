package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceImmutableFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;

import java.util.Optional;

/**
 * Representation of the {@code MPTokenIssuanceSet} transaction.
 */
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

  /**
   * A set of {@link MpTokenIssuanceSetFlags}.
   *
   * @return An {@link MpTokenIssuanceSetFlags}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default MpTokenIssuanceSetFlags flags() {
    return MpTokenIssuanceSetFlags.empty();
  }

  /**
   * The {@link MpTokenIssuanceId} of the issuance to update.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * An optional XRPL Address of an individual token holder balance to lock/unlock. If omitted, this transaction will
   * apply to all accounts holding MPTs. Mutually exclusive with {@link #immutableFlags()},
   * {@link #mpTokenMetadata()}, {@link #transferFee()}, and {@link #domainId()}.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Holder")
  Optional<Address> holder();

  /**
   * An optional set of flags declaring which fields or flags of the {@code MPTokenIssuance} should be permanently
   * immutable from this point on. Fields and flags are mutable by default; setting a bit here locks the
   * corresponding field or flag so it can never be changed again. Bits merge with (rather than overwrite) any bits
   * already recorded on the ledger object. Must not be {@code 0} and must only contain bits defined in
   * {@link MpTokenIssuanceImmutableFlags}. Mutually exclusive with {@link #flags()}
   * ({@code tfMPTLock}/{@code tfMPTUnlock}) and {@link #holder()}. Requires the {@code DynamicMPT} amendment.
   *
   * @return An optionally present {@link MpTokenIssuanceImmutableFlags}.
   */
  @JsonProperty("ImmutableFlags")
  Optional<MpTokenIssuanceImmutableFlags> immutableFlags();

  /**
   * New metadata to replace the existing {@code MPTokenMetadata} value. Setting an empty value removes the field.
   * Fails unless the field is still mutable (i.e. {@code lsifMPTMetadata} has not been set). Mutually exclusive with
   * {@link #holder()} and {@link #flags()} ({@code tfMPTLock}/{@code tfMPTUnlock}).
   * Requires the {@code DynamicMPT} amendment.
   *
   * @return An optionally-present {@link MpTokenMetadata}.
   */
  @JsonProperty("MPTokenMetadata")
  Optional<MpTokenMetadata> mpTokenMetadata();

  /**
   * New transfer fee value. Setting to zero removes the field. Fails unless the field is still mutable (i.e.
   * {@code lsifMPTTransferFee} has not been set). Mutually exclusive with {@link #holder()} and {@link #flags()}
   * ({@code tfMPTLock}/{@code tfMPTUnlock}). Requires the {@code DynamicMPT} amendment.
   *
   * @return An optionally-present {@link TransferFee}.
   */
  @JsonProperty("TransferFee")
  Optional<TransferFee> transferFee();

  /**
   * The {@link Hash256} of a {@link org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject} that restricts
   * who can hold this MPT. Mutually exclusive with {@link #holder()}.
   *
   * @return An optionally present {@link Hash256} representing the domain ID.
   */
  @JsonProperty("DomainID")
  Optional<Hash256> domainId();

  /**
   * Validates invariants for {@link MpTokenIssuanceSet}.
   * <ul>
   *   <li>{@code ImmutableFlags}, when present, must be non-zero and contain only known bits.</li>
   *   <li>{@code ImmutableFlags}, {@code MPTokenMetadata}, and {@code TransferFee} are mutually exclusive with
   *       {@code Holder} and with the {@code tfMPTLock}/{@code tfMPTUnlock} flags.</li>
   *   <li>{@code DomainID} is mutually exclusive with {@code Holder}.</li>
   * </ul>
   */
  @Value.Check
  default void check() {
    boolean hasDynamicField = immutableFlags().isPresent() ||
      mpTokenMetadata().isPresent() ||
      transferFee().isPresent();

    immutableFlags().ifPresent(mf -> {
      long val = mf.getValue();

      Preconditions.checkState(val != 0,
        "ImmutableFlags must not be 0.");

      Preconditions.checkState((val & ~MpTokenIssuanceImmutableFlags.VALID_MASK) == 0,
        "ImmutableFlags contains invalid bits.");
    });

    if (hasDynamicField) {
      Preconditions.checkState(!holder().isPresent(),
        "Holder must not be present when ImmutableFlags, MPTokenMetadata, or TransferFee is set.");

      Preconditions.checkState(!flags().tfMptLock() && !flags().tfMptUnlock(),
        "tfMPTLock and tfMPTUnlock must not be set when ImmutableFlags, MPTokenMetadata, or TransferFee is present.");
    }

    domainId().ifPresent($ -> Preconditions.checkState(
      !holder().isPresent(),
      "DomainID and Holder are mutually exclusive."
    ));
  }
}

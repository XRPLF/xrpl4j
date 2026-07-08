package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetMutableFlags;

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
   * apply to all accounts holding MPTs. Mutually exclusive with {@link #mutableFlags()},
   * {@link #mpTokenMetadata()}, {@link #transferFee()}, and {@link #domainId()}.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("Holder")
  Optional<Address> holder();

  /**
   * The 33-byte EC-ElGamal public key used for the issuer's mirror balances.
   *
   * <p>This key is used to encrypt confidential amounts that the issuer can decrypt to monitor
   * the total supply of confidential tokens.</p>
   *
   * @return An optionally-present {@link PublicKey}.
   */
  @JsonProperty("IssuerEncryptionKey")
  Optional<PublicKey> issuerEncryptionKey();

  /**
   * The 33-byte EC-ElGamal public key used for regulatory oversight (if applicable).
   *
   * <p>This key is used to encrypt confidential amounts that an auditor can decrypt for
   * compliance and regulatory purposes.</p>
   *
   * @return An optionally-present {@link PublicKey}.
   */
  @JsonProperty("AuditorEncryptionKey")
  Optional<PublicKey> auditorEncryptionKey();

  /**
   * An optional set of flags to set or clear on the {@code MPTokenIssuance}. Only flags that were declared mutable at
   * creation time may be modified. Must not be {@code 0}, must only contain bits {@code 0x001}–{@code 0x800}, and
   * must not set and clear the same flag in one transaction. Mutually exclusive with {@link #flags()}
   * ({@code tfMPTLock}/{@code tfMPTUnlock}) and {@link #holder()}. Requires the {@code DynamicMPT} amendment.
   *
   * @return An optionally present {@link MpTokenIssuanceSetMutableFlags}.
   */
  @JsonProperty("MutableFlags")
  Optional<MpTokenIssuanceSetMutableFlags> mutableFlags();

  /**
   * New metadata to replace the existing {@code MPTokenMetadata} value. Setting an empty value removes the field.
   * Requires {@code lsmfMPTCanMutateMetadata} to have been set at creation. Mutually exclusive with
   * {@link #holder()} and {@link #flags()} ({@code tfMPTLock}/{@code tfMPTUnlock}).
   * Requires the {@code DynamicMPT} amendment.
   *
   * @return An optionally-present {@link MpTokenMetadata}.
   */
  @JsonProperty("MPTokenMetadata")
  Optional<MpTokenMetadata> mpTokenMetadata();

  /**
   * New transfer fee value. Setting to zero removes the field. Requires {@code lsmfMPTCanMutateTransferFee} to have
   * been set at creation. Mutually exclusive with {@link #holder()} and {@link #flags()}
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
   *   <li>{@code MutableFlags}, when present, must be non-zero and contain only known bits
   *       ({@code 0x001}–{@code 0x800}).</li>
   *   <li>{@code MutableFlags} must not set and clear the same flag simultaneously.</li>
   *   <li>{@code MutableFlags}, {@code MPTokenMetadata}, and {@code TransferFee} are mutually exclusive with
   *       {@code Holder} and with the {@code tfMPTLock}/{@code tfMPTUnlock} flags.</li>
   *   <li>A non-zero {@code TransferFee} must not be combined with {@code tmfMPTClearCanTransfer}.</li>
   *   <li>{@code DomainID} is mutually exclusive with {@code Holder}.</li>
   *   <li>{@code Account} must not equal {@code Holder}.</li>
   *   <li>{@code Holder} is mutually exclusive with {@code IssuerEncryptionKey} and {@code AuditorEncryptionKey}.</li>
   *   <li>{@code AuditorEncryptionKey} may only be present when {@code IssuerEncryptionKey} is also present.</li>
   * </ul>
   */
  @Value.Check
  default void check() {
    boolean hasDynamicField = mutableFlags().isPresent() ||
      mpTokenMetadata().isPresent() ||
      transferFee().isPresent();

    mutableFlags().ifPresent(mf -> {
      long val = mf.getValue();

      Preconditions.checkState(val != 0,
        "MutableFlags must not be 0.");

      Preconditions.checkState((val & ~MpTokenIssuanceSetMutableFlags.VALID_MASK) == 0,
        "MutableFlags contains invalid bits. Only bits 0x001–0x800 are valid.");

      // Set/clear conflict checks for each flag pair
      Preconditions.checkState(
        (val & (MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_LOCK.getValue())) !=
          (MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_LOCK.getValue()),
        "Cannot set and clear lsfMPTCanLock in the same transaction.");

      Preconditions.checkState(
        (val & (MpTokenIssuanceSetMutableFlags.SET_REQUIRE_AUTH.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_REQUIRE_AUTH.getValue())) !=
          (MpTokenIssuanceSetMutableFlags.SET_REQUIRE_AUTH.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_REQUIRE_AUTH.getValue()),
        "Cannot set and clear lsfMPTRequireAuth in the same transaction.");

      Preconditions.checkState(
        (val & (MpTokenIssuanceSetMutableFlags.SET_CAN_ESCROW.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_ESCROW.getValue())) !=
          (MpTokenIssuanceSetMutableFlags.SET_CAN_ESCROW.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_ESCROW.getValue()),
        "Cannot set and clear lsfMPTCanEscrow in the same transaction.");

      Preconditions.checkState(
        (val & (MpTokenIssuanceSetMutableFlags.SET_CAN_TRADE.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRADE.getValue())) !=
          (MpTokenIssuanceSetMutableFlags.SET_CAN_TRADE.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRADE.getValue()),
        "Cannot set and clear lsfMPTCanTrade in the same transaction.");

      Preconditions.checkState(
        (val & (MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRANSFER.getValue())) !=
          (MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRANSFER.getValue()),
        "Cannot set and clear lsfMPTCanTransfer in the same transaction.");

      Preconditions.checkState(
        (val & (MpTokenIssuanceSetMutableFlags.SET_CAN_CLAWBACK.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_CLAWBACK.getValue())) !=
          (MpTokenIssuanceSetMutableFlags.SET_CAN_CLAWBACK.getValue() |
          MpTokenIssuanceSetMutableFlags.CLEAR_CAN_CLAWBACK.getValue()),
        "Cannot set and clear lsfMPTCanClawback in the same transaction.");
    });

    if (hasDynamicField) {
      Preconditions.checkState(!holder().isPresent(),
        "Holder must not be present when MutableFlags, MPTokenMetadata, or TransferFee is set.");

      Preconditions.checkState(!flags().tfMptLock() && !flags().tfMptUnlock(),
        "tfMPTLock and tfMPTUnlock must not be set when MutableFlags, MPTokenMetadata, or TransferFee is present.");
    }

    transferFee().ifPresent(fee -> {
      if (fee.value().compareTo(UnsignedInteger.ZERO) > 0) {
        mutableFlags().ifPresent(mf ->
          Preconditions.checkState(
            (mf.getValue() & MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRANSFER.getValue()) == 0,
            "A non-zero TransferFee cannot be combined with tmfMPTClearCanTransfer."
          )
        );
      }
    });

    domainId().ifPresent($ -> Preconditions.checkState(
      !holder().isPresent(),
      "DomainID and Holder are mutually exclusive."
    ));

    holder().ifPresent(h -> Preconditions.checkState(
      !h.equals(account()),
      "Account and Holder must not be the same."
    ));

    if (holder().isPresent()) {
      Preconditions.checkState(
        !issuerEncryptionKey().isPresent() && !auditorEncryptionKey().isPresent(),
        "Holder is mutually exclusive with IssuerEncryptionKey and AuditorEncryptionKey."
      );
    }

    if (auditorEncryptionKey().isPresent()) {
      Preconditions.checkState(
        issuerEncryptionKey().isPresent(),
        "AuditorEncryptionKey may only be present when IssuerEncryptionKey is also present."
      );
    }
  }
}

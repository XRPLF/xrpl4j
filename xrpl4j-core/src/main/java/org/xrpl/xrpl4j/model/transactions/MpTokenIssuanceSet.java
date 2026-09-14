package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
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
   * {@link #mpTokenMetadata()}, {@link #transferFee()}, the {@code tfMPTSet*} capability-setting flags on
   * {@link #flags()}, {@link #domainId()}, {@link #issuerEncryptionKey()}, and {@link #auditorEncryptionKey()}.
   * Must not equal {@link #account()}.
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
   * <p>Mutually exclusive with {@link #holder()}. Requires the {@code ConfidentialTransfer} amendment.</p>
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
   * <p>Mutually exclusive with {@link #holder()}, and may only be present when
   * {@link #issuerEncryptionKey()} is also present. Requires the {@code ConfidentialTransfer} amendment.</p>
   *
   * @return An optionally-present {@link PublicKey}.
   */
  @JsonProperty("AuditorEncryptionKey")
  Optional<PublicKey> auditorEncryptionKey();

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
   * {@link #holder()} and {@link #flags()} ({@code tfMPTLock}/{@code tfMPTUnlock}/{@code tfMPTSet*} capability
   * flags). Requires the {@code DynamicMPT} amendment.
   *
   * @return An optionally-present {@link MpTokenMetadata}.
   */
  @JsonProperty("MPTokenMetadata")
  Optional<MpTokenMetadata> mpTokenMetadata();

  /**
   * New transfer fee value. Setting to zero removes the field. Fails unless the field is still mutable (i.e.
   * {@code lsifMPTTransferFee} has not been set). Mutually exclusive with {@link #holder()} and {@link #flags()}
   * ({@code tfMPTLock}/{@code tfMPTUnlock}/{@code tfMPTSet*} capability flags). Requires the {@code DynamicMPT}
   * amendment.
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
   *   <li>{@code Account} must not equal {@code Holder}.</li>
   *   <li>{@code Holder} is mutually exclusive with {@code IssuerEncryptionKey} and {@code AuditorEncryptionKey}.</li>
   *   <li>{@code AuditorEncryptionKey} may only be present when {@code IssuerEncryptionKey} is also present.</li>
   * </ul>
   */
  @Value.Check
  default void check() {
    boolean hasCapabilitySettingFlag = flags().tfMptSetCanLock() ||
      flags().tfMptSetRequireAuth() ||
      flags().tfMptSetCanEscrow() ||
      flags().tfMptSetCanTrade() ||
      flags().tfMptSetCanTransfer() ||
      flags().tfMptSetCanClawback() ||
      flags().tfMptSetCanHoldConfidentialBalance();

    boolean hasDynamicField = immutableFlags().isPresent() ||
      mpTokenMetadata().isPresent() ||
      transferFee().isPresent() ||
      hasCapabilitySettingFlag;

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

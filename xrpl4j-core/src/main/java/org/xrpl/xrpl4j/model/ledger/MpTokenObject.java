package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.MpTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

import java.util.Optional;

/**
 * Represents an {@code MPToken} ledger object.
 */
@Immutable
@JsonSerialize(as = ImmutableMpTokenObject.class)
@JsonDeserialize(as = ImmutableMpTokenObject.class)
public interface MpTokenObject extends LedgerObject {

  /**
   * Construct a {@code MpTokenObject} builder.
   *
   * @return An {@link ImmutableMpTokenObject.Builder}.
   */
  static ImmutableMpTokenObject.Builder builder() {
    return ImmutableMpTokenObject.builder();
  }

  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.MP_TOKEN;
  }

  /**
   * The {@link MpTokenFlags} for this token.
   *
   * @return An {@link MpTokenFlags}.
   */
  @JsonProperty("Flags")
  MpTokenFlags flags();

  /**
   * The {@link Address} of the owner of this MPToken.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The {@link MpTokenIssuanceId} of the MPTokenIssuance that this token corresponds to.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * The balance of this MPToken. Defaults to 0.
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("MPTAmount")
  @Value.Default
  default MpTokenNumericAmount mptAmount() {
    return MpTokenNumericAmount.of(0);
  }

  /**
   * The amount of this MPToken that is locked in escrows. This field tracks the total amount held in escrows for this
   * specific holder.
   *
   * @return An optionally-present {@link MpTokenNumericAmount}.
   */
  @JsonProperty("LockedAmount")
  Optional<MpTokenNumericAmount> lockedAmount();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory consists of
   * multiple pages.
   *
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.</p>
   *
   * @return An {@link Optional} of type {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * The unique ID of this {@link MpTokenObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();
}

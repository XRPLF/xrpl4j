package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * Allows the issuer to clawback all confidential MPT value from a holder's account.
 *
 * <p>This transaction zeroes out the holder's confidential balances (inbox, spending, and issuer mirror)
 * and reduces the confidential outstanding amount (COA) and outstanding amount (OA) accordingly.</p>
 *
 * <p>The issuer must prove knowledge of the amount being clawed back using a Zero-Knowledge Proof
 * that demonstrates the ciphertext encrypts the claimed amount under the issuer's key.</p>
 *
 * <p>Unlike regular clawback, this transaction claws back ALL confidential tokens from the holder,
 * not a partial amount.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialMptClawback.class)
@JsonDeserialize(as = ImmutableConfidentialMptClawback.class)
public interface ConfidentialMptClawback extends Transaction {

  /**
   * The required length, in hex characters, of the {@link ZkProof} for a {@link ConfidentialMptClawback} (64 bytes),
   * the length of an Equality Plaintext Proof.
   */
  int CLAWBACK_ZK_PROOF_HEX_LENGTH = 128;

  /**
   * Construct a {@code ConfidentialMptClawback} builder.
   *
   * @return An {@link ImmutableConfidentialMptClawback.Builder}.
   */
  static ImmutableConfidentialMptClawback.Builder builder() {
    return ImmutableConfidentialMptClawback.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMptClawback}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The unique identifier for the MPT issuance.
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("MPTokenIssuanceID")
  MpTokenIssuanceId mpTokenIssuanceId();

  /**
   * The holder account from which to clawback confidential tokens.
   *
   * @return An {@link Address} representing the holder account.
   */
  @JsonProperty("Holder")
  Address holder();

  /**
   * The plaintext amount to clawback. This is the total confidential balance of the holder
   * (inbox + spending balance).
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("MPTAmount")
  MpTokenNumericAmount mptAmount();

  /**
   * A Zero-Knowledge Proof proving that the issuer's encrypted balance for this holder
   * encrypts the claimed MPTAmount. This uses the Equality Plaintext Proof protocol.
   *
   * @return A {@link ZkProof} containing the ZK proof (64 bytes).
   */
  @JsonProperty("ZKProof")
  ZkProof zkProof();

  /**
   * Validates invariants for {@link ConfidentialMptClawback}, mirroring the {@code temMALFORMED} and
   * {@code temBAD_AMOUNT} checks in {@code rippled}'s {@code ConfidentialMPTClawback} preflight.
   *
   * <ul>
   *   <li>The {@code Account} (issuer) must not equal the {@code Holder} — an issuer cannot clawback from itself.</li>
   *   <li>{@code MPTAmount} must be non-zero and no greater than the maximum allowable supply
   *       ({@code temBAD_AMOUNT} in {@code rippled}).</li>
   *   <li>{@code ZKProof} must be exactly {@value #CLAWBACK_ZK_PROOF_HEX_LENGTH} hex characters (64 bytes).</li>
   * </ul>
   */
  @Value.Check
  default void validateConfidentialMptClawback() {
    Preconditions.checkState(
      !account().equals(holder()),
      "Account and Holder must not be the same (an issuer cannot clawback from itself)."
    );

    Preconditions.checkState(
      !mptAmount().value().equals(UnsignedLong.ZERO),
      "MPTAmount must not be zero for ConfidentialMptClawback."
    );

    Preconditions.checkState(
      mptAmount().value().compareTo(MpTokenNumericAmount.MAX_AMOUNT) <= 0,
      "MPTAmount must not exceed the maximum allowable supply (%s).", MpTokenNumericAmount.MAX_AMOUNT
    );

    Preconditions.checkState(
      zkProof().value().length() == CLAWBACK_ZK_PROOF_HEX_LENGTH,
      "ZKProof must be %s bytes (%s hex characters) for ConfidentialMptClawback.",
      CLAWBACK_ZK_PROOF_HEX_LENGTH / 2, CLAWBACK_ZK_PROOF_HEX_LENGTH
    );
  }
}


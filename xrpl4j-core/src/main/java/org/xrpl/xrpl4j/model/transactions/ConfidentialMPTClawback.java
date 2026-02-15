package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonSerialize(as = ImmutableConfidentialMPTClawback.class)
@JsonDeserialize(as = ImmutableConfidentialMPTClawback.class)
public interface ConfidentialMPTClawback extends Transaction {

  /**
   * Construct a {@code ConfidentialMPTClawback} builder.
   *
   * @return An {@link ImmutableConfidentialMPTClawback.Builder}.
   */
  static ImmutableConfidentialMPTClawback.Builder builder() {
    return ImmutableConfidentialMPTClawback.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialMPTClawback}, which only allows the
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
   * @return A hex-encoded string containing the ZK proof (98 bytes: T1 || T2 || s).
   */
  @JsonProperty("ZKProof")
  String zkProof();
}


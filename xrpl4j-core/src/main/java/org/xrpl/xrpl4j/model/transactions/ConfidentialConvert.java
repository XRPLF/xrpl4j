package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

import java.util.Optional;

/**
 * Converts a holder's own visible (public) MPT balance into confidential form. The converted amount is credited to the
 * holder's confidential inbox balance (CB_IN) to avoid immediate proof staleness, requiring an explicit merge into the
 * spending balance (CB_S) before use.
 *
 * <p>This transaction also serves as the opt-in mechanism for confidential MPT participation: by executing it
 * (including a zero-amount conversion), a holder's HolderElGamalPublicKey is recorded on their MPToken object, enabling
 * the holder to receive and manage confidential funds.</p>
 *
 * <p>This transaction is a self-conversion only. Issuers introduce supply exclusively through existing XLS-33
 * public issuance mechanisms.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableConfidentialConvert.class)
@JsonDeserialize(as = ImmutableConfidentialConvert.class)
public interface ConfidentialConvert extends Transaction {

  /**
   * Construct a {@code ConfidentialConvert} builder.
   *
   * @return An {@link ImmutableConfidentialConvert.Builder}.
   */
  static ImmutableConfidentialConvert.Builder builder() {
    return ImmutableConfidentialConvert.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link ConfidentialConvert}, which only allows the
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
   * The public plaintext amount to convert from public to confidential balance.
   *
   * @return An {@link MpTokenNumericAmount}.
   */
  @JsonProperty("MPTAmount")
  MpTokenNumericAmount mptAmount();

  /**
   * The holder's ElGamal public key. Mandatory if the account has not yet registered a key (initialization). Forbidden
   * if a key is already registered.
   *
   * <p>This is a 64-byte hex-encoded string representing the ElGamal public key.</p>
   *
   * @return An optionally-present {@link ElGamalPublicKey}.
   */
  @JsonProperty("HolderElGamalPublicKey")
  Optional<ElGamalPublicKey> holderElGamalPublicKey();

  /**
   * ElGamal ciphertext credited to the holder's confidential inbox balance (CB_IN).
   *
   * @return An {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("HolderEncryptedAmount")
  EncryptedAmount holderEncryptedAmount();

  /**
   * ElGamal ciphertext credited to the issuer's mirror balance.
   *
   * @return An {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("IssuerEncryptedAmount")
  EncryptedAmount issuerEncryptedAmount();

  /**
   * ElGamal ciphertext for the auditor. Required if {@code sfAuditorElGamalPublicKey} is present on the issuance.
   *
   * @return An optionally-present {@link EncryptedAmount} containing the hex-encoded ciphertext.
   */
  @JsonProperty("AuditorEncryptedAmount")
  Optional<EncryptedAmount> auditorEncryptedAmount();

  /**
   * The 32-byte scalar value used to encrypt the amount. Used by validators to verify the ciphertexts match the
   * plaintext MPTAmount.
   *
   * @return A {@link BlindingFactor} containing the hex-encoded blinding factor.
   */
  @JsonProperty("BlindingFactor")
  BlindingFactor blindingFactor();

  /**
   * A Schnorr Proof of Knowledge (PoK): proves the knowledge of the private key for the provided ElGamal Public Key.
   * Required when registering a new HolderElGamalPublicKey.
   *
   * <p>This is a 65-byte hex-encoded string (130 hex characters).</p>
   *
   * @return An optionally-present {@link String} containing the hex-encoded ZK proof.
   */
  @JsonProperty("ZKProof")
  Optional<String> zkProof();
}

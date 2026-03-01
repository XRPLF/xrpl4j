package org.xrpl.xrpl4j.crypto.mpt.models;

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.util.Objects;
import java.util.Optional;

/**
 * Result object containing all cryptographic outputs needed for a ConfidentialMPTConvert transaction.
 *
 * <p>This includes:</p>
 * <ul>
 *   <li>Holder's encrypted amount (required)</li>
 *   <li>Issuer's encrypted amount (required)</li>
 *   <li>Auditor's encrypted amount (optional, only if auditor public key was provided)</li>
 *   <li>Blinding factor used for encryption (required)</li>
 *   <li>ZK proof (Schnorr Proof of Knowledge) (required)</li>
 * </ul>
 */
public final class ConfidentialMPTConvertResult {

  private final ElGamalCiphertext holderEncryptedAmount;
  private final ElGamalCiphertext issuerEncryptedAmount;
  private final Optional<ElGamalCiphertext> auditorEncryptedAmount;
  private final BlindingFactor blindingFactor;
  private final SecretKeyProof zkProof;

  /**
   * Private constructor. Use the builder to create instances.
   */
  private ConfidentialMPTConvertResult(
    final ElGamalCiphertext holderEncryptedAmount,
    final ElGamalCiphertext issuerEncryptedAmount,
    final Optional<ElGamalCiphertext> auditorEncryptedAmount,
    final BlindingFactor blindingFactor,
    final SecretKeyProof zkProof
  ) {
    this.holderEncryptedAmount = Objects.requireNonNull(holderEncryptedAmount, "holderEncryptedAmount must not be null");
    this.issuerEncryptedAmount = Objects.requireNonNull(issuerEncryptedAmount, "issuerEncryptedAmount must not be null");
    this.auditorEncryptedAmount = Objects.requireNonNull(auditorEncryptedAmount, "auditorEncryptedAmount must not be null");
    this.blindingFactor = Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");
    this.zkProof = Objects.requireNonNull(zkProof, "zkProof must not be null");
  }

  /**
   * Creates a new builder for ConfidentialMPTConvertResult.
   *
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the holder's encrypted amount.
   *
   * @return The holder's encrypted amount as an {@link ElGamalCiphertext}.
   */
  public ElGamalCiphertext holderEncryptedAmount() {
    return holderEncryptedAmount;
  }

  /**
   * Gets the issuer's encrypted amount.
   *
   * @return The issuer's encrypted amount as an {@link ElGamalCiphertext}.
   */
  public ElGamalCiphertext issuerEncryptedAmount() {
    return issuerEncryptedAmount;
  }

  /**
   * Gets the auditor's encrypted amount, if present.
   *
   * @return An {@link Optional} containing the auditor's encrypted amount, or empty if no auditor was specified.
   */
  public Optional<ElGamalCiphertext> auditorEncryptedAmount() {
    return auditorEncryptedAmount;
  }

  /**
   * Gets the blinding factor used for encryption.
   *
   * @return The {@link BlindingFactor}.
   */
  public BlindingFactor blindingFactor() {
    return blindingFactor;
  }

  /**
   * Gets the ZK proof (Schnorr Proof of Knowledge).
   *
   * @return The {@link SecretKeyProof}.
   */
  public SecretKeyProof zkProof() {
    return zkProof;
  }

  /**
   * Builder for {@link ConfidentialMPTConvertResult}.
   */
  public static final class Builder {
    private ElGamalCiphertext holderEncryptedAmount;
    private ElGamalCiphertext issuerEncryptedAmount;
    private ElGamalCiphertext auditorEncryptedAmount;
    private BlindingFactor blindingFactor;
    private SecretKeyProof zkProof;

    private Builder() {
    }

    public Builder holderEncryptedAmount(ElGamalCiphertext holderEncryptedAmount) {
      this.holderEncryptedAmount = holderEncryptedAmount;
      return this;
    }

    public Builder issuerEncryptedAmount(ElGamalCiphertext issuerEncryptedAmount) {
      this.issuerEncryptedAmount = issuerEncryptedAmount;
      return this;
    }

    public Builder auditorEncryptedAmount(ElGamalCiphertext auditorEncryptedAmount) {
      this.auditorEncryptedAmount = auditorEncryptedAmount;
      return this;
    }

    public Builder blindingFactor(BlindingFactor blindingFactor) {
      this.blindingFactor = blindingFactor;
      return this;
    }

    public Builder zkProof(SecretKeyProof zkProof) {
      this.zkProof = zkProof;
      return this;
    }

    public ConfidentialMPTConvertResult build() {
      Preconditions.checkNotNull(holderEncryptedAmount, "holderEncryptedAmount is required");
      Preconditions.checkNotNull(issuerEncryptedAmount, "issuerEncryptedAmount is required");
      Preconditions.checkNotNull(blindingFactor, "blindingFactor is required");
      Preconditions.checkNotNull(zkProof, "zkProof is required");

      return new ConfidentialMPTConvertResult(
        holderEncryptedAmount,
        issuerEncryptedAmount,
        Optional.ofNullable(auditorEncryptedAmount),
        blindingFactor,
        zkProof
      );
    }
  }
}


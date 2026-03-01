package org.xrpl.xrpl4j.crypto.mpt.service;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.tmp.ConfidentialMPTConvertResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

import java.util.Optional;

/**
 * Service interface for generating cryptographic data required for ConfidentialMPTConvert transactions.
 *
 * <p>This service encapsulates the complexity of:</p>
 * <ul>
 *   <li>ElGamal encryption of the amount for holder, issuer, and optionally auditor</li>
 *   <li>Generation of the Schnorr Proof of Knowledge (ZK proof)</li>
 *   <li>Blinding factor generation and management</li>
 * </ul>
 *
 * <p>The service is parameterized by {@code PK extends PrivateKeyable} to allow different implementations
 * to work with different private key types (e.g., in-memory {@link org.xrpl.xrpl4j.crypto.keys.PrivateKey}
 * or references to keys stored in external systems like AWS KMS).</p>
 *
 * @param <PK> The type of private key this service works with.
 */
public interface ConfidentialMPTConvertService<PK extends PrivateKeyable> {

  /**
   * Generates all cryptographic data required for a ConfidentialMPTConvert transaction.
   *
   * @param holderElGamalPrivateKey The holder's ElGamal private key for proof generation.
   * @param holderElGamalPublicKey  The holder's ElGamal public key for encryption.
   * @param issuerElGamalPublicKey  The issuer's ElGamal public key for encryption.
   * @param auditorElGamalPublicKey Optional auditor's ElGamal public key for encryption.
   * @param holderAddress           The holder's XRPL account address.
   * @param accountSequence         The holder's account sequence number.
   * @param mpTokenIssuanceId       The MPT issuance ID.
   * @param amountToConvert         The amount to convert from non-confidential to confidential.
   *
   * @return A {@link ConfidentialMPTConvertResult} containing all cryptographic outputs.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  ConfidentialMPTConvertResult generateConvertData(
    PK holderElGamalPrivateKey,
    PublicKey holderElGamalPublicKey,
    PublicKey issuerElGamalPublicKey,
    Optional<PublicKey> auditorElGamalPublicKey,
    Address holderAddress,
    UnsignedInteger accountSequence,
    MpTokenIssuanceId mpTokenIssuanceId,
    UnsignedLong amountToConvert
  );

  /**
   * Derives the ElGamal public key from the given private key.
   *
   * @param privateKey The ElGamal private key.
   *
   * @return The corresponding ElGamal public key.
   */
  PublicKey derivePublicKey(PK privateKey);
}


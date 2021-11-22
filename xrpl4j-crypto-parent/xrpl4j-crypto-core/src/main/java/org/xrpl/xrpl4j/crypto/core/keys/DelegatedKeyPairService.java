package org.xrpl.xrpl4j.crypto.core.keys;

import org.xrpl.xrpl4j.crypto.core.KeyMetadata;

/**
 * Defines how to create XRPL public/private key pairs in a delegated manner, meaning private-key material is never
 * accessible via the runtime operating this service.
 */
public interface DelegatedKeyPairService {

  /**
   * Create a new delegated key pair. Because private keys managed by a {@link DelegatedKeyPairService} are
   * assumed to be inaccessible to this runtime, this method simply returns the public key of the key pair.
   *
   * @param keyMetadata The {@link KeyMetadata} identifying the key pair to create.
   *
   * @return The {@link PublicKey} of the newly created key pair.
   */
  PublicKey createKeyPair(KeyMetadata keyMetadata);

  /**
   * Accessor for the public-key corresponding to the supplied key meta-data. This method exists to support
   * implementations that hold private-key material internally, yet need a way for external callers to determine the
   * actual public key for signature verification or other purposes.
   *
   * @param keyMetadata A {@link KeyMetadata} for a key-pair.
   *
   * @return A {@link PublicKey}.
   */
  PublicKey getPublicKey(KeyMetadata keyMetadata);

}

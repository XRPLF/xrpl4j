package org.xrpl.xrpl4j.crypto.core.keys;

import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedPublicKeyProvider;

/**
 * Defines how to create XRPL public/private key pairs in a delegated manner, meaning private-key material is never
 * accessible via the runtime operating this service.
 */
public interface DelegatedKeyPairService extends DelegatedPublicKeyProvider {

  /**
   * Create a new delegated key pair. Because private keys managed by a {@link DelegatedKeyPairService} are
   * assumed to be inaccessible to this runtime, this method simply returns the public key of the key pair.
   *
   * @param keyMetadata The {@link KeyMetadata} identifying the key pair to create.
   *
   * @return The {@link PublicKey} of the newly created key pair.
   */
  PublicKey createKeyPair(KeyMetadata keyMetadata);

}

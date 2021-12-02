package org.xrpl.xrpl4j.crypto.core.keys;

import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.signing.DelegatedPublicKeyProvider;

/**
 * Defines how to create XRPL public/private key pairs in a delegated manner, meaning private-key material is never
 * accessible via the runtime operating this service.
 */
public interface DelegatedKeyPairService extends DelegatedPublicKeyProvider {

  /**
   * Create a new delegated key pair.
   *
   * <p>There is often some inherent latency with creating new key pairs on external systems such as an HSM or
   * a cloud based key management system. In some cases, those external APIs will eagerly return a successful
   * result before the key pair has actually been generated. Therefore, this method does not return anything,
   * and callers should not expect that a key pair has been generated on the external system yet just
   * because this method did not throw an exception.</p>
   *
   * <p>With this in mind, users should call this method to create a key pair, then call
   * {@link #getPublicKey(KeyMetadata)} until it returns a result. Alternatively, the implementation of {@link
   * #getPublicKey(KeyMetadata)} could poll until it receives a successful result containing a public key.</p>
   *
   * @param keyMetadata The {@link KeyMetadata} identifying the key pair to create.
   */
  void createKeyPair(KeyMetadata keyMetadata);

}

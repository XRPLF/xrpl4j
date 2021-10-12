package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.KeyStoreType;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;

/**
 * Defines how to sign and verify an XRPL transaction in a delegated manner, meaning private-key material used for
 * signing is never accessible via the runtime operating this signing service.
 */
public interface DelegatedSignatureService extends DelegatedTransactionSigner, DelegatedTransactionVerifier {

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

  /**
   * The type of org.xrpl4j.crypto.keystore this signer can be used with.
   *
   * @return A {@link KeyStoreType}.
   */
  KeyStoreType keyStoreType();
}

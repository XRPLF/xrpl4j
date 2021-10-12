package org.xrpl.xrpl4j.crypto.signing;

import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PublicKey;

/**
 * Defines how to sign and verify an XRPL transaction using a single in-memory public/private key-pair.
 *
 * @deprecated Prefer the variant found in {@link org.xrpl.xrpl4j.crypto.core} instead.
 */
@Deprecated
public interface SignatureService extends TransactionSigner, TransactionVerifier {

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

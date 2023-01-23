package org.xrpl.xrpl4j.crypto.core.signing;

import org.xrpl.xrpl4j.crypto.core.keys.PrivateKeyable;

/**
 * Defines how to sign and verify an XRPL transaction using a single in-memory public/private key-pair.
 */
public interface SignatureService<PK extends PrivateKeyable> extends TransactionSigner<PK>, TransactionVerifier {
}

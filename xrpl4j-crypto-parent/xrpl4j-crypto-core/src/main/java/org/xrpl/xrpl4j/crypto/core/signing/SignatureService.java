package org.xrpl.xrpl4j.crypto.core.signing;

/**
 * Defines how to sign and verify an XRPL transaction using a single in-memory public/private key-pair.
 */
public interface SignatureService extends TransactionSigner, TransactionVerifier {

}

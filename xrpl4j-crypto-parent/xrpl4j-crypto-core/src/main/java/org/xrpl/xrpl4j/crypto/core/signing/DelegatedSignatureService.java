package org.xrpl.xrpl4j.crypto.core.signing;

/**
 * Defines how to sign and verify an XRPL transaction in a delegated manner, meaning private-key material used for
 * signing is never accessible via the runtime operating this signing service.
 */
public interface DelegatedSignatureService extends DelegatedTransactionSigner, DelegatedTransactionVerifier {

}

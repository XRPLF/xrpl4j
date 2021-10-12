package org.xrpl.xrpl4j.crypto.bc;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;

/**
 * Static constants for Secp256k1 operations.
 */
public interface Secp256k1 {

  X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");
  ECDomainParameters EC_DOMAIN_PARAMETERS = new ECDomainParameters(
    EC_PARAMETERS.getCurve(),
    EC_PARAMETERS.getG(),
    EC_PARAMETERS.getN(),
    EC_PARAMETERS.getH()
  );

}

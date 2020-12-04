package org.xrpl.xrpl4j.keypairs;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;

/**
 * Static constants for Secp256k1 cryptography.
 */
public interface Secp256k1 {

  X9ECParameters x9ECParameters = SECNamedCurves.getByName("secp256k1");
  ECDomainParameters ecDomainParameters = new ECDomainParameters(
      x9ECParameters.getCurve(),
      x9ECParameters.getG(),
      x9ECParameters.getN(),
      x9ECParameters.getH()
  );

}

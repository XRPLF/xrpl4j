/**
 * An implementation of Exponential ElGamal over the SECP256K1 Elliptic Curve. This is different from the standard
 * "textbook" ElGamal (which works over a prime field $Z_p$), and is why this implementation does not simply use
 * BouncyCastle or some other well-known implementation.  In this particular implementation, the code is using
 * BouncyCastle's SECP256K1 implementation to perform additive homomorphic encryption. To do this in Bouncy Castle Java,
 * we don't use the ElGamalEngine. Instead, we use Bouncy Castle's Math API and EC API to manipulate curve points
 * directly.
 */

package org.xrpl.xrpl4j.crypto.mpt.elgamal;

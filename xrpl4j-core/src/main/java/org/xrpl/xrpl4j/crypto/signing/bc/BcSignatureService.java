package org.xrpl.xrpl4j.crypto.signing.bc;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.signing.AbstractSignatureService;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigInteger;
import java.util.Objects;

/**
 * <p>A {@link SignatureService} that uses BouncyCastle internally to sign with an in-memory instance of
 * {@link PrivateKey}.</p>
 *
 * <p>WARNING: This implementation utilizes in-memory private-key material. Consider instead using a
 * {@link SignatureService} that uses instance of {@link PrivateKeyReference} for improved security.</p>
 *
 * @see "https://www.bouncycastle.org/java.html"
 * @see "https://www.bouncycastle.org/fips-java/BCFipsIn100.pdf"
 */
public class BcSignatureService extends AbstractSignatureService<PrivateKey> implements SignatureService<PrivateKey> {

  private final Ed25519Signer ed25519Signer;
  private final ECDSASigner ecdsaSigner;

  /**
   * Required-args Constructor for use in development mode.
   */
  public BcSignatureService() {
    this(
      new SignatureUtils(ObjectMapperFactory.create(), XrplBinaryCodec.getInstance()),
      new Ed25519Signer(),
      new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()))
    );
  }

  /**
   * Required-args Constructor.
   *
   * @param signatureUtils An {@link SignatureUtils}.
   * @param ed25519Signer  An {@link Ed25519Signer}.
   * @param ecdsaSigner    An {@link ECDSASigner}.
   */
  public BcSignatureService(
    final SignatureUtils signatureUtils,
    final Ed25519Signer ed25519Signer,
    final ECDSASigner ecdsaSigner
  ) {
    super(signatureUtils);
    this.ed25519Signer = Objects.requireNonNull(ed25519Signer);
    this.ecdsaSigner = Objects.requireNonNull(ecdsaSigner);
  }

  @Override
  protected synchronized Signature edDsaSign(
    final PrivateKey privateKey, final UnsignedByteArray signableTransactionBytes
  ) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(signableTransactionBytes);

    final Ed25519PrivateKeyParameters privateKeyParameters = BcKeyUtils.toEd25519PrivateKeyParams(privateKey);

    final byte[] signableBytes = signableTransactionBytes.toByteArray();

    ed25519Signer.reset();
    ed25519Signer.init(true, privateKeyParameters);
    ed25519Signer.update(signableBytes, 0, signableBytes.length);

    final UnsignedByteArray sigBytes = UnsignedByteArray.of(ed25519Signer.generateSignature());
    return Signature.builder()
      .value(sigBytes)
      .build();

    // Note: Ed25519PrivateKeyParameters does not provide a destroy function, but it will be eligible for cleanup (in
    // the next GC) once this function exits.
  }

  @SuppressWarnings("checkstyle:LocalVariableName")
  @Override
  protected synchronized Signature ecDsaSign(final PrivateKey privateKey, final UnsignedByteArray transactionBytes) {
    Objects.requireNonNull(privateKey);
    Objects.requireNonNull(transactionBytes);

    final UnsignedByteArray messageHash = HashingUtils.sha512Half(transactionBytes);

    final ECPrivateKeyParameters ecPrivateKeyParams = BcKeyUtils.toEcPrivateKeyParams(privateKey);

    ecdsaSigner.init(true, ecPrivateKeyParams);
    final BigInteger[] signatures = ecdsaSigner.generateSignature(messageHash.toByteArray());
    final BigInteger r = signatures[0];
    BigInteger s = signatures[1];
    final BigInteger otherS = BcKeyUtils.PARAMS.getN().subtract(s);
    if (s.compareTo(otherS) > 0) {
      s = otherS;
    }

    final EcDsaSignature sig = EcDsaSignature.builder()
      .r(r)
      .s(s)
      .build();

    UnsignedByteArray sigBytes = sig.der();
    return Signature.builder()
      .value(sigBytes)
      .build();
  }

  @Override
  protected synchronized boolean edDsaVerify(
    final PublicKey publicKey, final UnsignedByteArray transactionBytes, final Signature signature
  ) {
    Objects.requireNonNull(publicKey);
    Objects.requireNonNull(transactionBytes);
    Objects.requireNonNull(signature);

    final Ed25519PublicKeyParameters bcPublicKey = BcKeyUtils.toEd25519PublicKeyParameters(publicKey);

    ed25519Signer.reset();
    ed25519Signer.init(false, bcPublicKey);
    ed25519Signer.update(transactionBytes.toByteArray(), 0, transactionBytes.length());

    return ed25519Signer.verifySignature(signature.value().toByteArray());
  }


  @Override
  protected synchronized boolean ecDsaVerify(
    final PublicKey publicKey, final UnsignedByteArray transactionBytes, final Signature signature
  ) {
    Objects.requireNonNull(publicKey);
    Objects.requireNonNull(transactionBytes);
    Objects.requireNonNull(signature);

    final ECPublicKeyParameters bcPublicKey = BcKeyUtils.toEcPublicKeyParameters(publicKey);
    UnsignedByteArray messageHash = HashingUtils.sha512Half(transactionBytes);
    EcDsaSignature sig = EcDsaSignature.fromDer(signature.value().toByteArray());
    if (sig == null) {
      return false;
    }

    ecdsaSigner.init(false, bcPublicKey);
    return ecdsaSigner.verifySignature(messageHash.toByteArray(), sig.r(), sig.s());
  }

  @Override
  public PublicKey derivePublicKey(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    return BcKeyUtils.toPublicKey(privateKey);
  }

}

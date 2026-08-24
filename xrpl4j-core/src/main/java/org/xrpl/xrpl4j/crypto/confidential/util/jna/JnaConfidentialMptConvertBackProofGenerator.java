package org.xrpl.xrpl4j.crypto.confidential.util.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactorValue;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptConvertBackProofGenerator} that delegates to the native
 * mpt-crypto C library via {@link MptCryptoLibrary}.
 *
 * <p>Calls {@code mpt_get_convert_back_proof} from the native library to generate an 816-byte
 * proof (128-byte compact sigma + 688-byte range proof).</p>
 */
public class JnaConfidentialMptConvertBackProofGenerator implements ConfidentialMptConvertBackProofGenerator {

  private static final int PROOF_SIZE = 816;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaConfidentialMptConvertBackProofGenerator() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaConfidentialMptConvertBackProofGenerator(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public ConfidentialMptConvertBackProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final ConfidentialMptConvertBackContext context,
    final PedersenProofParams balanceParams
  ) {
    Objects.requireNonNull(senderKeyPair, "senderKeyPair must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(balanceParams, "balanceParams must not be null");

    Preconditions.checkArgument(
      senderKeyPair.publicKey().keyType() == KeyType.SECP256K1,
      "senderKeyPair must be SECP256K1"
    );

    // Validate the (public) sender key before copying out any secret, so a failure leaves no unscrubbed secret.
    // keyType() is content-derived and does not by itself guarantee length.
    byte[] publicKeyBytes = senderKeyPair.publicKey().value().toByteArray();
    Preconditions.checkArgument(
      publicKeyBytes.length == PublicKey.LENGTH, "senderKeyPair public key must be %s bytes", PublicKey.LENGTH);
    byte[] contextHash = context.value().toByteArray();

    byte[] outProof = new byte[PROOF_SIZE];
    byte[] privateKeyBytes = senderKeyPair.privateKey().naturalBytes().toByteArray();

    // The balance blinding factor is secret like the private key: an ElGamal ciphertext plus its blinding factor
    // reveals the amount. Populate the params struct (which copies it) inside the try, so any failure — including the
    // length check — still scrubs it in the finally. This clears only these Java copies; the caller's own
    // SecretBlindingFactor instance and any buffers JNA marshals into native memory are outside this method's control.
    MptCryptoLibrary.MptPedersenProofParams params = new MptCryptoLibrary.MptPedersenProofParams();
    byte[] balanceBlindingBytes = null;
    int result;
    try {
      Preconditions.checkArgument(
        privateKeyBytes.length == PrivateKey.LENGTH, "senderKeyPair private key must be %s bytes", PrivateKey.LENGTH);
      System.arraycopy(balanceParams.pedersenCommitment().toByteArray(), 0, params.pedersenCommitment, 0, 33);
      params.amount = balanceParams.amount().longValue();
      System.arraycopy(
        balanceParams.encryptedAmount().value().toByteArray(), 0, params.encryptedAmount, 0, EncryptedAmount.LENGTH);
      // Copy the secret blinding factor via a named local so the finally can scrub it too — not just the struct field.
      balanceBlindingBytes = balanceParams.blindingFactor().value().toByteArray();
      System.arraycopy(balanceBlindingBytes, 0, params.blindingFactor, 0, BlindingFactorValue.LENGTH);
      result = lib.mpt_get_convert_back_proof(
        privateKeyBytes, publicKeyBytes, contextHash, amount.longValue(),
        params, outProof
      );
    } finally {
      Arrays.fill(privateKeyBytes, (byte) 0);
      Arrays.fill(params.blindingFactor, (byte) 0);
      if (balanceBlindingBytes != null) {
        Arrays.fill(balanceBlindingBytes, (byte) 0);
      }
    }

    if (result != 0) {
      throw new IllegalStateException("mpt_get_convert_back_proof failed with error code: " + result);
    }

    return ConfidentialMptConvertBackProof.of(UnsignedByteArray.of(outProof));
  }
}

package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

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

    UnsignedByteArray naturalBytes = senderKeyPair.privateKey().naturalBytes();
    byte[] privkey = naturalBytes.toByteArray();
    byte[] pubkey = senderKeyPair.publicKey().value().toByteArray();
    byte[] ctxHash = context.value().toByteArray();

    // Construct the MptPedersenProofParams struct for the native library
    MptCryptoLibrary.MptPedersenProofParams params = new MptCryptoLibrary.MptPedersenProofParams();
    System.arraycopy(balanceParams.pedersenCommitment().toByteArray(), 0, params.pedersenCommitment, 0, 33);
    params.amount = balanceParams.amount().longValue();
    System.arraycopy(balanceParams.encryptedAmount().toBytes().toByteArray(), 0, params.encryptedAmount, 0, 66);
    System.arraycopy(balanceParams.blindingFactor().toBytes(), 0, params.blindingFactor, 0, 32);

    byte[] outProof = new byte[PROOF_SIZE];
    int result = lib.mpt_get_convert_back_proof(
      privkey, pubkey, ctxHash, amount.longValue(),
      params, outProof
    );

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_get_convert_back_proof failed with error code: " + result);
    }

    return ConfidentialMptConvertBackProof.of(UnsignedByteArray.of(outProof));
  }
}

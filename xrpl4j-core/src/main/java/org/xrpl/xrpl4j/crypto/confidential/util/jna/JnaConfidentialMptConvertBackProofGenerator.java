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
 * mpt-crypto C library via the {@link NativeMptCrypto} bridge.
 *
 * <p>Calls {@code mpt_get_convert_back_proof} from the native library to generate an 883-byte
 * proof (195-byte balance linkage + 688-byte range proof).</p>
 */
public class JnaConfidentialMptConvertBackProofGenerator implements ConfidentialMptConvertBackProofGenerator {

  private static final int PROOF_SIZE = 883;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge.
   */
  public JnaConfidentialMptConvertBackProofGenerator() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptConvertBackProofGenerator(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
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

    byte[] outProof = new byte[PROOF_SIZE];
    int result = nativeCrypto.generateConvertBackProof(
      privkey, pubkey, ctxHash, amount.longValue(),
      balanceParams.pedersenCommitment().toByteArray(), balanceParams.amount().longValue(),
      balanceParams.encryptedAmount().toBytes().toByteArray(), balanceParams.blindingFactor().toBytes(),
      outProof
    );

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_get_convert_back_proof failed with error code: " + result);
    }

    return ConfidentialMptConvertBackProof.of(UnsignedByteArray.of(outProof));
  }
}

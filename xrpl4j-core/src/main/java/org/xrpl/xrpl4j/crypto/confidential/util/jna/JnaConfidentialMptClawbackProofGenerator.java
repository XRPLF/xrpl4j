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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptClawbackProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptClawbackProofGenerator} that delegates to the native
 * mpt-crypto C library via {@link MptCryptoLibrary}.
 *
 * <p>Calls {@code mpt_get_clawback_proof} from the native library to generate a 64-byte
 * compact sigma proof.</p>
 */
public class JnaConfidentialMptClawbackProofGenerator implements ConfidentialMptClawbackProofGenerator {

  private static final int PROOF_SIZE = 64;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaConfidentialMptClawbackProofGenerator() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaConfidentialMptClawbackProofGenerator(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public ConfidentialMptClawbackProof generateProof(
    final EncryptedAmount issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final PrivateKey issuerPrivateKey,
    final ConfidentialMptClawbackContext context
  ) {
    Objects.requireNonNull(issuerEncryptedBalance, "issuerEncryptedBalance must not be null");
    Objects.requireNonNull(issuerPublicKey, "issuerPublicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(issuerPrivateKey, "issuerPrivateKey must not be null");
    Objects.requireNonNull(context, "context must not be null");

    Preconditions.checkArgument(
      issuerPublicKey.keyType() == KeyType.SECP256K1,
      "issuerPublicKey must be SECP256K1"
    );

    UnsignedByteArray naturalBytes = issuerPrivateKey.naturalBytes();
    byte[] privkey = naturalBytes.toByteArray();
    byte[] pubkey = issuerPublicKey.value().toByteArray();
    byte[] ctxHash = context.value().toByteArray();
    byte[] encryptedAmount = issuerEncryptedBalance.toBytes().toByteArray();

    byte[] outProof = new byte[PROOF_SIZE];
    int result = lib.mpt_get_clawback_proof(
      privkey, pubkey, ctxHash, amount.longValue(), encryptedAmount, outProof
    );

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_get_clawback_proof failed with error code: " + result);
    }

    return ConfidentialMptClawbackProof.of(UnsignedByteArray.of(outProof));
  }
}

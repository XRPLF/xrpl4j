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
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptConvertProofGenerator} that delegates to the native mpt-crypto
 * C library via the {@link NativeMptCrypto} bridge.
 *
 * <p>Calls {@code mpt_get_convert_proof} from the native library to generate a 64-byte
 * compact Schnorr proof.</p>
 */
public class JnaConfidentialMptConvertProofGenerator implements ConfidentialMptConvertProofGenerator {

  private static final int PUBKEY_SIZE = 33;
  private static final int PRIVKEY_SIZE = 32;
  private static final int CONTEXT_HASH_SIZE = 32;
  private static final int PROOF_SIZE = 64;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge from {@code xrpl4j-mpt-crypto}.
   *
   * @throws IllegalStateException if {@code xrpl4j-mpt-crypto} is not on the classpath.
   */
  public JnaConfidentialMptConvertProofGenerator() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptConvertProofGenerator(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public ConfidentialMptConvertProof generateProof(
    final KeyPair keyPair,
    final ConfidentialMptConvertContext context
  ) {
    Objects.requireNonNull(keyPair, "keyPair must not be null");
    Objects.requireNonNull(context, "context must not be null");

    Preconditions.checkArgument(
      keyPair.publicKey().keyType() == KeyType.SECP256K1,
      "keyPair must be SECP256K1, but was %s",
      keyPair.publicKey().keyType()
    );

    byte[] pubkeyBytes = keyPair.publicKey().value().toByteArray();
    Preconditions.checkArgument(
      pubkeyBytes.length == PUBKEY_SIZE,
      "publicKey must be %s bytes, but was %s bytes",
      PUBKEY_SIZE, pubkeyBytes.length
    );

    UnsignedByteArray naturalBytes = keyPair.privateKey().naturalBytes();
    byte[] privkeyBytes = naturalBytes.toByteArray();
    Preconditions.checkArgument(
      privkeyBytes.length == PRIVKEY_SIZE,
      "privateKey must be %s bytes, but was %s bytes",
      PRIVKEY_SIZE, privkeyBytes.length
    );

    byte[] ctxHash = context.value().toByteArray();
    Preconditions.checkArgument(
      ctxHash.length == CONTEXT_HASH_SIZE,
      "context hash must be %s bytes, but was %s bytes",
      CONTEXT_HASH_SIZE, ctxHash.length
    );

    byte[] outProof = new byte[PROOF_SIZE];
    int result = nativeCrypto.generateConvertProof(pubkeyBytes, privkeyBytes, ctxHash, outProof);

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_get_convert_proof failed with error code: " + result);
    }

    return ConfidentialMptConvertProof.of(UnsignedByteArray.of(outProof));
  }
}

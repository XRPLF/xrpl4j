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
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialCiphertextArithmetic;

import java.util.Objects;

/**
 * Implementation of {@link ConfidentialCiphertextArithmetic} that delegates to the native mpt-crypto C library via
 * {@link MptCryptoLibrary}.
 *
 * <p>A 66-byte wire ciphertext (C1 || C2) is parsed into two {@code secp256k1_pubkey} points via
 * {@code mpt_make_ec_pair}, combined with {@code secp256k1_elgamal_add} / {@code secp256k1_elgamal_subtract}, then
 * serialized back to 66 bytes via {@code mpt_serialize_ec_pair}.</p>
 */
public class JnaConfidentialCiphertextArithmetic implements ConfidentialCiphertextArithmetic {

  private static final int CIPHERTEXT_SIZE = 66;
  // sizeof(secp256k1_pubkey): an opaque `unsigned char data[64]` struct.
  private static final int PUBKEY_STRUCT_SIZE = 64;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaConfidentialCiphertextArithmetic() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaConfidentialCiphertextArithmetic(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public EncryptedAmount add(final EncryptedAmount augend, final EncryptedAmount addend) {
    return combine(augend, addend, true);
  }

  @Override
  public EncryptedAmount subtract(final EncryptedAmount minuend, final EncryptedAmount subtrahend) {
    return combine(minuend, subtrahend, false);
  }

  /**
   * Parse both ciphertexts to EC-point pairs, run the native add/subtract, and serialize the result.
   *
   * @param left  The left-hand ciphertext.
   * @param right The right-hand ciphertext under the same key.
   * @param add   {@code true} to add, {@code false} to subtract {@code right} from {@code left}.
   *
   * @return The combined {@link EncryptedAmount}.
   */
  private EncryptedAmount combine(final EncryptedAmount left, final EncryptedAmount right, final boolean add) {
    Objects.requireNonNull(left, "left must not be null");
    Objects.requireNonNull(right, "right must not be null");

    byte[] leftBytes = left.value().toByteArray();
    byte[] rightBytes = right.value().toByteArray();
    Preconditions.checkArgument(leftBytes.length == CIPHERTEXT_SIZE, "left must be %s bytes", CIPHERTEXT_SIZE);
    Preconditions.checkArgument(rightBytes.length == CIPHERTEXT_SIZE, "right must be %s bytes", CIPHERTEXT_SIZE);

    Pointer ctx = lib.mpt_secp256k1_context();
    if (ctx == null) {
      throw new IllegalStateException("mpt_secp256k1_context returned a null context");
    }

    // Two parsed points (C1, C2) per input, plus two for the result.
    Memory leftC1 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory leftC2 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory rightC1 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory rightC2 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory outC1 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory outC2 = new Memory(PUBKEY_STRUCT_SIZE);
    try {
      if (!lib.mpt_make_ec_pair(leftBytes, leftC1, leftC2)) {
        throw new IllegalStateException("mpt_make_ec_pair failed to parse the left ciphertext");
      }
      if (!lib.mpt_make_ec_pair(rightBytes, rightC1, rightC2)) {
        throw new IllegalStateException("mpt_make_ec_pair failed to parse the right ciphertext");
      }

      int result;
      if (add) {
        result = lib.secp256k1_elgamal_add(ctx, outC1, outC2, leftC1, leftC2, rightC1, rightC2);
      } else {
        result = lib.secp256k1_elgamal_subtract(ctx, outC1, outC2, leftC1, leftC2, rightC1, rightC2);
      }
      // secp256k1 convention: 1 == success.
      if (result != 1) {
        throw new IllegalStateException(
          "secp256k1_elgamal_" + (add ? "add" : "subtract") + " failed with error code: " + result
        );
      }

      byte[] out = new byte[CIPHERTEXT_SIZE];
      if (!lib.mpt_serialize_ec_pair(outC1, outC2, out)) {
        throw new IllegalStateException("mpt_serialize_ec_pair failed to serialize the combined ciphertext");
      }
      return EncryptedAmount.fromBytes(out);
    } finally {
      leftC1.close();
      leftC2.close();
      rightC1.close();
      rightC2.close();
      outC1.close();
      outC2.close();
    }
  }
}

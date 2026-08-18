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
   * @param a   The left-hand ciphertext.
   * @param b   The right-hand ciphertext.
   * @param add {@code true} to add {@code b} to {@code a}, {@code false} to subtract {@code b} from {@code a}.
   *
   * @return The combined {@link EncryptedAmount}.
   */
  private EncryptedAmount combine(final EncryptedAmount a, final EncryptedAmount b, final boolean add) {
    Objects.requireNonNull(a, "a must not be null");
    Objects.requireNonNull(b, "b must not be null");

    byte[] aBytes = a.value().toByteArray();
    byte[] bBytes = b.value().toByteArray();
    Preconditions.checkArgument(aBytes.length == CIPHERTEXT_SIZE, "a must be %s bytes", CIPHERTEXT_SIZE);
    Preconditions.checkArgument(bBytes.length == CIPHERTEXT_SIZE, "b must be %s bytes", CIPHERTEXT_SIZE);

    Pointer ctx = lib.mpt_secp256k1_context();
    if (ctx == null) {
      throw new IllegalStateException("mpt_secp256k1_context returned a null context");
    }

    // Two parsed points per input, plus two for the result.
    Memory apoint1 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory apoint2 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory bpoint1 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory bpoint2 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory outPoint1 = new Memory(PUBKEY_STRUCT_SIZE);
    Memory outPoint2 = new Memory(PUBKEY_STRUCT_SIZE);
    try {
      if (!lib.mpt_make_ec_pair(aBytes, apoint1, apoint2)) {
        throw new IllegalStateException("mpt_make_ec_pair failed to parse ciphertext a");
      }
      if (!lib.mpt_make_ec_pair(bBytes, bpoint1, bpoint2)) {
        throw new IllegalStateException("mpt_make_ec_pair failed to parse ciphertext b");
      }

      int result = add
        ? lib.secp256k1_elgamal_add(ctx, outPoint1, outPoint2, apoint1, apoint2, bpoint1, bpoint2)
        : lib.secp256k1_elgamal_subtract(ctx, outPoint1, outPoint2, apoint1, apoint2, bpoint1, bpoint2);
      // secp256k1 convention: 1 == success.
      if (result != 1) {
        throw new IllegalStateException(
          "secp256k1_elgamal_" + (add ? "add" : "subtract") + " failed with error code: " + result
        );
      }

      byte[] out = new byte[CIPHERTEXT_SIZE];
      if (!lib.mpt_serialize_ec_pair(outPoint1, outPoint2, out)) {
        throw new IllegalStateException("mpt_serialize_ec_pair failed to serialize the combined ciphertext");
      }
      return EncryptedAmount.fromBytes(out);
    } finally {
      apoint1.close();
      apoint2.close();
      bpoint1.close();
      bpoint2.close();
      outPoint1.close();
      outPoint2.close();
    }
  }
}

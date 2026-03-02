package org.xrpl.xrpl4j.crypto.mpt.port.bc;

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

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.ByteUtils;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.port.PedersenCommitmentPort;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link PedersenCommitmentPort}.
 *
 * <p>Port of {@code secp256k1_mpt_pedersen_commit} from commitments.c.</p>
 */
public class BcPedersenCommitmentPort implements PedersenCommitmentPort {

  private static final String DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";
  private static final String CURVE_LABEL = "secp256k1";

  /**
   * Cached H generator point (lazily initialized).
   */
  private ECPoint cachedH;

  @Override
  public UnsignedByteArray generateCommitment(UnsignedLong amount, UnsignedByteArray rho) {
    // 0. Input Check - matches: if (!secp256k1_ec_seckey_verify(ctx, rho)) return 0;
    if (!Secp256k1Operations.isValidScalar(rho.toByteArray())) {
      throw new IllegalArgumentException("rho is not a valid scalar");
    }

    byte[] mScalar = new byte[32];

    try {
      // 1. Calculate rho*H (Blinding Term)
      // matches: if (!secp256k1_mpt_get_h_generator(ctx, &H)) return 0;
      ECPoint H = getHGenerator();

      // matches: rH = H; if (!secp256k1_ec_pubkey_tweak_mul(ctx, &rH, rho)) return 0;
      BigInteger rhoInt = new BigInteger(1, rho.toByteArray());
      ECPoint rH = Secp256k1Operations.multiply(H, rhoInt);

      if (rH.isInfinity()) {
        throw new IllegalStateException("rH is point at infinity");
      }

      // 2. Handle Zero Amount Case
      // matches: if (amount == 0) { *commitment = rH; return 1; }
      if (amount.equals(UnsignedLong.ZERO)) {
        return UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(rH));
      }

      // 3. Calculate m*G (Value Term)
      // matches: for (int i = 0; i < 8; i++) { m_scalar[31 - i] = (amount >> (i * 8)) & 0xFF; }
      long amountValue = amount.longValue();
      for (int i = 0; i < 8; i++) {
        mScalar[31 - i] = (byte) ((amountValue >> (i * 8)) & 0xFF);
      }

      // matches: if (!secp256k1_ec_pubkey_create(ctx, &mG, m_scalar)) goto cleanup;
      BigInteger mInt = new BigInteger(1, mScalar);
      ECPoint mG = Secp256k1Operations.multiplyG(mInt);

      if (mG.isInfinity()) {
        throw new IllegalStateException("mG is point at infinity");
      }

      // 4. Combine: C = mG + rH
      // matches: if (!secp256k1_ec_pubkey_combine(ctx, commitment, points, 2)) goto cleanup;
      ECPoint commitment = Secp256k1Operations.add(mG, rH);

      if (commitment.isInfinity()) {
        throw new IllegalStateException("commitment is point at infinity");
      }

      return UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(commitment));

    } finally {
      // matches: OPENSSL_cleanse(m_scalar, 32);
      Arrays.fill(mScalar, (byte) 0);
    }
  }

  /**
   * Gets the H generator point for Pedersen commitments.
   *
   * <p>Port of {@code secp256k1_mpt_get_h_generator} which derives a NUMS point
   * using the label "H" at index 0.</p>
   *
   * @return The H generator point.
   */
  private ECPoint getHGenerator() {
    if (cachedH == null) {
      cachedH = hashToPointNums("H".getBytes(StandardCharsets.UTF_8), 0);
    }
    return cachedH;
  }

  /**
   * Deterministically derives a NUMS (Nothing-Up-My-Sleeve) generator point.
   *
   * <p>Port of {@code secp256k1_mpt_hash_to_point_nums} from commitments.c.</p>
   *
   * @param label The domain/vector label (e.g., "H").
   * @param index The vector index.
   *
   * @return The derived generator point.
   */
  private ECPoint hashToPointNums(byte[] label, int index) {
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] curveBytes = CURVE_LABEL.getBytes(StandardCharsets.UTF_8);
    byte[] indexBe = ByteUtils.toByteArray(index, 4);

    // Try-and-increment loop
    for (long ctr = 0; ctr < 0xFFFFFFFFL; ctr++) {
      byte[] ctrBe = ByteUtils.toByteArray((int) ctr, 4);

      // Build hash input: domainSeparator || curveLabel || label || index || counter
      int inputLen = domainBytes.length + curveBytes.length +
        (label != null ? label.length : 0) + 4 + 4;
      byte[] hashInput = new byte[inputLen];
      int offset = 0;

      System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
      offset += domainBytes.length;
      System.arraycopy(curveBytes, 0, hashInput, offset, curveBytes.length);
      offset += curveBytes.length;
      if (label != null && label.length > 0) {
        System.arraycopy(label, 0, hashInput, offset, label.length);
        offset += label.length;
      }
      System.arraycopy(indexBe, 0, hashInput, offset, 4);
      offset += 4;
      System.arraycopy(ctrBe, 0, hashInput, offset, 4);

      byte[] hash = HashingUtils.sha256(hashInput).toByteArray();

      // Construct compressed point candidate: 0x02 || hash
      byte[] compressed = new byte[33];
      compressed[0] = 0x02;
      System.arraycopy(hash, 0, compressed, 1, 32);

      try {
        ECPoint point = Secp256k1Operations.deserialize(compressed);
        if (point != null && !point.isInfinity()) {
          return point;
        }
      } catch (Exception e) {
        // Invalid point, continue to next counter
      }
    }

    throw new IllegalStateException("Failed to derive NUMS point (extremely unlikely)");
  }
}


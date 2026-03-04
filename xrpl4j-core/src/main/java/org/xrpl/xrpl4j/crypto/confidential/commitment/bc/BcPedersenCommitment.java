package org.xrpl.xrpl4j.crypto.confidential.commitment.bc;

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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.commitment.PedersenCommitment;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link PedersenCommitment}.
 *
 * <p>Port of {@code secp256k1_mpt_pedersen_commit} from commitments.c.</p>
 */
public class BcPedersenCommitment implements PedersenCommitment {

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
      ECPoint H = Secp256k1Operations.getH();

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
}


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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalDecryptorPort;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link ElGamalDecryptorPort}.
 *
 * <p>Port of {@code secp256k1_elgamal_decrypt} from elgamal.c.</p>
 */
public class BcElGamalDecryptorPort implements ElGamalDecryptorPort {

  @Override
  public UnsignedLong decrypt(
    final ElGamalCiphertext ciphertext,
    final UnsignedByteArray privkey,
    final UnsignedLong minAmount,
    final UnsignedLong maxAmount
  ) {
    ECPoint C1 = Secp256k1Operations.deserialize(ciphertext.c1().toByteArray());
    ECPoint C2 = Secp256k1Operations.deserialize(ciphertext.c2().toByteArray());
    byte[] sk = privkey.toByteArray();

    long min = minAmount.longValue();
    long max = maxAmount.longValue();

    /* 1. Recover Shared Secret: S = privkey * C1 */
    ECPoint S = Secp256k1Operations.multiply(C1, new BigInteger(1, sk));
    if (S.isInfinity()) {
      Arrays.fill(sk, (byte) 0);
      throw new IllegalStateException("Decryption failed: shared secret is point at infinity");
    }

    /* 2. Check for Amount = 0 (C2 == S) */
    if (min == 0 && Secp256k1Operations.pointsEqual(C2, S)) {
      Arrays.fill(sk, (byte) 0);
      return UnsignedLong.ZERO;
    }

    /* 3. Prepare Target: M_target = C2 - S */
    ECPoint negS = Secp256k1Operations.negate(S);
    ECPoint M_target = Secp256k1Operations.add(C2, negS);
    if (M_target.isInfinity()) {
      Arrays.fill(sk, (byte) 0);
      throw new IllegalStateException("Decryption failed: M_target is point at infinity");
    }

    /* 4. Brute Force Search (minAmount to maxAmount) */
    ECPoint G_point = Secp256k1Operations.getG();

    // Start at minAmount * G (or 1*G if minAmount is 0, since 0 is handled above)
    long startAmount = (min == 0) ? 1 : min;
    ECPoint current_M = Secp256k1Operations.multiplyG(BigInteger.valueOf(startAmount));

    for (long i = startAmount; i <= max; i++) {
      if (Secp256k1Operations.pointsEqual(current_M, M_target)) {
        Arrays.fill(sk, (byte) 0);
        return UnsignedLong.valueOf(i);
      }

      /* Increment: current_M = current_M + G */
      current_M = Secp256k1Operations.add(current_M, G_point);
    }

    Arrays.fill(sk, (byte) 0);
    throw new IllegalStateException("Decryption failed: amount not found in range [" + min + ", " + max + "]");
  }
}


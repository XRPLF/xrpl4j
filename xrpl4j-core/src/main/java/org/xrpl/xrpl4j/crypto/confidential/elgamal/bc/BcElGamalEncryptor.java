package org.xrpl.xrpl4j.crypto.confidential.elgamal.bc;

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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.elgamal.ElGamalEncryptor;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * BouncyCastle implementation of {@link ElGamalEncryptor}.
 */
@SuppressWarnings("checkstyle")
public class BcElGamalEncryptor implements ElGamalEncryptor {

  @Override
  public EncryptedAmount encrypt(
    final UnsignedLong amount,
    final UnsignedByteArray pubkeyQ,
    final UnsignedByteArray blindingFactor
  ) {
    byte[] r = blindingFactor.toByteArray();
    ECPoint Q = Secp256k1Operations.deserialize(pubkeyQ.toByteArray());

    /* 1. C1 = r * G */
    ECPoint c1 = Secp256k1Operations.multiplyG(new BigInteger(1, r));
    if (c1.isInfinity()) {
      throw new IllegalStateException("Encryption failed: C1 is point at infinity");
    }

    /* 2. S = r * Q (Shared Secret) */
    ECPoint S = Secp256k1Operations.multiply(Q, new BigInteger(1, r));
    if (S.isInfinity()) {
      throw new IllegalStateException("Encryption failed: shared secret is point at infinity");
    }

    /* 3. C2 = S + m*G */
    ECPoint c2;
    if (amount.equals(UnsignedLong.ZERO)) {
      c2 = S; // m*G is infinity, so C2 = S
    } else {
      ECPoint mG = computeAmountPoint(amount);
      if (mG.isInfinity()) {
        throw new IllegalStateException("Encryption failed: amount point is point at infinity");
      }
      c2 = Secp256k1Operations.add(mG, S);
      if (c2.isInfinity()) {
        throw new IllegalStateException("Encryption failed: C2 is point at infinity");
      }
    }

    byte[] c1Bytes = Secp256k1Operations.serializeCompressed(c1);
    byte[] c2Bytes = Secp256k1Operations.serializeCompressed(c2);

    Arrays.fill(r, (byte) 0);

    return EncryptedAmount.of(
      UnsignedByteArray.of(c1Bytes),
      UnsignedByteArray.of(c2Bytes)
    );
  }

  private ECPoint computeAmountPoint(UnsignedLong amount) {
    byte[] amountScalar = new byte[32];
    long amountLong = amount.longValue();
    for (int i = 0; i < 8; i++) {
      amountScalar[31 - i] = (byte) ((amountLong >> (i * 8)) & 0xFF);
    }

    ECPoint mG = Secp256k1Operations.multiplyG(new BigInteger(1, amountScalar));

    Arrays.fill(amountScalar, (byte) 0);

    return mG;
  }
}
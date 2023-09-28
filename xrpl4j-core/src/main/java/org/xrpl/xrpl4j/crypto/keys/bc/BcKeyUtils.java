package org.xrpl.xrpl4j.crypto.keys.bc;

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

import static org.xrpl.xrpl4j.codec.addresses.KeyType.ED25519;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.math.BigInteger;
import java.security.Security;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for converting between XRPL-4j public/private keys and BouncyCastle implementations.
 */
public final class BcKeyUtils {

  private static final String SECP256K1 = "secp256k1";
  private static final ECNamedCurveParameterSpec EC_PARAMS = ECNamedCurveTable.getParameterSpec(SECP256K1);
  public static final ECDomainParameters PARAMS =
    new ECDomainParameters(
      EC_PARAMS.getCurve(),
      EC_PARAMS.getG(),
      EC_PARAMS.getN(),
      EC_PARAMS.getH()
    );

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      final BouncyCastleProvider bcProvider = new BouncyCastleProvider();
      if (Security.addProvider(bcProvider) == -1) {
        throw new RuntimeException("Could not configure BouncyCastle provider");
      }
    }
  }

  /**
   * No-args Constructor to prevent instantiation.
   */
  private BcKeyUtils() {
  }

  /**
   * Convert from a {@link Ed25519PrivateKeyParameters} to a {@link PrivateKey}.
   *
   * @param ed25519PrivateKeyParameters A {@link Ed25519PrivateKeyParameters}.
   *
   * @return A {@link PrivateKey}.
   */
  public static PrivateKey toPrivateKey(final Ed25519PrivateKeyParameters ed25519PrivateKeyParameters) {
    Objects.requireNonNull(ed25519PrivateKeyParameters);

    // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of sekp256k1 keys.
    // Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
    UnsignedByteArray prefixedPrivateKey = UnsignedByteArray.of(PrivateKey.ED2559_PREFIX)
      .append(UnsignedByteArray.of(ed25519PrivateKeyParameters.getEncoded()));
    return PrivateKey.of(prefixedPrivateKey);
  }

  /**
   * Convert from a {@link ECPrivateKeyParameters} to a {@link PrivateKey}.
   *
   * @param ecPrivateKeyParameters A {@link ECPrivateKeyParameters}.
   *
   * @return A {@link PrivateKey}.
   */
  public static PrivateKey toPrivateKey(final ECPrivateKeyParameters ecPrivateKeyParameters) {
    // Convert the HEX representation of the BigInteger into bytes.
    byte[] privateKeyBytes = BaseEncoding.base16().decode(ecPrivateKeyParameters.getD().toString(16).toUpperCase());
    return PrivateKey.of(
      UnsignedByteArray.of(PrivateKey.SECP256K1_PREFIX) // <-- Per #486, always append 0x00 to secp256k1 private keys
        .append(UnsignedByteArray.of(privateKeyBytes))
    );
  }

  /**
   * Convert from a {@link PublicKey} to a {@link Ed25519PublicKeyParameters}.
   *
   * @param publicKey A {@link PublicKey} with
   *
   * @return A {@link Ed25519PublicKeyParameters}.
   */
  public static Ed25519PublicKeyParameters toEd25519PublicKeyParameters(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);
    Preconditions.checkArgument(publicKey.keyType() == ED25519);

    final byte[] bytes = publicKey.value().toByteArray();
    Preconditions.checkArgument(bytes.length == 33);

    // Remove the leading prefix byte, which will be `0xED`
    final byte[] truncatedBytes = Arrays.copyOfRange(bytes, 1, bytes.length);
    return new Ed25519PublicKeyParameters(truncatedBytes, 0);
  }

  /**
   * Convert from a {@link Ed25519PublicKeyParameters} to a {@link PublicKey}.
   *
   * @param ed25519PublicKeyParameters A {@link Ed25519PublicKeyParameters}.
   *
   * @return A {@link PublicKey}.
   */
  public static PublicKey toPublicKey(final Ed25519PublicKeyParameters ed25519PublicKeyParameters) {
    Objects.requireNonNull(ed25519PublicKeyParameters);
    // XRPL ED25519 keys are prefixed with 0xED so that the keys are 33 bytes and match the length of sekp256k1 keys.
    // Bouncy Castle only deals with 32 byte keys, so we need to manually add the prefix
    UnsignedByteArray prefixedPublicKey = UnsignedByteArray.of(PrivateKey.ED2559_PREFIX)
      .append(UnsignedByteArray.of(ed25519PublicKeyParameters.getEncoded()));

    return PublicKey.builder()
      .value(prefixedPublicKey)
      .build();
  }

  /**
   * Convert from a {@link ECPublicKeyParameters} to a {@link PublicKey}.
   *
   * @param ecPublicKeyParameters A {@link ECPublicKeyParameters}.
   *
   * @return A {@link PublicKey}.
   */
  public static PublicKey toPublicKey(final ECPublicKeyParameters ecPublicKeyParameters) {
    Objects.requireNonNull(ecPublicKeyParameters);
    // The binary version of an EC PublicKey is the encoded ECPoint, compressed.
    final byte[] encodedPublicKey = ecPublicKeyParameters.getQ().getEncoded(true);
    return PublicKey.builder()
      .value(UnsignedByteArray.of(encodedPublicKey))
      .build();
  }

  /**
   * Convert from a {@link ECPrivateKeyParameters} to a {@link PublicKey}.
   *
   * @param ecPrivateKeyParameters A {@link ECPrivateKeyParameters}.
   *
   * @return A {@link PublicKey}.
   */
  public static ECPublicKeyParameters toPublicKey(final ECPrivateKeyParameters ecPrivateKeyParameters) {
    Objects.requireNonNull(ecPrivateKeyParameters);
    ECPoint ecPoint = BcKeyUtils.PARAMS.getG().multiply(ecPrivateKeyParameters.getD());
    return new ECPublicKeyParameters(ecPoint, PARAMS);
  }

  /**
   * Convert from a {@link PrivateKey} to a {@link PublicKey}.
   *
   * @param privateKey A {@link PrivateKey}.
   *
   * @return A {@link PublicKey}.
   */
  public static PublicKey toPublicKey(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);

    if (privateKey.keyType() == ED25519) {
      final Ed25519PrivateKeyParameters ed25519PrivateKeyParameters = toEd25519PrivateKeyParams(privateKey);
      return toPublicKey(ed25519PrivateKeyParameters.generatePublicKey());
    } else if (privateKey.keyType() == KeyType.SECP256K1) {
      final ECPrivateKeyParameters ecPrivateKeyParameters = toEcPrivateKeyParams(privateKey);
      final ECPublicKeyParameters ecPublicKeyParameters = toPublicKey(ecPrivateKeyParameters);
      return toPublicKey(ecPublicKeyParameters);
    } else {
      throw new IllegalArgumentException("Invalid KeyType: " + privateKey.keyType());
    }
  }

  /**
   * Convert from a {@link PrivateKey} to a {@link Ed25519PrivateKeyParameters}.
   *
   * @param privateKey A {@link PrivateKey}.
   *
   * @return A {@link Ed25519PrivateKeyParameters}.
   */
  public static Ed25519PrivateKeyParameters toEd25519PrivateKeyParams(PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    Preconditions.checkArgument(privateKey.keyType() == ED25519);
    return new Ed25519PrivateKeyParameters(privateKey.value().toByteArray(), 1); // <-- Strip leading prefix byte.
  }

  /**
   * Convert from a {@link PublicKey} to a {@link ECPublicKeyParameters}.
   *
   * @param publicKey A {@link PublicKey} with
   *
   * @return A {@link ECPublicKeyParameters}.
   */
  public static ECPublicKeyParameters toEcPublicKeyParameters(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);
    Preconditions.checkArgument(publicKey.keyType() == KeyType.SECP256K1);

    ECPoint ecPoint = PARAMS.getCurve()
      .decodePoint(publicKey.value().toByteArray());
    return new ECPublicKeyParameters(ecPoint, PARAMS);
  }

  /**
   * Convert from a {@link PrivateKey} to a {@link ECPrivateKeyParameters}.
   *
   * @param privateKey A {@link PrivateKey}.
   *
   * @return A {@link ECPrivateKeyParameters}.
   */
  public static ECPrivateKeyParameters toEcPrivateKeyParams(final PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    Preconditions.checkArgument(privateKey.keyType() == KeyType.SECP256K1, "KeyType must be SECP256K1");

    final BigInteger privateKeyInt = new BigInteger(
      BaseEncoding.base16().encode(privateKey.value().toByteArray()), 16
    );
    return new ECPrivateKeyParameters(privateKeyInt, BcKeyUtils.PARAMS);
  }
}

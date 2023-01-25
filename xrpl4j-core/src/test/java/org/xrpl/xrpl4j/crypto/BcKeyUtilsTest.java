package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: bouncycastle
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.BcKeyUtils.PARAMS;
import static org.xrpl.xrpl4j.keypairs.Secp256k1.ecDomainParameters;

import com.google.common.io.BaseEncoding;
import org.assertj.core.api.Assertions;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * Unit tests for {@link BcKeyUtils}.
 */
@Deprecated
class BcKeyUtilsTest {

  // BouncyCastle Encoding
  private static final String EC_PRIVATE_KEY_HEX = "D12D2FACA9AD92828D89683778CB8DFCCDBD6C9E92F6AB7D6065E8AACC1FF6D6";
  private static final BigInteger EC_PRIVATE_KEY_BIGINTEGER = new BigInteger(EC_PRIVATE_KEY_HEX, 16);
  private static final String EC_PUBLIC_KEY_HEX = "03661BA57FED0D115222E30FE7E9509325EE30E7E284D3641E6FB5E67368C2DB18";

  private static final String ED_PRIVATE_KEY_HEX = "B224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";
  private static final String ED_PUBLIC_KEY_HEX = "94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE";

  @BeforeEach
  public void setUp() {

  }

  @Test
  void edPrivateKeyParametersToPrivateKeyAndBack() {
    Ed25519PrivateKeyParameters ed25519PrivateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX), 0
    );

    // To PrivateKey
    PrivateKey privateKey = BcKeyUtils.toPrivateKey(ed25519PrivateKeyParameters);
    assertThat(privateKey.base58Encoded()).isEqualTo("pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh");
    assertThat(privateKey.base16Encoded()).isEqualTo("ED" + ED_PRIVATE_KEY_HEX);

    // Convert back
    Ed25519PrivateKeyParameters converted = BcKeyUtils.toEd25519PrivateKeyParams(privateKey);
    Assertions.assertThat(converted).isEqualToComparingFieldByField(ed25519PrivateKeyParameters);
  }

  @Test
  void ecPrivateKeyParametersToPrivateKeyAndBack() {
    ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, PARAMS);

    // To PrivateKey
    PrivateKey privateKey = BcKeyUtils.toPrivateKey(ecPrivateKeyParameters);
    assertThat(privateKey.base58Encoded()).isEqualTo("EnYwxojogCYKG3F5Bf7zvcZjo76pEqKwG9wGH14JngcV");
    assertThat(privateKey.base16Encoded())
      .isEqualTo("D12D2FACA9AD92828D89683778CB8DFCCDBD6C9E92F6AB7D6065E8AACC1FF6D6");
    assertThat(BcKeyUtils.toEc25519PrivateKeyParams(privateKey)).isEqualToComparingFieldByField(ecPrivateKeyParameters);

    // Convert back
    ECPrivateKeyParameters converted = BcKeyUtils.toEc25519PrivateKeyParams(privateKey);
    Assertions.assertThat(converted).isEqualToComparingFieldByField(ecPrivateKeyParameters);
  }

  @Test
  void edPublicKeyParametersToPublicKeyAndBack() {
    Ed25519PublicKeyParameters ed25519PublicKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX), 0
    ).generatePublicKey();

    PublicKey publicKey = BcKeyUtils.toPublicKey(ed25519PublicKeyParameters);
    assertThat(publicKey.base16Encoded()).isEqualTo("ED" + ED_PUBLIC_KEY_HEX);

    Ed25519PublicKeyParameters converted = BcKeyUtils.toEd25519PublicKeyParameters(publicKey);
    Assertions.assertThat(converted).isEqualToComparingFieldByField(ed25519PublicKeyParameters);
  }

  @Test
  void ecPublicKeyParametersToPublicKeyAndBack() {
    ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, PARAMS);
    ECPoint ecPoint = ecDomainParameters.getG().multiply(ecPrivateKeyParameters.getD());
    ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(ecPoint, PARAMS);

    PublicKey publicKey = BcKeyUtils.toPublicKey(ecPublicKeyParameters);
    assertThat(publicKey.base16Encoded()).isEqualTo(EC_PUBLIC_KEY_HEX);

    ECPublicKeyParameters converted = BcKeyUtils.toEcPublicKeyParameters(publicKey);
    Assertions.assertThat(converted).isEqualToComparingFieldByField(ecPublicKeyParameters);
  }

  @Test
  void toPublicKeyEc() {
    ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, PARAMS);
    PrivateKey ecPrivateKey = BcKeyUtils.toPrivateKey(ecPrivateKeyParameters);

    // To Public Key
    PublicKey publicKey = BcKeyUtils.toPublicKey(ecPrivateKey);
    assertThat(publicKey.base16Encoded()).isEqualTo(EC_PUBLIC_KEY_HEX);
  }

  @Test
  void toPublicKeyEd() {
    Ed25519PrivateKeyParameters ed25519PrivateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX), 0
    );
    PrivateKey edPrivateKey = BcKeyUtils.toPrivateKey(ed25519PrivateKeyParameters);

    // To Public Key
    PublicKey publicKey = BcKeyUtils.toPublicKey(edPrivateKey);
    assertThat(publicKey.base16Encoded()).isEqualTo("ED" + ED_PUBLIC_KEY_HEX);
  }

}

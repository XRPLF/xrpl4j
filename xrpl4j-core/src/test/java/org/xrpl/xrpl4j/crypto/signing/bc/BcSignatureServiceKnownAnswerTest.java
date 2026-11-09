package org.xrpl.xrpl4j.crypto.signing.bc;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_PUBLIC_KEY;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PUBLIC_KEY;
import static org.xrpl.xrpl4j.crypto.TestConstants.getEcPrivateKey;
import static org.xrpl.xrpl4j.crypto.TestConstants.getEdPrivateKey;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.Signature;

/**
 * Known-answer and negative-rejection unit tests for the secp256k1 and Ed25519 sign and verify paths in
 * {@link BcSignatureService}. Each expected value here is produced by the library itself, so the signatures are
 * recorded fixtures for deterministic inputs.
 */
class BcSignatureServiceKnownAnswerTest {

  private static final UnsignedByteArray MESSAGE = UnsignedByteArray.of(
    BaseEncoding.base16().decode("00112233445566778899AABBCCDDEEFF")
  );

  private static final UnsignedByteArray TAMPERED_MESSAGE = UnsignedByteArray.of(
    BaseEncoding.base16().decode("00112233445566778899AABBCCDDEE00")
  );

  private static final String ED_SIGNATURE_HEX =
    "08853EBC6EB6781A203F72163CC9D47F72C69D9C134BD7D26D4FAC05D6D5D27B" +
      "08586F125997CAC00D9AF8D2BD52C4BED19D46690BC53636B75FB27E697F8604";

  private static final String EC_SIGNATURE_HEX =
    "3045022100B6E15DB54AAC73165BACA1DD90C97215AC578DFF12BFC9DDDB6B178A8544" +
      "368102203F61BEA0B3583D13291D9FCAE8746EF0AB53E178FB9771F52F1ABB7109D38734";

  private BcSignatureService signatureService;

  private PrivateKey edPrivateKey;
  private PrivateKey ecPrivateKey;

  private PublicKey wrongEdPublicKey;
  private PublicKey wrongEcPublicKey;

  @BeforeEach
  public void setUp() {
    this.signatureService = new BcSignatureService();

    this.edPrivateKey = getEdPrivateKey();
    this.ecPrivateKey = getEcPrivateKey();

    this.wrongEdPublicKey = Seed.ed25519SeedFromPassphrase(Passphrase.of("wrong")).deriveKeyPair().publicKey();
    this.wrongEcPublicKey = Seed.secp256k1SeedFromPassphrase(Passphrase.of("wrong")).deriveKeyPair().publicKey();
  }

  @Test
  void ed25519SignProducesKnownAnswerAndVerifies() {
    Signature signature = signatureService.edDsaSign(edPrivateKey, MESSAGE);

    assertThat(signature.value().hexValue()).isEqualTo(ED_SIGNATURE_HEX);
    assertThat(signatureService.edDsaVerify(ED_PUBLIC_KEY, MESSAGE, signature)).isTrue();
  }

  @Test
  void secp256k1SignProducesKnownAnswerAndVerifies() {
    Signature signature = signatureService.ecDsaSign(ecPrivateKey, MESSAGE);

    assertThat(signature.value().hexValue()).isEqualTo(EC_SIGNATURE_HEX);
    assertThat(signatureService.ecDsaVerify(EC_PUBLIC_KEY, MESSAGE, signature)).isTrue();
  }

  @Test
  void ed25519VerifyRejectsTamperedMessage() {
    Signature signature = signatureService.edDsaSign(edPrivateKey, MESSAGE);

    assertThat(signatureService.edDsaVerify(ED_PUBLIC_KEY, TAMPERED_MESSAGE, signature)).isFalse();
  }

  @Test
  void secp256k1VerifyRejectsTamperedMessage() {
    Signature signature = signatureService.ecDsaSign(ecPrivateKey, MESSAGE);

    assertThat(signatureService.ecDsaVerify(EC_PUBLIC_KEY, TAMPERED_MESSAGE, signature)).isFalse();
  }

  @Test
  void ed25519VerifyRejectsWrongPublicKey() {
    Signature signature = signatureService.edDsaSign(edPrivateKey, MESSAGE);

    assertThat(signatureService.edDsaVerify(wrongEdPublicKey, MESSAGE, signature)).isFalse();
  }

  @Test
  void secp256k1VerifyRejectsWrongPublicKey() {
    Signature signature = signatureService.ecDsaSign(ecPrivateKey, MESSAGE);

    assertThat(signatureService.ecDsaVerify(wrongEcPublicKey, MESSAGE, signature)).isFalse();
  }

  @Test
  void ed25519VerifyRejectsMalformedSignature() {
    Signature malformedSignature = Signature.builder()
      .value(UnsignedByteArray.of(new byte[] {0x01, 0x02, 0x03}))
      .build();

    assertThat(signatureService.edDsaVerify(ED_PUBLIC_KEY, MESSAGE, malformedSignature)).isFalse();
  }

  @Test
  void secp256k1VerifyRejectsMalformedSignature() {
    Signature malformedSignature = Signature.builder()
      .value(UnsignedByteArray.of(BaseEncoding.base16().decode("30060101FF0101FF")))
      .build();

    assertThat(signatureService.ecDsaVerify(EC_PUBLIC_KEY, MESSAGE, malformedSignature)).isFalse();
  }

  @Test
  void ed25519KeyDerivationRoundTrip() {
    KeyPair keyPair = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello")).deriveKeyPair();

    assertThat(keyPair.publicKey()).isEqualTo(ED_PUBLIC_KEY);
    assertThat(signatureService.derivePublicKey(keyPair.privateKey())).isEqualTo(keyPair.publicKey());
  }

  @Test
  void secp256k1KeyDerivationRoundTrip() {
    KeyPair keyPair = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello")).deriveKeyPair();

    assertThat(keyPair.publicKey()).isEqualTo(EC_PUBLIC_KEY);
    assertThat(signatureService.derivePublicKey(keyPair.privateKey())).isEqualTo(keyPair.publicKey());
  }
}

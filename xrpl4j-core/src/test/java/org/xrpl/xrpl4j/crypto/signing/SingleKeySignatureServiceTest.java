package org.xrpl.xrpl4j.crypto.signing;

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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Unit tests for {@link SingleKeySignatureService}.
 */
@Deprecated
class SingleKeySignatureServiceTest {

  private static final String SECP256K1 = "secp256k1";
  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName(SECP256K1);
  private static final ECDomainParameters EC_CURVE =
    new ECDomainParameters(
      CURVE_PARAMS.getCurve(),
      CURVE_PARAMS.getG(),
      CURVE_PARAMS.getN(),
      CURVE_PARAMS.getH()
    );

  // Source ed25519 Key
  // 32 Bytes (no XRPL prefix here)
  private static final String ED_PRIVATE_KEY_HEX = "B224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";
  private static final String sourceClassicAddressED = "rwGWYtRR6jJJJq7FKQg74YwtkiPyUqJ466";
  // Source secp256k1 Key
  // 33 Bytes
  private static final String EC_PRIVATE_KEY_HEX = "0093CC77E2333958D1480FC36811A68A1785258F65251DE100012FA18D0186FFB0";
  private static final String sourceClassicAddressEC = "rDt78kzcAfRf5NwmwL4f3E5pK14iM4CxRi";
  // Dest address
  private static final String destinationClassicAddress = "rD8ATvjj9mfnFuYYTGRNb9DygnJW9JNN1C";

  private ECPrivateKeyParameters knownEcPrivateKeyParameters;
  private Ed25519PrivateKeyParameters knownEd25519PrivateKeyParameters;

  private SingleKeySignatureService edSignatureService;
  private SingleKeySignatureService ecSignatureService;

  @BeforeEach
  public void setUp() {
    this.knownEd25519PrivateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX), 0
    );
    this.edSignatureService = new SingleKeySignatureService(BcKeyUtils.toPrivateKey(knownEd25519PrivateKeyParameters));

    this.knownEcPrivateKeyParameters = new ECPrivateKeyParameters(
      new BigInteger(EC_PRIVATE_KEY_HEX, 16), EC_CURVE
    );
    this.ecSignatureService = new SingleKeySignatureService(BcKeyUtils.toPrivateKey(knownEcPrivateKeyParameters));

  }

  @Test
  void getPublicKeyEc() {
    PublicKey actualEcPublicKey = this.ecSignatureService.getPublicKey(keyMetadata("single_key"));
    assertThat(actualEcPublicKey.base16Encoded())
      .isEqualTo("0378272C2A8F6146FE94BA3D116F548179A9875CBBD52E9D9B91A0FA44AEC4684D");
    assertThat(actualEcPublicKey.base58Encoded()).isEqualTo("aBQFwK1G6ErqTM52SMAT5f6qaj4ARazaEw5fKHRbR1tYy5djdWAu");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(KeyType.SECP256K1);
  }

  @Test
  void getPublicKeyEd() {
    PublicKey actualEcPublicKey = this.edSignatureService.getPublicKey(keyMetadata("single_key"));
    assertThat(actualEcPublicKey.base16Encoded())
      .isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
    assertThat(actualEcPublicKey.base58Encoded()).isEqualTo("aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(KeyType.ED25519);
  }

  @Test
  void edDsaSignAndVerify() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final PublicKey publicKey = this.edSignatureService.getPublicKey(keyMetadata);
    assertThat(publicKey.base16Encoded())
      .isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
    // 94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE
    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressED))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(
        org.xrpl.xrpl4j.crypto.core.keys.PublicKey.fromBase16EncodedPublicKey(publicKey.base16Encoded())
      )
      .build();

    SignedTransaction<Payment> transactionWithSignature = this.edSignatureService.sign(keyMetadata, paymentTransaction);
    assertThat(transactionWithSignature).isNotNull();
    assertThat(transactionWithSignature.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = edSignatureService.verify(keyMetadata, transactionWithSignature);
    assertThat(signatureResult).isTrue();

    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .account(Address.of(sourceClassicAddressED))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .flags(Flags.NfTokenMintFlags.builder().tfBurnable(true).tfTransferable(true).tfOnlyXRP(true).build())
      .uri(NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .signingPublicKey(
        org.xrpl.xrpl4j.crypto.core.keys.PublicKey.fromBase16EncodedPublicKey(publicKey.base16Encoded())
      )
      .build();

    SignedTransaction<NfTokenMint> mintTxWithSignature = this.edSignatureService.sign(keyMetadata, nfTokenMint);
    assertThat(mintTxWithSignature).isNotNull();
    assertThat(mintTxWithSignature.unsignedTransaction()).isEqualTo(nfTokenMint);

    assertThat(edSignatureService.verify(keyMetadata, mintTxWithSignature)).isTrue();
  }

  @Test
  void ecDsaSignAndVerify() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final PublicKey publicKey = this.ecSignatureService.getPublicKey(keyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEC))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(
        org.xrpl.xrpl4j.crypto.core.keys.PublicKey.fromBase16EncodedPublicKey(publicKey.base16Encoded())
      )
      .build();

    SignedTransaction<Payment> transactionWithSignature = this.ecSignatureService.sign(keyMetadata, paymentTransaction);
    assertThat(transactionWithSignature).isNotNull();
    assertThat(transactionWithSignature.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = ecSignatureService.verify(keyMetadata, transactionWithSignature);
    assertThat(signatureResult).isTrue();
  }

  @Test
  void addSignatureUsingSingleKeySignatureService() {

    WalletFactory walletFactory = DefaultWalletFactory.getInstance();
    Wallet testWallet = walletFactory.fromSeed("shhf1NSyjZw1JLj9Y67XdiAeaqDou", true);

    NfTokenMint nfTokenMint2 = NfTokenMint.builder()
      .account(testWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .flags(Flags.NfTokenMintFlags.builder().tfBurnable(true).tfTransferable(true).tfOnlyXRP(true).build())
      .uri(NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi"))
      .tokenTaxon(UnsignedLong.valueOf(146999694L))
      .signingPublicKey(
        org.xrpl.xrpl4j.crypto.core.keys.PublicKey.fromBase16EncodedPublicKey(testWallet.publicKey())
      )
      .build();

    // Sign transaction -----------------------------------------------------------
    // Construct a SignatureService to sign the NfTokenMint
    PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(
      testWallet.privateKey().get()
    );
    SignatureService signatureService = new SingleKeySignatureService(privateKey);

    // Sign the tx
    assertDoesNotThrow(
      () -> signatureService.sign(
        KeyMetadata.EMPTY,
        nfTokenMint2
      )
    );
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Helper function to generate Key meta-data based upon the supplied inputs.
   *
   * @param keyIdentifier A {@link String} identifying the key.
   *
   * @return A {@link KeyMetadata}.
   */
  private KeyMetadata keyMetadata(final String keyIdentifier) {
    Objects.requireNonNull(keyIdentifier);

    return KeyMetadata.builder()
      .platformIdentifier("jks")
      .keyringIdentifier("n/a")
      .keyIdentifier(keyIdentifier)
      .keyVersion("1")
      .keyPassword("password")
      .build();
  }

}

package org.xrpl.xrpl4j.crypto.bc.keys;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;

/**
 * Unit test to validate seed decoding using BouncyCastle.
 */
public class SeedSecretTest {

  @Test
  public void seedFromBase58EncodedSecretEd25519() {
    Seed actual = Seed.seedFromBase58EncodedSecret("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM");
    assertThat(actual.decodedSeed().bytes().hexValue()).isEqualTo("2C74FD17EDAFD80E8447B0D46741EE24");

    Ed25519KeyPairService keypairService = Ed25519KeyPairService.getInstance();
    KeyPair keypair = keypairService.deriveKeyPair(actual);
    assertThat(BcAddressUtils.getInstance().deriveAddress(keypair.publicKey()).value())
      .isEqualTo("rpsAiz1JjunVeGk5QipvZt8QxY3hRcmKRR");
  }

  @Test
  public void seedFromBase58EncodedSecretSecp256k1() {
    Seed actual = Seed.seedFromBase58EncodedSecret("snjs1zg8jq6xqV2Q9VykB9uZwqafo");
    assertThat(actual.decodedSeed().bytes().hexValue()).isEqualTo("D980421171205A0D79A69A37D6C7CB1C");

    Secp256k1KeyPairService keypairService = Secp256k1KeyPairService.getInstance();
    KeyPair keypair = keypairService.deriveKeyPair(actual);
    assertThat(BcAddressUtils.getInstance().deriveAddress(keypair.publicKey()).value())
      .isEqualTo("rK981k1uW4JZBq8xTy3DYmDETyeCj5mMKQ");
  }

}

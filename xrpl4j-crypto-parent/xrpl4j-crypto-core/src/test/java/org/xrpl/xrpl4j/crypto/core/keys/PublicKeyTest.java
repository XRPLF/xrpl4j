package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link PublicKey}.
 */
public class PublicKeyTest {

  private Ed25519KeyPairService ed25519KeyPairService;
  private Secp256k1KeyPairService secp256k1KeyPairService;

  private PublicKey edPublicKey;
  private PublicKey ecPublicKey;

  @BeforeEach
  public void setUp() {
    this.ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    this.secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();

    this.edPublicKey = ed25519KeyPairService
      .deriveKeyPair(Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))).publicKey();
    this.ecPublicKey = secp256k1KeyPairService
      .deriveKeyPair(Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))).publicKey();
  }

  @Test
  public void fromBase58EncodedStringEd25519() {
    final String base58EncodedPublicKey = "aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B";
    assertThat(PublicKey.fromBase58EncodedPublicKey(base58EncodedPublicKey).base58Value())
      .isEqualTo(base58EncodedPublicKey);
  }

  @Test
  public void fromBase58EncodedStringSecp256k1() {
    final String base58EncodedPublicKey = "aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk";
    assertThat(PublicKey.fromBase58EncodedPublicKey(base58EncodedPublicKey).base58Value())
      .isEqualTo(base58EncodedPublicKey);
  }

  @Test
  public void fromBase16EncodedStringEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    final KeyPair keyPair = ed25519KeyPairService.deriveKeyPair(seed);
    final PublicKey publicKey = keyPair.publicKey();

    final String expectedBase58EncodedPublicKey = "aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B";
    final String expectedBase16EncodedPublicKey = "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE";

    assertThat(publicKey.base58Value()).isEqualTo(expectedBase58EncodedPublicKey);
    assertThat(publicKey.hexValue()).isEqualTo(expectedBase16EncodedPublicKey);
    assertThat(AddressCodec.getInstance().encodeAccountPublicKey(publicKey.value()))
      .isEqualTo(expectedBase58EncodedPublicKey);
  }

  @Test
  public void fromBase16EncodedStringSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"));
    final KeyPair keyPair = secp256k1KeyPairService.deriveKeyPair(seed);

    final String expectedBase58EncodedPublicKey = "aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk";
    final String expectedBase16EncodedPublicKey = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";

    assertThat(keyPair.publicKey().base58Value()).isEqualTo(expectedBase58EncodedPublicKey);
    assertThat(keyPair.publicKey().hexValue()).isEqualTo(expectedBase16EncodedPublicKey);
    assertThat(AddressCodec.getInstance().encodeAccountPublicKey(keyPair.publicKey().value()))
      .isEqualTo(expectedBase58EncodedPublicKey);
  }

  @Test
  public void versionTypeSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"));
    final PublicKey publicKey = secp256k1KeyPairService.deriveKeyPair(seed).publicKey();
    assertThat(publicKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void versionTypeEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    final PublicKey publicKey = ed25519KeyPairService.deriveKeyPair(seed).publicKey();
    assertThat(publicKey.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  void hexValue() {
    assertThat(edPublicKey.base16Value()).isEqualTo(
      "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
    assertThat(edPublicKey.base16Value()).isEqualTo(
      "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
    assertThat(edPublicKey.hexValue()).isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
    assertThat(ecPublicKey.hexValue()).isEqualTo("027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9");
  }

  @Test
  void hexValueInterface() {
    PublicKey publicKey = new PublicKey() {
      @Override
      public UnsignedByteArray value() {
        return null;
      }

      @Override
      public String base58Value() {
        return null;
      }

      @Override
      public String base16Value() {
        return "ABCD";
      }

      @Override
      public VersionType versionType() {
        return null;
      }
    };

    assertThat(publicKey.hexValue()).isEqualTo("ABCD");
  }

  @Test
  void base58Value() {
    assertThat(edPublicKey.base58Value()).isEqualTo("aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B");
    assertThat(ecPublicKey.base58Value()).isEqualTo("aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk");
  }

  @Test
  void testEquals() {
    assertThat(edPublicKey).isEqualTo(edPublicKey);
    assertThat(ecPublicKey).isEqualTo(ecPublicKey);
    assertThat(edPublicKey).isNotEqualTo(ecPublicKey);
    assertThat(ecPublicKey).isNotEqualTo(edPublicKey);
    assertThat(ecPublicKey).isNotEqualTo(new Object());
  }

  @Test
  void testHashCode() {
    assertThat(edPublicKey.hashCode()).isEqualTo(edPublicKey.hashCode());
    assertThat(ecPublicKey.hashCode()).isEqualTo(ecPublicKey.hashCode());
    assertThat(edPublicKey.hashCode()).isNotEqualTo(ecPublicKey.hashCode());
  }

  @Test
  void testToString() {
    assertThat(edPublicKey.toString()).isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
    assertThat(ecPublicKey.toString()).isEqualTo("027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9");
  }

  @Test
  void jsonSerializeAndDeserializeEd() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(edPublicKey);
    assertThat(json).isEqualTo("\"ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE\"");

    PublicKey actual = ObjectMapperFactory.create().readValue(json, PublicKey.class);
    assertThat(actual.base16Value()).isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
  }

  @Test
  void jsonSerializeAndDeserializeEc() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(ecPublicKey);
    assertThat(json).isEqualTo("\"027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9\"");

    PublicKey actual = ObjectMapperFactory.create().readValue(json, PublicKey.class);
    assertThat(actual.base16Value()).isEqualTo("027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9");
  }
}
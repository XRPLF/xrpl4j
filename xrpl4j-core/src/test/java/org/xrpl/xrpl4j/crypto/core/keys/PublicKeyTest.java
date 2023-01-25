package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.EC_PUBLIC_KEY;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.EC_PUBLIC_KEY_B58;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.EC_PUBLIC_KEY_HEX;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_PUBLIC_KEY;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_PUBLIC_KEY_B58;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_PUBLIC_KEY_HEX;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link PublicKey}.
 */
public class PublicKeyTest {

  @Test
  public void fromBase58EncodedStringEd25519() {
    assertThat(PublicKey.fromBase58EncodedPublicKey(ED_PUBLIC_KEY_B58).base58Value()).isEqualTo(ED_PUBLIC_KEY_B58);
  }

  @Test
  public void fromBase58EncodedStringSecp256k1() {
    assertThat(PublicKey.fromBase58EncodedPublicKey(EC_PUBLIC_KEY_B58).base58Value()).isEqualTo(EC_PUBLIC_KEY_B58);
  }

  @Test
  public void fromBase16EncodedStringEd25519() {
    assertThat(PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX).base58Value()).isEqualTo(ED_PUBLIC_KEY_B58);
    assertThat(PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX.toLowerCase()).base58Value())
      .isEqualTo(ED_PUBLIC_KEY_B58);
  }

  @Test
  public void fromBase16EncodedStringSecp256k1() {
    assertThat(PublicKey.fromBase16EncodedPublicKey(EC_PUBLIC_KEY_HEX).base58Value()).isEqualTo(EC_PUBLIC_KEY_B58);
    assertThat(PublicKey.fromBase16EncodedPublicKey(EC_PUBLIC_KEY_HEX.toLowerCase()).base58Value())
      .isEqualTo(EC_PUBLIC_KEY_B58);
  }

  @Test
  public void versionTypeSecp256k1() {
    assertThat(EC_PUBLIC_KEY.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void versionTypeEd25519() {
    assertThat(ED_PUBLIC_KEY.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  void hexValue() {
    // Call this multiple times to ensure immutability...
    assertThat(ED_PUBLIC_KEY.base16Value()).isEqualTo(ED_PUBLIC_KEY_HEX);
    assertThat(ED_PUBLIC_KEY.base16Value()).isEqualTo(ED_PUBLIC_KEY_HEX);

    // Call this multiple times to ensure immutability...
    assertThat(EC_PUBLIC_KEY.base16Value()).isEqualTo(EC_PUBLIC_KEY_HEX);
    assertThat(EC_PUBLIC_KEY.base16Value()).isEqualTo(EC_PUBLIC_KEY_HEX);
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

    assertThat(publicKey.base16Value()).isEqualTo("ABCD");
  }

  @Test
  void base58Value() {
    assertThat(ED_PUBLIC_KEY.base58Value()).isEqualTo(ED_PUBLIC_KEY_B58);
    assertThat(EC_PUBLIC_KEY.base58Value()).isEqualTo(EC_PUBLIC_KEY_B58);
  }

  @Test
  void testEquals() {
    assertThat(ED_PUBLIC_KEY).isEqualTo(ED_PUBLIC_KEY);
    assertThat(ED_PUBLIC_KEY).isNotEqualTo(EC_PUBLIC_KEY);
    assertThat(ED_PUBLIC_KEY).isNotEqualTo(new Object());

    assertThat(EC_PUBLIC_KEY).isEqualTo(EC_PUBLIC_KEY);
    assertThat(EC_PUBLIC_KEY).isNotEqualTo(ED_PUBLIC_KEY);
    assertThat(EC_PUBLIC_KEY).isNotEqualTo(new Object());
  }

  @Test
  void testHashCode() {
    assertThat(ED_PUBLIC_KEY.hashCode()).isEqualTo(ED_PUBLIC_KEY.hashCode());
    assertThat(ED_PUBLIC_KEY.hashCode()).isNotEqualTo(EC_PUBLIC_KEY.hashCode());

    assertThat(EC_PUBLIC_KEY.hashCode()).isEqualTo(EC_PUBLIC_KEY.hashCode());
    assertThat(EC_PUBLIC_KEY.hashCode()).isNotEqualTo(ED_PUBLIC_KEY.hashCode());
  }

  @Test
  void testToString() {
    assertThat(ED_PUBLIC_KEY.toString()).isEqualTo(
      "PublicKey{value=UnsignedByteArray{" + "unsignedBytes=List(size=33)}, " +
        "base58Value=aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B, " +
        "base16Value=ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE, " +
        "versionType=ED25519" +
        "}");
    assertThat(EC_PUBLIC_KEY.toString()).isEqualTo(
      "PublicKey{value=UnsignedByteArray{" + "unsignedBytes=List(size=33)}, " +
        "base58Value=aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk, " +
        "base16Value=027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9, " +
        "versionType=SECP256K1" +
        "}");
  }

  @Test
  void jsonSerializeAndDeserializeEd() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(ED_PUBLIC_KEY);
    assertThat(json).isEqualTo("\"ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE\"");

    PublicKey actual = ObjectMapperFactory.create().readValue(json, PublicKey.class);
    assertThat(actual.base16Value()).isEqualTo("ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE");
  }

  @Test
  void jsonSerializeAndDeserializeEc() throws JsonProcessingException {
    String json = ObjectMapperFactory.create().writeValueAsString(EC_PUBLIC_KEY);
    assertThat(json).isEqualTo("\"027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9\"");

    PublicKey actual = ObjectMapperFactory.create().readValue(json, PublicKey.class);
    assertThat(actual.base16Value()).isEqualTo("027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9");
  }

  @Test
  void deriveAddress() {
    assertThat(ED_PUBLIC_KEY.deriveAddress().value()).isEqualTo("rwGWYtRR6jJJJq7FKQg74YwtkiPyUqJ466");
    assertThat(EC_PUBLIC_KEY.deriveAddress().value()).isEqualTo("rD8ATvjj9mfnFuYYTGRNb9DygnJW9JNN1C");
  }
}
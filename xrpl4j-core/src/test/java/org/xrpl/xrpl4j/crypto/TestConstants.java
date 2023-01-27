package org.xrpl.xrpl4j.crypto;

import com.google.common.io.BaseEncoding;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Constants used for testing.
 */
public interface TestConstants {

  // ED25519 Public Key
  String ED_PUBLIC_KEY_HEX = "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE";
  String ED_PUBLIC_KEY_B58 = "aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B";
  PublicKey ED_PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX);

  String ED_PRIVATE_KEY_HEX = "EDB224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";
  String ED_PRIVATE_KEY_B58 = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
  PrivateKey ED_PRIVATE_KEY = PrivateKey.of(UnsignedByteArray.of(BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX)));

  // Secp256k1 Public Key
  String EC_PUBLIC_KEY_HEX = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";
  String EC_PUBLIC_KEY_B58 = "aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk";
  PublicKey EC_PUBLIC_KEY = PublicKey.fromBase16EncodedPublicKey(EC_PUBLIC_KEY_HEX);

  String EC_PRIVATE_KEY_HEX = "00DAD3C2B4BF921398932C889DE5335F89D90249355FC6FFB73F1256D2957F9F17";
  String EC_PRIVATE_KEY_B58 = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
  PrivateKey EC_PRIVATE_KEY = PrivateKey.of(UnsignedByteArray.of(BaseEncoding.base16().decode(EC_PRIVATE_KEY_HEX)));

  // Both generated from Passphrase.of("hello")
  Address ED_ADDRESS = Address.of("rwGWYtRR6jJJJq7FKQg74YwtkiPyUqJ466");
  Address EC_ADDRESS = Address.of("rD8ATvjj9mfnFuYYTGRNb9DygnJW9JNN1C");
}

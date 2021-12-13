package org.xrpl.xrpl4j.crypto.bc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.bc.wallet.BcWalletFactory;
import org.xrpl.xrpl4j.crypto.core.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Unit tests for {@link BcAddressUtils}.
 */
class BcAddressUtilsTest {

  @Test
  public void deriveAddressWithNull() {
    Assertions.assertThrows(NullPointerException.class, () -> BcAddressUtils.getInstance().deriveAddress(null));
  }

  @Test
  public void deriveAddress() {
    PublicKey publicKey = BcWalletFactory.getInstance()
      .fromSeed(Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"))).publicKey();
    Address address = BcAddressUtils.getInstance().deriveAddress(publicKey);
    assertThat(address.value()).isEqualTo("rwGWYtRR6jJJJq7FKQg74YwtkiPyUqJ466");
  }

}
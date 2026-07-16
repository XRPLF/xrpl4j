package org.xrpl.xrpl4j.crypto.confidential.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;

/**
 * Unit tests for {@link MptConfidentialParty}, which mirrors the C struct {@code mpt_confidential_recipient} (a public
 * key plus an encrypted amount).
 */
class MptConfidentialPartyTest {

  private static final PublicKey PUBLIC_KEY =
    Seed.secp256k1SeedFromPassphrase(Passphrase.of("party")).deriveKeyPair().publicKey();
  private static final EncryptedAmount ENCRYPTED_AMOUNT = EncryptedAmount.of(Strings.repeat("03", 66));

  @Test
  void ofExposesFields() {
    MptConfidentialParty party = MptConfidentialParty.of(PUBLIC_KEY, ENCRYPTED_AMOUNT);
    assertThat(party.publicKey()).isEqualTo(PUBLIC_KEY);
    assertThat(party.encryptedAmount()).isEqualTo(ENCRYPTED_AMOUNT);
  }

  @Test
  void builderMatchesOf() {
    MptConfidentialParty built = MptConfidentialParty.builder()
      .publicKey(PUBLIC_KEY)
      .encryptedAmount(ENCRYPTED_AMOUNT)
      .build();
    assertThat(built).isEqualTo(MptConfidentialParty.of(PUBLIC_KEY, ENCRYPTED_AMOUNT));
  }
}

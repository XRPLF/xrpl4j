package org.xrpl.xrpl4j.crypto.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AddressConstants}.
 */
class AddressConstantsTest {

  @Test
  void testAddressConstants() {
    Assertions.assertThat(AddressConstants.ACCOUNT_ZERO.value()).isEqualTo("rrrrrrrrrrrrrrrrrrrrrhoLvTp");
    Assertions.assertThat(AddressConstants.ACCOUNT_ONE.value()).isEqualTo("rrrrrrrrrrrrrrrrrrrrBZbvji");
    Assertions.assertThat(AddressConstants.GENESIS_ACCOUNT.value()).isEqualTo("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    Assertions.assertThat(AddressConstants.NAME_RESERVATION_BLACKHOLE.value()).isEqualTo("rrrrrrrrrrrrrrrrrNAMEtxvNvQ");
    Assertions.assertThat(AddressConstants.NAN_ADDRESS.value()).isEqualTo("rrrrrrrrrrrrrrrrrrrn5RM1rHd");
  }

}
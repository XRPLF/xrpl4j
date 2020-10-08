package com.ripple.xrpl4j.wallet;

import org.junit.Before;
import org.junit.Test;

public class Ed25519WalletFactoryTest {

  Ed25519WalletFactory testnetWalletFactory;
  Ed25519WalletFactory mainnetWalletFactory;

  @Before
  public void setUp() throws Exception {
    this.testnetWalletFactory = new Ed25519WalletFactory(true);
    this.mainnetWalletFactory = new Ed25519WalletFactory(false);
  }

}

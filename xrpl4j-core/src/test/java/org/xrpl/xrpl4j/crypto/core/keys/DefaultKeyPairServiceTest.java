package org.xrpl.xrpl4j.crypto.core.keys;

import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;

/**
 * Unit tests for {@link DefaultKeyPairService}.
 */
class DefaultKeyPairServiceTest {

  private DefaultKeyPairService keyPairService;

  @BeforeEach
  void setUp() {
    openMocks(this);

    keyPairService = new DefaultKeyPairService();

  }



}
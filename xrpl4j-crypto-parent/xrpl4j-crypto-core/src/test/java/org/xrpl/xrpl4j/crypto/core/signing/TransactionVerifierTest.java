package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test for {@link TransactionVerifier}.
 */
class TransactionVerifierTest {

  private AtomicBoolean verify1Called;
  private AtomicBoolean verify2Called;

  private TransactionVerifier transactionVerifier;

  @BeforeEach
  void setUp() {
    this.verify1Called = new AtomicBoolean();
    this.verify2Called = new AtomicBoolean();

    transactionVerifier = new TransactionVerifier() {
      @Override
      public <T extends Transaction> boolean verify(
        SignatureWithPublicKey signatureWithPublicKey, T unsignedTransaction
      ) {
        verify1Called.set(true);
        return true;
      }

      @Override
      public <T extends Transaction> boolean verifyMultiSigned(
        Set<SignatureWithPublicKey> signatureWithPublicKeys, T unsignedTransaction, int minSigners
      ) {
        verify2Called.set(true);
        return true;
      }
    };
  }

  @Test
  void verify() {
    transactionVerifier.verifyMultiSigned(mock(Set.class), mock(Payment.class));
    assertThat(verify1Called.get()).isFalse();
    assertThat(verify2Called.get()).isTrue();
  }

}
package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Unit test for {@link SignableTransaction}.
 */
class SignableTransactionTest {

  @Mock
  private Transaction transactionMock;

  SignableTransaction signableTransaction;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    signableTransaction = SignableTransaction.builder()
      .signableTransactionBytes(UnsignedByteArray.empty())
      .originalUnsignedTransaction(transactionMock)
      .build();
  }

  @Test
  void originalUnsignedTransaction() {
    assertThat(signableTransaction.originalUnsignedTransaction()).isEqualTo(transactionMock);
  }

  @Test
  void signableTransactionBytes() {
    assertThat(signableTransaction.signableTransactionBytes()).isEqualTo(UnsignedByteArray.empty());
  }
}
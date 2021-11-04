package org.xrpl.xrpl4j.crypto.core.signing;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit test for {@link SignableTransaction}.
 */
class SignableTransactionTest {

  private static final String HEX_32_BYTES = "0000000000000000000000000000000000000000000000000000000000000000";

  @Mock
  private Transaction transactionMock;

  SignableTransaction signableTransaction;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    signableTransaction = SignableTransaction.builder()
      .signableTransactionBytes(UnsignedByteArray.fromHex(HEX_32_BYTES))
      .originalUnsignedTransaction(transactionMock)
      .build();
  }

  @Test
  void originalUnsignedTransaction() {
    assertThat(signableTransaction.originalUnsignedTransaction()).isEqualTo(transactionMock);
  }

  @Test
  void signableTransactionBytes() {
    assertThat(signableTransaction.signableTransactionBytes().hexValue()).isEqualTo(HEX_32_BYTES);
  }

  @Test
  void jsonSerializeAndDeserialize() throws JsonProcessingException {
    SignableTransaction signableTransaction = SignableTransaction.builder()
      .signableTransactionBytes(UnsignedByteArray.fromHex(HEX_32_BYTES))
      .originalUnsignedTransaction(Payment.builder()
        .account(Address.of(""))
        .fee(XrpCurrencyAmount.of(UnsignedLong.ONE))
        .sequence(UnsignedInteger.ONE)
        .signingPublicKey("")
        .amount(XrpCurrencyAmount.ofDrops(12345))
        .destination(Address.of(""))
        .build())
      .build();

    String json = ObjectMapperFactory.create().writeValueAsString(signableTransaction);
    with(json).assertThat("$.originalUnsignedTransaction.Account", is(""));
    with(json).assertThat("$.originalUnsignedTransaction.Fee", is("1"));
    with(json).assertThat("$.originalUnsignedTransaction.Sequence", is(1));
    with(json).assertThat("$.originalUnsignedTransaction.SigningPubKey", is(""));
    with(json).assertThat("$.originalUnsignedTransaction.Flags", is(2147483648L));
    with(json).assertThat("$.originalUnsignedTransaction.Amount", is("12345"));
    with(json).assertThat("$.originalUnsignedTransaction.Destination", is(""));
    with(json).assertThat("$.originalUnsignedTransaction.TransactionType", is("Payment"));
    with(json).assertThat("$.signableTransactionBytes", is(HEX_32_BYTES));

    SignableTransaction actual = ObjectMapperFactory.create().readValue(json, SignableTransaction.class);
    assertThat(actual).isEqualTo(signableTransaction);
  }
}
package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.transactions.SignedTransaction;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@linkn SignedTransaction}.
 */
class SignedTransactionTest {

  @Test
  public void computesCorrectTransactionHash() {
    SignedTransaction<Payment> signedTransaction = SignedTransaction.<Payment>builder()
      .signedTransaction(
        Payment.builder()
          .account(Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1"))
          .fee(XrpCurrencyAmount.ofDrops(10))
          .sequence(UnsignedInteger.valueOf(4))
          .destination(Address.of("rEqrVunkmDhWNGHELTzQmn4mX7LKvdomfq"))
          .amount(XrpCurrencyAmount.ofDrops(12345))
          .signingPublicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
          .transactionSignature("304402207B82800C3289427D6F60421CDF88545BEFC6A7C9CED15A2C53E39994E52BCED402204" +
            "43865800626F7FD02B369A875FA449E6204A46C5910E406018776CC08C948CA")
          .build()
      )
      .signedTransactionBlob("1200002280000000240000000461400000000000303968400000000000000A7321030D58EB48B4420B1F" +
        "7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D4357446304402207B82800C3289427D6F60421CDF88545BEFC6A7C9CED15" +
        "A2C53E39994E52BCED40220443865800626F7FD02B369A875FA449E6204A46C5910E406018776CC08C948CA81148049717CC94878" +
        "9F32F267ADC2582484E3DFA698831495FD80922EDD581C663FF9F8E948D0E13CBBE41C")
      .build();

    String expectedHash = "AD616E7F93DC9E5749222FCC644A95F19FB1893446A0FF47CA9B550F4D5DAB5D";
    assertThat(signedTransaction.hash().value()).isEqualTo(expectedHash);
  }

}

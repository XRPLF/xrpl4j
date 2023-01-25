package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Unit tests for {@link SingleSignedTransaction}.
 */
class SignedTransactionTest {

  /**
   * This test constructs the transaction found here:
   * https://livenet.xrpl.org/transactions/A7AE53FE15B02E6E2F3C610FB4BA30B12392EB110F1D5E8C20880555E8639B05 to check
   * that the hash that's on livenet matches what this library computes. The hash you see in this test is
   * different than the hash found on livenet because the real transaction did not set any flags on the transaction
   * and {@link Payment} requires a flags field (Even if you set flags to 0, it affects the hash). However,
   * we made {@link Payment#flags()} nullable during development and verified that the hashes match, so we are confident
   * that our hash calculation is accurate.
   */
  @Test
  public void computesCorrectTransactionHash() throws JsonProcessingException {
    final Payment unsignedTransaction = Payment.builder()
      .account(Address.of("rf56THCDKWb348ks9hvaD4YXq6U1qBJNsJ"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.valueOf(76829518))
      .destination(Address.of("rDNvjMc6LjtpR7BdfiSNvavUBjznhhmpNq"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey("0281E58C76A7EB8397C008CB6B6D325FF6765F008CF845AF5EB02DAB6D222C612C")
      .destinationTag(UnsignedInteger.valueOf(371969))
      .build();

    final Payment signedPayment = Payment.builder().from(unsignedTransaction)
      .transactionSignature("304502210093257D8E88D2A92CE55977641F72CCD235AB76B1AE189BE3377F30A6" +
        "9B131C4902200B79836114069F0D331418D05818908D85DE755AE5C2DDF42E9637FE1C11754F")
      .build();

    final Signature signature = Signature.builder().value(
      UnsignedByteArray.of(BaseEncoding.base16()
        .decode("304502210093257D8E88D2A92CE55977641F72CCD235AB76B1AE189BE3377F30A69B131C49" +
          "02200B79836114069F0D331418D05818908D85DE755AE5C2DDF42E9637FE1C11754F"))
    ).build();

    SingleSignedTransaction<Payment> signedTransaction = SingleSignedTransaction.<Payment>builder()
      .signedTransaction(signedPayment)
      .signature(signature)
      .unsignedTransaction(unsignedTransaction)
      .build();

    String expectedHash = "F847C96B2EEB0609F16C9DB9D74A0CB123B5EAF5B626207977335BF0A1EF53C3";
    assertThat(signedTransaction.hash().value()).isEqualTo(expectedHash);
    assertThat(signedTransaction.unsignedTransaction()).isEqualTo(unsignedTransaction);
    assertThat(signedTransaction.signedTransaction()).isEqualTo(signedPayment);
    assertThat(signedTransaction.signedTransactionBytes().hexValue()).isEqualTo(
      XrplBinaryCodec.getInstance().encode(ObjectMapperFactory.create().writeValueAsString(signedPayment))
    );
  }
}

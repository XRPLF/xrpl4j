package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UriTest {

  @Test
  public void uriEquality() {

    String hexUri = "697066733A2F2F62616679626569676479727A74357366703775646D37687537367568377932366E6634646675796" +
      "C71616266336F636C67747179353566627A6469";
    String plaintextUri = "ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi";

    assertThat(Uri.of(hexUri).equals(Uri.of(hexUri)));

    assertThat(Uri.ofPlainText(plaintextUri).equals(Uri.ofPlainText(plaintextUri)));

    assertThat(Uri.of("0000000000000000000000000000000000000000000000000000000000000000")).isNotEqualTo(null);
    assertThat(Uri.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(new Object());

    assertThat(Uri.ofPlainText(plaintextUri).equals(Uri.of(hexUri)));
  }

}
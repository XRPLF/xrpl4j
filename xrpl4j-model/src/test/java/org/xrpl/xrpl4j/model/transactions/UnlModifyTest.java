package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

public class UnlModifyTest {

  @Test
  public void testBuilder() {
    String validator = "EDB6FC8E803EE8EDC2793F1EC917B2EE41D35255618DEB91D3F9B1FC89B75D4539";
    UnlModify unlModify = UnlModify.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .signingPublicKey("")
      .ledgerSequence(LedgerIndex.of(UnsignedInteger.valueOf(67850752)))
      .unlModifyValidator(validator)
      .unlModifyDisabling(UnsignedInteger.valueOf(1))
      .build();

    assertThat(unlModify.transactionType()).isEqualTo(TransactionType.UNL_MODIFY);
    assertThat(unlModify.account()).isEqualTo(UnlModify.ACCOUNT_ZERO);
    assertThat(unlModify.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(unlModify.sequence()).isEqualTo(UnsignedInteger.valueOf(2470665));
    assertThat(unlModify.ledgerSequence()).isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(67850752)));
    assertThat(unlModify.unlModifyValidator()).isEqualTo(validator);
  }
}

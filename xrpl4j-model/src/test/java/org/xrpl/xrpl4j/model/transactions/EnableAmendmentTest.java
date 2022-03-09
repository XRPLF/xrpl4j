package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.Optional;

public class EnableAmendmentTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {

    Hash256 amendment = Hash256.of("42426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369F97ABF044EE");
    EnableAmendment enableAmendment = EnableAmendment.builder()
      .account(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .signingPublicKey("")
      .amendment(amendment)
      .ledgerSequence(Optional.of(LedgerIndex.of(UnsignedInteger.valueOf(67850752))))
      .build();

    assertThat(enableAmendment.transactionType()).isEqualTo(TransactionType.ENABLE_AMENDMENT);
    assertThat(enableAmendment.account()).isEqualTo(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"));
    assertThat(enableAmendment.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(enableAmendment.sequence()).isEqualTo(UnsignedInteger.valueOf(2470665));
    assertThat(enableAmendment.ledgerSequence()).isNotEmpty().get().isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(67850752)));
    assertThat(enableAmendment.amendment()).isEqualTo(amendment);
  }
}

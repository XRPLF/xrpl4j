package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.Optional;

public class SetFeeTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    SetFee setFee = SetFee.builder()
      .account(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(2470665))
      .signingPublicKey("")
      .baseFee("000000000000000A")
      .referenceFeeUnits(UnsignedInteger.valueOf(10))
      .reserveBase(UnsignedInteger.valueOf(20000000))
      .reserveIncrement(UnsignedInteger.valueOf(5000000))
      .ledgerSequence(Optional.of(LedgerIndex.of(UnsignedInteger.valueOf(67850752))))
      .build();

    assertThat(setFee.transactionType()).isEqualTo(TransactionType.SET_FEE);
    assertThat(setFee.account()).isEqualTo(Address.of("rrrrrrrrrrrrrrrrrrrrrhoLvTp"));
    assertThat(setFee.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(setFee.sequence()).isEqualTo(UnsignedInteger.valueOf(2470665));
    assertThat(setFee.ledgerSequence()).isNotEmpty().get().isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(67850752)));
    assertThat(setFee.referenceFeeUnits()).isEqualTo(UnsignedInteger.valueOf(10));
    assertThat(setFee.reserveIncrement()).isEqualTo(UnsignedInteger.valueOf(5000000));
    assertThat(setFee.reserveBase()).isEqualTo(UnsignedInteger.valueOf(20000000));
  }
}

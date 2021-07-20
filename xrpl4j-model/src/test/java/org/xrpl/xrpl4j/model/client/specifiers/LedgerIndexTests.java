package org.xrpl.xrpl4j.model.client.specifiers;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

public class LedgerIndexTests {

  @Test
  public void constructLedgerIndex() {
    LedgerIndex minLedgerIndex = LedgerIndex.of(UnsignedLong.ONE);
    assertThat(minLedgerIndex.value()).isEqualTo(UnsignedLong.ONE);

    LedgerIndex maxLedgerIndex = LedgerIndex.of(UnsignedLong.MAX_VALUE);
    assertThat(maxLedgerIndex.value()).isEqualTo(UnsignedLong.MAX_VALUE);
  }

  @Test
  public void addTwoLedgerIndexes() {
    LedgerIndex ledgerIndex1 = LedgerIndex.of(UnsignedLong.valueOf(1000));
    LedgerIndex ledgerIndex2 = LedgerIndex.of(UnsignedLong.valueOf(100));
    LedgerIndex added = ledgerIndex1.plus(ledgerIndex2);
    assertThat(added.value()).isEqualTo(ledgerIndex1.value().plus(ledgerIndex2.value()));
  }

  @Test
  public void addUnsignedLongToLedgerIndex() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedLong.valueOf(1000));
    UnsignedLong toAdd = UnsignedLong.valueOf(100);
    final LedgerIndex added = ledgerIndex.plus(toAdd);
    assertThat(added.value()).isEqualTo(ledgerIndex.value().plus(toAdd));
  }

}

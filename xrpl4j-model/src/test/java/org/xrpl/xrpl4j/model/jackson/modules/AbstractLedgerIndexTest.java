package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

public class AbstractLedgerIndexTest {

  @Value.Immutable
  @JsonSerialize(as = ImmutableLedgerIndexContainer.class)
  @JsonDeserialize(as = ImmutableLedgerIndexContainer.class)
  interface LedgerIndexContainer {

    static LedgerIndexContainer of(LedgerIndex ledgerIndex) {
      return ImmutableLedgerIndexContainer.builder()
        .ledgerIndex(ledgerIndex)
        .build();
    }

    LedgerIndex ledgerIndex();
  }
}

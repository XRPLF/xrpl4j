package org.xrpl.xrpl4j.model.client.specifiers;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;

public class LedgerIndexWrapper {

  /**
   * Represents a Ledger Index on the XRP Ledger.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = LedgerIndex.class)
  @JsonDeserialize(as = LedgerIndex.class)
  abstract static class _LedgerIndex extends Wrapper<UnsignedLong> implements Serializable {

    @Override
    public String toString() {
      return value().toString();
    }

    public LedgerIndex plus(LedgerIndex other) {
      return LedgerIndex.of(this.value().plus(other.value()));
    }

    public LedgerIndex plus(UnsignedLong unsignedLong) {
      return LedgerIndex.of(this.value().plus(unsignedLong));
    }

  }

}

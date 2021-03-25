package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.OfferObject;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableBookOffersResult.class)
@JsonDeserialize(as = ImmutableBookOffersResult.class)
public interface BookOffersResult extends XrplResult {

  static ImmutableBookOffersResult.Builder builder() {
    return ImmutableBookOffersResult.builder();
  }

  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  List<OfferObject> offers();
}

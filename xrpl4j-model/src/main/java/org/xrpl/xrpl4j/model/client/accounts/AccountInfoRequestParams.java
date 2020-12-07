package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.rippled.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoRequestParams.class)
@JsonDeserialize(as = ImmutableAccountInfoRequestParams.class)
public interface AccountInfoRequestParams extends XrplRequestParams {

  static ImmutableAccountInfoRequestParams.Builder builder() {
    return ImmutableAccountInfoRequestParams.builder();
  }

  static AccountInfoRequestParams of(Address account) {
    return builder()
      .account(account)
      .build();
  }

  Address account();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonProperty("ledger_index")
  @Value.Default
  default LedgerIndex ledgerIndex() {
    return LedgerIndex.CURRENT;
  }

  @Value.Default
  default boolean strict() {
    return true;
  }

  @Value.Default
  default boolean queue() {
    return false;
  }

  @Value.Default
  @JsonProperty("signer_lists")
  default boolean signerLists() {
    return true;
  }

}

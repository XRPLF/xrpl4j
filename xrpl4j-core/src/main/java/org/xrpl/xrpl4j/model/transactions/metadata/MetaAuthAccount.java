package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.ImmutableAuthAccount;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * An account that is authorized to trade at the discounted fee for an AMM instance.
 */
@Immutable
@JsonSerialize(as = ImmutableMetaAuthAccount.class)
@JsonDeserialize(as = ImmutableMetaAuthAccount.class)
public interface MetaAuthAccount {

  /**
   * The address of the account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

}

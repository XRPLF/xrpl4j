package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * An account that is authorized to trade at the discounted fee for an AMM instance.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAuthAccount.class)
@JsonDeserialize(as = ImmutableAuthAccount.class)
public interface AuthAccount {

  /**
   * Construct an {@link AuthAccount} containing the specified {@link Address}.
   *
   * @param account An {@link Address}.
   *
   * @return An {@link AuthAccount}.
   */
  static AuthAccount of(Address account) {
    return ImmutableAuthAccount.builder()
      .account(account)
      .build();
  }

  /**
   * The address of the account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

}

package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

@Value.Immutable
@JsonSerialize(as = ImmutableAmmInfoAuthAccount.class)
@JsonDeserialize(as = ImmutableAmmInfoAuthAccount.class)
public interface AmmInfoAuthAccount {

  /**
   * Construct a {@code AmmInfoAuthAccount} builder.
   *
   * @return An {@link ImmutableAmmInfoAuthAccount.Builder}.
   */
  static ImmutableAmmInfoAuthAccount.Builder builder() {
    return ImmutableAmmInfoAuthAccount.builder();
  }

  static AmmInfoAuthAccount of(Address account) {
    return builder()
      .account(account)
      .build();
  }

  Address account();

}

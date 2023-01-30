package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * An account that is authorized to trade at the discounted fee for an AMM instance.
 */
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

  /**
   * Construct an {@link AmmInfoAuthAccount} containing the given {@link Address}.
   *
   * @param account An {@link Address}.
   *
   * @return An {@link AmmInfoAuthAccount} containing the address.
   */
  static AmmInfoAuthAccount of(Address account) {
    return builder()
      .account(account)
      .build();
  }

  /**
   * The address of the authorized account.
   *
   * @return An {@link Address}.
   */
  Address account();

}

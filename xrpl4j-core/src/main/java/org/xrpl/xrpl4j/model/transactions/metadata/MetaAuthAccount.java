package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.ImmutableAuthAccount;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * An account that is authorized to trade at the discounted fee for an AMM instance.
 *
 * <p>This class will be marked {@link Beta} until the AMM amendment is enabled on mainnet. Its API is subject to
 * change.</p>
 */
@Immutable
@JsonSerialize(as = ImmutableMetaAuthAccount.class)
@JsonDeserialize(as = ImmutableMetaAuthAccount.class)
@Beta
public interface MetaAuthAccount {

  /**
   * The address of the account.
   *
   * @return An {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

}

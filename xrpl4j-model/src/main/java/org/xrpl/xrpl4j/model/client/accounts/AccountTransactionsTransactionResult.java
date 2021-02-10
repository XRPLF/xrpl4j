package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Transaction;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsTransactionResult.class)
@JsonDeserialize(as = ImmutableAccountTransactionsTransactionResult.class)
public interface AccountTransactionsTransactionResult<T extends Transaction> extends XrplResult {

  static <T extends Transaction> ImmutableAccountTransactionsTransactionResult.Builder<T> builder() {
    return ImmutableAccountTransactionsTransactionResult.builder();
  }

  @JsonProperty("tx")
  T transaction();

  @Value.Default
  default boolean validated() {
    return false;
  }
}

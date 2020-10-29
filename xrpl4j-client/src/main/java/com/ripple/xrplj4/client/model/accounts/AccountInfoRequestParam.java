package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParam;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoRequestParam.class)
@JsonDeserialize(as = ImmutableAccountInfoRequestParam.class)
public interface AccountInfoRequestParam extends JsonRpcRequestParam<AccountInfoRequestParam> {

  static ImmutableAccountInfoRequestParam.Builder builder() {
    return ImmutableAccountInfoRequestParam.builder();
  }

  static AccountInfoRequestParam of(String account) {
    return builder()
        .account(account)
        .build();
  }

  String account();

  @Value.Default
  default boolean strict() {
    return true;
  }

  @Value.Default
  @JsonSerialize(using = LedgerIndexSerializer.class)
  default String ledger_index() {
    return "current";
  }

  @Value.Default
  default boolean queue() {
    return false;
  }

}

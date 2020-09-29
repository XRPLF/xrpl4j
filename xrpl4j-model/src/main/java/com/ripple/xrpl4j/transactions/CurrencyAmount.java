package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.immutables.value.Value;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "@class"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = XrpCurrencyAmount.class, name = "XrpCurrencyAmount"),
  @JsonSubTypes.Type(value = IssuedCurrencyAmount.class, name = "IssuedCurrencyAmount"),
})
public interface CurrencyAmount {

}

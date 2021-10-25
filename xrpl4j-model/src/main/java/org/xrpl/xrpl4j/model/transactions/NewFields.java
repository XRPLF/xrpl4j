package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableNewFields.class)
@JsonDeserialize(as = ImmutableNewFields.class)
public interface NewFields {
    
  @JsonProperty("Account") 
  Optional<Address> account();
    
}

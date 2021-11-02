package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The content fields of a newly-created ledger object. 
 * Which fields are present depends on what type of ledger object was created.
 * 
 * @see <a href="https://xrpl.org/transaction-metadata.html#creatednode-fields">
 * https://xrpl.org/transaction-metadata.html#creatednode-fields</a>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNewFields.class)
@JsonDeserialize(as = ImmutableNewFields.class)
public interface NewFields {
  
  /**
   * The {@link Address} of a newly created account.
   * @return if an account was created, this returns the {@link Address} of that account.
   */  
  @JsonProperty("Account") 
  Optional<Address> account();
    
}

package org.xrpl.xrpl4j.model.transactions.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.ledger.ImmutableAuthAccountWrapper;

/**
 * A wrapper around {@link MetaAuthAccount}s.
 */
@Immutable
@JsonSerialize(as = ImmutableMetaAuthAccountWrapper.class)
@JsonDeserialize(as = ImmutableMetaAuthAccountWrapper.class)
public interface MetaAuthAccountWrapper {

  /**
   * An {@link MetaAuthAccount}.
   *
   * @return An {@link MetaAuthAccount}.
   */
  @JsonProperty("AuthAccount")
  MetaAuthAccount authAccount();

}

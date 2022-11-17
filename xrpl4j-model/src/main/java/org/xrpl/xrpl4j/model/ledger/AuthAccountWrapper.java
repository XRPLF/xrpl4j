package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * A wrapper around {@link AuthAccount}s.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAuthAccountWrapper.class)
@JsonDeserialize(as = ImmutableAuthAccountWrapper.class)
public interface AuthAccountWrapper {

  /**
   * Construct an {@link AuthAccountWrapper} containing the specified {@link AuthAccount}.
   *
   * @param authAccount An {@link AuthAccount}.
   *
   * @return An {@link AuthAccountWrapper}.
   */
  static AuthAccountWrapper of(AuthAccount authAccount) {
    return ImmutableAuthAccountWrapper.builder()
      .authAccount(authAccount)
      .build();
  }

  /**
   * An {@link AuthAccount}.
   *
   * @return An {@link AuthAccount}.
   */
  @JsonProperty("AuthAccount")
  AuthAccount authAccount();

}

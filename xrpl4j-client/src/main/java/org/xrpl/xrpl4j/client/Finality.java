package org.xrpl.xrpl4j.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableFinality.class)
@JsonDeserialize(as = ImmutableFinality.class)
public interface Finality {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableFinality.Builder}.
   */
  static ImmutableFinality.Builder builder() {
    return ImmutableFinality.builder();
  }

  XrplClient.FinalityStatus finalityStatus();

  Optional<String> resultCode();

  @Value.Auxiliary
  default String resultCodeSafe() {
    return resultCode().orElseThrow(() -> new IllegalStateException("Finality does not contain resultCode."));
  }
}
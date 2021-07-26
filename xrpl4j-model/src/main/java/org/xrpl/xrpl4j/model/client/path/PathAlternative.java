package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.PathStep;

import java.util.List;

/**
 * Represents a path from one possible source currency, held by the initiating account of a "ripple_path_find" request,
 * to the destination account and currency of the request.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathAlternative.class)
@JsonDeserialize(as = ImmutablePathAlternative.class)
public interface PathAlternative {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutablePathAlternative.Builder}.
   */
  static ImmutablePathAlternative.Builder builder() {
    return ImmutablePathAlternative.builder();
  }

  /**
   * A {@link List} of {@link List}s of {@link PathStep}s containing the different payment paths available.
   *
   * @return A {@link List} of {@link List}s of type {@link PathStep}.
   */
  @JsonProperty("paths_computed")
  List<List<PathStep>> pathsComputed();

  /**
   * {@link CurrencyAmount} that the source would have to send along this path for the destination to receive the
   * desired amount.
   *
   * @return A {@link CurrencyAmount} denoting the source amount.
   */
  @JsonProperty("source_amount")
  CurrencyAmount sourceAmount();

}

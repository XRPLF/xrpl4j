package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * A step in a Path for cross-currency payments on the XRP Ledger.
 *
 * @see "https://xrpl.org/paths.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePathStep.class)
@JsonDeserialize(as = ImmutablePathStep.class)
public interface PathStep {

  static ImmutablePathStep.Builder builder() {
    return ImmutablePathStep.builder();
  }

  /**
   * If present, this {@link PathStep} represents rippling through the specified {@link Address}.
   * MUST NOT be provided if this {@link PathStep} specifies the {@link PathStep#currency()} or
   * {@link PathStep#issuer()} fields.
   */
  Optional<Address> account();

  /**
   * If present, this {@link PathStep} represents changing currencies through an order book.
   * The currency specified indicates the new currency. MUST NOT be provided if this {@link PathStep} specifies the
   * {@link PathStep#account()} field.
   */
  Optional<String> currency();

  /**
   * If present, this path step represents changing currencies and this address defines the issuer of the new currency.
   * If omitted in a step with a non-XRP currency, a previous step of the path defines the issuer.
   * If present when currency is omitted, indicates a path step that uses an order book between same-named
   * currencies with different issuers.
   * MUST be omitted if the currency is XRP. MUST NOT be provided if this step specifies the {@link PathStep#account()}
   * field.
   */
  Optional<Address> issuer();

}

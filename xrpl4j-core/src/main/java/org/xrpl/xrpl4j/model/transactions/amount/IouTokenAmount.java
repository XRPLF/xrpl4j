package org.xrpl.xrpl4j.model.transactions.amount;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.jackson.modules.IouTokenAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.IouTokenAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

/**
 * A {@link TokenAmount} for issued currencies (IOUs) on the XRP Ledger.
 *
 * <p>Construct via the builder:
 * <pre>{@code
 *   IouTokenAmount amount = IouTokenAmount.builder()
 *     .amount(IouAmount.of("100.50"))
 *     .currency("USD")
 *     .issuer(Address.of("r..."))
 *     .build();
 * }</pre>
 *
 * <p>The underlying {@link IouAmount} holds a quoted decimal string (optionally in scientific
 * notation, e.g. {@code "1.23e11"}), matching the representation returned by the XRPL RPC. Non-XRP values can have up
 * to 16 decimal digits of precision, with a maximum value of {@code 9999999999999999e80}. The smallest positive non-XRP
 * value is {@code 1e-81}.</p>
 *
 * <p>On the wire this serializes as a JSON object with {@code value}, {@code currency}, and
 * {@code issuer} fields — the same format as {@link IssuedCurrencyAmount}.</p>
 *
 * @see "https://xrpl.org/rippleapi-reference.html#value"
 */
@Immutable
@JsonSerialize(using = IouTokenAmountSerializer.class)
@JsonDeserialize(using = IouTokenAmountDeserializer.class)
public interface IouTokenAmount extends TokenAmount {

  /**
   * The maximum value that an {@link IouTokenAmount} can have.
   */
  String MAX_VALUE = "9999999999999999e80";

  /**
   * The minimum value that an {@link IouTokenAmount} can have.
   */
  String MIN_VALUE = "-9999999999999999e80";

  /**
   * The smallest possible positive value that an {@link IouTokenAmount} can have.
   */
  String MIN_POSITIVE_VALUE = "1000000000000000e-96";

  /**
   * The largest possible negative value that an {@link IouTokenAmount} can have.
   */
  String MAX_NEGATIVE_VALUE = "-1000000000000000e-96";

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableIouTokenAmount.Builder}.
   */
  static ImmutableIouTokenAmount.Builder builder() {
    return ImmutableIouTokenAmount.builder();
  }

  /**
   * The scalar numeric value of this IOU amount.
   *
   * @return An {@link IouAmount} holding the decimal/scientific-notation string.
   */
  @JsonIgnore
  IouAmount amount();

  /**
   * Arbitrary code for currency to issue. Cannot be XRP.
   *
   * @return A {@link String} containing the currency code.
   */
  String currency();

  /**
   * Unique account {@link Address} of the entity issuing the currency.
   *
   * @return The {@link Address} of the issuer account.
   */
  Address issuer();
}

package org.xrpl.xrpl4j.model.flags;

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

import com.fasterxml.jackson.annotation.JsonValue;
import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;

import java.util.Arrays;
import java.util.Objects;

/**
 * A wrapper around a long value containing various XRPL Transaction Flags.
 *
 * @see "https://xrpl.org/transaction-common-fields.html#flags-field"
 */
public class Flags {

  /**
   * Constant for an unset flag.
   */
  public static final Flags UNSET = new Flags(0);

  private final long value;

  Flags(long value) {
    this.value = value;
  }

  /**
   * Construct {@link Flags} for the given value.
   *
   * @param value A long flags value.
   *
   * @return A new {@link Flags}.
   */
  public static Flags of(long value) {
    return new Flags(value);
  }

  /**
   * Construct {@link Flags} from one or more {@link Flags} by performing a bitwise OR on all.
   *
   * @param flag   The first {@link Flags}.
   * @param others Zero or more other {@link Flags} to include.
   *
   * @return A new {@link Flags}.
   */
  public static Flags of(Flags flag, Flags... others) {
    return flag.bitwiseOr(
      Arrays.stream(others).reduce(Flags::bitwiseOr).orElse(UNSET)
    );
  }

  /**
   * Get the underlying value of this {@link Flags}.
   *
   * @return The underlying {@code long} value of this {@link Flags}.
   */
  @JsonValue
  public long getValue() {
    return value;
  }

  /**
   * Performs a bitwise OR on this {@link Flags} and another {@link Flags}.
   *
   * @param other The {@link Flags} to perform the OR with.
   *
   * @return The {@link Flags} resulting from the OR operation.
   */
  Flags bitwiseOr(Flags other) {
    return Flags.of(this.value | other.value);
  }

  /**
   * Performs a bitwise AND on this {@link Flags} and another {@link Flags}.
   *
   * @param other The {@link Flags} to perform the AND with.
   *
   * @return The {@link Flags} resulting from the AND operation.
   */
  Flags bitwiseAnd(Flags other) {
    return Flags.of(this.value & other.value);
  }

  /**
   * Determines if a specific transaction flag is set by performing a bitwise AND on this {@link Flags} and the {@link
   * Flags} in question, and checking if the result of that operation is equal to the given flag.
   *
   * @param flag The {@link Flags} that this method determines is set or not.
   *
   * @return true if the flag is set, false if not.
   */
  // TODO: Unit test.
  public boolean isSet(Flags flag) {
    return !flag.equals(Flags.UNSET) && this.bitwiseAnd(flag).equals(flag);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    Flags flags = (Flags) obj;
    return getValue() == flags.getValue();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

}

package org.xrpl.xrpl4j.model.transactions;

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

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.immutables.Wrapped;
import org.xrpl.xrpl4j.model.immutables.Wrapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Wrapped immutable classes for providing type-safe objects.
 */
@SuppressWarnings("TypeName")
public class Wrappers {

  /**
   * A wrapped {@link String} representing an address on the XRPL.
   */
  @Value.Immutable(builder = true) // This is the default, but it's omitted without this.
  @Wrapped
  @JsonSerialize(as = Address.class)
  @JsonDeserialize(as = Address.class)
  abstract static class _Address extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    /**
     * Validates that a {@link Address}'s value's length is equal to 34 characters and starts with `r`.
     */
    @Value.Check
    public void validateAddress() {
      Preconditions.checkArgument(this.value().startsWith("r"),"Invalid Address: Bad Prefix");
      Preconditions.checkArgument(this.value().length() >= 25 && this.value().length() <= 35,
        "Classic Addresses must be (25,35) characters long inclusive.");
    }

  }

  /**
   * A wrapped {@link String} representing an X-Address on the XRPL.
   */
  @Value.Immutable(builder = true) // This is the default, but it's omitted without this.
  @Wrapped
  @JsonSerialize(as = XAddress.class)
  @JsonDeserialize(as = XAddress.class)
  abstract static class _XAddress extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing the Hex representation of a 256-bit Hash.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = Hash256.class)
  @JsonDeserialize(as = Hash256.class)
  abstract static class _Hash256 extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    /**
     * Validates that a {@link Hash256}'s value's length is equal to 64 characters.
     */
    @Value.Check
    public void validateLength() {
      Preconditions.checkArgument(this.value().length() == 64, "Hash256 Strings must be 64 characters long.");
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof Hash256) {
        String otherValue = ((Hash256) obj).value();
        if (otherValue != null) {
          return otherValue.toUpperCase(Locale.ENGLISH).equals(value().toUpperCase(Locale.ENGLISH));
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      return value().toUpperCase(Locale.ENGLISH).hashCode();
    }
  }

  /**
   * A {@link CurrencyAmount} for the XRP currency (non-issued). {@link XrpCurrencyAmount}s are a {@link String}
   * representation of an unsigned integer representing the amount in XRP drops.
   */
  @Value.Immutable(builder = true) // This is the default, but it's omitted without this.
  @Wrapped
  @JsonSerialize(as = XrpCurrencyAmount.class)
  @JsonDeserialize(as = XrpCurrencyAmount.class)
  abstract static class _XrpCurrencyAmount extends Wrapper<UnsignedLong> implements Serializable, CurrencyAmount {

    /**
     * One XRP, in drops.
     *
     * @deprecated Prefer {@link CurrencyAmount#ONE_XRP_IN_DROPS}.
     */
    @Deprecated
    static final long ONE_XRP_IN_DROPS = 1_000_000L;

    /**
     * The largest XRP amount.
     *
     * @deprecated Prefer {@link CurrencyAmount#MAX_XRP}.
     */
    @Deprecated
    static final long MAX_XRP = 100_000_000_000L; // <-- per https://xrpl.org/rippleapi-reference.html#value

    /**
     * The largest XRP amount, in drops.
     *
     * @deprecated Prefer {@link CurrencyAmount#MAX_XRP_IN_DROPS}.
     */
    @Deprecated
    static final long MAX_XRP_IN_DROPS = MAX_XRP * ONE_XRP_IN_DROPS;

    static final BigDecimal SMALLEST_XRP = new BigDecimal("0.000001");
    static final DecimalFormat FORMATTER = new DecimalFormat("###,###");

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops A long representing the number of drops of XRP of this amount.
     *
     * @return An {@link XrpCurrencyAmount} of {@code drops}.
     */
    public static XrpCurrencyAmount ofDrops(long drops) {
      return ofDrops(UnsignedLong.valueOf(drops));
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using a number of drops.
     *
     * @param drops An {@link UnsignedLong} representing the number of drops of XRP of this amount.
     *
     * @return An {@link XrpCurrencyAmount} of {@code drops}.
     */
    public static XrpCurrencyAmount ofDrops(UnsignedLong drops) {
      return XrpCurrencyAmount.of(drops);
    }

    /**
     * Constructs an {@link XrpCurrencyAmount} using decimal amount of XRP.
     *
     * @param amount A {@link BigDecimal} amount of XRP.
     *
     * @return An {@link XrpCurrencyAmount} of the amount of drops in {@code amount}.
     */
    public static XrpCurrencyAmount ofXrp(BigDecimal amount) {
      if (FluentCompareTo.is(amount).notEqualTo(BigDecimal.ZERO)) {
        Preconditions.checkArgument(FluentCompareTo.is(amount).greaterThanEqualTo(SMALLEST_XRP));
      }
      return ofDrops(UnsignedLong.valueOf(amount.scaleByPowerOfTen(6).toBigIntegerExact()));
    }

    /**
     * Convert this XRP amount into a decimal representing a value denominated in whole XRP units. For example, a value
     * of `1.0` represents 1 unit of XRP; a value of `0.5` represents a half of an XRP unit.
     *
     * @return A {@link BigDecimal} representing this value denominated in whole XRP units.
     */
    public BigDecimal toXrp() {
      return new BigDecimal(this.value().bigIntegerValue())
        .divide(BigDecimal.valueOf(ONE_XRP_IN_DROPS), MathContext.DECIMAL128);
    }

    /**
     * Adds another {@link XrpCurrencyAmount} to this amount.
     *
     * @param other An {@link XrpCurrencyAmount} to add to this.
     *
     * @return The sum of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount plus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().plus(other.value()));
    }

    /**
     * Subtract another {@link XrpCurrencyAmount} from this amount.
     *
     * @param other An {@link XrpCurrencyAmount} to subtract from this.
     *
     * @return The difference of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount minus(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().minus(other.value()));
    }

    /**
     * Multiplies this amount by another {@link XrpCurrencyAmount}.
     *
     * @param other An {@link XrpCurrencyAmount} to multiply to this by.
     *
     * @return The product of this amount and the {@code other} amount, as an {@link XrpCurrencyAmount}.
     */
    public XrpCurrencyAmount times(XrpCurrencyAmount other) {
      return XrpCurrencyAmount.of(this.value().times(other.value()));
    }

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Validates that this {@link XrpCurrencyAmount} does not exceed the maximum number of drops.
     */
    @Value.Check
    protected void check() {
      Preconditions.checkState(
        FluentCompareTo.is(value()).lessThanOrEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS)),
        String.format(
          "XRP Amounts may not exceed %s drops (100B XRP, denominated in Drops)", FORMATTER.format(MAX_XRP_IN_DROPS)
        )
      );
    }

  }

  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = Marker.class)
  @JsonDeserialize(as = Marker.class)
  abstract static class _Marker extends Wrapper<String> implements Serializable {

    @Override
    @JsonRawValue
    public String toString() {
      return this.value();
    }

  }

  /**
   * A wrapped {@link String} containing the NFT Id.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = NfTokenId.class)
  @JsonDeserialize(as = NfTokenId.class)
  abstract static class _NfTokenId extends Wrapper<String> implements Serializable {

    @Override
    public String toString() {
      return this.value();
    }

    /**
     * Validates that a NfTokenId value's length is equal to 64 characters.
     */
    @Value.Check
    public void validateLength() {
      Preconditions.checkArgument(this.value().length() == 64, "TokenId must be 64 characters long.");
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof NfTokenId) {
        String otherValue = ((NfTokenId) obj).value();
        if (otherValue != null) {
          return otherValue.toUpperCase(Locale.ENGLISH).equals(value().toUpperCase(Locale.ENGLISH));
        }
      }
      return false;
    }
  }

  /**
   * A wrapped {@link String} containing the Uri.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = NfTokenUri.class)
  @JsonDeserialize(as = NfTokenUri.class)
  abstract static class _NfTokenUri extends Wrapper<String> implements Serializable {

    /**
     * Constructs an {@link NfTokenUri} using a String value.
     *
     * @param plaintext A string value representing the Uri in plaintext.
     *
     * @return An {@link NfTokenUri} of plaintext.
     */
    public static NfTokenUri ofPlainText(String plaintext) {
      return NfTokenUri.of(BaseEncoding.base16().encode(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof NfTokenUri) {
        String otherValue = ((NfTokenUri) obj).value();
        if (otherValue != null) {
          return otherValue.toUpperCase(Locale.ENGLISH).equals(value().toUpperCase(Locale.ENGLISH));
        }
      }
      return false;
    }
  }

  /**
   * A wrapped {@link com.google.common.primitives.UnsignedInteger} containing the TransferFee.
   */
  @Value.Immutable
  @Wrapped
  @JsonSerialize(as = TransferFee.class)
  @JsonDeserialize(as = TransferFee.class)
  abstract static class _TransferFee extends Wrapper<UnsignedInteger> implements Serializable {

    @Override
    public String toString() {
      return this.value().toString();
    }

    /**
     * Construct {@link TransferFee} as a percentage value.
     *
     * @param percent of type {@link BigDecimal}
     * @return {@link TransferFee}
     */
    static TransferFee ofPercent(BigDecimal percent) {
      Preconditions.checkArgument(
        Math.max(0, percent.stripTrailingZeros().scale()) <= 2,
        "Percent value should have a maximum of 2 decimal places."
      );
      return TransferFee.of(UnsignedInteger.valueOf(percent.scaleByPowerOfTen(2).toBigIntegerExact()));
    }


    /**
     * Validates that a NfTokenId value's length is equal to 64 characters.
     */
    @Value.Check
    public void validateBounds() {
      Preconditions.checkArgument(
        FluentCompareTo.is(value()).lessThanOrEqualTo(UnsignedInteger.valueOf(50000)) &&
          FluentCompareTo.is(value()).greaterThanEqualTo(UnsignedInteger.valueOf(0)),
        "TransferFee should be in the range 0 to 50000.");
    }

  }
  
}

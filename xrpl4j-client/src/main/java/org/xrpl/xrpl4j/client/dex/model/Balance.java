package org.xrpl.xrpl4j.client.dex.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Balance
 */
@Immutable
@Value.Style(jdkOnly=true, redactedMask = "####")
@JsonSerialize(as = ImmutableBalance.class)
@JsonDeserialize(as = ImmutableBalance.class)
public interface Balance {

    static ImmutableBalance.Builder builder() {
        return ImmutableBalance.builder();
    }

    static ImmutableBalance zeroBalance(String currency) {
        return ImmutableBalance.builder().currency(currency)
          .total(BigDecimal.ZERO)
          .locked(BigDecimal.ZERO)
          .available(BigDecimal.ZERO)
          .build();
    }

    @JsonProperty("currency")
    String currency();

    @JsonProperty("total")
    BigDecimal total();

    @JsonProperty("available")
    BigDecimal available();

    @JsonProperty("locked")
    Optional<BigDecimal> locked();

    default Balance add(Balance toAdd) {
        return builder().from(this)
          .total(this.total().add(toAdd.total()))
          .available(this.available().add(toAdd.available()))
          .locked(this.locked().orElse(BigDecimal.ZERO).add(toAdd.locked().orElse(BigDecimal.ZERO)))
          .build();
    }

}


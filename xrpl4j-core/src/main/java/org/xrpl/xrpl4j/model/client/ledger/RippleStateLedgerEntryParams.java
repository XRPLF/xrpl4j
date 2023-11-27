package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.List;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject} on ledger that can be used
 * in a {@link LedgerEntryRequestParams} to request a {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableRippleStateLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableRippleStateLedgerEntryParams.class)
public interface RippleStateLedgerEntryParams {

  /**
   * Construct a {@code RippleStateLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableRippleStateLedgerEntryParams.Builder}.
   */
  static ImmutableRippleStateLedgerEntryParams.Builder builder() {
    return ImmutableRippleStateLedgerEntryParams.builder();
  }

  /**
   * A {@link RippleStateAccounts} containing the two accounts linked by the
   * {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}.
   *
   * @return A {@link RippleStateAccounts}.
   */
  @JsonUnwrapped
  RippleStateAccounts accounts();

  /**
   * The currency code of the {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject} to retrieve.
   *
   * @return A {@link String}.
   */
  String currency();

  /**
   * Specifies two {@link Address}es of accounts that are linked by a
   * {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}.
   */
  @Immutable
  @JsonSerialize(as = ImmutableRippleStateAccounts.class)
  @JsonDeserialize(as = ImmutableRippleStateAccounts.class)
  interface RippleStateAccounts {

    /**
     * Construct a new {@link RippleStateAccounts}.
     *
     * @param account      The {@link Address} of one of the accounts linked in the object.
     * @param otherAccount The {@link Address} of the other account linked in the object.
     *
     * @return A {@link RippleStateAccounts}.
     */
    static RippleStateAccounts of(Address account, Address otherAccount) {
      return ImmutableRippleStateAccounts.builder()
        .addAccounts(account, otherAccount)
        .build();
    }

    /**
     * The {@link Address}es of the accounts linked by the {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject}.
     *
     * <p>Note that this is typed as a {@link List} so that this object is serialized as a JSON array.</p>
     *
     * @return A {@link List} of {@link Address}es.
     */
    List<Address> accounts();

  }
}

package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Parameters that uniquely identify a {@link org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject} on ledger that can be
 * used in a {@link LedgerEntryRequestParams} to request an {@link org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject}.
 */
@Immutable
@JsonSerialize(as = ImmutableDepositPreAuthLedgerEntryParams.class)
@JsonDeserialize(as = ImmutableDepositPreAuthLedgerEntryParams.class)
public interface DepositPreAuthLedgerEntryParams {

  /**
   * Construct a {@code DepositPreAuthLedgerEntryParams} builder.
   *
   * @return An {@link ImmutableDepositPreAuthLedgerEntryParams.Builder}.
   */
  static ImmutableDepositPreAuthLedgerEntryParams.Builder builder() {
    return ImmutableDepositPreAuthLedgerEntryParams.builder();
  }

  /**
   * The {@link Address} of the account that provided the preauthorization.
   *
   * @return An {@link Address}.
   */
  Address owner();

  /**
   * The {@link Address} of the account that received the preauthorization.
   *
   * @return An {@link Address}.
   */
  Optional<Address> authorized();

  /**
   * A list of {@link DepositPreAuthCredential} that received the preauthorization.
   *
   * @return A list of type {@link DepositPreAuthCredential}.
   */
  @JsonProperty("authorized_credentials")
  List<DepositPreAuthCredential> authorizedCredentials();

  /**
   * Validate {@link DepositPreAuthLedgerEntryParams#authorizedCredentials} has less than or equal to 8 credentials.
   */
  @Value.Check
  default void validateCredentialsLength() {
    if (!authorizedCredentials().isEmpty()) {
      Preconditions.checkArgument(
        authorizedCredentials().size() <= 8,
        "authorizedCredentials should have less than or equal to 8 items."
      );
    }
  }

  /**
   * Validate {@link DepositPreAuthLedgerEntryParams#authorizedCredentials} are unique.
   */
  @Value.Check
  default void validateUniqueCredentials() {
    if (!authorizedCredentials().isEmpty()) {
      Preconditions.checkArgument(
        new HashSet<>(authorizedCredentials()).size() == authorizedCredentials().size(),
        "authorizedCredentials should have unique values."
      );
    }
  }
}

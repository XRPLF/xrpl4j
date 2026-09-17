package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.Beta;

/**
 * Distinguishes a closed-ended {@code Vault} from the default open-ended kind. Only a closed-ended vault may
 * have a {@code LoanBroker} attached to it once the {@code LendingProtocolV1_1} amendment is enabled.
 */
@Beta
public enum VaultKind {

  /**
   * The default vault kind. Deposits and withdrawals are allowed at any time, and the vault has no defined
   * lifecycle. An open-ended vault cannot have a {@code LoanBroker} attached once {@code LendingProtocolV1_1}
   * is enabled.
   */
  OPEN_ENDED(0),

  /**
   * A vault with a defined {@link VaultCreate#subscriptionDate()} and {@link VaultCreate#redemptionDate()},
   * required in order to attach a {@code LoanBroker} once {@code LendingProtocolV1_1} is enabled.
   */
  CLOSED_ENDED(1);

  private final int value;

  VaultKind(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this {@link VaultKind}.
   *
   * @return The integer value.
   */
  @JsonValue
  public int getValue() {
    return value;
  }

  /**
   * Construct a {@link VaultKind} from an integer value.
   *
   * @param value The integer value.
   *
   * @return The {@link VaultKind} for the given value.
   */
  @JsonCreator
  public static VaultKind forValue(int value) {
    for (VaultKind kind : VaultKind.values()) {
      if (kind.value == value) {
        return kind;
      }
    }
    throw new IllegalArgumentException("No matching VaultKind for value " + value);
  }
}

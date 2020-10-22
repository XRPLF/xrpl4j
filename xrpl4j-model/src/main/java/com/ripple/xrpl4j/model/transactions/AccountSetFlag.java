package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountSetFlag {

  REQUIRE_DEST(1),
  REQUIRE_AUTH(2),
  DISALLOW_XRP(3),
  DISABLE_MASTER(4),
  ACCOUNT_TXN_ID(5),
  NO_FREEZE(6),
  GLOBAL_FREEZE(7),
  DEFAULT_RIPPLE(8),
  DEPOSIT_AUTH(9);

  int value;

  AccountSetFlag(int value) {
    this.value = value;
  }

  /**
   * To deserialize enums with integer values, you need to specify this factory method with the {@link JsonCreator}
   * annotation, otherwise Jackson treats the JSON integer value as an ordinal.
   *
   * @see "https://github.com/FasterXML/jackson-databind/issues/1850"
   */
  @JsonCreator
  public static AccountSetFlag forValue(int value) {
    for (AccountSetFlag flag : values()) {
      if (flag.value == value) {
        return flag;
      }
    }

    throw new IllegalArgumentException("No matching AccountSetFlag enum value for int value " + value);
  }

  @JsonValue
  public int getValue() {
    return value;
  }
}

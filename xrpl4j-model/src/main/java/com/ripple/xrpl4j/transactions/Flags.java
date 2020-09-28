package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Flags {

  public static final Flags UNSET = new Flags(0);

  private long value;

  private Flags(long value) {
    this.value = value;
  }

  public static Flags of(long value) {
    return new Flags(value);
  }

  public long getValue() {
    return value;
  }

  public Flags bitwiseOr(Flags other) {
    return Flags.of(this.value | other.value);
  }

  static class Universal extends Flags {

    public static final Universal FULLY_CANONICAL_SIG = new Universal(0x80000000L);

    private Universal(long value) {
      super(value);
    }
  }

  static class Payment extends Flags {

    public static final Universal NO_RIPPLE_DIRECT = new Universal(0x00010000L);
    public static final Universal PARTIAL_PAYMENT = new Universal(0x00020000L);
    public static final Universal LIMIT_QUALITY = new Universal(0x00040000L);

    private Payment(long value) {
      super(value);
    }
  }
}

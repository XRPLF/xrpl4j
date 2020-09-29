package com.ripple.xrpl4j.transactions;

import java.util.Objects;

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

  public Flags bitwiseAnd(Flags other) {
    return Flags.of(this.value & other.value);
  }

  public boolean isSet(Flags flag) {
    return this.bitwiseAnd(flag).equals(flag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Flags flags = (Flags) o;
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

  public static class Universal extends Flags {

    public static final Universal FULLY_CANONICAL_SIG = new Universal(0x80000000L);

    public static final Universal BITMASK = new Universal(0xff000000L);

    private Universal(long value) {
      super(value);
    }
  }

  public static class Payment extends Flags {

    public static final Universal NO_DIRECT_RIPPLE = new Universal(0x00010000L);
    public static final Universal PARTIAL_PAYMENT = new Universal(0x00020000L);
    public static final Universal LIMIT_QUALITY = new Universal(0x00040000L);
    public static final Universal BITMASK = new Universal(0x00ff0000);

    private Payment(long value) {
      super(value);
    }
  }
}

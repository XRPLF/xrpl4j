package com.ripple.xrpl4j.transactions;

import com.google.common.primitives.UnsignedLong;

public class Flags {

  public static final Flags UNSET = new Flags(UnsignedLong.valueOf(0));

  private UnsignedLong value;

  private Flags(UnsignedLong value) {
    this.value = value;
  }

  public static Flags of(UnsignedLong value) {
    return new Flags(value);
  }

  public static Flags of(long value) {
    return new Flags(UnsignedLong.valueOf(value));
  }

  public Flags bitwiseOr(Flags other) {
    return Flags.of(this.value.longValue() | other.value.longValue());
  }

  static class Universal extends Flags {

    public static final Universal FULLY_CANONICAL_SIG = new Universal(UnsignedLong.valueOf(0x80000000));

    private Universal(UnsignedLong value) {
      super(value);
    }
  }

  static class Payment extends Flags {

    public static final Universal NO_RIPPLE_DIRECT = new Universal(UnsignedLong.valueOf(0x00010000));
    public static final Universal PARTIAL_PAYMENT = new Universal(UnsignedLong.valueOf(0x00020000));
    public static final Universal LIMIT_QUALITY = new Universal(UnsignedLong.valueOf(0x00040000));

    private Payment(UnsignedLong value) {
      super(value);
    }
  }
}

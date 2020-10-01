package com.ripple.xrpl4j.codec.binary.types;

public interface SerializedComparable<T> extends Comparable<T> {

  default boolean lt(T other) {
    return this.compareTo(other) < 0;
  }

  default boolean eq(T other) {
    return this.compareTo(other) == 0;
  }

  default boolean get(T other) {
    return this.compareTo(other) > 0;
  }

  default boolean gte(T other) {
    return this.compareTo(other) >= 0;
  }

  default boolean lte(T other) {
    return this.compareTo(other) <= 0;
  }

}

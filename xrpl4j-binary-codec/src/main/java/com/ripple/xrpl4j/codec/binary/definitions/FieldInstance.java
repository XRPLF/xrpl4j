package com.ripple.xrpl4j.codec.binary.definitions;

import com.ripple.xrpl4j.codec.binary.FieldHeader;
import org.immutables.value.Value.Immutable;

/**
 * Holder of {@link FieldHeader} and {@link com.ripple.xrpl4j.codec.binary.definitions.FieldInfo} data.
 * Provided by {@link DefinitionsService} for looking up fields and their related type data.
 */
@Immutable
public interface FieldInstance extends Comparable {

  static ImmutableFieldInstance.Builder builder() {
    return ImmutableFieldInstance.builder();
  }

  /**
   * Sort order position for fields of the same type.
   * For example, "Fee" has a type "Amount" and has a sort order of 8th.
   *
   * @return
   */
  int nth();

  /**
   * If field is included in signed transactions.
   *
   * @return
   */
  boolean isSigningField();

  /**
   * If field is included in binary serialized representation.
   *
   * @return
   */
  boolean isSerialized();

  /**
   * Field name
   *
   * @return
   */
  String name();

  /**
   * XRPL type (e.g. UInt32, AccountID, etc.)
   *
   * @return
   */
  String type();

  boolean isVariableLengthEncoded();

  /**
   * Globally unique ordinal position based on type code and field code
   *
   * @return
   */
  default int ordinal() {
    return (header().typeCode() << 16) | nth();
  }

  FieldHeader header();

  @Override
  default int compareTo(Object o) {
    if (!(o instanceof FieldInstance)) {
      throw new IllegalArgumentException("cannot compare to type " + o.getClass());
    }
    FieldInstance other = (FieldInstance) o;
    return ((FieldInstance) o).ordinal() - other.ordinal();
  }
}

package com.ripple.xrpl4j.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Contains arbitrary messaging data within an XRPL Transaction.
 *
 * The MemoType and MemoFormat fields should only consist of the following characters:
 * ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=%
 *
 * The Memos field is limited to no more than 1 KB in size (when serialized in binary format).
 *
 * @see "https://xrpl.org/transaction-common-fields.html#memos-field"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMemo.class)
@JsonDeserialize(as = ImmutableMemo.class)
public interface Memo {

  static ImmutableMemo.Builder builder() {
    return ImmutableMemo.builder();
  }

  /**
   * Arbitrary hex value, conventionally containing the content of the memo.
   */
  @JsonProperty("MemoData")
  Optional<String> memoData();

  /**
   * Hex value representing characters allowed in URLs. Conventionally containing information on how the memo
   * is encoded.
   */
  @JsonProperty("MemoFormat")
  Optional<String> memoFormat();

  /**
   * Hex value representing characters allowed in URLs. Conventionally, a unique relation that defines the format
   * of this memo.
   */
  @JsonProperty("MemoType")
  Optional<String> memoType();

}

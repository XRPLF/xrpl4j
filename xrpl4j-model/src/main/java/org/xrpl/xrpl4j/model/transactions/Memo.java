package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Contains arbitrary messaging data within an XRPL Transaction.
 *
 * <p>The MemoType and MemoFormat fields should only consist of the following characters:
 * ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$%26'()*+,;=%</p>
 *
 * <p>The Memos field is limited to no more than 1 KB in size (when serialized in binary format).</p>
 *
 * @see "https://xrpl.org/transaction-common-fields.html#memos-field"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMemo.class)
@JsonDeserialize(as = ImmutableMemo.class)
public interface Memo {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableMemo.Builder}.
   */
  static ImmutableMemo.Builder builder() {
    return ImmutableMemo.builder();
  }

  /**
   * Arbitrary hex value, conventionally containing the content of the memo.
   *
   * @return An {@link Optional} of type {@link String} containing the memo data.
   */
  @JsonProperty("MemoData")
  Optional<String> memoData();

  /**
   * Hex value representing characters allowed in URLs. Conventionally containing information on how the memo
   * is encoded.
   *
   * @return An {@link Optional} of type {@link String} containing the memo format.
   */
  @JsonProperty("MemoFormat")
  Optional<String> memoFormat();

  /**
   * Hex value representing characters allowed in URLs. Conventionally, a unique relation that defines the format
   * of this memo.
   *
   * @return An {@link Optional} of type {@link String} containing the memo type.
   */
  @JsonProperty("MemoType")
  Optional<String> memoType();

}

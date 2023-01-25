package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
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
public abstract class Memo {

  /**
   * Construct a {@link ImmutableMemo.Builder} by properly hex-encoding {@code plaintext}.
   *
   * @param plaintext A UTF-8 {@link String}.
   *
   * @return A {@link ImmutableMemo.Builder}.
   */
  public static ImmutableMemo.Builder withPlaintext(final String plaintext) {
    return builder()
      .memoFormat(BaseEncoding.base16().encode("text/plain".getBytes(StandardCharsets.UTF_8)))
      .memoData(BaseEncoding.base16().encode(plaintext.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableMemo.Builder}.
   */
  public static ImmutableMemo.Builder builder() {
    return ImmutableMemo.builder();
  }

  /**
   * Arbitrary hex value, conventionally containing the content of the memo.
   *
   * @return An {@link Optional} of type {@link String} containing the memo data.
   */
  @JsonProperty("MemoData")
  public abstract Optional<String> memoData();

  /**
   * Hex value representing characters allowed in URLs. Conventionally containing information on how the memo is
   * encoded.
   *
   * @return An {@link Optional} of type {@link String} containing the memo format.
   */
  @JsonProperty("MemoFormat")
  public abstract Optional<String> memoFormat();

  /**
   * Hex value representing characters allowed in URLs. Conventionally, a unique relation that defines the format of
   * this memo.
   *
   * @return An {@link Optional} of type {@link String} containing the memo type.
   */
  @JsonProperty("MemoType")
  public abstract Optional<String> memoType();

  @Check
  void check() {
    memoData().ifPresent(
      memoData -> Preconditions.checkState(isHex(memoData), "MemoData must be a hex-encoded string")
    );
    memoFormat().ifPresent(
      memoFormat -> Preconditions.checkState(isHex(memoFormat), "MemoFormat must be a hex-encoded string")
    );
    memoType().ifPresent(
      memoType -> Preconditions.checkState(isHex(memoType), "MemoType must be a hex-encoded string")
    );
  }

  /**
   * Determines if an input string is hex-encoded.
   *
   * @param input A {@link String}.
   *
   * @return {@code true} if every character in {@code input} is hex-encoded; {@code false} otherwise.
   */
  private static boolean isHex(final String input) {
    Objects.requireNonNull(input);
    for (char c : input.toCharArray()) {
      if (!isHex(c)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Determines if an input char is hex-encoded.
   *
   * @param input A {@link char}.
   *
   * @return {@code true} if {@code input} is hex-encoded; {@code false} otherwise.
   */
  private static boolean isHex(final char input) {
    return (input >= '0' && input <= '9') || (input >= 'a' && input <= 'f') || (input >= 'A' && input <= 'F');
  }
}

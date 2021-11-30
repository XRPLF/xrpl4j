package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Optional;

/**
 * The result of a "submit_multisigned" rippled API call.
 *
 * @param <TxnType> The type of {@link Transaction} that was submitted.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitMultiSignedResult.class)
@JsonDeserialize(as = ImmutableSubmitMultiSignedResult.class)
public interface SubmitMultiSignedResult<TxnType extends Transaction> extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @param <T> The actual type of {@link Transaction} that was submitted.
   *
   * @return An {@link ImmutableSubmitMultiSignedResult.Builder}
   */
  static <T extends Transaction> ImmutableSubmitMultiSignedResult.Builder<T> builder() {
    return ImmutableSubmitMultiSignedResult.builder();
  }

  /**
   * Text result code indicating the preliminary result of the transaction, for example "tesSUCCESS".
   *
   * @return An optionally-present {@link String} containing the result of the submission.
   * @deprecated Use {@link #result()} instead.
   */
  @Deprecated
  @Value.Auxiliary
  default Optional<String> engineResult() {
    return Optional.of(result());
  }

  /**
   * Text result code indicating the preliminary result of the transaction, for example "tesSUCCESS".
   *
   * @return A {@link String} containing the result of the submission.
   * @deprecated This will be removed in a future version and replaced by a field of the same type called
   *   {@link #engineResult()}.
   */
  @Deprecated
  @JsonProperty("engine_result")
  String result();

  /**
   * Numeric code indicating the preliminary result of the transaction, directly correlated to {@link #engineResult()}.
   *
   * @return An optionally-present {@link String} containing the result code of the submission.
   * @deprecated Use {@link #resultCode()} instead.
   */
  @Deprecated
  @Value.Auxiliary
  default Optional<String> engineResultCode() {
    return Optional.of(resultCode().toString());
  }

  /**
   * Numeric code indicating the preliminary result of the transaction, directly correlated to {@link #engineResult()}.
   *
   * @return An {@link Integer} containing the result code of the submission.
   */
  @JsonProperty("engine_result_code")
  Integer resultCode();

  /**
   * Human-readable explanation of the transaction's preliminary result.
   *
   * @return An optionally-present {@link String} containing the result message of the submission.
   * @deprecated Use {@link #resultMessage()} instead.
   */
  @Deprecated
  @Value.Auxiliary
  default Optional<String> engineResultMessage() {
    return Optional.of(resultMessage());
  }

  /**
   * Human-readable explanation of the transaction's preliminary result.
   *
   * @return A {@link String} containing the result message of the submission.
   */
  @JsonProperty("engine_result_message")
  String resultMessage();

  /**
   * The complete transaction in hex {@link String} format.
   *
   * @return A hexadecimal encoded {@link String} containing the binary encoded transaction that was submitted.
   */
  @JsonProperty("tx_blob")
  String transactionBlob();

  /**
   * The complete {@link Transaction} that was submitted, as a {@link TransactionResult}.
   *
   * @return A {@link TransactionResult}.
   */
  @JsonProperty("tx_json")
  TransactionResult<TxnType> transaction();

}

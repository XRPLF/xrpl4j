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

  static <T extends Transaction> ImmutableSubmitMultiSignedResult.Builder<T> builder() {
    return ImmutableSubmitMultiSignedResult.builder();
  }

  /**
   * Text result code indicating the preliminary result of the transaction, for example "tesSUCCESS".
   *
   * @return An optionally-present {@link String} containing the result of the submission.
   */
  @JsonProperty("engine_result")
  Optional<String> engineResult();

  /**
   * Numeric code indicating the preliminary result of the transaction, directly correlated to {@link #engineResult()}.
   *
   * @return An optionally-present {@link String} containing the result code of the submission.
   */
  @JsonProperty("engine_result_code")
  Optional<String> engineResultCode();

  /**
   * Human-readable explanation of the transaction's preliminary result.
   *
   * @return An optionally-present {@link String} containing the result message of the submission.
   */
  @JsonProperty("engine_result_message")
  Optional<String> engineResultMessage();

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

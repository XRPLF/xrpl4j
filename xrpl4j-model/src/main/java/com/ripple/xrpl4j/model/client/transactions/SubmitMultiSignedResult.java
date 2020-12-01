package com.ripple.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.client.rippled.XrplResult;
import com.ripple.xrpl4j.model.transactions.Transaction;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The result of a submit_multisigned rippled API call.
 * @param <TxnType> The type of {@link Transaction} that was submitted.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitMultiSignedResult.class)
@JsonDeserialize(as = ImmutableSubmitMultiSignedResult.class)
public interface SubmitMultiSignedResult<TxnType extends Transaction> extends XrplResult {

  /**
   * Text result code indicating the preliminary result of the transaction, for example "tesSUCCESS".
   */
  @JsonProperty("engine_result")
  Optional<String> engineResult();

  /**
   * Numeric code indicating the preliminary result of the transaction, directly correlated to {@link #engineResult()}.
   */
  @JsonProperty("engine_result_code")
  Optional<String> engineResultCode();

  /**
   * Human-readable explanation of the transaction's preliminary result.
   */
  @JsonProperty("engine_result_message")
  Optional<String> engineResultMessage();

  /**
   * The complete transaction in hex {@link String} format.
   */
  @JsonProperty("tx_blob")
  String transactionBlob();

  /**
   * The complete {@link Transaction} that was submitted.
   */
  @JsonProperty("tx_json")
  TxnType transaction();


}

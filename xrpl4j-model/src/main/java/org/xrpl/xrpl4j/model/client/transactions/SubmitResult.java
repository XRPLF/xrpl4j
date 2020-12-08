package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.rippled.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Optional;

/**
 * The result of a submit rippled API call.
 *
 * @param <TxnType> The type of {@link Transaction} that was submitted.
 */
@Immutable
@JsonSerialize(as = ImmutableSubmitResult.class)
@JsonDeserialize(as = ImmutableSubmitResult.class)
public interface SubmitResult<TxnType extends Transaction> extends XrplResult {

  static <T extends Transaction> ImmutableSubmitResult.Builder<T> builder() {
    return ImmutableSubmitResult.builder();
  }

  /**
   * Text result code indicating the preliminary result of the transaction, for example "tesSUCCESS".
   */
  @JsonProperty("engine_result")
  Optional<String> engineResult();

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
   * The complete {@link Transaction} that was submitted, as a {@link TransactionResult}.
   */
  @JsonProperty("tx_json")
  TransactionResult<TxnType> transactionResult();

  /**
   * The value true indicates that the transaction was applied, queued, broadcast, or kept for later.
   * The value false indicates that none of those happened, so the transaction cannot possibly succeed as long
   * as you do not submit it again and have not already submitted it another time.
   */
  boolean accepted();

  /**
   * The next Sequence number available for the sending account after all pending and queued transactions.
   *
   * @return
   */
  @JsonProperty("account_sequence_available")
  UnsignedInteger accountSequenceAvailable();

  /**
   * The next Sequence number for the sending account after all transactions that have been provisionally applied,
   * but not transactions in the queue.
   */
  @JsonProperty("account_sequence_next")
  UnsignedInteger accountSequenceNext();

  /**
   * The value true indicates that this transaction was applied to the open ledger.
   * In this case, the transaction is likely, but not guaranteed, to be validated in the next ledger version.
   */
  boolean applied();

  /**
   * true indicates this transaction was broadcast to peer servers in the peer-to-peer XRP Ledger network.
   * false indicates the transaction was not broadcast to any other servers.
   */
  boolean broadcast();

  /**
   * The value true indicates that the transaction was kept to be retried later.
   */
  boolean kept();

  /**
   * The value true indicates the transaction was put in the Transaction Queue,
   * which means it is likely to be included in a future ledger version.
   */
  boolean queued();

  /**
   * The current open ledger cost before processing this transaction. Transactions with a lower cost are
   * likely to be queued.
   */
  @JsonProperty("open_ledger_cost")
  String openLedgerCost();

  /**
   * The ledger index of the newest validated ledger at the time of submission.
   * This provides a lower bound on the ledger versions that the transaction can appear in as a result of this request.
   */
  @JsonProperty("validated_ledger_index")
  LedgerIndex validatedLedgerIndex();

}

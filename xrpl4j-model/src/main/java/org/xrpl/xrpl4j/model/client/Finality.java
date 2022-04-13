package org.xrpl.xrpl4j.model.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Defines how an XRP Ledger finality decision is represented; includes both a finality status and an engine code.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFinality.class)
@JsonDeserialize(as = ImmutableFinality.class)
public interface Finality {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableFinality.Builder}.
   */
  static ImmutableFinality.Builder builder() {
    return ImmutableFinality.builder();
  }

  /**
   * Get {@link FinalityStatus} status value for a transaction.
   *
   * @return {@link FinalityStatus} value for a
   *   {@link org.xrpl.xrpl4j.model.transactions.Transaction}.
   */
  FinalityStatus finalityStatus();

  /**
   * The rippled server summarizes transaction results with result codes, which appear in fields such as engine_result
   * and meta.TransactionResult. These codes are grouped into several categories of with different prefixes.
   *
   * @return A {@link String} containing the result of the submission.
   * @see "https://xrpl.org/transaction-results.html#transaction-results"
   */
  Optional<String> resultCode();

  /**
   * Text result code indicating the preliminary result of the transaction.
   *
   * @return The #result() value for the particular transaction.
   */
  @Value.Auxiliary
  default String resultCodeSafe() {
    return resultCode().orElseThrow(() -> new IllegalStateException("Finality does not contain resultCode."));
  }
}
package org.xrpl.xrpl4j.client;

/**
 * Enumeration of the types of responses from isFinal function.
 */
public enum FinalityStatus {
  /**
   * Transaction has not been validated until this moment, which means it could be validated in the
   * following closed ledgers, or, the XRPL peer you are connected to could be missing the ledgers
   * it would have been validated on.
   */
  NOT_FINAL,
  /**
   * Transaction is final/validated on the ledger successfully.
   */
  VALIDATED_SUCCESS,
  /**
   * Transaction is final/validated on the ledger with some failure.
   */
  VALIDATED_FAILURE,
  /**
   * In the past, the XRP Ledger supported transactions without metadata. While uncommon, this is a
   * potential state for older transactions.
   */
  VALIDATED_UNKNOWN,
  /**
   * The lastLedgerSequence of the tx has passed, i.e., transaction was not included in any closed
   * ledgers ranging from the one it was submitted on to the lastLedgerSequence.
   */
  EXPIRED
}

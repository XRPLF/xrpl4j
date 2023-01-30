package org.xrpl.xrpl4j.model.client;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
   * The transaction is validated, but it's unclear if the transaction was successful or not. For example,
   * in the past, the XRP Ledger supported transactions without metadata. While uncommon, this is a
   * potential state for older transactions that prevents this library from determining a success/failure state.
   */
  VALIDATED_UNKNOWN,
  /**
   * The lastLedgerSequence of the tx has passed, i.e., transaction was not included in any closed
   * ledgers ranging from the one it was submitted on to the lastLedgerSequence.
   */
  EXPIRED,
  /**
   * An unknown transaction has been validated ahead of or instead of this transaction, so manual intervention and
   * investigation is recommended.<br>Some reasons an unknown transaction may have been validated instead of this one:
   * The transaction was malleable and succeeded with a different hash; A different program or person with your secret
   * key is also sending transactions from the same account; You previously sent a transaction, but lost your record
   * of it.
   */
  EXPIRED_WITH_SPENT_ACCOUNT_SEQUENCE
}

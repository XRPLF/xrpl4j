package org.xrpl.xrpl4j.model.transactions.metadata;

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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.ImmutableNfTokenPageObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.NfToken;
import org.xrpl.xrpl4j.model.ledger.NfTokenWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.Optional;

/**
 * Represents the NFTokenPage ledger object, which represents a collection of NFToken objects owned by the same account.
 * An account can have multiple NFTokenPage ledger objects, which form a doubly linked list.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaNfTokenPageObject.class)
@JsonDeserialize(as = ImmutableMetaNfTokenPageObject.class)
public interface MetaNfTokenPageObject extends MetaLedgerObject {

  /**
   * The collection of NFToken objects contained in this NFTokenPage object. This specification places an upper bound
   * of 32 NFToken objects per page. Objects are sorted from low to high with the NFTokenID used as the sorting
   * parameter.
   *
   * @return A {@link List} of {@link NfToken}s.
   */
  @JsonProperty("NFTokens")
  List<NfTokenWrapper> nfTokens();

  /**
   * The locator of the next page, if any.
   *
   * @return A {@link Hash256} containing the ID of the next page, or {@link Optional#empty()} if there is no next
   * page.
   */
  @JsonProperty("NextPageMin")
  Optional<Hash256> nextPageMin();

  /**
   * The locator of the previous page, if any.
   *
   * @return A {@link Hash256} containing the ID of the previous page, or {@link Optional#empty()} if there is no
   * previous page.
   */
  @JsonProperty("PreviousPageMin")
  Optional<Hash256> previousPageMin();

  /**
   * Identifies the transaction ID of the transaction that most recently modified this NFTokenPage object.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The sequence of the ledger that contains the transaction that most recently modified this NFTokenPage object.
   *
   * @return A {@link LedgerIndex}.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

}

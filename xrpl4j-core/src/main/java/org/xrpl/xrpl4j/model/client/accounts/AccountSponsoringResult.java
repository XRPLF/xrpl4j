package org.xrpl.xrpl4j.model.client.accounts;

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
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;

/**
 * The result of an {@code account_sponsoring} RPC request.
 *
 * <p>This class will be marked {@link Beta} until the featureSponsorship amendment is enabled on mainnet.
 * Its API is subject to change.</p>
 */
@Beta
@Value.Immutable
@JsonSerialize(as = ImmutableAccountSponsoringResult.class)
@JsonDeserialize(as = ImmutableAccountSponsoringResult.class)
public interface AccountSponsoringResult extends XrplResult {

  static ImmutableAccountSponsoringResult.Builder builder() {
    return ImmutableAccountSponsoringResult.builder();
  }

  @JsonProperty("account")
  Address account();

  @JsonProperty("sponsored_objects")
  List<LedgerObject> sponsoredObjects();

  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  @JsonProperty("limit")
  Optional<UnsignedInteger> limit();

  @JsonProperty("marker")
  Optional<Marker> marker();

  @JsonProperty("validated")
  @Value.Default
  default boolean validated() {
    return false;
  }

}


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
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.ledger.ImmutableSignerEntry;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Represents an individual signer in a {@link MetaSignerListObject}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaSignerEntry.class)
@JsonDeserialize(as = ImmutableMetaSignerEntry.class)
public interface MetaSignerEntry {

  /**
   * An XRP Ledger classic {@link Address} whose signature contributes to the multi-signature. It does not need to be a
   * funded address in the ledger.
   *
   * @return The {@link Address} of the signer.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The weight of a signature from this signer. A multi-signature is only valid if the sum weight of the
   * signatures provided meets or exceeds the {@link MetaSignerListObject#signerQuorum()} value.
   *
   * @return An {@link UnsignedInteger} representing the signer weight.
   */
  @JsonProperty("SignerWeight")
  UnsignedInteger signerWeight();

}

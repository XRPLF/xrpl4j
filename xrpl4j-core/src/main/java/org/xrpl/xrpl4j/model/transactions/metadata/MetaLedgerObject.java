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

/**
 * Market interface for XRP Ledger Objects.
 * TODO: pull common fields up.
 */
/*@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  // This will look one level up, where LedgerEntryType exists for tx metadata
  include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
  property = "LedgerEntryType"
)
@JsonSubTypes( {
  @JsonSubTypes.Type(value = ImmutableMetaAccountRootObject.class, name = "AccountRoot"),
  //    @JsonSubTypes.Type(value = ImmutableAmendmentsObject.class, name = "Amendments"),
  @JsonSubTypes.Type(value = ImmutableMetaCheckObject.class, name = "Check"),
  @JsonSubTypes.Type(value = ImmutableMetaDepositPreAuthObject.class, name = "DepositPreauth"),
  //    @JsonSubTypes.Type(value = ImmutableDirectoryNodeObject.class, name = "DirectoryNode"),
  @JsonSubTypes.Type(value = ImmutableMetaEscrowObject.class, name = "Escrow"),
  //    @JsonSubTypes.Type(value = ImmutableFeeSettingsObject.class, name = "FeeSettings"),
  //    @JsonSubTypes.Type(value = ImmutableLedgerHashesObject.class, name = "LedgerHashes"),
  //    @JsonSubTypes.Type(value = ImmutableNegativeUnlObject.class, name = "NegativeUNL"),
  @JsonSubTypes.Type(value = ImmutableMetaNfTokenOfferObject.class, name = "NFTokenOffer"),
  @JsonSubTypes.Type(value = ImmutableMetaOfferObject.class, name = "Offer"),
  @JsonSubTypes.Type(value = ImmutableMetaPayChannelObject.class, name = "PayChannel"),
  @JsonSubTypes.Type(value = ImmutableMetaRippleStateObject.class, name = "RippleState"),
  @JsonSubTypes.Type(value = ImmutableMetaSignerListObject.class, name = "SignerList"),
  @JsonSubTypes.Type(value = ImmutableMetaTicketObject.class, name = "Ticket"),
})
// TODO: Uncomment subtypes as we implement*/
public interface MetaLedgerObject {

}

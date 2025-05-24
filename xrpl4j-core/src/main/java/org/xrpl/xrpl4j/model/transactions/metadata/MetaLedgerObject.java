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
 * Marker interface for XRP Ledger Objects as represented in transaction metadata. Unlike descendants of
 * {@link org.xrpl.xrpl4j.model.ledger.LedgerObject}, all descendants of this interface will have all fields typed as
 * {@link java.util.Optional} because ledger objects represented in transaction metadata often do not contain
 * all fields of the ledger object.
 */
public interface MetaLedgerObject {

}

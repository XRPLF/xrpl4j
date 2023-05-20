package org.xrpl.xrpl4j.client.jsonrpc;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: client
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

import com.google.common.annotations.Beta;
import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.client.jsonrpc.model.JsonRpcClient;
import org.xrpl.xrpl4j.client.jsonrpc.model.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.jsonrpc.model.JsonRpcRequest;
import org.xrpl.xrpl4j.model.client.admin.AcceptLedgerResult;

/**
 * A client that can call Rippled Admin API methods. @see "https://xrpl.org/admin-rippled-methods.html".
 *
 * <p>Note: This client is currently marked as {@link Beta}, and should be used as a reference implementation ONLY.
 */
@Beta
public class XrplAdminClient {

  private final JsonRpcClient jsonRpcClient;

  /**
   * Public constructor.
   *
   * @param rippledUrl The {@link HttpUrl} of the rippled node to connect to.
   */
  public XrplAdminClient(HttpUrl rippledUrl) {
    this.jsonRpcClient = JsonRpcClient.construct(rippledUrl);
  }

  /**
   * Advances the ledger. When running rippled in standalone mode, this method is useful to manually trigger
   * the ledger to close.
   *
   * @return A {@link AcceptLedgerResult} containing information about the accepted ledger.
   * @throws JsonRpcClientErrorException If {@code jsonRpcClient} throws an error.
   * @see "https://xrpl.org/ledger_accept.html"
   */
  public AcceptLedgerResult acceptLedger() throws JsonRpcClientErrorException {
    JsonRpcRequest request = JsonRpcRequest.builder()
      .method("ledger_accept")
      .build();

    return jsonRpcClient.send(request, AcceptLedgerResult.class);
  }

}

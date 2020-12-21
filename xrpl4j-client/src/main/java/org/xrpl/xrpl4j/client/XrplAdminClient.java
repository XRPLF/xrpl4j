package org.xrpl.xrpl4j.client;

import com.google.common.annotations.Beta;
import okhttp3.HttpUrl;
import org.xrpl.xrpl4j.model.client.admin.AcceptLedgerResult;

/**
 * A client that can call Rippled Admin API methods. @see "https://xrpl.org/admin-rippled-methods.html".
 *
 * Note: This client is currently marked as {@link Beta}, and should be used as a reference implementation ONLY.
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

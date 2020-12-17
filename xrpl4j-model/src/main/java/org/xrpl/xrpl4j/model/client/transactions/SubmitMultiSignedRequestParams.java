package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Request parameters for the "submit_multisigned" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitMultiSignedRequestParams.class)
@JsonDeserialize(as = ImmutableSubmitMultiSignedRequestParams.class)
public interface SubmitMultiSignedRequestParams extends XrplRequestParams {

  static ImmutableSubmitMultiSignedRequestParams.Builder builder() {
    return ImmutableSubmitMultiSignedRequestParams.builder();
  }

  /**
   * Construct a {@link SubmitMultiSignedRequestParams} with the given {@link Transaction}.
   *
   * @param multiSigTransaction A {@link Transaction} that has been signed by multiple accounts.
   * @return A {@link SubmitMultiSignedRequestParams} populated with the given {@link Transaction}.
   */
  static SubmitMultiSignedRequestParams of(Transaction multiSigTransaction) {
    return SubmitMultiSignedRequestParams.builder().transaction(multiSigTransaction).build();
  }

  /**
   * The {@link Transaction} to submit.
   */
  @JsonProperty("tx_json")
  Transaction transaction();

}

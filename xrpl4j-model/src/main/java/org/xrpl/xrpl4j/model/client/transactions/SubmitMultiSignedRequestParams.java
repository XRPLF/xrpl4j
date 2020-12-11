package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Request parameters for the submit_multisigned API method.
 *
 * @param <TxnType> The type of {@link Transaction} to submit.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitMultiSignedRequestParams.class)
@JsonDeserialize(as = ImmutableSubmitMultiSignedRequestParams.class)
public interface SubmitMultiSignedRequestParams<TxnType extends Transaction> extends XrplRequestParams {

  static <T extends Transaction> ImmutableSubmitMultiSignedRequestParams.Builder<T> builder() {
    return ImmutableSubmitMultiSignedRequestParams.builder();
  }

  static <TxnType extends Transaction> SubmitMultiSignedRequestParams<TxnType> of(TxnType multiSigTransaction) {
    return SubmitMultiSignedRequestParams.<TxnType>builder().transaction(multiSigTransaction).build();
  }

  /**
   * The {@link Transaction} to submit.
   */
  @JsonProperty("tx_json")
  TxnType transaction();

}

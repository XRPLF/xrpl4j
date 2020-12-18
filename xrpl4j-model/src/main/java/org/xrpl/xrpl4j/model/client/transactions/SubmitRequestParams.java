package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;

/**
 * Request parameters for the "submit" rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmitRequestParams.class)
@JsonDeserialize(as = ImmutableSubmitRequestParams.class)
public interface SubmitRequestParams extends XrplRequestParams {

  /**
   * Construct a {@link SubmitRequestParams} containing the given transaction blob as its only parameter.
   *
   * @param blobHex The binary serialized transaction to submit, as a hexadecimal encoded {@link String}.
   *
   * @return A new {@link SubmitRequestParams}.
   */
  static SubmitRequestParams of(String blobHex) {
    return ImmutableSubmitRequestParams.builder().txBlob(blobHex).build();
  }

  /**
   * The hex encoded {@link String} containing a signed, binary encoded transaction.
   *
   * @return A {@link String} containing the transaction blob.
   */
  @JsonProperty("tx_blob")
  String txBlob();

}

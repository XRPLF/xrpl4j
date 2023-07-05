package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.ledger.Issue;

/**
 * Request parameters for the {@code amm_info} rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmInfoRequestParams.class)
@JsonDeserialize(as = ImmutableAmmInfoRequestParams.class)
public interface AmmInfoRequestParams extends XrplRequestParams {

  /**
   * Construct a {@code AmmInfoRequestParams} builder.
   *
   * @return An {@link ImmutableAmmInfoRequestParams.Builder}.
   */
  static ImmutableAmmInfoRequestParams.Builder builder() {
    return ImmutableAmmInfoRequestParams.builder();
  }

  /**
   * One of the assets of the AMM to look up.
   *
   * @return An {@link Issue}.
   */
  Issue asset();

  /**
   * The other of the assets of the AMM.
   *
   * @return An {@link Issue}.
   */
  Issue asset2();

}

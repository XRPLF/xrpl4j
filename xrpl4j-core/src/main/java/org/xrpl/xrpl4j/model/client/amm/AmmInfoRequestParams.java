package org.xrpl.xrpl4j.model.client.amm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

/**
 * Request parameters for the {@code amm_info} rippled API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmInfoRequestParams.class)
@JsonDeserialize(as = ImmutableAmmInfoRequestParams.class)
public interface AmmInfoRequestParams extends XrplRequestParams {

  /**
   * Construct a new {@link AmmInfoRequestParams} that specifies the AMM account to query.
   *
   * @param ammAccount The {@link Address} of the AMM account.
   *
   * @return An {@link AmmInfoRequestParams}.
   */
  static AmmInfoRequestParams from(Address ammAccount) {
    return ImmutableAmmInfoRequestParams.builder()
      .ammAccount(ammAccount)
      .build();
  }

  /**
   * Construct a new {@link AmmInfoRequestParams} that specifies {@code asset} and {@code asset2}.
   *
   * @param asset The first asset of the AMM, as an {@link Issue}.
   * @param asset2 The second asset of the AMM, as an {@link Issue}.
   *
   * @return An {@link AmmInfoRequestParams}.
   */
  static AmmInfoRequestParams from(Issue asset, Issue asset2) {
    return ImmutableAmmInfoRequestParams.builder()
      .asset(asset)
      .asset2(asset2)
      .build();
  }

  /**
   * The address of the AMM's special AccountRoot. (This is the issuer of the AMM's LP Tokens).
   *
   * <p>If this field is specified, {@link #asset()} and {@link #asset2()} must be empty.</p>
   *
   * @return An {@link Optional} {@link Address}.
   */
  @JsonProperty("amm_account")
  Optional<Address> ammAccount();

  /**
   * One of the assets of the AMM to look up.
   *
   * <p>If this field is specified, {@link #asset2()} must be present, and {@link #ammAccount()} must be empty.</p>
   *
   * @return An {@link Issue}.
   */
  Optional<Issue> asset();

  /**
   * The other of the assets of the AMM.
   *
   * <p>If this field is specified, {@link #asset()} must be present, and {@link #ammAccount()} must be empty.</p>
   *
   * @return An {@link Issue}.
   */
  Optional<Issue> asset2();

}

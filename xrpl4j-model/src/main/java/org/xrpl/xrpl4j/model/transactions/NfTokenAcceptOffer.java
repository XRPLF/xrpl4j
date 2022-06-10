package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * The {@link NfTokenAcceptOffer} transaction creates an NfT object and adds it to the
 * relevant NfTPage object of the minter. If the transaction is
 * successful, the newly minted token will be owned by the minter account
 * specified by the transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenAcceptOffer.class)
@JsonDeserialize(as = ImmutableNfTokenAcceptOffer.class)
public interface NfTokenAcceptOffer extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenAcceptOffer.Builder}.
   */
  static ImmutableNfTokenAcceptOffer.Builder builder() {
    return ImmutableNfTokenAcceptOffer.builder();
  }

  /**
   * Identifies the NfTOffer that offers to sell the NfT.
   *
   * <p>In direct mode this field is optional, but either SellOffer or
   * BuyOffer must be specified. In brokered mode, both SellOffer
   * and BuyOffer MUST be specified.
   *
   * @return An {@link Optional} NfTOffer of type {@link String} that offers to sell the NfT.
   */
  @JsonProperty("NFTokenSellOffer")
  Optional<Hash256> sellOffer();

  /**
   * Identifies the NfTOffer that offers to buy the NfT.
   *
   * <p>In direct mode this field is optional, but either SellOffer or
   * BuyOffer must be specified. In brokered mode, both SellOffer
   * and BuyOffer MUST be specified.
   *
   * @return An {@link Optional} NfTOffer of type {@link String} that offers to buy the NfT.
   */
  @JsonProperty("NFTokenBuyOffer")
  Optional<Hash256> buyOffer();

  /**
   * <p>This field is only valid in brokered mode and specifies the
   * amount that the broker will keep as part of their fee for
   * bringing the two offers together; the remaining amount will
   * be sent to the seller of the NfT being bought. If
   * specified, the fee must be such that, prior to accounting
   * for the transfer fee charged by the issuer, the amount that
   * the seller would receive is at least as much as the amount
   * indicated in the sell offer.
   *
   * This functionality is intended to allow the owner of an
   * NfT to offer their token for sale to a third party
   * broker, who may then attempt to sell the NfT on for a
   * larger amount, without the broker having to own the NfT
   * or custody funds.
   *
   * If both offers are for the same asset, it is possible that
   * the order in which funds are transferred might cause a
   * transaction that would succeed to fail due to an apparent
   * lack of funds. To ensure deterministic transaction execution
   * and maximimize the chances of successful execution, this
   * proposal requires that the account attempting to buy the
   * NfT is debited first and that funds due to the broker
   * are credited before crediting the seller.
   *
   * Note: in brokered mode, The offers referenced by BuyOffer
   * and SellOffer must both specify the same TokenID; that is,
   * both must be for the same NfT.</p>
   *
   * @return An {@link Optional} of type {@link CurrencyAmount}.
   */
  @JsonProperty("NFTokenBrokerFee")
  Optional<CurrencyAmount> brokerFee();

  /**
   * Validate that either {@link NfTokenAcceptOffer#buyOffer()} or {@link NfTokenAcceptOffer#sellOffer()} is present
   * for direct mode and both are present for brokered mode.
   */
  @Value.Check
  default void atleastOneOfferPresent() {
    if (brokerFee().isPresent()) {
      Preconditions.checkState( buyOffer().isPresent() && sellOffer().isPresent(),
        "In brokered mode, you must specify both the SellOffer and the BuyOffer.");
    } else {
      Preconditions.checkState( (buyOffer().isPresent() || sellOffer().isPresent()) &&
        !(buyOffer().isPresent() && sellOffer().isPresent()),
        "In direct mode, you must specify either the SellOffer or the BuyOffer.");
    }
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link NfTokenAcceptOffer}, which only allows
   * {@code tfFullyCanonicalSig} flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

}
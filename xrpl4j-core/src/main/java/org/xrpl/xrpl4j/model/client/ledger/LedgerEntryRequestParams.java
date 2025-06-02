package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.BuilderVisibility;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.ledger.AmmObject;
import org.xrpl.xrpl4j.model.ledger.BridgeObject;
import org.xrpl.xrpl4j.model.ledger.CheckObject;
import org.xrpl.xrpl4j.model.ledger.CredentialObject;
import org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject;
import org.xrpl.xrpl4j.model.ledger.DidObject;
import org.xrpl.xrpl4j.model.ledger.EscrowObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.ledger.NfTokenPageObject;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.ledger.OracleObject;
import org.xrpl.xrpl4j.model.ledger.PayChannelObject;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.ledger.TicketObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.XChainBridge;

import java.util.Optional;

/**
 * Request parameters for the {@code ledger_entry} RPC.
 *
 * <p>Unlike most other immutable objects in this library, this class's builder is not accessible to developers.
 * Instead, developers should construct instances of {@link LedgerEntryRequestParams} via its various static
 * constructors.</p>
 *
 * <p>Each static constructor constructs {@link LedgerEntryRequestParams} for a particular type of ledger entry
 * as described on <a href="https://xrpl.org/ledger_entry.html#general-fields">xrpl.org</a>.</p>
 *
 * @param <T> The type of {@link LedgerObject} that will be returned in the response to the {@code ledger_entry} RPC
 *            call with these {@link LedgerEntryRequestParams}.
 */
@Value.Immutable
// Note: These parameters should only be constructed via the provided static constructors. Exposing the builder to
// developers allows them to specify multiple types of ledger_entry request, which is unsafe to do.
@Value.Style(builderVisibility = BuilderVisibility.PACKAGE)
@JsonSerialize(as = ImmutableLedgerEntryRequestParams.class)
@JsonDeserialize(as = ImmutableLedgerEntryRequestParams.class)
@SuppressWarnings("OverloadMethodsDeclarationOrder")
public interface LedgerEntryRequestParams<T extends LedgerObject> extends XrplRequestParams {

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a ledger entry by index.
   *
   * @param id                The index or ID of the ledger entry as a {@link Hash256}.
   * @param ledgerSpecifier   A {@link LedgerSpecifier} indicating the ledger to query data from.
   * @param ledgerObjectClass The class of {@link LedgerObject} that should be returned by rippled as a {@link Class} of
   *                          {@link T}.
   * @param <T>               The actual type of {@link LedgerObject} that should be returned by rippled.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link T}.
   */
  static <T extends LedgerObject> LedgerEntryRequestParams<T> index(
    Hash256 id,
    Class<T> ledgerObjectClass,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<T>builder()
      .index(id)
      .ledgerObjectClass(ledgerObjectClass)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a ledger entry by index but does not narrow down the
   * polymorphic type of {@link LedgerObject} that is returned. These parameters are useful when querying a ledger entry
   * by ID that the developer does not know the type of at compile time.
   *
   * @param id              The index or ID of the ledger entry as a {@link Hash256}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link LedgerObject}.
   */
  static LedgerEntryRequestParams<LedgerObject> index(
    Hash256 id,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.builder()
      .index(id)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests an {@link AccountRootObject} ledger entry by address.
   *
   * @param address         The {@link Address} of the account who owns the {@link AccountRootObject}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link AccountRootObject}.
   */
  static LedgerEntryRequestParams<AccountRootObject> accountRoot(
    Address address,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<AccountRootObject>builder()
      .accountRoot(address)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests an {@link AmmObject} ledger entry.
   *
   * <p>Note that although the rippled API allows you to specify either the AMM's ID or its two assets, this
   * class does not allow developers to request an {@link AmmObject} by ID via this method. Instead, developers should
   * use {@link LedgerEntryRequestParams#index()} and specify {@link AmmObject} as the {@code ledgerObjectClass}
   * parameter.</p>
   *
   * @param params          The {@link AmmLedgerEntryParams} that uniquely identify the {@link AmmObject} on ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link AmmObject}.
   */
  static LedgerEntryRequestParams<AmmObject> amm(
    AmmLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<AmmObject>builder()
      .amm(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests an {@link OfferObject} ledger entry.
   *
   * <p>Note that although the rippled API allows you to specify either the Offer's ID or the account that created it
   * and the sequence number of the transaction that created it, this class does not allow developers to request an
   * {@link OfferObject} by ID via this method. Instead, developers should use {@link LedgerEntryRequestParams#index()}
   * and specify {@link OfferObject} as the {@code ledgerObjectClass} parameter.</p>
   *
   * @param params          The {@link OfferLedgerEntryParams} that uniquely identify the {@link OfferObject} on
   *                        ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link OfferObject}.
   */
  static LedgerEntryRequestParams<OfferObject> offer(
    OfferLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<OfferObject>builder()
      .offer(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link RippleStateObject} ledger entry.
   *
   * @param params          The {@link RippleStateLedgerEntryParams} that uniquely identify the
   *                        {@link RippleStateObject} on ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link RippleStateObject}.
   */
  static LedgerEntryRequestParams<RippleStateObject> rippleState(
    RippleStateLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<RippleStateObject>builder()
      .rippleState(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link CheckObject} ledger entry.
   *
   * @param id              The index or ID of the {@link CheckObject}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link RippleStateObject}.
   */
  static LedgerEntryRequestParams<CheckObject> check(
    Hash256 id,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<CheckObject>builder()
      .check(id)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link CredentialObject} ledger entry.
   *
   * <p>Note that although the rippled API allows you to specify either the Credential's ID
   * or the [subject, issuer and credential_type] of the transaction that created the Credential,
   * this class does not allow developers to request a
   * {@link CredentialObject} by ID via this method.
   * Instead, developers should use {@link LedgerEntryRequestParams#index()}
   * and specify {@link CredentialObject} as the {@code ledgerObjectClass} parameter.</p>
   *
   * @param params          The {@link CredentialLedgerEntryParams} that uniquely identify the {@link CredentialObject}
   *                        on ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link CredentialObject}.
   */
  static LedgerEntryRequestParams<CredentialObject> credential(
    CredentialLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<CredentialObject>builder()
      .credential(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link EscrowObject} ledger entry.
   *
   * <p>Note that although the rippled API allows you to specify either the Escrow's ID or the owner and sequence
   * number of the transaction that created the Escrow, this class does not allow developers to request an
   * {@link EscrowObject} by ID via this method. Instead, developers should use {@link LedgerEntryRequestParams#index()}
   * and specify {@link EscrowObject} as the {@code ledgerObjectClass} parameter.</p>
   *
   * @param params          The {@link EscrowLedgerEntryParams} that uniquely identify the {@link EscrowObject} on
   *                        ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link EscrowObject}.
   */
  static LedgerEntryRequestParams<EscrowObject> escrow(
    EscrowLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<EscrowObject>builder()
      .escrow(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link PayChannelObject} ledger entry.
   *
   * @param id              The index or ID of the {@link PayChannelObject}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link PayChannelObject}.
   */
  static LedgerEntryRequestParams<PayChannelObject> paymentChannel(
    Hash256 id,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<PayChannelObject>builder()
      .paymentChannel(id)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link DepositPreAuthObject} ledger entry.
   *
   * <p>Note that although the rippled API allows you to specify either the DepositPreAuth's ID or the owner and the
   * account that is authorized, this class does not allow developers to request an {@link DepositPreAuthObject} by ID
   * via this method. Instead, developers should use {@link LedgerEntryRequestParams#index()} and specify
   * {@link DepositPreAuthObject} as the {@code ledgerObjectClass} parameter.</p>
   *
   * @param params          The {@link DepositPreAuthLedgerEntryParams} that uniquely identify the
   *                        {@link DepositPreAuthObject} on ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link DepositPreAuthObject}.
   */
  static LedgerEntryRequestParams<DepositPreAuthObject> depositPreAuth(
    DepositPreAuthLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<DepositPreAuthObject>builder()
      .depositPreAuth(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link TicketObject} ledger entry.
   *
   * <p>Note that although the rippled API allows you to specify either the Ticket's ID or the owner of the Ticket and
   * the Ticket's sequence, this class does not allow developers to request an {@link TicketObject} by ID via this
   * method. Instead, developers should use {@link LedgerEntryRequestParams#index()} and specify {@link TicketObject} as
   * the {@code ledgerObjectClass} parameter.</p>
   *
   * @param params          The {@link TicketLedgerEntryParams} that uniquely identify the {@link TicketObject} on
   *                        ledger.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link TicketObject}.
   */
  static LedgerEntryRequestParams<TicketObject> ticket(
    TicketLedgerEntryParams params,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<TicketObject>builder()
      .ticket(params)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link NfTokenPageObject} ledger entry.
   *
   * @param id              The index or ID of the {@link NfTokenPageObject}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link NfTokenPageObject}.
   */
  static LedgerEntryRequestParams<NfTokenPageObject> nftPage(
    Hash256 id,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<NfTokenPageObject>builder()
      .nftPage(id)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link BridgeObject} ledger entry.
   *
   * @param bridgeAccount   The address of the account that owned the {@link BridgeObject}.
   * @param bridge          The bridge spec, as an {@link XChainBridge}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link NfTokenPageObject}.
   */
  static LedgerEntryRequestParams<BridgeObject> bridge(
    Address bridgeAccount,
    XChainBridge bridge,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<BridgeObject>builder()
      .bridgeAccount(bridgeAccount)
      .bridge(bridge)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link DidObject} ledger entry.
   *
   * @param address         The address of the owner of the {@link DidObject}.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link DidObject}.
   */
  static LedgerEntryRequestParams<DidObject> did(
    Address address,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<DidObject>builder()
      .did(address)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link OracleObject} ledger entry.
   *
   * @param oracle          The {@link OracleLedgerEntryParams} specifying the oracle.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link OracleObject}.
   */
  static LedgerEntryRequestParams<OracleObject> oracle(
    OracleLedgerEntryParams oracle,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<OracleObject>builder()
      .oracle(oracle)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link MpTokenIssuanceObject} ledger entry.
   *
   * @param issuanceId      The {@link MpTokenIssuanceId} of the token.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link OracleObject}.
   */
  static LedgerEntryRequestParams<MpTokenIssuanceObject> mpTokenIssuance(
    MpTokenIssuanceId issuanceId,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<MpTokenIssuanceObject>builder()
      .mptIssuance(issuanceId)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Construct a {@link LedgerEntryRequestParams} that requests a {@link MpTokenObject} ledger entry.
   *
   * @param mpToken         The {@link MpTokenLedgerEntryParams} specifying the MPToken.
   * @param ledgerSpecifier A {@link LedgerSpecifier} indicating the ledger to query data from.
   *
   * @return A {@link LedgerEntryRequestParams} for {@link OracleObject}.
   */
  static LedgerEntryRequestParams<MpTokenObject> mpToken(
    MpTokenLedgerEntryParams mpToken,
    LedgerSpecifier ledgerSpecifier
  ) {
    return ImmutableLedgerEntryRequestParams.<MpTokenObject>builder()
      .mpToken(mpToken)
      .ledgerSpecifier(ledgerSpecifier)
      .build();
  }

  /**
   * Specifies the ledger version to request. A ledger version can be specified by ledger hash, numerical ledger index,
   * or a shortcut value.
   *
   * @return A {@link LedgerSpecifier} specifying the ledger version to request.
   */
  @JsonUnwrapped
  LedgerSpecifier ledgerSpecifier();

  /**
   * If true, return the requested ledger entry's contents as a hex string in the XRP Ledger's binary format. Otherwise,
   * return data in JSON format. This field will always be {@code false}.
   *
   * @return A boolean.
   */
  @Value.Derived
  default boolean binary() {
    return false;
  }

  /**
   * Look up a ledger entry by ID/index.
   *
   * @return An optionally-present {@link Hash256}.
   */
  Optional<Hash256> index();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.AccountRootObject} by {@link Address}.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("account_root")
  Optional<Address> accountRoot();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.AmmObject} by {@link AmmLedgerEntryParams}.
   *
   * @return An optionally-present {@link AmmLedgerEntryParams}.
   */
  Optional<AmmLedgerEntryParams> amm();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.OfferObject} by {@link OfferLedgerEntryParams}.
   *
   * @return An optionally-present {@link OfferLedgerEntryParams}.
   */
  Optional<OfferLedgerEntryParams> offer();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.RippleStateObject} by {@link RippleStateLedgerEntryParams}.
   *
   * @return An optionally-present {@link RippleStateLedgerEntryParams}.
   */
  @JsonProperty("ripple_state")
  Optional<RippleStateLedgerEntryParams> rippleState();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.CheckObject} by ID.
   *
   * @return An optionally-present {@link Hash256}.
   */
  Optional<Hash256> check();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.CredentialObject} by {@link CredentialLedgerEntryParams}.
   *
   * @return An optionally-present {@link CredentialLedgerEntryParams}.
   */
  Optional<CredentialLedgerEntryParams> credential();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.EscrowObject} by {@link EscrowLedgerEntryParams}.
   *
   * @return An optionally-present {@link EscrowLedgerEntryParams}.
   */
  Optional<EscrowLedgerEntryParams> escrow();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.PayChannelObject} by ID.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("payment_channel")
  Optional<Hash256> paymentChannel();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.NfTokenPageObject} by ID.
   *
   * @return An optionally-present {@link Hash256}.
   */
  @JsonProperty("nft_page")
  Optional<Hash256> nftPage();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.DepositPreAuthObject} by {@link DepositPreAuthLedgerEntryParams}.
   *
   * @return An optionally-present {@link DepositPreAuthLedgerEntryParams}.
   */
  @JsonProperty("deposit_preauth")
  Optional<DepositPreAuthLedgerEntryParams> depositPreAuth();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.TicketObject} by {@link TicketLedgerEntryParams}.
   *
   * @return An optionally-present {@link TicketLedgerEntryParams}.
   */
  Optional<TicketLedgerEntryParams> ticket();

  /**
   * Loop up a {@link org.xrpl.xrpl4j.model.ledger.DidObject} by {@link Address}.
   *
   * @return An optionally-present {@link Address}.
   */
  Optional<Address> did();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.BridgeObject} by {@link Address}. The {@link #bridge()} field must
   * also be present.
   *
   * @return An optionally-present {@link Address}.
   */
  @JsonProperty("bridge_account")
  Optional<Address> bridgeAccount();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.BridgeObject} by {@link XChainBridge}. The {@link #bridgeAccount()}
   * field must also be present.
   *
   * @return An optionally-present {@link XChainBridge}.
   */
  Optional<XChainBridge> bridge();

  /**
   * Look up a {@link org.xrpl.xrpl4j.model.ledger.OracleObject} by {@link OracleLedgerEntryParams}.
   *
   * @return An {@link Optional} {@link OracleLedgerEntryParams}.
   */
  Optional<OracleLedgerEntryParams> oracle();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject} by {@link MpTokenIssuanceId}.
   *
   * @return An {@link Optional} {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance")
  Optional<MpTokenIssuanceId> mptIssuance();

  /**
   * Look up an {@link org.xrpl.xrpl4j.model.ledger.MpTokenObject} by {@link MpTokenLedgerEntryParams}.
   *
   * @return An {@link Optional} {@link MpTokenLedgerEntryParams}.
   */
  @JsonProperty("mptoken")
  Optional<MpTokenLedgerEntryParams> mpToken();

  /**
   * The {@link Class} of {@link T}. This field is helpful when telling Jackson how to deserialize rippled's response to
   * a {@link T}.
   *
   * @return A {@link Class} of type {@link T}.
   */
  @JsonIgnore
  @Value.Default
  default Class<T> ledgerObjectClass() {
    if (accountRoot().isPresent()) {
      return (Class<T>) AccountRootObject.class;
    }

    if (amm().isPresent()) {
      return (Class<T>) AmmObject.class;
    }

    if (offer().isPresent()) {
      return (Class<T>) OfferObject.class;
    }

    if (rippleState().isPresent()) {
      return (Class<T>) RippleStateObject.class;
    }

    if (check().isPresent()) {
      return (Class<T>) CheckObject.class;
    }

    if (credential().isPresent()) {
      return (Class<T>) CredentialObject.class;
    }

    if (escrow().isPresent()) {
      return (Class<T>) EscrowObject.class;
    }

    if (paymentChannel().isPresent()) {
      return (Class<T>) PayChannelObject.class;
    }

    if (nftPage().isPresent()) {
      return (Class<T>) NfTokenPageObject.class;
    }

    if (depositPreAuth().isPresent()) {
      return (Class<T>) DepositPreAuthObject.class;
    }

    if (ticket().isPresent()) {
      return (Class<T>) TicketObject.class;
    }

    if (bridgeAccount().isPresent() || bridge().isPresent()) {
      return (Class<T>) BridgeObject.class;
    }

    if (did().isPresent()) {
      return (Class<T>) DidObject.class;
    }

    if (oracle().isPresent()) {
      return (Class<T>) OracleObject.class;
    }

    if (mptIssuance().isPresent()) {
      return (Class<T>) MpTokenIssuanceObject.class;
    }

    if (mpToken().isPresent()) {
      return (Class<T>) MpTokenObject.class;
    }

    return (Class<T>) LedgerObject.class;
  }
}

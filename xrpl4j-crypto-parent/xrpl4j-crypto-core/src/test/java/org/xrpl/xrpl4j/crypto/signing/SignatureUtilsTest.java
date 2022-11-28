package org.xrpl.xrpl4j.crypto.signing;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.Asset;
import org.xrpl.xrpl4j.model.ledger.AuthAccount;
import org.xrpl.xrpl4j.model.ledger.AuthAccountWrapper;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AmmBid;
import org.xrpl.xrpl4j.model.transactions.AmmCreate;
import org.xrpl.xrpl4j.model.transactions.AmmDeposit;
import org.xrpl.xrpl4j.model.transactions.AmmVote;
import org.xrpl.xrpl4j.model.transactions.AmmWithdraw;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.DepositPreAuth;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.NfTokenAcceptOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenBurn;
import org.xrpl.xrpl4j.model.transactions.NfTokenCancelOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.model.transactions.TradingFee;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Unit tests for {@link SignatureUtils}.
 */
public class SignatureUtilsTest {

  private static Wallet sourceWallet;

  @Mock
  private Transaction transactionMock;

  @Mock
  private Signature signatureMock;

  @Mock
  private ObjectMapper objectMapperMock;

  @Mock
  private XrplBinaryCodec xrplBinaryCodecMock;

  private SignatureUtils signatureUtils;

  @BeforeEach
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    sourceWallet = this.getSourceWallet();
    when(objectMapperMock.writeValueAsString(any())).thenReturn("{foo}"); // <-- Unused JSON value.
    when(xrplBinaryCodecMock.encodeForSigning(anyString())).thenReturn("ED");
    when(xrplBinaryCodecMock.encodeForMultiSigning(any(), anyString())).thenReturn("ED");
    when(xrplBinaryCodecMock.encode(anyString())).thenReturn("0123456789"); // <-- Unused HEX value.
    this.signatureUtils = new SignatureUtils(objectMapperMock, xrplBinaryCodecMock);
  }

  //////////////////
  // toSignableBytes
  //////////////////

  @Test
  public void toSignableBytesWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureUtils.toSignableBytes(null));
  }

  @Test
  public void toMultiSignableBytesWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class, () -> signatureUtils.toMultiSignableBytes(null, ""));
  }

  @Test
  public void toMultiSignableBytesWithNullSignerAddress() {
    Assertions.assertThrows(
      NullPointerException.class,
      () -> signatureUtils.toMultiSignableBytes(transactionMock, null)
    );
  }

  @Test
  public void toSignableBytesWithJsonException() throws JsonProcessingException {
    doThrow(new JsonParseException("", mock(JsonLocation.class))).when(objectMapperMock).writeValueAsString(any());
    Assertions.assertThrows(RuntimeException.class, () -> signatureUtils.toSignableBytes(transactionMock));
  }

  @Test
  public void toMutliSignableBytesWithJsonException() throws JsonProcessingException {
    doThrow(new JsonParseException("", mock(JsonLocation.class))).when(objectMapperMock).writeValueAsString(any());
    Assertions.assertThrows(RuntimeException.class, () -> signatureUtils.toMultiSignableBytes(transactionMock, ""));
  }

  @Test
  public void toSignableBytes() throws JsonProcessingException {
    UnsignedByteArray actual = signatureUtils.toSignableBytes(transactionMock);
    assertThat(actual.length()).isEqualTo(1);

    verify(objectMapperMock).writeValueAsString(transactionMock);
    verifyNoMoreInteractions(objectMapperMock);
    verify(xrplBinaryCodecMock).encodeForSigning(anyString());
    verifyNoMoreInteractions(xrplBinaryCodecMock);
  }

  @Test
  public void toMultiSignableBytes() throws JsonProcessingException {
    UnsignedByteArray actual = signatureUtils.toMultiSignableBytes(transactionMock, "");
    assertThat(actual.length()).isEqualTo(1);

    verify(objectMapperMock).writeValueAsString(transactionMock);
    verifyNoMoreInteractions(objectMapperMock);
    verify(xrplBinaryCodecMock).encodeForMultiSigning(anyString(), anyString());
    verifyNoMoreInteractions(xrplBinaryCodecMock);
  }

  ////////////////////////////
  // addSignatureToTransaction
  ////////////////////////////

  @Test
  public void addSignatureToTransactionWithNullTransaction() {
    Assertions
      .assertThrows(NullPointerException.class, () -> signatureUtils.addSignatureToTransaction(null, signatureMock));
  }

  @Test
  public void addSignatureToTransactionWithNullSignature() {
    Assertions
      .assertThrows(NullPointerException.class, () -> signatureUtils.addSignatureToTransaction(transactionMock, null));
  }

  @Test
  public void addSignatureToTransactionWithMissingSignature() {
    when(transactionMock.transactionSignature()).thenReturn(Optional.empty());
    when(transactionMock.signingPublicKey()).thenReturn(Optional.of(""));
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> signatureUtils.addSignatureToTransaction(transactionMock, signatureMock));
  }

  @Test
  public void addSignatureToTransactionWithMissingPublicKey() {
    when(transactionMock.transactionSignature()).thenReturn(Optional.of(""));
    when(transactionMock.signingPublicKey()).thenReturn(Optional.empty());

    Assertions.assertThrows(IllegalArgumentException.class,
      () -> signatureUtils.addSignatureToTransaction(transactionMock, signatureMock));
  }

  @Test
  public void addSignatureToTransactionPayment() {
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .destination(sourceWallet.classicAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourceWallet.publicKey())
      .build();
    addSignatureToTransactionHelper(payment);
  }

  @Test
  public void addSignatureToTransactionAccountSet() {
    AccountSet accountSet = AccountSet.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();
    addSignatureToTransactionHelper(accountSet);
  }

  @Test
  public void addSignatureToTransactionAccountDelete() {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(sourceWallet.classicAddress())
      .destination(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();
    addSignatureToTransactionHelper(accountDelete);
  }

  @Test
  public void addSignatureToTransactionCheckCancel() {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .checkId(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    addSignatureToTransactionHelper(checkCancel);
  }

  @Test
  public void addSignatureToTransactionCheckCash() {
    CheckCash checkCash = CheckCash.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .checkId(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .build();
    addSignatureToTransactionHelper(checkCash);
  }

  @Test
  public void addSignatureToTransactionCheckCreate() {
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .destination(sourceWallet.classicAddress())
      .sendMax(XrpCurrencyAmount.ofDrops(100))
      .build();
    addSignatureToTransactionHelper(checkCreate);
  }

  @Test
  public void addSignatureToTransactionDepositPreAuth() {
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .authorize(sourceWallet.classicAddress())
      .build();
    addSignatureToTransactionHelper(depositPreAuth);
  }

  @Test
  public void addSignatureToTransactionEscrowCancel() {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .offerSequence(UnsignedInteger.ONE)
      .owner(sourceWallet.classicAddress())
      .build();
    addSignatureToTransactionHelper(escrowCancel);
  }

  @Test
  public void addSignatureToTransactionEscrowFinish() {
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .offerSequence(UnsignedInteger.ONE)
      .owner(sourceWallet.classicAddress())
      .build();
    addSignatureToTransactionHelper(escrowFinish);
  }

  @Test
  public void addSignatureToTransactionEscrowCreate() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(sourceWallet.classicAddress())
      .build();
    addSignatureToTransactionHelper(escrowCreate);
  }

  @Test
  public void addSignatureToTransactionTrustSet() {
    TrustSet trustSet = TrustSet.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .limitAmount(IssuedCurrencyAmount.builder()
        .issuer(sourceWallet.classicAddress())
        .currency("USD")
        .value("10")
        .build())
      .build();
    addSignatureToTransactionHelper(trustSet);
  }

  @Test
  public void addSignatureToTransactionNfTokenAcceptOffer() {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(sourceWallet.publicKey())
      .buyOffer(offer)
      .build();
    addSignatureToTransactionHelper(nfTokenAcceptOffer);
  }

  @Test
  public void addSignatureToTransactionNfTokenBurn() {
    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(sourceWallet.classicAddress())
      .signingPublicKey(sourceWallet.publicKey())
      .nfTokenId(id)
      .build();
    addSignatureToTransactionHelper(nfTokenBurn);
  }

  @Test
  public void addSignatureToTransactionNfTokenCancelOffer() {
    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    List<Hash256> offers = new ArrayList<>();
    offers.add(offer);
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .account(sourceWallet.classicAddress())
      .signingPublicKey(sourceWallet.publicKey())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .tokenOffers(offers)
      .build();
    addSignatureToTransactionHelper(nfTokenCancelOffer);
  }

  @Test
  public void addSignatureToTransactionNfTokenCreateOffer() {
    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(sourceWallet.classicAddress())
      .signingPublicKey(sourceWallet.publicKey())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .nfTokenId(id)
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .build();
    addSignatureToTransactionHelper(nfTokenCreateOffer);
  }

  @Test
  public void addSignatureToTransactionNfTokenMint() {
    UnsignedLong taxon = UnsignedLong.valueOf(146999694L);
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(sourceWallet.classicAddress())
      .signingPublicKey(sourceWallet.publicKey())
      .tokenTaxon(taxon)
      .build();
    addSignatureToTransactionHelper(nfTokenMint);
  }

  @Test
  public void addSignatureToTransactionOfferOfferCreate() {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .takerPays(XrpCurrencyAmount.ofDrops(100))
      .takerGets(XrpCurrencyAmount.ofDrops(100))
      .build();
    addSignatureToTransactionHelper(offerCreate);
  }

  @Test
  public void addSignatureToTransactionOfferCancel() {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();
    addSignatureToTransactionHelper(offerCancel);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelCreate() {
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(sourceWallet.classicAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("123")
      .build();
    addSignatureToTransactionHelper(paymentChannelCreate);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelClaim() {
    PaymentChannelClaim paymentChannelClaim = PaymentChannelClaim.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    addSignatureToTransactionHelper(paymentChannelClaim);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelFund() {
    PaymentChannelFund paymentChannelFund = PaymentChannelFund.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100L))
      .build();
    addSignatureToTransactionHelper(paymentChannelFund);
  }

  @Test
  public void addSignatureToTransactionSetRegularKey() {
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();
    addSignatureToTransactionHelper(setRegularKey);
  }

  @Test
  public void addSignatureToTransactionSignerListSet() {
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .signerQuorum(UnsignedInteger.ONE)
      .build();
    addSignatureToTransactionHelper(signerListSet);
  }

  @Test
  void addSignatureToTicketCreate() {
    TicketCreate ticketCreate = TicketCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .ticketCount(UnsignedInteger.ONE)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    addSignatureToTransactionHelper(ticketCreate);
  }

  @Test
  void addSignatureToAmmBid() {
    AmmBid bid = AmmBid.builder()
      .account(sourceWallet.classicAddress())
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .addAuthAccounts(
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rMKXGCbJ5d8LbrqthdG46q3f969MVK2Qeg"))),
        AuthAccountWrapper.of(AuthAccount.of(Address.of("rBepJuTLFJt3WmtLXYAxSjtBWAeQxVbncv")))
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(9))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    addSignatureToTransactionHelper(bid);
  }

  @Test
  void addSignatureToAmmCreate() {
    AmmCreate ammCreate = AmmCreate.builder()
      .account(sourceWallet.classicAddress())
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .value("25")
          .build()
      )
      .amount2(XrpCurrencyAmount.ofDrops(250000000))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(6))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(500)))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    addSignatureToTransactionHelper(ammCreate);
  }

  @Test
  void addSignatureToAmmDeposit() {
    AmmDeposit deposit = AmmDeposit.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .lpTokenOut(
        IssuedCurrencyAmount.builder()
          .currency("039C99CD9AB0B70B32ECDA51EAAE471625608EA2")
          .issuer(Address.of("rE54zDvgnghAoPopCgvtiqWNq3dU5y836S"))
          .value("100")
          .build()
      )
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    addSignatureToTransactionHelper(deposit);
  }

  @Test
  void addSignatureToAmmVote() {
    AmmVote vote = AmmVote.builder()
      .account(sourceWallet.classicAddress())
      .asset(Asset.XRP)
      .asset2(
        Asset.builder()
          .currency("TST")
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .build()
      )
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.valueOf(8))
      .tradingFee(TradingFee.of(UnsignedInteger.valueOf(600)))
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    addSignatureToTransactionHelper(vote);
  }

  @Test
  void addSignatureToAmmWithdraw() {
    AmmWithdraw withdraw = AmmWithdraw.builder()
      .account(sourceWallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(10))
      .asset(
        Asset.builder()
          .issuer(Address.of("rP9jPyP5kyvFRb6ZiRghAGw5u8SGAmU4bd"))
          .currency("TST")
          .build()
      )
      .asset2(Asset.XRP)
      .flags(Flags.AmmWithdrawFlags.WITHDRAW_ALL)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    addSignatureToTransactionHelper(withdraw);
  }

  @Test
  public void addSignatureToTransactionUnsupported() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> addSignatureToTransactionHelper(transactionMock));
  }

  private void addSignatureToTransactionHelper(final Transaction transaction) {
    Objects.requireNonNull(transaction);
    when(signatureMock.base16Value()).thenReturn("ED");
    SignedTransaction result = signatureUtils.addSignatureToTransaction(transaction, signatureMock);
    assertThat(result.unsignedTransaction()).isEqualTo(transaction);
    assertThat(result.signature().base16Value()).isEqualTo("ED");
    assertThat(result.signedTransaction().transactionSignature()).isPresent();
    assertThat(result.signedTransaction().transactionSignature().get()).isEqualTo("ED");
  }


  private Wallet getSourceWallet() {
    final PublicKey publicKey = PublicKey
      .fromBase16EncodedPublicKey("0093CC77E2333958D1480FC36811A68A1785258F65251DE100012FA18D0186FFB0");
    final Address classicAddress = new DefaultKeyPairService().deriveAddress(publicKey.value());
    return Wallet.builder()
      .publicKey(publicKey.base16Encoded())
      .isTest(true)
      .classicAddress(classicAddress)
      .xAddress(new AddressCodec().classicAddressToXAddress(classicAddress, true))
      .build();
  }

}

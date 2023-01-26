package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
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
import org.xrpl.xrpl4j.model.transactions.OfferCancel;
import org.xrpl.xrpl4j.model.transactions.OfferCreate;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate;
import org.xrpl.xrpl4j.model.transactions.PaymentChannelFund;
import org.xrpl.xrpl4j.model.transactions.SetRegularKey;
import org.xrpl.xrpl4j.model.transactions.SignerListSet;
import org.xrpl.xrpl4j.model.transactions.SignerWrapper;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Unit tests for {@link SignatureUtils}.
 */
public class SignatureUtilsTest {

  private static final String HEX_PUBLIC_KEY = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";

  PublicKey sourcePublicKey;

  @Mock
  Transaction transactionMock;

  @Mock
  Signature signatureMock;

  @Mock
  SignerWrapper signer1;

  @Mock
  SignerWrapper signer2;

  @Mock
  ObjectMapper objectMapperMock;

  @Mock
  XrplBinaryCodec xrplBinaryCodecMock;

  SignatureUtils signatureUtils;

  @BeforeEach
  public void setUp() throws JsonProcessingException {
    openMocks(this);

    this.sourcePublicKey = PublicKey.fromBase16EncodedPublicKey(HEX_PUBLIC_KEY);

    when(objectMapperMock.writeValueAsString(any())).thenReturn("{foo}"); // <-- Unused JSON value.
    when(xrplBinaryCodecMock.encodeForSigning(anyString())).thenReturn("ED");
    when(xrplBinaryCodecMock.encodeForMultiSigning(any(), anyString())).thenReturn("ED");
    when(xrplBinaryCodecMock.encode(anyString())).thenReturn("0123456789"); // <-- Unused HEX value.
    this.signatureUtils = new SignatureUtils(objectMapperMock, xrplBinaryCodecMock);
  }

  //////////////////
  // toSignableBytes (Transaction)
  //////////////////

  @Test
  public void toSignableBytesWithNullTransaction() {
    assertThrows(NullPointerException.class, () -> signatureUtils.toSignableBytes((Transaction) null));
  }

  @Test
  public void toMultiSignableBytesWithNullTransaction() {
    assertThrows(NullPointerException.class,
      () -> signatureUtils.toMultiSignableBytes(null, sourcePublicKey.deriveAddress()));
  }

  @Test
  public void toMultiSignableBytesWithNullSignerAddress() {
    assertThrows(
      NullPointerException.class,
      () -> signatureUtils.toMultiSignableBytes(transactionMock, null)
    );
  }

  @Test
  public void toSignableBytesWithJsonException() throws JsonProcessingException {
    doThrow(new JsonParseException(mock(JsonParser.class), "", mock(JsonLocation.class)))
      .when(objectMapperMock).writeValueAsString(any());
    assertThrows(RuntimeException.class, () -> signatureUtils.toSignableBytes(transactionMock));
  }

  @Test
  public void toMutliSignableBytesWithJsonException() throws JsonProcessingException {
    doThrow(new JsonParseException(mock(JsonParser.class), "", mock(JsonLocation.class)))
      .when(objectMapperMock).writeValueAsString(any());
    assertThrows(RuntimeException.class,
      () -> signatureUtils.toMultiSignableBytes(transactionMock, mock(Address.class)));
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

  //////////////////
  // toSignableBytes (UnsignedClaim)
  //////////////////

  @Test
  void unsignedClaimToSignableBytesWhenNull() {
    assertThrows(NullPointerException.class, () -> signatureUtils.toSignableBytes((UnsignedClaim) null));
  }

  @Test
  void unsignedClaimToSignableBytes() throws JsonProcessingException {
    when(xrplBinaryCodecMock.encodeForSigningClaim(any())).thenReturn("ABCD1234");
    UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .amount(XrpCurrencyAmount.of(UnsignedLong.ONE))
      .channel(Hash256.of("ABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCD"))
      .build();
    assertThat(signatureUtils.toSignableBytes(unsignedClaim).hexValue()).isEqualTo("ABCD1234");

    verify(objectMapperMock).writeValueAsString(any());
    verifyNoMoreInteractions(objectMapperMock);
    verify(xrplBinaryCodecMock).encodeForSigningClaim(any());
    verifyNoMoreInteractions(xrplBinaryCodecMock);
  }

  @Test
  public void unsignedClaimToSignableBytesWithJsonException() throws JsonProcessingException {
    UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .amount(XrpCurrencyAmount.of(UnsignedLong.ONE))
      .channel(Hash256.of("ABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCD"))
      .build();
    doThrow(new JsonParseException(mock(JsonParser.class), "", mock(JsonLocation.class)))
      .when(objectMapperMock).writeValueAsString(unsignedClaim);
    assertThrows(RuntimeException.class, () -> signatureUtils.toSignableBytes(unsignedClaim));
  }

  @Test
  void unsignedClaimToSignableBytesActual() {
    UnsignedClaim unsignedClaim = UnsignedClaim.builder()
      .amount(XrpCurrencyAmount.of(UnsignedLong.ONE))
      .channel(Hash256.of("ABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCD"))
      .build();
    assertThat(SignatureUtils.getInstance().toSignableBytes(unsignedClaim).hexValue())
      .isEqualTo("434C4D00ABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCDABCD0000000000000001");
  }

  //////////////////
  // toMultiSignableBytes
  //////////////////


  @Test
  public void toMultiSignableBytes() throws JsonProcessingException {
    UnsignedByteArray actual = signatureUtils.toMultiSignableBytes(transactionMock, sourcePublicKey.deriveAddress());
    assertThat(actual.length()).isEqualTo(1);

    verify(objectMapperMock).writeValueAsString(transactionMock);
    verifyNoMoreInteractions(objectMapperMock);
    verify(xrplBinaryCodecMock).encodeForMultiSigning(anyString(), anyString());
    verifyNoMoreInteractions(xrplBinaryCodecMock);
  }

  @Test
  public void toMultiSignableBytesWithJsonException() throws JsonProcessingException {
    doThrow(new JsonParseException(mock(JsonParser.class), "", mock(JsonLocation.class)))
      .when(objectMapperMock).writeValueAsString(any());
    assertThrows(
      RuntimeException.class,
      () -> signatureUtils.toMultiSignableBytes(transactionMock, sourcePublicKey.deriveAddress())
    );
  }

  ////////////////////////////
  // addSignatureToTransaction
  ////////////////////////////

  @Test
  public void addSignatureToTransactionWithNullTransaction() {
    assertThrows(NullPointerException.class, () -> signatureUtils.addSignatureToTransaction(null, signatureMock));
  }

  @Test
  public void addSignatureToTransactionWithNullSignature() {
    assertThrows(NullPointerException.class, () -> signatureUtils.addSignatureToTransaction(transactionMock, null));
  }

  @Test
  public void addSignatureToTransactionWithMissingSignature() {
    when(transactionMock.transactionSignature()).thenReturn(Optional.empty());
    when(transactionMock.signingPublicKey()).thenReturn(PublicKey.MULTI_SIGN_PUBLIC_KEY);
    assertThrows(IllegalArgumentException.class,
      () -> signatureUtils.addSignatureToTransaction(transactionMock, signatureMock));
  }

  @Test
  public void addSignatureToTransactionPayment() {
    Payment payment = Payment.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .destination(sourcePublicKey.deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourcePublicKey)
      .build();
    addSignatureToTransactionHelper(payment);
  }

  @Test
  public void addSignatureToTransactionAccountSet() {
    AccountSet accountSet = AccountSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .build();
    addSignatureToTransactionHelper(accountSet);
  }

  @Test
  public void addSignatureToTransactionAccountDelete() {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(sourcePublicKey.deriveAddress())
      .destination(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .build();
    addSignatureToTransactionHelper(accountDelete);
  }

  @Test
  public void addSignatureToTransactionCheckCancel() {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .checkId(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    addSignatureToTransactionHelper(checkCancel);
  }

  @Test
  public void addSignatureToTransactionCheckCash() {
    CheckCash checkCash = CheckCash.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .checkId(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .build();
    addSignatureToTransactionHelper(checkCash);
  }

  @Test
  public void addSignatureToTransactionCheckCreate() {
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .destination(sourcePublicKey.deriveAddress())
      .sendMax(XrpCurrencyAmount.ofDrops(100))
      .build();
    addSignatureToTransactionHelper(checkCreate);
  }

  @Test
  public void addSignatureToTransactionDepositPreAuth() {
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .authorize(sourcePublicKey.deriveAddress())
      .build();
    addSignatureToTransactionHelper(depositPreAuth);
  }

  @Test
  public void addSignatureToTransactionEscrowCancel() {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .offerSequence(UnsignedInteger.ONE)
      .owner(sourcePublicKey.deriveAddress())
      .build();
    addSignatureToTransactionHelper(escrowCancel);
  }

  @Test
  public void addSignatureToTransactionEscrowFinish() {
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .offerSequence(UnsignedInteger.ONE)
      .owner(sourcePublicKey.deriveAddress())
      .build();
    addSignatureToTransactionHelper(escrowFinish);
  }

  @Test
  public void addSignatureToTransactionEscrowCreate() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(sourcePublicKey.deriveAddress())
      .build();
    addSignatureToTransactionHelper(escrowCreate);
  }

  @Test
  public void addSignatureToTransactionTrustSet() {
    TrustSet trustSet = TrustSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .limitAmount(IssuedCurrencyAmount.builder()
        .issuer(sourcePublicKey.deriveAddress())
        .currency("USD")
        .value("10")
        .build())
      .build();
    addSignatureToTransactionHelper(trustSet);
  }

  @Test
  public void addSignatureToTransactionOfferOfferCreate() {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .takerPays(XrpCurrencyAmount.ofDrops(100))
      .takerGets(XrpCurrencyAmount.ofDrops(100))
      .build();
    addSignatureToTransactionHelper(offerCreate);
  }

  @Test
  public void addSignatureToTransactionOfferCancel() {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .build();
    addSignatureToTransactionHelper(offerCancel);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelCreate() {
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(sourcePublicKey.deriveAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("123")
      .build();
    addSignatureToTransactionHelper(paymentChannelCreate);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelClaim() {
    PaymentChannelClaim paymentChannelClaim = PaymentChannelClaim.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    addSignatureToTransactionHelper(paymentChannelClaim);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelFund() {
    PaymentChannelFund paymentChannelFund = PaymentChannelFund.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100L))
      .build();
    addSignatureToTransactionHelper(paymentChannelFund);
  }

  @Test
  public void addSignatureToTransactionSetRegularKey() {
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .build();
    addSignatureToTransactionHelper(setRegularKey);
  }

  @Test
  public void addSignatureToTransactionSignerListSet() {
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .signerQuorum(UnsignedInteger.ONE)
      .build();
    addSignatureToTransactionHelper(signerListSet);
  }

  @Test
  public void addSignatureToTransactionNfTokenAcceptOffer() {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .signingPublicKey(sourcePublicKey)
      .buyOffer(offer)
      .build();
    addSignatureToTransactionHelper(nfTokenAcceptOffer);
  }

  @Test
  public void addSignatureToTransactionNfTokenBurn() {
    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(sourcePublicKey.deriveAddress())
      .signingPublicKey(sourcePublicKey)
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
      .account(sourcePublicKey.deriveAddress())
      .signingPublicKey(sourcePublicKey)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .tokenOffers(offers)
      .build();
    addSignatureToTransactionHelper(nfTokenCancelOffer);
  }

  @Test
  public void addSignatureToTransactionNfTokenCreateOffer() {
    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(sourcePublicKey.deriveAddress())
      .signingPublicKey(sourcePublicKey)
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
      .account(sourcePublicKey.deriveAddress())
      .signingPublicKey(sourcePublicKey)
      .tokenTaxon(taxon)
      .build();
    addSignatureToTransactionHelper(nfTokenMint);
  }

  @Test
  void addSignatureToTicketCreate() {
    TicketCreate ticketCreate = TicketCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .ticketCount(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey)
      .build();

    addSignatureToTransactionHelper(ticketCreate);
  }

  @Test
  public void addSignatureToTransactionUnsupported() {
    assertThrows(IllegalArgumentException.class, () -> addSignatureToTransactionHelper(transactionMock));
  }

  @Test
  void addMultiSignaturesWithNulls() {
    assertThatThrownBy(
      () -> signatureUtils.addMultiSignaturesToTransaction(null, Lists.newArrayList(signer1))
    ).isInstanceOf(NullPointerException.class);

    assertThatThrownBy(
      () -> signatureUtils.addMultiSignaturesToTransaction(mock(Transaction.class), null)
    ).isInstanceOf(NullPointerException.class);
  }

  @Test
  void addMultiSignaturesWithTransactionSignaturePresent() {
    when(transactionMock.transactionSignature()).thenReturn(Optional.of("sig"));
    assertThatThrownBy(
      () -> signatureUtils.addMultiSignaturesToTransaction(transactionMock, Lists.newArrayList(signer1))
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Transactions to be signed must not already include a signature.");
  }
  
  @Test
  void addMultiSignaturesWithSigningPublicKeyNonBlank() {
    when(transactionMock.transactionSignature()).thenReturn(Optional.empty());
    when(transactionMock.signingPublicKey())
      .thenReturn(
        PublicKey.fromBase16EncodedPublicKey("ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A")
      );
    assertThatThrownBy(
      () -> signatureUtils.addMultiSignaturesToTransaction(transactionMock, Lists.newArrayList(signer1))
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Transactions to be multisigned must set signingPublicKey to an empty String.");
  }

  @Test
  public void addMultiSignaturesToTransactionPayment() {
    Payment payment = Payment.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .destination(sourcePublicKey.deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .build();
    addMultiSignatureToTransactionHelper(payment);
  }

  @Test
  public void addMultiSignaturesToTransactionAccountSet() {
    AccountSet accountSet = AccountSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .build();
    addMultiSignatureToTransactionHelper(accountSet);
  }

  @Test
  public void addMultiSignaturesToTransactionAccountDelete() {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(sourcePublicKey.deriveAddress())
      .destination(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .build();
    addMultiSignatureToTransactionHelper(accountDelete);
  }

  @Test
  public void addMultiSignaturesToTransactionCheckCancel() {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .checkId(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    addMultiSignatureToTransactionHelper(checkCancel);
  }

  @Test
  public void addMultiSignaturesToTransactionCheckCash() {
    CheckCash checkCash = CheckCash.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .checkId(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100))
      .build();
    addMultiSignatureToTransactionHelper(checkCash);
  }

  @Test
  public void addMultiSignaturesToTransactionCheckCreate() {
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .destination(sourcePublicKey.deriveAddress())
      .sendMax(XrpCurrencyAmount.ofDrops(100))
      .build();
    addMultiSignatureToTransactionHelper(checkCreate);
  }

  @Test
  public void addMultiSignaturesToTransactionDepositPreAuth() {
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .authorize(sourcePublicKey.deriveAddress())
      .build();
    addMultiSignatureToTransactionHelper(depositPreAuth);
  }

  @Test
  public void addMultiSignaturesToTransactionEscrowCancel() {
    EscrowCancel escrowCancel = EscrowCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .offerSequence(UnsignedInteger.ONE)
      .owner(sourcePublicKey.deriveAddress())
      .build();
    addMultiSignatureToTransactionHelper(escrowCancel);
  }

  @Test
  public void addMultiSignaturesToTransactionEscrowFinish() {
    EscrowFinish escrowFinish = EscrowFinish.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .offerSequence(UnsignedInteger.ONE)
      .owner(sourcePublicKey.deriveAddress())
      .build();
    addMultiSignatureToTransactionHelper(escrowFinish);
  }

  @Test
  public void addMultiSignaturesToTransactionEscrowCreate() {
    EscrowCreate escrowCreate = EscrowCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(sourcePublicKey.deriveAddress())
      .build();
    addMultiSignatureToTransactionHelper(escrowCreate);
  }

  @Test
  public void addMultiSignaturesToTransactionTrustSet() {
    TrustSet trustSet = TrustSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .limitAmount(IssuedCurrencyAmount.builder()
        .issuer(sourcePublicKey.deriveAddress())
        .currency("USD")
        .value("10")
        .build())
      .build();
    addMultiSignatureToTransactionHelper(trustSet);
  }

  @Test
  public void addMultiSignaturesToTransactionOfferOfferCreate() {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .takerPays(XrpCurrencyAmount.ofDrops(100))
      .takerGets(XrpCurrencyAmount.ofDrops(100))
      .build();
    addMultiSignatureToTransactionHelper(offerCreate);
  }

  @Test
  public void addMultiSignaturesToTransactionOfferCancel() {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .build();
    addMultiSignatureToTransactionHelper(offerCancel);
  }

  @Test
  public void addMultiSignaturesToTransactionPaymentChannelCreate() {
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(100))
      .destination(sourcePublicKey.deriveAddress())
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("123")
      .build();
    addMultiSignatureToTransactionHelper(paymentChannelCreate);
  }

  @Test
  public void addMultiSignaturesToTransactionPaymentChannelClaim() {
    PaymentChannelClaim paymentChannelClaim = PaymentChannelClaim.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .build();
    addMultiSignatureToTransactionHelper(paymentChannelClaim);
  }

  @Test
  public void addMultiSignaturesToTransactionPaymentChannelFund() {
    PaymentChannelFund paymentChannelFund = PaymentChannelFund.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .channel(Hash256.of("0123456789012345678901234567890123456789012345678901234567891234"))
      .amount(XrpCurrencyAmount.ofDrops(100L))
      .build();
    addMultiSignatureToTransactionHelper(paymentChannelFund);
  }

  @Test
  public void addMultiSignaturesToTransactionSetRegularKey() {
    SetRegularKey setRegularKey = SetRegularKey.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .build();
    addMultiSignatureToTransactionHelper(setRegularKey);
  }

  @Test
  public void addMultiSignaturesToTransactionSignerListSet() {
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signerQuorum(UnsignedInteger.ONE)
      .build();
    addMultiSignatureToTransactionHelper(signerListSet);
  }

  @Test
  public void addMultiSignaturesToTransactionNfTokenAcceptOffer() {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .buyOffer(offer)
      .build();
    addMultiSignatureToTransactionHelper(nfTokenAcceptOffer);
  }

  @Test
  public void addMultiSignaturesToTransactionNfTokenBurn() {
    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(sourcePublicKey.deriveAddress())
      .nfTokenId(id)
      .build();
    addMultiSignatureToTransactionHelper(nfTokenBurn);
  }

  @Test
  public void addMultiSignaturesToTransactionNfTokenCancelOffer() {
    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    List<Hash256> offers = new ArrayList<>();
    offers.add(offer);
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .tokenOffers(offers)
      .build();
    addMultiSignatureToTransactionHelper(nfTokenCancelOffer);
  }

  @Test
  public void addMultiSignaturesToTransactionNfTokenCreateOffer() {
    NfTokenId id = NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(1))
      .nfTokenId(id)
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .build();
    addMultiSignatureToTransactionHelper(nfTokenCreateOffer);
  }

  @Test
  public void addMultiSignaturesToTransactionNfTokenMint() {
    UnsignedLong taxon = UnsignedLong.valueOf(146999694L);
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(sourcePublicKey.deriveAddress())
      .tokenTaxon(taxon)
      .build();
    addMultiSignatureToTransactionHelper(nfTokenMint);
  }

  @Test
  void addMultiSignaturesToTicketCreate() {
    TicketCreate ticketCreate = TicketCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .ticketCount(UnsignedInteger.ONE)
      .build();

    addMultiSignatureToTransactionHelper(ticketCreate);
  }

  @Test
  public void addMultiSignaturesToTransactionUnsupported() {
    when(transactionMock.transactionSignature()).thenReturn(Optional.empty());
    when(transactionMock.signingPublicKey()).thenReturn(PublicKey.MULTI_SIGN_PUBLIC_KEY);
    assertThatThrownBy(
      () -> addMultiSignatureToTransactionHelper(transactionMock)
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Signing fields could not be added to the transaction.");
  }


  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private void addSignatureToTransactionHelper(final Transaction transaction) {
    Objects.requireNonNull(transaction);
    when(signatureMock.base16Value()).thenReturn("ED");
    SingleSignedTransaction<?> result = signatureUtils.addSignatureToTransaction(transaction, signatureMock);
    assertThat(result.unsignedTransaction()).isEqualTo(transaction);
    assertThat(result.signature().base16Value()).isEqualTo("ED");
    assertThat(result.signedTransaction().transactionSignature()).isPresent();
    assertThat(result.signedTransaction().transactionSignature().get()).isEqualTo("ED");
  }

  private void addMultiSignatureToTransactionHelper(final Transaction transaction) {
    Objects.requireNonNull(transaction);

    Transaction signedTransaction = signatureUtils.addMultiSignaturesToTransaction(
      transaction,
      Lists.newArrayList(signer1, signer2)
    );

    assertThat(signedTransaction).usingRecursiveComparison().ignoringFields("signers").isEqualTo(transaction);
    assertThat(signedTransaction.signers()).asList().containsExactly(signer1, signer2);
  }
}
package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  ObjectMapper objectMapperMock;

  @Mock
  XrplBinaryCodec xrplBinaryCodecMock;

  SignatureUtils signatureUtils;

  @BeforeEach
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);

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
    Assertions.assertThrows(NullPointerException.class, () -> signatureUtils.toSignableBytes((Transaction) null));
  }

  @Test
  public void toMultiSignableBytesWithNullTransaction() {
    Assertions.assertThrows(NullPointerException.class,
      () -> signatureUtils.toMultiSignableBytes(null, sourcePublicKey.deriveAddress()));
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
    doThrow(new JsonParseException(mock(JsonParser.class), "", mock(JsonLocation.class)))
      .when(objectMapperMock).writeValueAsString(any());
    Assertions.assertThrows(RuntimeException.class, () -> signatureUtils.toSignableBytes(transactionMock));
  }

  @Test
  public void toMutliSignableBytesWithJsonException() throws JsonProcessingException {
    doThrow(new JsonParseException(mock(JsonParser.class), "", mock(JsonLocation.class)))
      .when(objectMapperMock).writeValueAsString(any());
    Assertions.assertThrows(RuntimeException.class,
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
    Assertions.assertThrows(NullPointerException.class, () -> signatureUtils.toSignableBytes((UnsignedClaim) null));
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
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .destination(sourcePublicKey.deriveAddress())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(sourcePublicKey.base16Value())
      .build();
    addSignatureToTransactionHelper(payment);
  }

  @Test
  public void addSignatureToTransactionAccountSet() {
    AccountSet accountSet = AccountSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
      .build();
    addSignatureToTransactionHelper(accountDelete);
  }

  @Test
  public void addSignatureToTransactionCheckCancel() {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
      .build();
    addSignatureToTransactionHelper(offerCancel);
  }

  @Test
  public void addSignatureToTransactionPaymentChannelCreate() {
    PaymentChannelCreate paymentChannelCreate = PaymentChannelCreate.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
      .build();
    addSignatureToTransactionHelper(setRegularKey);
  }

  @Test
  public void addSignatureToTransactionSignerListSet() {
    SignerListSet signerListSet = SignerListSet.builder()
      .account(sourcePublicKey.deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE))
      .sequence(UnsignedInteger.ONE)
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
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
      .signingPublicKey(sourcePublicKey.base16Value())
      .build();

    addSignatureToTransactionHelper(ticketCreate);
  }

  @Test
  public void addSignatureToTransactionUnsupported() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> addSignatureToTransactionHelper(transactionMock));
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

}
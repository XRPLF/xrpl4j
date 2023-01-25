package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountNftsResult;
import org.xrpl.xrpl4j.model.client.accounts.NfTokenObject;
import org.xrpl.xrpl4j.model.client.nft.NftBuyOffersRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftBuyOffersResult;
import org.xrpl.xrpl4j.model.client.nft.NftSellOffersRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftSellOffersResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.NfTokenAcceptOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenBurn;
import org.xrpl.xrpl4j.model.transactions.NfTokenCancelOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * IT for NfToken operations.
 */
public class NfTokenIT extends AbstractIT {

  @Test
  void mint() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair wallet = createRandomAccountEd25519();

    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );

    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence())
      .uri(uri)
      .build();

    SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(wallet.privateKey(), nfTokenMint);
    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was minted successfully.");
  }

  @Test
  void mintAndBurn() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair wallet = createRandomAccountEd25519();

    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );

    // nft mint
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence())
      .uri(uri)
      .build();

    SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(wallet.privateKey(), nfTokenMint);
    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was minted successfully.");

    // nft burn
    AccountNftsResult accountNftsResult = xrplClient.accountNfts(wallet.publicKey().deriveAddress());

    NfTokenId tokenId = accountNftsResult.accountNfts().get(0).nfTokenId();

    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .nfTokenId(tokenId)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .build();

    SingleSignedTransaction<NfTokenBurn> signedBurn = signatureService.sign(wallet.privateKey(), nfTokenBurn);
    SubmitResult<NfTokenBurn> burnSubmitResult = xrplClient.submit(signedBurn);
    assertThat(burnSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(burnSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(burnSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .noneMatch(nft -> nft.uri().get().equals(uri))
    );

    AccountNftsResult accountNftsResult1 = xrplClient.accountNfts(wallet.publicKey().deriveAddress());
    assertThat(
      accountNftsResult1.accountNfts().stream().noneMatch(
        object -> object.equals(NfTokenObject.builder().nfTokenId(tokenId).build())
      )
    ).isTrue();
    logger.info("NFT burned successfully.");
  }

  @Test
  void mintAndCreateOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair wallet = createRandomAccountEd25519();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence())
      .uri(uri)
      .build();

    SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(wallet.privateKey(), nfTokenMint);
    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = xrplClient.accountNfts(wallet.publicKey().deriveAddress()).accountNfts().get(0).nfTokenId();

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet.publicKey().deriveAddress())
      .nfTokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.NfTokenCreateOfferFlags.builder()
        .tfSellToken(true)
        .build())
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenCreateOffer> signedOffer = signatureService.sign(
      wallet.privateKey(),
      nfTokenCreateOffer
    );
    SubmitResult<NfTokenCreateOffer> nfTokenCreateOfferSubmitResult = xrplClient.submit(signedOffer);
    assertThat(nfTokenCreateOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenCreateOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenCreateOfferSubmitResult.transactionResult().hash());

    //verify the offer was created
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenCreateOfferSubmitResult.transactionResult().hash(),
        NfTokenCreateOffer.class
      )
    );
    logger.info("NFT Create Offer (Sell) transaction was validated successfully.");

    this.scanForResult(
      () -> this.getValidatedAccountObjects(wallet.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet.publicKey().deriveAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");
  }

  @Test
  void mintAndCreateThenAcceptOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair wallet = createRandomAccountEd25519();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence())
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .uri(uri)
      .build();

    SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(wallet.privateKey(), nfTokenMint);
    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = xrplClient.accountNfts(wallet.publicKey().deriveAddress()).accountNfts().get(0).nfTokenId();

    // create buy offer from another account
    KeyPair wallet2 = createRandomAccountEd25519();

    AccountInfoResult accountInfoResult2 = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet2.publicKey().deriveAddress())
    );

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet2.publicKey().deriveAddress())
      .owner(wallet.publicKey().deriveAddress())
      .nfTokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult2.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .signingPublicKey(wallet2.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenCreateOffer> signedOffer = signatureService.sign(
      wallet2.privateKey(),
      nfTokenCreateOffer
    );
    SubmitResult<NfTokenCreateOffer> nfTokenCreateOfferSubmitResult = xrplClient.submit(signedOffer);
    assertThat(nfTokenCreateOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenCreateOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenCreateOfferSubmitResult.transactionResult().hash());

    //verify the offer was created
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenCreateOfferSubmitResult.transactionResult().hash(),
        NfTokenCreateOffer.class
      )
    );
    logger.info("NFT Create Offer (Buy) transaction was validated successfully.");

    this.scanForResult(
      () -> this.getValidatedAccountObjects(wallet2.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet2.publicKey().deriveAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");

    NftBuyOffersResult nftBuyOffersResult = xrplClient.nftBuyOffers(NftBuyOffersRequestParams.builder()
      .nfTokenId(tokenId)
      .build());
    assertThat(nftBuyOffersResult.offers().size()).isEqualTo(1);

    // accept offer from a different account
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(wallet.publicKey().deriveAddress())
      .buyOffer(nftBuyOffersResult.offers().get(0).nftOfferIndex())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenAcceptOffer> signedAccept = signatureService.sign(
      wallet.privateKey(),
      nfTokenAcceptOffer
    );
    SubmitResult<NfTokenAcceptOffer> nfTokenAcceptOfferSubmitResult = xrplClient.submit(signedAccept);
    assertThat(nfTokenAcceptOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenAcceptOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenAcceptOfferSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenAcceptOfferSubmitResult.transactionResult().hash(),
        NfTokenAcceptOffer.class
      )
    );
    logger.info("NFT Accept Offer transaction was validated successfully.");

    assertThat(xrplClient.accountNfts(wallet.publicKey().deriveAddress()).accountNfts().size()).isEqualTo(0);
    assertThat(xrplClient.accountNfts(wallet2.publicKey().deriveAddress()).accountNfts().size()).isEqualTo(1);

    logger.info("The NFT ownership was transferred.");
  }

  @Test
  void mintAndCreateOfferThenCancelOffer() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair wallet = createRandomAccountEd25519();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence())
      .uri(uri)
      .build();

    SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(wallet.privateKey(), nfTokenMint);
    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = xrplClient.accountNfts(wallet.publicKey().deriveAddress()).accountNfts().get(0).nfTokenId();

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet.publicKey().deriveAddress())
      .nfTokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.NfTokenCreateOfferFlags.SELL_NFTOKEN)
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenCreateOffer> signedOffer = signatureService.sign(
      wallet.privateKey(),
      nfTokenCreateOffer
    );
    SubmitResult<NfTokenCreateOffer> nfTokenCreateOfferSubmitResult = xrplClient.submit(signedOffer);
    assertThat(nfTokenCreateOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenCreateOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenCreateOfferSubmitResult.transactionResult().hash());

    //verify the offer was created
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenCreateOfferSubmitResult.transactionResult().hash(),
        NfTokenCreateOffer.class
      )
    );
    logger.info("NFT Create Offer (Sell) transaction was validated successfully.");

    this.scanForResult(
      () -> this.getValidatedAccountObjects(wallet.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet.publicKey().deriveAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");

    NftSellOffersResult nftSellOffersResult = xrplClient.nftSellOffers(NftSellOffersRequestParams.builder()
      .nfTokenId(tokenId)
      .build());

    // cancel the created offer
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .addTokenOffers(nftSellOffersResult.offers().get(0).nftOfferIndex())
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(wallet.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenCancelOffer> signedCancel = signatureService.sign(
      wallet.privateKey(),
      nfTokenCancelOffer
    );
    SubmitResult<NfTokenCancelOffer> nfTokenCancelOfferSubmitResult = xrplClient.submit(signedCancel);
    assertThat(nfTokenCancelOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenCancelOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenCancelOfferSubmitResult.transactionResult().hash());

    //verify the offer was created
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenCancelOfferSubmitResult.transactionResult().hash(),
        NfTokenCreateOffer.class
      )
    );
    logger.info("NFT Cancel Offer transaction was validated successfully.");

    this.scanForResult(
      () -> this.getValidatedAccountObjects(wallet.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .noneMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass())
        )
    );
    logger.info("NFTokenOffer object was deleted successfully.");
  }

  @Test
  void acceptOfferDirectModeWithBrokerFee() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair wallet = createRandomAccountEd25519();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.publicKey().deriveAddress())
    );
    NfTokenUri uri = NfTokenUri.ofPlainText("ipfs://bafybeigdyrzt5sfp7udm7hu76uh7y26nf4dfuylqabf3oclgtqy55fbzdi");

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.publicKey().deriveAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey().base16Value())
      .sequence(accountInfoResult.accountData().sequence())
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .uri(uri)
      .build();

    SingleSignedTransaction<NfTokenMint> signedMint = signatureService.sign(wallet.privateKey(), nfTokenMint);
    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = xrplClient.accountNfts(wallet.publicKey().deriveAddress()).accountNfts().get(0).nfTokenId();

    // the owner creates the sell offer
    NfTokenCreateOffer nfTokenCreateSellOffer = NfTokenCreateOffer.builder()
      .account(wallet.publicKey().deriveAddress())
      .nfTokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .signingPublicKey(wallet.publicKey().base16Value())
      .flags(Flags.NfTokenCreateOfferFlags.SELL_NFTOKEN)
      .build();

    SingleSignedTransaction<NfTokenCreateOffer> signedOffer = signatureService.sign(
      wallet.privateKey(),
      nfTokenCreateSellOffer
    );
    SubmitResult<NfTokenCreateOffer> nfTokenCreateSellOfferSubmitResult = xrplClient.submit(signedOffer);
    assertThat(nfTokenCreateSellOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenCreateSellOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenCreateSellOfferSubmitResult.transactionResult().hash());

    //verify the offer was created
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenCreateSellOfferSubmitResult.transactionResult().hash(),
        NfTokenCreateOffer.class
      )
    );
    logger.info("NFT Create Offer (Sell) transaction was validated successfully.");

    this.scanForResult(
      () -> this.getValidatedAccountObjects(wallet.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet.publicKey().deriveAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");

    NftSellOffersResult nftSellOffersResult = xrplClient.nftSellOffers(NftSellOffersRequestParams.builder()
      .nfTokenId(tokenId)
      .build());
    assertThat(nftSellOffersResult.offers().size()).isEqualTo(1);

    // create buy offer from another account
    KeyPair wallet2 = createRandomAccountEd25519();

    AccountInfoResult accountInfoResult2 = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet2.publicKey().deriveAddress())
    );

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet2.publicKey().deriveAddress())
      .owner(wallet.publicKey().deriveAddress())
      .nfTokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult2.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .signingPublicKey(wallet2.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenCreateOffer> signedOffer2 = signatureService.sign(
      wallet2.privateKey(),
      nfTokenCreateOffer
    );
    SubmitResult<NfTokenCreateOffer> nfTokenCreateOfferSubmitResult = xrplClient.submit(signedOffer2);
    assertThat(nfTokenCreateOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenCreateOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenCreateOfferSubmitResult.transactionResult().hash());

    //verify the offer was created
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenCreateOfferSubmitResult.transactionResult().hash(),
        NfTokenCreateOffer.class
      )
    );
    logger.info("NFT Create Offer (Buy) transaction was validated successfully.");

    this.scanForResult(
      () -> this.getValidatedAccountObjects(wallet2.publicKey().deriveAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet2.publicKey().deriveAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");

    NftBuyOffersResult nftBuyOffersResult = xrplClient.nftBuyOffers(NftBuyOffersRequestParams.builder()
      .nfTokenId(tokenId)
      .build());
    assertThat(nftBuyOffersResult.offers().size()).isEqualTo(1);

    // accept offer from a different account
    KeyPair wallet3 = createRandomAccountEd25519();

    AccountInfoResult accountInfoResult3 = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet3.publicKey().deriveAddress())
    );
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(wallet3.publicKey().deriveAddress())
      .buyOffer(nftBuyOffersResult.offers().get(0).nftOfferIndex())
      .sellOffer(nftSellOffersResult.offers().get(0).nftOfferIndex())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult3.accountData().sequence())
      .signingPublicKey(wallet3.publicKey().base16Value())
      .build();

    SingleSignedTransaction<NfTokenAcceptOffer> signedAccept = signatureService.sign(
      wallet3.privateKey(),
      nfTokenAcceptOffer
    );
    SubmitResult<NfTokenAcceptOffer> nfTokenAcceptOfferSubmitResult = xrplClient.submit(signedAccept);
    assertThat(nfTokenAcceptOfferSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(nfTokenAcceptOfferSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(nfTokenAcceptOfferSubmitResult.transactionResult().hash());

    //verify the offer was accepted
    this.scanForResult(
      () -> this.getValidatedTransaction(
        nfTokenAcceptOfferSubmitResult.transactionResult().hash(),
        NfTokenAcceptOffer.class
      )
    );

    this.scanForResult(
      () -> {
        try {
          return xrplClient.accountNfts(wallet2.publicKey().deriveAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().stream()
        .anyMatch(nft -> nft.uri().get().equals(uri))
    );
    logger.info("NFT was transferred successfully.");
  }
}

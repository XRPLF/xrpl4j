package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
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
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.NftDevnetEnvironment;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;
import org.xrpl.xrpl4j.wallet.Wallet;

public class NfTokenIT extends AbstractIT {

  private static final XrplEnvironment nftDevnetEnvironment = new NftDevnetEnvironment();

  @Test
  void mint() throws JsonRpcClientErrorException {
    Wallet wallet = createRandomAccount();

    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    );
    int previousLength = this.getAccountNfts(wallet.classicAddress()).accountNfts().size();

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(accountInfoResult.accountData().sequence())
      .build();

    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient().submit(wallet, nfTokenMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return this.getAccountNfts(wallet.classicAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().size() == previousLength + 1
    );
    logger.info("NFT was minted successfully.");
  }

  @Test
  void mintAndBurn() throws JsonRpcClientErrorException {
    Wallet wallet = createRandomAccount();

    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    );

    // nft mint
    int previousLength = this.getAccountNfts(wallet.classicAddress()).accountNfts().size();

    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(accountInfoResult.accountData().sequence())
      .build();

    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient().submit(wallet, nfTokenMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return this.getAccountNfts(wallet.classicAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().size() == previousLength + 1
    );
    logger.info("NFT was minted successfully.");

    // nft burn
    AccountNftsResult accountNftsResult = this.getAccountNfts(wallet.classicAddress());

    int initialNftsCount = accountNftsResult.accountNfts().size();

    NfTokenId tokenId = accountNftsResult.accountNfts().get(0).tokenId();

    NfTokenBurn nfTokenBurn = NfTokenBurn.builder()
      .tokenId(tokenId)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .build();

    SubmitResult<NfTokenBurn> burnSubmitResult = xrplClient().submit(wallet, nfTokenBurn);
    assertThat(burnSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(burnSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(burnSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return this.getAccountNfts(wallet.classicAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().size() == initialNftsCount - 1
    );

    AccountNftsResult accountNftsResult1 = this.getAccountNfts(wallet.classicAddress());
    assertThat(
      accountNftsResult1.accountNfts().stream().noneMatch(
        object -> object.equals(NfTokenObject.builder().tokenId(tokenId).build())
      )
    ).isTrue();
    logger.info("NFT burned successfully.");
  }

  @Test
  void mintAndCreateOffer() throws JsonRpcClientErrorException {
    Wallet wallet = createRandomAccount();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    );
    int previousLength = this.getAccountNfts(wallet.classicAddress()).accountNfts().size();

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(accountInfoResult.accountData().sequence())
      .build();

    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient().submit(wallet, nfTokenMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return this.getAccountNfts(wallet.classicAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().size() == previousLength + 1
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = this.getAccountNfts(wallet.classicAddress()).accountNfts().get(0).tokenId();

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet.classicAddress())
      .tokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.NfTokenCreateOfferFlags.builder()
        .tfSellToken(true)
        .build())
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult nfTokenCreateOfferSubmitResult = xrplClient().submit(wallet, nfTokenCreateOffer);
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
      () -> this.getValidatedAccountObjects(wallet.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet.classicAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");
  }

  @Test
  void mintAndCreateThenAcceptOffer() throws JsonRpcClientErrorException {
    Wallet wallet = createRandomAccount();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    );
    int previousLength = this.getAccountNfts(wallet.classicAddress()).accountNfts().size();

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(accountInfoResult.accountData().sequence())
      .flags(Flags.NfTokenMintFlags.builder()
        .tfTransferable(true)
        .build())
      .build();

    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient().submit(wallet, nfTokenMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return this.getAccountNfts(wallet.classicAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().size() == previousLength + 1
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = this.getAccountNfts(wallet.classicAddress()).accountNfts().get(0).tokenId();

    // create buy offer from another account
    Wallet wallet2 = createRandomAccount();

    AccountInfoResult accountInfoResult2 = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet2.classicAddress())
    );

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet2.classicAddress())
      .owner(wallet.classicAddress())
      .tokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult2.accountData().sequence())
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .signingPublicKey(wallet2.publicKey())
      .build();

    SubmitResult nfTokenCreateOfferSubmitResult = xrplClient().submit(wallet2, nfTokenCreateOffer);
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
      () -> this.getValidatedAccountObjects(wallet2.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet2.classicAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");

    NftBuyOffersResult nftBuyOffersResult = xrplClient().nftBuyOffers(NftBuyOffersRequestParams.builder()
      .tokenId(tokenId)
      .build());
    assertThat(nftBuyOffersResult.offers().size()).isEqualTo(1);

    // accept offer from a different account
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(wallet.classicAddress())
      .buyOffer(nftBuyOffersResult.offers().get(0).nftOfferIndex())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult nfTokenAcceptOfferSubmitResult = xrplClient().submit(wallet, nfTokenAcceptOffer);
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

    assertThat(this.getAccountNfts(wallet.classicAddress()).accountNfts().size()).isEqualTo(0);
    assertThat(this.getAccountNfts(wallet2.classicAddress()).accountNfts().size()).isEqualTo(1);

    logger.info("The NFT ownership was transferred.");
  }

  @Test
  void mintAndCreateOfferThenCancelOffer() throws JsonRpcClientErrorException {
    Wallet wallet = createRandomAccount();

    //mint NFT from one account
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    );
    int previousLength = this.getAccountNfts(wallet.classicAddress()).accountNfts().size();

    //Nft mint transaction
    NfTokenMint nfTokenMint = NfTokenMint.builder()
      .tokenTaxon(UnsignedLong.ONE)
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .signingPublicKey(wallet.publicKey())
      .sequence(accountInfoResult.accountData().sequence())
      .build();

    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient().submit(wallet, nfTokenMint);
    assertThat(mintSubmitResult.result()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(mintSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(mintSubmitResult.transactionResult().hash());

    this.scanForResult(
      () -> {
        try {
          return this.getAccountNfts(wallet.classicAddress());
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> result.accountNfts().size() == previousLength + 1
    );
    logger.info("NFT was minted successfully.");

    //create a sell offer for the NFT that was created above

    NfTokenId tokenId = this.getAccountNfts(wallet.classicAddress()).accountNfts().get(0).tokenId();

    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(wallet.classicAddress())
      .tokenId(tokenId)
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .amount(XrpCurrencyAmount.ofDrops(1000))
      .flags(Flags.NfTokenCreateOfferFlags.builder()
        .tfSellToken(true)
        .build())
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult nfTokenCreateOfferSubmitResult = xrplClient().submit(wallet, nfTokenCreateOffer);
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
      () -> this.getValidatedAccountObjects(wallet.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .anyMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass()) &&
            ((NfTokenOfferObject) object).owner().equals(wallet.classicAddress())
        )
    );
    logger.info("NFTokenOffer object was found in account's objects.");

    NftSellOffersResult nftSellOffersResult = xrplClient().nftSellOffers(NftSellOffersRequestParams.builder()
      .tokenId(tokenId)
      .build());

    // cancel the created offer
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .addTokenOffers(nftSellOffersResult.offers().get(0).nftOfferIndex())
      .account(wallet.classicAddress())
      .fee(XrpCurrencyAmount.ofDrops(50))
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult nfTokenCancelOfferSubmitResult = xrplClient().submit(wallet, nfTokenCancelOffer);
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
      () -> this.getValidatedAccountObjects(wallet.classicAddress()),
      objectsResult -> objectsResult.accountObjects().stream()
        .noneMatch(object ->
          NfTokenOfferObject.class.isAssignableFrom(object.getClass())
        )
    );
    logger.info("NFTokenOffer object was deleted successfully.");
  }

  @Override
  protected XrplEnvironment xrplEnvironment() {
    return nftDevnetEnvironment;
  }
}

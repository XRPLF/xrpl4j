package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.NfTokenCreateOffer;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

public class NfTokenCreateOfferIT extends AbstractIT {
  
  @Test
  void mintAndCreateOffer() throws JsonRpcClientErrorException {
    assumeTrue(System.getProperty("useNftDevnet") != null);

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

    SubmitResult<NfTokenMint> mintSubmitResult = xrplClient.submit(wallet, nfTokenMint);
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

    SubmitResult nfTokenCreateOfferSubmitResult = xrplClient.submit(wallet, nfTokenCreateOffer);
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
}

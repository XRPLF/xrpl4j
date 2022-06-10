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
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.NfTokenBurn;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

public class NfTokenBurnIT extends AbstractIT {

  @Test
  void mintAndBurn() throws JsonRpcClientErrorException {
    assumeTrue(System.getProperty("useNftDevnet") != null);

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

    SubmitResult<NfTokenBurn> burnSubmitResult = xrplClient.submit(wallet, nfTokenBurn);
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

}

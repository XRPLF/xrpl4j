package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

public class NfTokenMintIT extends AbstractIT {

  @Test
  void mint() throws JsonRpcClientErrorException {
    assumeTrue(System.getProperty("useNftDevnet") != null);

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
  }
}

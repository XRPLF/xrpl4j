package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.XrplMethods;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountNftsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountNftsResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenMint;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

public class NftIT extends AbstractIT {
  private final XrplClient xrplClient = new XrplClient(HttpUrl.parse("http://xls20-sandbox.rippletest.net:51234"));

  private final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

//  Wallet wallet1 = walletFactory.fromSeed("shPVCWYEbwa7jnkiQ28HV6fm5uLfs", true);
  Wallet wallet1 = walletFactory.fromSeed("ssRKx55ibwH4WTmww7AH5daez15K5", true);

  AccountInfoResult accountInfoResult1 = this.scanForResult(
      () -> this.getValidatedAccountInfo(Address.of(
          "rMp1yPjPWJdiiGvdFFbeFTeodJv17uFwgx"
      ))
  );
//  AccountInfoResult accountInfoResult2 = this.scanForResult(
//      () -> this.getValidatedAccountInfo(wallet2.classicAddress())
//  );
  @Test
  public void nftServerTest() throws JsonRpcClientErrorException {
    System.out.println(xrplClient.serverInfo());
  }


  @Test
  public void mintToken() throws JsonRpcClientErrorException {

    FeeResult feeResult = xrplClient.fee();
    System.out.println(feeResult);

    NfTokenMint nftMint = NfTokenMint.builder()
        .account(wallet1.classicAddress())
        .fee(feeResult.drops().openLedgerFee())
        .sequence(accountInfoResult1.accountData().sequence())
        .tokenTaxon(Long.parseLong("2147483650"))
        .signingPublicKey(wallet1.publicKey())
        .build();

    SubmitResult<NfTokenMint> response = xrplClient.submit(wallet1, nftMint);
    System.out.println(response);
  }

  /**
   * To check if the NFT was actually minted by the account, we can check
   * the list of NFTs in their account by making account_nfts request.
   */
  @Test
  public void accountTokens() throws JsonRpcClientErrorException {

    AccountNftsResult nfts = xrplClient.accountNfts(
        AccountNftsRequestParams.builder()
            .account(wallet1.classicAddress())
            .build()
    );

      System.out.println(nfts);
  }

}

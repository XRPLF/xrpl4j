package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags.AccountRootFlags;
import com.ripple.xrpl4j.model.transactions.ImmutableAccountSet;
import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.wallet.WalletFactory;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.faucet.FundAccountRequest;
import com.ripple.xrplj4.client.model.accounts.AccountInfoRequestParam;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResponse;
import com.ripple.xrplj4.client.model.fees.FeeInfoResponse;
import com.ripple.xrplj4.client.model.transactions.SubmitAccountSetResponse;
import com.ripple.xrplj4.client.rippled.ImmutableJsonRpcRequest;
import com.ripple.xrplj4.client.rippled.JsonRpcRequest;
import com.ripple.xrplj4.client.rippled.RippledClient;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import com.ripple.xrplj4.client.rippled.TransactionBlobWrapper;
import com.ripple.xrplj4.client.rippled.XrplMethods;
import okhttp3.HttpUrl;
import org.immutables.value.Value;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An integration test that submits an AccountSet transaction for each AccountSet flag for an account, validates each
 * one is applied, and then clears each flag and ensure the clearing operation is validated in the ledger.
 *
 * @see "https://xrpl.org/accountset.html"
 */
public class AccountSetIT {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public final FaucetClient faucetClient =
      FaucetClient.construct(HttpUrl.parse("https://faucet.altnet.rippletest.net"));

  public final SimpleAccountSetClient client = new SimpleAccountSetClient();

  public final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  // TODO: Make an IT that sets all flags, and unsets only 1, and validate that only that 1 single flag was cleared.


  @Test
  public void disableAndEnableAllFlags() {

    ///////////////////////
    // Create the account
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    final Wallet wallet = seedResult.wallet();
    logger.info("Generated source testnet wallet with address {}", wallet.xAddress());

    ///////////////////////
    // Fund the account
    FaucetAccountResponse fundResponse = faucetClient.fundAccount(FundAccountRequest.of(wallet.classicAddress()));
    logger.info("Source account has been funded: {}", fundResponse);
    assertThat(fundResponse.amount()).isGreaterThan(0);

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResponse accountInfo = scanLedgerFor30Seconds(() -> this.getValidatedAccountInfo(wallet));
    assertThat(accountInfo.status()).isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    //////////////////////
    // Set asfAccountTxnID (no corresponding ledger flag)
    ImmutableAccountSet.Builder accountSetBuilder = AccountSet.builder()
        .account(Address.of(wallet.classicAddress()))
        .setFlag(AccountSetFlag.ACCOUNT_TXN_ID);
    AccountSetResponse response = client.submit(wallet, accountSetBuilder);
    logger.info(
        "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + response.transactionHash()
            .orElse("n/a")
    );
    assertThat(response.engineResult()).isNotEmpty();
    assertThat(response.engineResult().get()).isEqualTo("tesSUCCESS")
        .withFailMessage("EngineResult was not as expected.");

    ///////////////////////
    // Set flags one-by-one
    assertSetFlag(wallet, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
    assertSetFlag(wallet, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    assertSetFlag(wallet, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    assertSetFlag(wallet, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    assertSetFlag(wallet, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    assertSetFlag(wallet, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);

    assertClearFlag(wallet, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    assertClearFlag(wallet, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    assertClearFlag(wallet, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    assertClearFlag(wallet, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    assertClearFlag(wallet, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    assertClearFlag(wallet, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
  }

  //////////////////////
  // Test Helpers
  //////////////////////

  /**
   * Scan the ledger for the requested account until the requested flags show up in a validated version of the account.
   * If the requested form of the account doesn't show up after 30 seconds, throw an exception.
   *
   * @return
   */
  private Optional<Boolean> validatedAccountHasFlags(final Wallet wallet, final AccountRootFlags... flags) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(flags);

    // If all flags are not present, return Optional.empty so the scanner will keep trying.
    Optional<Boolean> hasAllRequiredFlags = this.scanLedgerFor30Seconds(() -> {
      // If the accountInfo has all the requested flags, return true. Otherwise return false.
      return this.getValidatedAccountInfo(wallet)
          .filter(accountInfoResponse -> accountInfoResponse.validated())
          .map(accountInfoResponse -> {
            logger.info("AccountInfoResponse Flags: {}", accountInfoResponse.accountData().flags());
            // If the accountInfo has all the requested flags, return true. Otherwise return false.
            boolean allFlagsPresent = Arrays.stream(flags)
                .allMatch(flag -> accountInfoResponse.accountData().flags().isSet(flag));
            return allFlagsPresent ? Optional.of(true) : Optional.empty();
          });
    });
    return hasAllRequiredFlags;
  }

  /**
   * Scan the ledger for the requested account until the requested flags show up in a validated version of the account.
   * If the requested form of the account doesn't show up after 30 seconds, throw an exception.
   *
   * @return
   */
  private Optional<Boolean> validatedAccountHasNotFlags(final Wallet wallet, final AccountRootFlags... flags) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(flags);

    // If all flags are not present, return Optional.empty so the scanner will keep trying.
    Optional<Boolean> hasAllRequiredFlags = this.scanLedgerFor30Seconds(() -> {
      // If the accountInfo has all the requested flags, return true. Otherwise return false.
      return this.getValidatedAccountInfo(wallet)
          .filter(accountInfoResponse -> accountInfoResponse.validated())
          .map(accountInfoResponse -> {
            logger.info("AccountInfoResponse Flags: {}", accountInfoResponse.accountData().flags());
            // If the accountInfo has all the requested flags, return true. Otherwise return false.
            boolean noFlagsPresent = Arrays.stream(flags)
                .allMatch(flag -> !accountInfoResponse.accountData().flags().isSet(flag));
            return noFlagsPresent ? Optional.of(true) : Optional.empty();
          });
    });
    return hasAllRequiredFlags;
  }

  //////////////////////
  // Ledger Helpers
  //////////////////////

  /**
   * Get the requested account from the most recently validated ledger, if the account exists.
   */
  private Optional<AccountInfoResponse> getValidatedAccountInfo(final Wallet wallet) {
    Objects.requireNonNull(wallet);
    try {
      return Optional.ofNullable(client.getAccountInfo(wallet.classicAddress(), "validated"))
          .filter(accountInfoResponse -> accountInfoResponse.validated());
    } catch (Exception | RippledClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Scan the ledger by calling the {@code supplier} until a value becomes present. If a value never becomes present
   * after 30 seconds, then throw an exception.
   *
   * @param supplier
   * @param <T>
   * @return
   */
  private <T> T scanLedgerFor30Seconds(final Supplier<Optional<T>> supplier) {
    Objects.requireNonNull(supplier);
    for (int i = 0; i < 30; i++) {
      try {
        Optional<T> value = supplier.get();
        if (value.isPresent()) {
          return value.get();
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            throw new RuntimeException(e1.getMessage(), e1);
          }
        }
      } catch (Exception e) {
        // The rippleclient throws an exception if an account is not found.
        logger.warn(e.getMessage());
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          throw new RuntimeException(e1.getMessage(), e1);
        }
      }
    }

    throw new RuntimeException("Unable to obtain value from XRPL before 30s timeout.");
  }

  private void assertSetFlag(
      final Wallet wallet, final AccountSetFlag accountSetFlag, final AccountRootFlags accountRootFlag
  ) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(accountSetFlag);

    ImmutableAccountSet.Builder accountSetBuilder = AccountSet.builder()
        .account(Address.of(wallet.classicAddress()))
        .setFlag(accountSetFlag);
    AccountSetResponse response = client.submit(wallet, accountSetBuilder);
    logger.info(
        "AccountSet SetFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
        accountSetFlag, accountRootFlag, response.transactionHash().orElse("n/a")
    );
    assertThat(response.engineResult()).isNotEmpty();
    assertThat(response.engineResult().get()).isEqualTo("tesSUCCESS")
        .withFailMessage("EngineResult was not as expected.");

    /////////////////////////
    // Validate Account State
    // Returns true, or else throws...
    boolean result = scanLedgerFor30Seconds(
        () -> validatedAccountHasFlags(wallet, accountRootFlag)
    );
    assertThat(result).isTrue();
  }

  private void assertClearFlag(
      final Wallet wallet, final AccountSetFlag accountSetFlag, final AccountRootFlags accountRootFlag
  ) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(accountSetFlag);

    ImmutableAccountSet.Builder accountSetBuilder = AccountSet.builder()
        .account(Address.of(wallet.classicAddress()))
        .clearFlag(accountSetFlag);
    AccountSetResponse response = client.submit(wallet, accountSetBuilder);
    logger.info(
        "AccountSet ClearFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
        accountSetFlag, accountRootFlag, response.transactionHash().orElse("n/a")
    );
    assertThat(response.engineResult()).isNotEmpty();
    assertThat(response.engineResult().get()).isEqualTo("tesSUCCESS")
        .withFailMessage("EngineResult was not as expected.");

    /////////////////////////
    // Validate Account State
    // Returns true, or else throws...
    boolean result = scanLedgerFor30Seconds(
        () -> validatedAccountHasNotFlags(wallet, accountRootFlag)
    );
    assertThat(result).isTrue();
  }

  /**
   * A simple client for helping to submit an AccountSet transaction.
   *
   * @deprecated This client will go away once the websocket testing client is implemented by nkramer.
   */
  @Deprecated
  static class SimpleAccountSetClient {

    private ObjectMapper objectMapper = ObjectMapperFactory.create();

    private XrplBinaryCodec binaryCodec = new XrplBinaryCodec();

    private RippledClient rippledClient =
        RippledClient.construct(HttpUrl.parse("https://s.altnet.rippletest.net:51234"));

    private KeyPairService keyPairService = DefaultKeyPairService.getInstance();

    public AccountSetResponse submit(Wallet wallet, ImmutableAccountSet.Builder unsignedAccountSetRequestBuilder) {
      Objects.requireNonNull(wallet);
      Objects.requireNonNull(unsignedAccountSetRequestBuilder);

      try {
        String trx = accountSetRequest(wallet, unsignedAccountSetRequestBuilder);

        JsonRpcRequest submitRequest = JsonRpcRequest.builder()
            .method(XrplMethods.SUBMIT)
            .addParams(TransactionBlobWrapper.of(trx))
            .build();

        SubmitAccountSetResponse response = rippledClient.sendRequest(submitRequest, SubmitAccountSetResponse.class);
        if (response.accepted() && response.engineResult().equals("tesSUCCESS")) {
          return AccountSetResponse.builder()
              .engineResult(response.engineResult())
              .transactionHash(response.txJson().hash().get())
              .build();
        }
        return AccountSetResponse.builder().engineResult(response.engineResult()).build();
      } catch (JsonProcessingException e) {
        throw new IllegalStateException("Houston, we have a bug", e);
      } catch (RippledClientErrorException e) {
        return AccountSetResponse.builder().error(e.getMessage()).build();
      }
    }

    private FeeInfoResponse getFeeInfo() throws RippledClientErrorException {
      ImmutableJsonRpcRequest request = JsonRpcRequest.builder()
          .method(XrplMethods.FEE)
          .build();
      return rippledClient.sendRequest(request, FeeInfoResponse.class);
    }

    private UnsignedInteger getAccountSequence(String account) throws RippledClientErrorException {
      JsonRpcRequest request = JsonRpcRequest.builder()
          .method(XrplMethods.ACCOUNT_INFO)
          .addParams(AccountInfoRequestParam.of(account))
          .build();
      return rippledClient.sendRequest(request, AccountInfoResponse.class).accountData().sequence();
    }

    private AccountInfoResponse getAccountInfo(String account, String ledger_index)
        throws RippledClientErrorException {
      JsonRpcRequest request = JsonRpcRequest.builder()
          .method(XrplMethods.ACCOUNT_INFO)
          .addParams(AccountInfoRequestParam.builder().account(account).ledger_index(ledger_index).build())
          .build();
      return rippledClient.sendRequest(request, AccountInfoResponse.class);
    }

    private String accountSetRequest(Wallet wallet, ImmutableAccountSet.Builder unsignedAccountSetRequestBuilder)
        throws JsonProcessingException, RippledClientErrorException {
      Objects.requireNonNull(wallet);
      Objects.requireNonNull(unsignedAccountSetRequestBuilder);

      FeeInfoResponse feeInfo = getFeeInfo();
      UnsignedInteger accountSequence = getAccountSequence(wallet.classicAddress());

      AccountSet unsignedAccountSet = unsignedAccountSetRequestBuilder
          .sequence(accountSequence)
          .fee(feeInfo.drops().minimumFee())
          .signingPublicKey(wallet.publicKey())
          .build();

      return signNewAccountSet(wallet, unsignedAccountSet);
    }

    private String signNewAccountSet(final Wallet wallet, final AccountSet unsignedAccountSet)
        throws JsonProcessingException {
      Objects.requireNonNull(wallet);
      Objects.requireNonNull(unsignedAccountSet);

      String unsignedAccountSetJson = objectMapper.writeValueAsString(unsignedAccountSet);

      String unsignedBinaryHex = binaryCodec.encodeForSigning(unsignedAccountSetJson);

      String signature = keyPairService.sign(unsignedBinaryHex, wallet.privateKey().get());

      AccountSet signed = AccountSet.builder()
          .from(unsignedAccountSet)
          .transactionSignature(signature)
          .build();

      String signedAccountSetJson = objectMapper.writeValueAsString(signed);

      return binaryCodec.encode(signedAccountSetJson);
    }
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAccountSetResponse.class)
  @JsonDeserialize(as = ImmutableAccountSetResponse.class)
  public interface AccountSetResponse {

    static ImmutableAccountSetResponse.Builder builder() {
      return ImmutableAccountSetResponse.builder();
    }

    Optional<String> engineResult();

    Optional<String> transactionHash();

    Optional<String> error();

  }
}

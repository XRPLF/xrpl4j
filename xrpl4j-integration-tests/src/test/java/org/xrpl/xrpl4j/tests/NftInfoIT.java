package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.nft.NftInfoRequestParams;
import org.xrpl.xrpl4j.model.client.nft.NftInfoResult;
import org.xrpl.xrpl4j.model.flags.NfTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransferFee;
import org.xrpl.xrpl4j.tests.environment.ClioMainnetEnvironment;
import org.xrpl.xrpl4j.tests.environment.ReportingMainnetEnvironment;

import java.math.BigDecimal;

public class NftInfoIT {

  XrplClient xrplClient = new ClioMainnetEnvironment().getXrplClient();

  @Test
  void getNftInfo() throws JsonRpcClientErrorException {
    NftInfoRequestParams params = NftInfoRequestParams.builder()
      .nfTokenId(NfTokenId.of("0008138808C4E53F4F6EF5D5B2AF64F96B457F42E0ED9530FE9B131300001178"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();
    NftInfoResult nftInfo = xrplClient.nftInfo(
      params
    );

    assertThat(nftInfo.nftId()).isEqualTo(params.nfTokenId());
    assertThat(nftInfo.owner()).isEqualTo(Address.of("rLpunkscgfzS8so59bUCJBVqZ3eHZue64r"));
    assertThat(nftInfo.burned()).isFalse();
    assertThat(nftInfo.flags()).isEqualTo(NfTokenFlags.TRANSFERABLE);
    assertThat(nftInfo.transferFee()).isEqualTo(TransferFee.ofPercent(BigDecimal.valueOf(5)));
    assertThat(nftInfo.issuer()).isEqualTo(Address.of("ro4HnG6G1Adz2cWSnZ3Dcr39kmXk4ztA5"));
    assertThat(nftInfo.nftTaxon()).isEqualTo(UnsignedLong.ZERO);
    assertThat(nftInfo.nftSerial()).isEqualTo(UnsignedInteger.valueOf(4472));
    assertThat(nftInfo.uri()).isNotEmpty().get()
      .isEqualTo(NfTokenUri.of("68747470733A2F2F62616679626569656E7662786B756F6C6B3778336333366177686A34346E6F6" +
        "F687776613370683568376B746A78616D686D6F63333265733632712E697066732E6E667473746F726167652E6C696E6B2F7" +
        "26567756C61725F626972645F6E6F5F323633372E6A7067"));
  }

  @Test
  void getNftInfoFromReportingModeThrows() throws JsonRpcClientErrorException {
    XrplClient client = new ReportingMainnetEnvironment().getXrplClient();
    NftInfoRequestParams params = NftInfoRequestParams.builder()
      .nfTokenId(NfTokenId.of("0008138808C4E53F4F6EF5D5B2AF64F96B457F42E0ED9530FE9B131300001178"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();
    assertThatThrownBy(
      () -> client.nftInfo(params)
    ).isInstanceOf(JsonRpcClientErrorException.class)
      .hasMessage("unknownCmd (Unknown method.)");

  }
}

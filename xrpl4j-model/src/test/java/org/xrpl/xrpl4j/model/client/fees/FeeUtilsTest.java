package org.xrpl.xrpl4j.model.client.fees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.calculateFeeDynamically;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeMultiSigFee;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.SignerEntry;
import org.xrpl.xrpl4j.model.ledger.SignerEntryWrapper;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class FeeUtilsTest {

  @Test
  public void nullInputForComputeMultiSigFee() {
    assertThrows(
      NullPointerException.class,
      () -> computeMultiSigFee(null, null)
    );

    assertThrows(
      NullPointerException.class,
      () -> computeMultiSigFee(XrpCurrencyAmount.ofDrops(20), null)
    );
  }

  @Test
  public void simpleComputeMultiSigFee() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(20);
    SignerListObject object = SignerListObject.builder()
      .flags(Flags.SignerListFlags.UNSET)
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("5904C0DC72C58A83AEFED2FFC5386356AA83FCA6A88C89D00646E51E687CDBE4"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(16061435))
      .addSignerEntries(
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
            .signerWeight(UnsignedInteger.valueOf(2))
            .build()
        ),
        SignerEntryWrapper.of(
          SignerEntry.builder()
            .account(Address.of("raKEEVSGnKSD9Zyvxu4z6Pqpm4ABH8FS6n"))
            .signerWeight(UnsignedInteger.valueOf(1))
            .build()
        )
      )
      .signerListId(UnsignedInteger.ZERO)
      .signerQuorum(UnsignedInteger.valueOf(3))
      .index(Hash256.of("A9C28A28B85CD533217F5C0A0C7767666B093FA58A0F2D80026FCC4CD932DDC7"))
      .build();

    assertThat(computeMultiSigFee(xrpCurrencyAmount, object)).isEqualTo(XrpCurrencyAmount.ofDrops(60));
  }

  @Test
  public void nullInputForCalculateFeeDynamically() {
    assertThrows(
      NullPointerException.class,
      () -> calculateFeeDynamically(null)
    );
  }

  @Test
  public void calculateFeeDynamicallyForAlmostEmptyQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(1))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();

    assertThat(calculateFeeDynamically(feeResult)).isEqualTo(XrpCurrencyAmount.ofDrops(5005));
  }

  @Test
  public void calculateFeeDynamicallyForModeratelyFilledQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(220))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();

    assertThat(calculateFeeDynamically(feeResult)).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
  }

  @Test
  public void calculateFeeDynamicallyForLessThanModerateTraffic() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(100))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(10000))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2653937))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(1100))
      .status("success")
      .build();

    assertThat(calculateFeeDynamically(feeResult)).isEqualTo(XrpCurrencyAmount.ofDrops(5005));
  }

  @Test
  public void calculateFeeDynamicallyForCompletelyFilledQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(110))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(100))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2657))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(110))
      .status("success")
      .build();

    assertThat(calculateFeeDynamically(feeResult)).isEqualTo(XrpCurrencyAmount.ofDrops(2923));
  }

  @Test
  public void calculateFeeDynamicallyForEmptyQueue() {
    FeeResult feeResult = FeeResult.builder()
      .currentLedgerSize(UnsignedInteger.valueOf(56))
      .currentQueueSize(UnsignedInteger.valueOf(0))
      .drops(
        FeeDrops.builder()
          .baseFee(XrpCurrencyAmount.ofDrops(10))
          .medianFee(XrpCurrencyAmount.ofDrops(100))
          .minimumFee(XrpCurrencyAmount.ofDrops(10))
          .openLedgerFee(XrpCurrencyAmount.ofDrops(2657))
          .build()
      )
      .expectedLedgerSize(UnsignedInteger.valueOf(55))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(26575101)))
      .levels(
        FeeLevels.builder()
          .medianLevel(XrpCurrencyAmount.ofDrops(256000))
          .minimumLevel(XrpCurrencyAmount.ofDrops(256))
          .openLedgerLevel(XrpCurrencyAmount.ofDrops(67940792))
          .referenceLevel(XrpCurrencyAmount.ofDrops(256))
          .build()
      )
      .maxQueueSize(UnsignedInteger.valueOf(110))
      .status("success")
      .build();

    assertThat(calculateFeeDynamically(feeResult)).isEqualTo(XrpCurrencyAmount.ofDrops(15));
  }
}

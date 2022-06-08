package org.xrpl.xrpl4j.model.client.fees;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.ledger.SignerListObject;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Objects;

public class FeeUtils {

  /**
   * Computes the fee necessary for a multisigned transaction.
   *
   * <p>The transaction cost of a multisigned transaction must be at least {@code (N + 1) * (the normal
   * transaction cost)}, where {@code N} is the number of signatures provided.
   *
   * @param currentLedgerFeeDrops The current ledger fee, represented as an {@link XrpCurrencyAmount}.
   * @param signerList            The {@link SignerListObject} containing the signers of the transaction.
   *
   * @return An {@link XrpCurrencyAmount} representing the multisig fee.
   */
  public static XrpCurrencyAmount computeMultiSigFee(
    final XrpCurrencyAmount currentLedgerFeeDrops,
    final SignerListObject signerList
  ) {
    Objects.requireNonNull(currentLedgerFeeDrops);
    Objects.requireNonNull(signerList);

    return currentLedgerFeeDrops
      .times(XrpCurrencyAmount.of(UnsignedLong.valueOf(signerList.signerEntries().size() + 1)));
  }


  /**
   * Get value of fee to be used for submitting a transaction on the ledger. The value is calculated depending on the
   * load on the job queue.
   *
   * @param feeResult {@link FeeResult} object received from XrplClient#fee() rippled call.
   *
   * @return {@link XrpCurrencyAmount} value of the fee that should be used for the transaction.
   */
  public static XrpCurrencyAmount calculateFeeDynamically(final FeeResult feeResult) {
    Objects.requireNonNull(feeResult);

    double currentQueueSize = feeResult.currentQueueSize().doubleValue();
    double maxQueueSize = feeResult.maxQueueSize().get().doubleValue();
    double queuePercentage = currentQueueSize / maxQueueSize;
    FeeDrops drops = feeResult.drops();
    int minimumFee = drops.minimumFee().value().intValue();
    int medianFee = drops.medianFee().value().intValue();
    int openLedgerFee = drops.openLedgerFee().value().intValue();

    // calculate the lowest fee the user is able to pay if the queue is empty
    final long feeLow = Math.round(
      Math.min(
        Math.max(
          minimumFee * 1.5,
          Math.round(Math.max(medianFee, openLedgerFee) / 500)
        ),
        1000
      )
    );

    long possibleFeeMedium;
    if (queuePercentage > 0.1) {
      possibleFeeMedium = Math.round((minimumFee + openLedgerFee) / 3);
    } else if (queuePercentage == 0) {
      possibleFeeMedium = Math.max(10 * minimumFee, openLedgerFee);
    } else {
      possibleFeeMedium = Math.max(10 * minimumFee, Math.round((minimumFee + medianFee) / 2));
    }

    // calculate the lowest fee the user is able to pay if there are txns in the queue
    final long feeMedium = Math.round(Math.min(possibleFeeMedium, Math.min((int) (feeLow * 15), 10000)));

    // calculate the lowest fee the user is able to pay if the txn queue is full
    final long feeHigh = Math.round(Math.min(
      Math.max(10 * minimumFee, Math.round(Math.max(medianFee, openLedgerFee) * 1.1)),
      100000
    ));

    XrpCurrencyAmount fee;
    if (queuePercentage == 0) {  // if queue is empty
      fee = XrpCurrencyAmount.ofDrops(feeLow);
    } else if (0 < queuePercentage && queuePercentage < 1) {  // queue has txns in it but is not full
      fee = XrpCurrencyAmount.ofDrops(feeMedium);
    } else {  // if queue is full
      fee = XrpCurrencyAmount.ofDrops(feeHigh);
    }

    return fee;
  }

}

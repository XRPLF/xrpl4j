package org.xrpl.xrpl4j.model;

import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;

/**
 * Utilities for operating on {@link Transaction} objects.
 */
// TODO: DELETE ME!
public class TransactionUtils {

  public static <T extends Transaction> T withSigners(T unsignedTransaction, Set<Signer> signatures) {
    return null; // Is this possible?
  }

  // TODO: Create a new IT that is overtly called "MultisigngedPaymentIT" so it's easier to find and also see how the
  //  API is.
  // i.e., Given a set of SignatureWithPublicKey (or whatever) construct a MultisignTransaction that can be submitted
  // to the
  // the ledger (is this obvious?)

  // Thought 2
  ///////////

  // For a multisigned transaction with a quorum of signatures.
  //  computeHash(Transaction t) --> check for signers vs signature
  //  computeMultiSignHash(Transaction t, int minSigners)
  //  computeSingleSignHash(Transaction t)

  ///////////
  // Thought 5
  ///////////

  // MultisignedTransaction assembleSignedTransaction(T transaction, Set<Signer> signers)

}

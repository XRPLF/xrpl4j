package org.xrpl.xrpl4j.model;

import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Set;

/**
 * Utilities for operating on {@link Transaction} objects.
 */
public class TransactionUtils {

  public static <T extends Transaction> T withSigners(T unsignedTransaction, Set<Signer> signatures) {
    return null; // Is this possible?
  }

  // TODO: Reproduce all ITs with new crypto.

  // TODO: see SignerListSetIT and fix line 135 (e.g., move to a factory or helper method on Signers).
  // TODO: Nice to have is hash computation on SignedTransaction (i.e., probably MultiSignedTransaction -- requires a
  //  quorum, unsigned tx, and assembles it for you).
  // See DefaultWorkItemEventHandler (new impl in nk/submit-its).

  // TODO: constructMultisignedTransaction(signers, unsignedTX)

  ///////////
  // Thought 1
  ///////////
  // Consider moving SignatureUtils.addSignatureToTransaction to here?
  // 1. Add signature to transaction
  // 2. Add fee to transaction
  // 3. Add signers to transaction?

  // Alt Idea: TransactionBuilder that has field-setters for the above values and all Transaction fields.

  // TransactionBuild.from(payment) // <-- This
  // .fee(fee)
  // .signers(...)
  // signature(...)
  // .build();

  ///////////
  // Thought 2
  ///////////

  // For a multisigned transaction with a quorum of signatures.
  //  computeHash(Transaction t) --> check for signers vs signature
  //  computeMultiSignHash(Transaction t, int minSigners)
  //  computeSingleSignHash(Transaction t)

  ///////////
  // Thought 3
  ///////////
  // Consider moving SignatureUtils functionality into here (only TX's are signed).

  ///////////
  // Thought 4
  ///////////
  // Given a set of SignatureWithPublicKey (or whatever) construct a MultisignTransaction that can be submitted to the
  // the ledger (so

  ///////////
  // Thought 5
  ///////////

  // MultisignedTransaction assembleSignedTransaction(T transaction, Set<Signer> signers)

}

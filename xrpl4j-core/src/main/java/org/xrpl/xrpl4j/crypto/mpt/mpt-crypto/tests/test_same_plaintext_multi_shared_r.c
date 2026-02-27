#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void)
{
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  // 1. Context Randomization
  unsigned char seed[32];
  random_bytes(seed);
  EXPECT(secp256k1_context_randomize(ctx, seed));

  printf("=== Running Test: Proof of Equality (Shared Randomness) ===\n");

  const int N_RECIPIENTS = 3;
  printf("Generating proof for %d recipients...\n", N_RECIPIENTS);

  // 2. Setup Variables
  uint64_t amount = 123456789;
  unsigned char r[32];
  unsigned char tx_context[32];

  random_scalar(ctx, r);
  random_scalar(ctx, tx_context);

  // 3. Generate Recipient Keys & Encrypt
  secp256k1_pubkey pks[3];
  secp256k1_pubkey C2s[3];
  secp256k1_pubkey C1;

  // Shared C1 = r*G
  EXPECT(secp256k1_ec_pubkey_create(ctx, &C1, r));

  for (int i = 0; i < N_RECIPIENTS; i++)
  {
    unsigned char sk[32];
    random_scalar(ctx, sk);
    EXPECT(secp256k1_ec_pubkey_create(ctx, &pks[i], sk));

    // Construct C2[i] = amount*G + r*PK[i]
    secp256k1_pubkey mG;
    unsigned char m_scalar[32] = {0};
    for (int b = 0; b < 8; b++)
      m_scalar[31 - b] = (amount >> (b * 8)) & 0xFF;

    EXPECT(secp256k1_ec_pubkey_create(ctx, &mG, m_scalar));

    secp256k1_pubkey rPK = pks[i];
    EXPECT(secp256k1_ec_pubkey_tweak_mul(ctx, &rPK, r));

    const secp256k1_pubkey *summands[2];
    summands[0] = &mG;
    summands[1] = &rPK;
    EXPECT(secp256k1_ec_pubkey_combine(ctx, &C2s[i], summands, 2));
  }

  // 4. Generate Proof
  size_t proof_len = secp256k1_mpt_proof_equality_shared_r_size(N_RECIPIENTS);
  unsigned char proof[proof_len];

  int res = secp256k1_mpt_prove_equality_shared_r(
      ctx, proof, amount, r, N_RECIPIENTS, &C1, C2s, pks, tx_context);
  EXPECT(res == 1);
  printf("Proof generated successfully.\n");

  // 5. Verify Proof (Positive Case)
  res = secp256k1_mpt_verify_equality_shared_r(ctx, proof, N_RECIPIENTS, &C1,
                                               C2s, pks, tx_context);
  EXPECT(res == 1);
  printf("Proof verified successfully.\n");

  /* ---------------------------------------------------------------- */
  /* 6. Negative Test: Tamper with Transaction Context                */
  /* ---------------------------------------------------------------- */
  printf("Verifying proof with wrong TxID (Expecting Failure)...\n");

  unsigned char tx_context_fake[32];
  memcpy(tx_context_fake, tx_context, 32);
  tx_context_fake[0] ^= 0xFF; // Corrupt first byte

  int res_fake_ctx = secp256k1_mpt_verify_equality_shared_r(
      ctx, proof, N_RECIPIENTS, &C1, C2s, pks, tx_context_fake);
  EXPECT(res_fake_ctx == 0);
  printf("Tamper detection (Context): OK.\n");

  /* ---------------------------------------------------------------- */
  /* 7. Negative Test: Tamper with Ciphertext (C1)                    */
  /* ---------------------------------------------------------------- */
  printf(
      "Verifying proof with tampered Ciphertext C1 (Expecting Failure)...\n");

  secp256k1_pubkey C1_fake = C1;
  // Tweak C1 by adding a small scalar to it, effectively changing the point
  unsigned char tweak[32] = {0};
  tweak[31] = 1;
  EXPECT(secp256k1_ec_pubkey_tweak_add(ctx, &C1_fake, tweak));

  int res_fake_c1 = secp256k1_mpt_verify_equality_shared_r(
      ctx, proof, N_RECIPIENTS,
      &C1_fake, // <--- Passing tampered C1
      C2s, pks, tx_context);
  EXPECT(res_fake_c1 == 0);
  printf("Tamper detection (Ciphertext): OK.\n");

  /* ---------------------------------------------------------------- */

  printf("Test passed!\n");
  secp256k1_context_destroy(ctx);
  return 0;
}

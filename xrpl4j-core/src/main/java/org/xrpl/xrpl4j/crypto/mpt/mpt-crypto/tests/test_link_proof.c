#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void test_link_proof()
{
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  unsigned char sk[32], r[32], rho[32], context_id[32];
  uint64_t amount = 12345;
  secp256k1_pubkey pk, c1, c2, pcm;
  unsigned char proof[195];

  printf("DEBUG: Starting Link Proof test with NUMS generators...\n");

  // 1. Setup Keys and Randomness
  random_scalar(ctx, sk);
  EXPECT(secp256k1_ec_pubkey_create(ctx, &pk, sk));

  random_scalar(ctx, r);
  random_scalar(ctx, rho);
  EXPECT(RAND_bytes(context_id, 32) == 1);

  // 2. Create ElGamal Ciphertext (C1, C2) manually for the test
  // C1 = r * G
  // Note: ec_pubkey_create computes scalar * G
  EXPECT(secp256k1_ec_pubkey_create(ctx, &c1, r));

  // C2 = m * G + r * Pk
  secp256k1_pubkey mG, rPk;

  // Convert amount to scalar (big endian)
  unsigned char m_scalar[32] = {0};
  for (int i = 0; i < 8; i++)
  {
    m_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
  }

  // mG = m * G
  EXPECT(secp256k1_ec_pubkey_create(ctx, &mG, m_scalar));

  // rPk = r * Pk
  rPk = pk; // Copy pk first
  EXPECT(secp256k1_ec_pubkey_tweak_mul(ctx, &rPk, r));

  // Combine: C2 = mG + rPk
  const secp256k1_pubkey *addends[] = {&mG, &rPk};
  EXPECT(secp256k1_ec_pubkey_combine(ctx, &c2, addends, 2));

  // 3. Create Pedersen Commitment (PCm)
  // Using the NEW centralized function that uses NUMS H internally
  // Note: The library function handles the commitment logic (m*G + rho*H)
  EXPECT(secp256k1_mpt_pedersen_commit(ctx, &pcm, amount, rho));

  // 4. Generate Proof
  // This proves that (C1, C2) and PCm commit to the same 'amount'
  EXPECT(secp256k1_elgamal_pedersen_link_prove(
             ctx, proof, &c1, &c2, &pk, &pcm, amount, r, rho, context_id) == 1);
  printf("SUCCESS: Link Proof generated.\n");

  // 5. Verify Proof
  EXPECT(secp256k1_elgamal_pedersen_link_verify(ctx, proof, &c1, &c2, &pk, &pcm,
                                                context_id) == 1);
  printf("SUCCESS: Link Proof verified against NUMS H.\n");

  // 6. Test Failure (Wrong Amount)
  // Create a commitment to (amount + 1) using the SAME blinding factor
  secp256k1_pubkey pcm_wrong;
  EXPECT(secp256k1_mpt_pedersen_commit(ctx, &pcm_wrong, amount + 1, rho));

  // Verification should fail because PCm doesn't match the amount in ElGamal
  // ciphertext
  EXPECT(secp256k1_elgamal_pedersen_link_verify(ctx, proof, &c1, &c2, &pk,
                                                &pcm_wrong, context_id) == 0);
  printf("SUCCESS: Invalid commitment correctly rejected.\n");

  secp256k1_context_destroy(ctx);
}

int main()
{
  test_link_proof();
  printf("ALL TESTS PASSED\n");
  return 0;
}

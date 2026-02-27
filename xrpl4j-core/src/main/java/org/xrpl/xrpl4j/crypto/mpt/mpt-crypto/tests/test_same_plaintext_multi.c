#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* --- Test Cases --- */

/**
 * Test 1: Generates N ciphertexts encrypting the SAME amount and verifies the
 * proof.
 */
static void test_valid_multi_proof(const secp256k1_context *ctx, size_t n)
{
  printf("Running test: same plaintext proof (N=%zu)... ", n);

  // C99 Variable Length Arrays (VLAs) for cleaner test code
  // Note: MSVC might not support VLAs, but standard GCC/Clang does.
  // If strict C90 compliance is needed, use malloc here.
  secp256k1_pubkey *Pk = malloc(n * sizeof(secp256k1_pubkey));
  secp256k1_pubkey *R = malloc(n * sizeof(secp256k1_pubkey));
  secp256k1_pubkey *S = malloc(n * sizeof(secp256k1_pubkey));
  unsigned char *r = malloc(n * 32); // Randomness scalars flattened

  EXPECT(Pk && R && S && r);

  unsigned char tx_id[32];
  uint64_t amount = 555666;
  unsigned char *proof;
  size_t proof_len;
  size_t i;

  // 1. Setup: Keys and Randomness
  random_scalar(ctx, tx_id);
  for (i = 0; i < n; ++i)
  {
    unsigned char priv[32];
    EXPECT(secp256k1_elgamal_generate_keypair(ctx, priv, &Pk[i]) == 1);
    random_scalar(ctx, &r[i * 32]);
  }

  // 2. Encrypt the SAME amount N times
  for (i = 0; i < n; ++i)
  {
    EXPECT(secp256k1_elgamal_encrypt(ctx, &R[i], &S[i], &Pk[i], amount,
                                     &r[i * 32]) == 1);
  }

  // 3. Generate Proof
  proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
  proof = (unsigned char *)malloc(proof_len);
  EXPECT(proof != NULL);

  size_t out_len = proof_len;
  EXPECT(secp256k1_mpt_prove_same_plaintext_multi(ctx, proof, &out_len, amount,
                                                  n, R, S, Pk, r, tx_id) == 1);
  EXPECT(out_len == proof_len);

  // 4. Verify Proof
  EXPECT(secp256k1_mpt_verify_same_plaintext_multi(ctx, proof, proof_len, n, R,
                                                   S, Pk, tx_id) == 1);

  free(proof);
  free(Pk);
  free(R);
  free(S);
  free(r);
  printf("Passed\n");
}

/**
 * Test 2: Mismatched amounts.
 * Encrypts DIFFERENT amounts, attempts to generate a proof for one, checks
 * verification fails.
 */
static void test_different_amounts_fail(const secp256k1_context *ctx)
{
  printf("Running test: different amounts (should fail)... ");

  size_t n = 2;
  secp256k1_pubkey Pk[2], R[2], S[2];
  unsigned char r[2][32];
  unsigned char tx_id[32];

  uint64_t amount_1 = 100;
  uint64_t amount_2 = 200; // Different!

  random_scalar(ctx, tx_id);
  for (int i = 0; i < 2; ++i)
  {
    unsigned char priv[32];
    EXPECT(secp256k1_elgamal_generate_keypair(ctx, priv, &Pk[i]) == 1);
    random_scalar(ctx, r[i]);
  }

  // Encrypt DIFFERENT amounts
  EXPECT(secp256k1_elgamal_encrypt(ctx, &R[0], &S[0], &Pk[0], amount_1, r[0]) ==
         1);
  EXPECT(secp256k1_elgamal_encrypt(ctx, &R[1], &S[1], &Pk[1], amount_2, r[1]) ==
         1);

  size_t proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
  unsigned char *proof = (unsigned char *)malloc(proof_len);
  EXPECT(proof != NULL);

  unsigned char r_flat[64];
  memcpy(&r_flat[0], r[0], 32);
  memcpy(&r_flat[32], r[1], 32);

  // PROVER: We try to prove they both equal amount_1.
  // The prover function will technically generate a valid proof math-wise
  // based on the inputs we GIVE it (amount_1, r1, r2),
  // but it won't match the actual C2 ciphertext because C2 uses amount_2.
  // However, depending on implementation, the prover might succeed (generating
  // a valid Sigma proof for the scalars provided), but the VERIFIER must catch
  // the discrepancy against the public keys/ciphertexts.
  size_t out_len = proof_len;
  // We assume the prover succeeds in generating *something* (it doesn't
  // validate ciphertexts, just scalars usually)
  int prove_res = secp256k1_mpt_prove_same_plaintext_multi(
      ctx, proof, &out_len, amount_1, n, R, S, Pk, r_flat, tx_id);
  // If prover is smart and checks consistency, it might fail here.
  // If it's just a Sigma protocol engine, it might succeed.
  // We only care that the SYSTEM fails eventually.

  if (prove_res == 1)
  {
    // VERIFIER: Should fail because the proof corresponds to (amount_1,
    // amount_1) but the public ciphertexts correspond to (amount_1, amount_2).
    int verify_result = secp256k1_mpt_verify_same_plaintext_multi(
        ctx, proof, proof_len, n, R, S, Pk, tx_id);
    EXPECT(verify_result == 0); // Must fail
  }

  free(proof);
  printf("Passed\n");
}

/**
 * Test 3: Tampered proof.
 * Generates valid proof, flips a bit, checks verification fails.
 */
static void test_tampered_proof_fail(const secp256k1_context *ctx)
{
  printf("Running test: tampered proof (should fail)... ");

  size_t n = 2;
  secp256k1_pubkey Pk[2], R[2], S[2];
  unsigned char r[64];
  unsigned char tx_id[32];
  uint64_t amount = 500;

  // Setup valid scenario
  random_scalar(ctx, tx_id);
  for (int i = 0; i < 2; ++i)
  {
    unsigned char priv[32];
    EXPECT(secp256k1_elgamal_generate_keypair(ctx, priv, &Pk[i]) == 1);
    random_scalar(ctx, &r[i * 32]);
    EXPECT(secp256k1_elgamal_encrypt(ctx, &R[i], &S[i], &Pk[i], amount,
                                     &r[i * 32]) == 1);
  }

  size_t proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
  unsigned char *proof = (unsigned char *)malloc(proof_len);
  EXPECT(proof != NULL);
  size_t out_len = proof_len;

  EXPECT(secp256k1_mpt_prove_same_plaintext_multi(ctx, proof, &out_len, amount,
                                                  n, R, S, Pk, r, tx_id) == 1);

  // Tamper: Flip a bit in the middle of the proof
  proof[proof_len / 2] ^= 0xFF;

  EXPECT(secp256k1_mpt_verify_same_plaintext_multi(ctx, proof, proof_len, n, R,
                                                   S, Pk, tx_id) == 0);

  free(proof);
  printf("Passed\n");
}

int main()
{
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  unsigned char seed[32];
  EXPECT(RAND_bytes(seed, sizeof(seed)) == 1);
  EXPECT(secp256k1_context_randomize(ctx, seed) == 1);

  // Test N=2 (Standard Send)
  test_valid_multi_proof(ctx, 2);

  // Test N=3 (e.g., with Issuer)
  test_valid_multi_proof(ctx, 3);

  // Test N=5 (Stress test)
  test_valid_multi_proof(ctx, 5);

  // Negative tests
  test_different_amounts_fail(ctx);
  test_tampered_proof_fail(ctx);

  secp256k1_context_destroy(ctx);
  printf("ALL TESTS PASSED\n");
  return 0;
}

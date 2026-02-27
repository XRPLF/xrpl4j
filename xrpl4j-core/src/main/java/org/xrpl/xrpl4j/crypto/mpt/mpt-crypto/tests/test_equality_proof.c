#include "secp256k1_mpt.h"
#include "test_utils.h"
#include <secp256k1.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Forward declarations for all test functions
static void test_equality_proof_valid(const secp256k1_context *ctx);
static void test_equality_proof_invalid_amount(const secp256k1_context *ctx);
static void test_equality_proof_invalid_tampered(const secp256k1_context *ctx);
static void test_equality_proof_zero_amount(const secp256k1_context *ctx);

// Main test runner
int main()
{
  secp256k1_context *ctx = secp256k1_context_create(SECP256K1_CONTEXT_SIGN |
                                                    SECP256K1_CONTEXT_VERIFY);
  EXPECT(ctx != NULL);

  unsigned char seed[32];
  random_bytes(seed);
  EXPECT(secp256k1_context_randomize(ctx, seed) == 1);

  // Run tests for this module
  test_equality_proof_valid(ctx);
  test_equality_proof_invalid_amount(ctx);
  test_equality_proof_invalid_tampered(ctx);
  test_equality_proof_zero_amount(ctx);

  secp256k1_context_destroy(ctx);
  printf("ALL TESTS PASSED\n");
  return 0;
}

// --- Test Implementations ---

// Test case 1: Generate and verify a valid proof
static void test_equality_proof_valid(const secp256k1_context *ctx)
{
  unsigned char privkey[32];
  secp256k1_pubkey pubkey;
  unsigned char randomness_r[32];
  secp256k1_pubkey c1, c2;
  uint64_t amount = 98765;
  unsigned char tx_context_id[32];
  unsigned char proof[98];

  printf("Running test: equality proof (valid case)...\n");

  // 1. Setup: Generate keys, randomness, and encrypt
  EXPECT(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
  random_bytes(randomness_r);

  // Ensure randomness is a valid scalar (needed for proof gen)
  while (secp256k1_ec_seckey_verify(ctx, randomness_r) != 1)
  {
    random_bytes(randomness_r);
  }

  EXPECT(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, amount,
                                   randomness_r) == 1);
  random_bytes(tx_context_id);

  // 2. Generate the proof
  int prove_result = secp256k1_equality_plaintext_prove(
      ctx, proof, &c1, &c2, &pubkey, amount, randomness_r, tx_context_id);
  EXPECT(prove_result == 1);

  // 3. Verify the proof
  int verify_result = secp256k1_equality_plaintext_verify(
      ctx, proof, &c1, &c2, &pubkey, amount, tx_context_id);
  EXPECT(verify_result == 1);

  printf("Test passed!\n");
}

// Test case 2: Verify a valid proof with the wrong amount
static void test_equality_proof_invalid_amount(const secp256k1_context *ctx)
{
  unsigned char privkey[32];
  secp256k1_pubkey pubkey;
  unsigned char randomness_r[32];
  secp256k1_pubkey c1, c2;
  uint64_t correct_amount = 98765;
  uint64_t wrong_amount = 11111; // Different amount
  unsigned char tx_context_id[32];
  unsigned char proof[98];

  printf("Running test: equality proof (invalid amount)...\n");

  // 1. Setup: Generate keys, randomness, encrypt correct amount
  EXPECT(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
  random_bytes(randomness_r);

  while (secp256k1_ec_seckey_verify(ctx, randomness_r) != 1)
  {
    random_bytes(randomness_r);
  }

  EXPECT(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, correct_amount,
                                   randomness_r) == 1);
  random_bytes(tx_context_id);

  // 2. Generate the proof for the correct amount
  EXPECT(secp256k1_equality_plaintext_prove(ctx, proof, &c1, &c2, &pubkey,
                                            correct_amount, randomness_r,
                                            tx_context_id) == 1);

  // 3. Verify the proof using the WRONG amount
  int verify_result = secp256k1_equality_plaintext_verify(
      ctx, proof, &c1, &c2, &pubkey, wrong_amount,
      tx_context_id);         // Pass wrong amount
  EXPECT(verify_result == 0); // Verification should fail

  printf("Test passed!\n");
}

// Test case 3: Verify a tampered proof
static void test_equality_proof_invalid_tampered(const secp256k1_context *ctx)
{
  unsigned char privkey[32];
  secp256k1_pubkey pubkey;
  unsigned char randomness_r[32];
  secp256k1_pubkey c1, c2;
  uint64_t amount = 98765;
  unsigned char tx_context_id[32];
  unsigned char proof[98];

  printf("Running test: equality proof (tampered proof)...\n");

  // 1. Setup and generate a valid proof
  EXPECT(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
  random_bytes(randomness_r);

  while (secp256k1_ec_seckey_verify(ctx, randomness_r) != 1)
  {
    random_bytes(randomness_r);
  }

  EXPECT(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, amount,
                                   randomness_r) == 1);
  random_bytes(tx_context_id);
  EXPECT(secp256k1_equality_plaintext_prove(ctx, proof, &c1, &c2, &pubkey,
                                            amount, randomness_r,
                                            tx_context_id) == 1);

  // 2. Tamper with the proof (e.g., flip a bit in the scalar 's')
  proof[97] ^= 0x01; // Flip the last bit of the last byte

  // 3. Verify the tampered proof
  int verify_result = secp256k1_equality_plaintext_verify(
      ctx, proof, &c1, &c2, &pubkey, amount, tx_context_id);
  EXPECT(verify_result == 0); // Verification should fail

  printf("Test passed!\n");
}

static void test_equality_proof_zero_amount(const secp256k1_context *ctx)
{
  unsigned char privkey[32];
  secp256k1_pubkey pubkey;
  unsigned char randomness_r[32];
  secp256k1_pubkey c1, c2;
  uint64_t amount = 0; // Test the zero case
  unsigned char tx_context_id[32];
  unsigned char proof[98];

  printf("Running test: equality proof (zero amount)...\n");

  // 1. Setup: Generate keys, randomness, and encrypt
  EXPECT(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
  random_bytes(randomness_r);

  while (secp256k1_ec_seckey_verify(ctx, randomness_r) != 1)
  {
    random_bytes(randomness_r);
  }

  EXPECT(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, amount,
                                   randomness_r) == 1);
  random_bytes(tx_context_id);

  // 2. Generate the proof for amount 0
  int prove_result = secp256k1_equality_plaintext_prove(
      ctx, proof, &c1, &c2, &pubkey, amount, randomness_r, tx_context_id);
  EXPECT(prove_result == 1);

  // 3. Verify the proof for amount 0
  int verify_result = secp256k1_equality_plaintext_verify(
      ctx, proof, &c1, &c2, &pubkey, amount, tx_context_id);
  EXPECT(verify_result == 1);

  printf("Test passed!\n");
}

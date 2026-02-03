#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <stdlib.h> // For malloc/free
#include <secp256k1.h>
#include <openssl/rand.h>
#include "secp256k1_mpt.h"

/* --- Helper Functions --- */

static int get_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    secp256k1_pubkey temp_pubkey;
    return secp256k1_elgamal_generate_keypair(ctx, scalar, &temp_pubkey);
}

/* --- Test Cases --- */

/**
 * Test 1: Generates N ciphertexts encrypting the SAME amount and verifies the proof.
 */
static void test_valid_multi_proof(const secp256k1_context* ctx, size_t n) {
    printf("Running test: same plaintext proof (N=%zu)... ", n);

    // C99 Variable Length Arrays (VLAs) for cleaner test code
    secp256k1_pubkey Pk[n];
    secp256k1_pubkey R[n];
    secp256k1_pubkey S[n];
    unsigned char r[n][32]; // Array of randomness scalars

    unsigned char tx_id[32];
    uint64_t amount = 555666;
    unsigned char* proof;
    size_t proof_len;
    size_t i;

    // 1. Setup: Keys and Randomness
    assert(get_random_scalar(ctx, tx_id) == 1);
    for (i = 0; i < n; ++i) {
        unsigned char priv[32];
        assert(secp256k1_elgamal_generate_keypair(ctx, priv, &Pk[i]) == 1);
        assert(get_random_scalar(ctx, r[i]) == 1);
    }

    // 2. Encrypt the SAME amount N times
    for (i = 0; i < n; ++i) {
        assert(secp256k1_elgamal_encrypt(ctx, &R[i], &S[i], &Pk[i], amount, r[i]) == 1);
    }

    // 3. Generate Proof
    proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
    proof = (unsigned char*)malloc(proof_len);
    assert(proof != NULL);

    // Flatten the randomness array for the API
    unsigned char r_flat[n * 32];
    for (i = 0; i < n; ++i) {
        memcpy(&r_flat[i * 32], r[i], 32);
    }

    size_t out_len = proof_len;
    assert(secp256k1_mpt_prove_same_plaintext_multi(
            ctx, proof, &out_len, amount, n,
            R, S, Pk, r_flat, tx_id
    ) == 1);
    assert(out_len == proof_len);

    // 4. Verify Proof
    assert(secp256k1_mpt_verify_same_plaintext_multi(
            ctx, proof, proof_len, n,
            R, S, Pk, tx_id
    ) == 1);

    free(proof);
    printf("Passed\n");
}

/**
 * Test 2: Mismatched amounts.
 * Encrypts DIFFERENT amounts, attempts to generate a proof for one, checks verification fails.
 */
static void test_different_amounts_fail(const secp256k1_context* ctx) {
    printf("Running test: different amounts (should fail)... ");

    size_t n = 2;
    secp256k1_pubkey Pk[2], R[2], S[2];
    unsigned char r[2][32];
    unsigned char tx_id[32];

    uint64_t amount_1 = 100;
    uint64_t amount_2 = 200; // Different!

    assert(get_random_scalar(ctx, tx_id) == 1);
    for (int i = 0; i < 2; ++i) {
        unsigned char priv[32];
        assert(secp256k1_elgamal_generate_keypair(ctx, priv, &Pk[i]) == 1);
        assert(get_random_scalar(ctx, r[i]) == 1);
    }

    // Encrypt DIFFERENT amounts
    assert(secp256k1_elgamal_encrypt(ctx, &R[0], &S[0], &Pk[0], amount_1, r[0]) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &R[1], &S[1], &Pk[1], amount_2, r[1]) == 1);

    size_t proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
    unsigned char* proof = (unsigned char*)malloc(proof_len);

    unsigned char r_flat[64];
    memcpy(&r_flat[0], r[0], 32);
    memcpy(&r_flat[32], r[1], 32);

    // PROVER: We try to prove they both equal amount_1.
    // The prover function will technically generate a valid proof math-wise
    // based on the inputs we GIVE it (amount_1, r1, r2),
    // but it won't match the actual C2 ciphertext because C2 uses amount_2.
    size_t out_len = proof_len;
    assert(secp256k1_mpt_prove_same_plaintext_multi(
            ctx, proof, &out_len, amount_1, n,
            R, S, Pk, r_flat, tx_id
    ) == 1);

    // VERIFIER: Should fail because C2 does not encrypt amount_1
    int verify_result = secp256k1_mpt_verify_same_plaintext_multi(
            ctx, proof, proof_len, n,
            R, S, Pk, tx_id
    );
    assert(verify_result == 0); // Must fail

    free(proof);
    printf("Passed\n");
}

/**
 * Test 3: Tampered proof.
 * Generates valid proof, flips a bit, checks verification fails.
 */
static void test_tampered_proof_fail(const secp256k1_context* ctx) {
    printf("Running test: tampered proof (should fail)... ");

    size_t n = 2;
    secp256k1_pubkey Pk[2], R[2], S[2];
    unsigned char r[64];
    unsigned char tx_id[32];
    uint64_t amount = 500;

    // Setup valid scenario
    assert(get_random_scalar(ctx, tx_id) == 1);
    for(int i=0; i<2; ++i) {
        unsigned char priv[32];
        assert(secp256k1_elgamal_generate_keypair(ctx, priv, &Pk[i]) == 1);
        get_random_scalar(ctx, &r[i*32]);
        assert(secp256k1_elgamal_encrypt(ctx, &R[i], &S[i], &Pk[i], amount, &r[i*32]) == 1);
    }

    size_t proof_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
    unsigned char* proof = (unsigned char*)malloc(proof_len);
    size_t out_len = proof_len;

    assert(secp256k1_mpt_prove_same_plaintext_multi(
            ctx, proof, &out_len, amount, n, R, S, Pk, r, tx_id) == 1);

    // Tamper: Flip a bit in the middle of the proof
    proof[proof_len / 2] ^= 0xFF;

    assert(secp256k1_mpt_verify_same_plaintext_multi(
            ctx, proof, proof_len, n, R, S, Pk, tx_id) == 0);

    free(proof);
    printf("Passed\n");
}

int main() {
    secp256k1_context* ctx = secp256k1_context_create(
            SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    assert(ctx != NULL);

    unsigned char seed[32];
    assert(RAND_bytes(seed, sizeof(seed)) == 1);
    assert(secp256k1_context_randomize(ctx, seed) == 1);

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
    return 0;
}

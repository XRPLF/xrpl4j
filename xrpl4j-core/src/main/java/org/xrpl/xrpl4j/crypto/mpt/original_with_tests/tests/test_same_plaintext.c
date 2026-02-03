#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <secp256k1.h>
#include <openssl/rand.h>
#include "secp256k1_mpt.h" // Your library's header

/*
Helper function to get a random 32-byte scalar.
We re-use the elgamal_generate_keypair function as a convenient
way to get a validated, random 32-byte scalar, ignoring the pubkey.
*/
static int get_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    secp256k1_pubkey temp_pubkey;
    return secp256k1_elgamal_generate_keypair(ctx, scalar, &temp_pubkey);
}

/**
 * Test 1: Valid proof generation and verification.
 */
static void test_same_plaintext_valid(const secp256k1_context* ctx) {
    unsigned char priv_1[32], priv_2[32];
    secp256k1_pubkey pub_1, pub_2;
    unsigned char r1[32], r2[32];
    unsigned char tx_context_id[32];
    uint64_t amount_m = 123456;

    secp256k1_pubkey R1, S1, R2, S2;
    unsigned char proof[261];

    printf("Running test: same plaintext proof (valid case)...\n");

    // 1. Setup: Generate two keypairs (P1, P2)
    assert(secp256k1_elgamal_generate_keypair(ctx, priv_1, &pub_1) == 1);
    assert(secp256k1_elgamal_generate_keypair(ctx, priv_2, &pub_2) == 1);

    // 2. Setup: Generate randomness r1, r2, and tx_id
    assert(get_random_scalar(ctx, r1) == 1);
    assert(get_random_scalar(ctx, r2) == 1);
    assert(get_random_scalar(ctx, tx_context_id) == 1); // Use for a random tx_id

    // 3. Encrypt the *same amount* for both keys
    assert(secp256k1_elgamal_encrypt(ctx, &R1, &S1, &pub_1, amount_m, r1) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &R2, &S2, &pub_2, amount_m, r2) == 1);

    // 4. Generate the proof
    int prove_result = secp256k1_mpt_prove_same_plaintext(
            ctx, proof,
            &R1, &S1, &pub_1,
            &R2, &S2, &pub_2,
            amount_m, r1, r2, tx_context_id
    );
    assert(prove_result == 1);

    // 5. Verify the proof
    int verify_result = secp256k1_mpt_verify_same_plaintext(
            ctx, proof,
            &R1, &S1, &pub_1,
            &R2, &S2, &pub_2,
            tx_context_id
    );
    assert(verify_result == 1);

    printf("Test passed!\n");
}

/**
 * Test 2: Verifying a tampered proof (should fail).
 */
static void test_same_plaintext_tampered_proof(const secp256k1_context* ctx) {
    unsigned char priv_1[32], priv_2[32];
    secp256k1_pubkey pub_1, pub_2;
    unsigned char r1[32], r2[32];
    unsigned char tx_context_id[32];
    uint64_t amount_m = 123456;

    secp256k1_pubkey R1, S1, R2, S2;
    unsigned char proof[261];

    printf("Running test: same plaintext proof (tampered proof)...\n");

    // 1. Setup and generate a valid proof
    assert(secp256k1_elgamal_generate_keypair(ctx, priv_1, &pub_1) == 1);
    assert(secp256k1_elgamal_generate_keypair(ctx, priv_2, &pub_2) == 1);
    assert(get_random_scalar(ctx, r1) == 1);
    assert(get_random_scalar(ctx, r2) == 1);
    assert(get_random_scalar(ctx, tx_context_id) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &R1, &S1, &pub_1, amount_m, r1) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &R2, &S2, &pub_2, amount_m, r2) == 1);
    assert(secp256k1_mpt_prove_same_plaintext(
            ctx, proof, &R1, &S1, &pub_1, &R2, &S2, &pub_2,
            amount_m, r1, r2, tx_context_id) == 1);

    // 2. Tamper with the proof (flip a bit in a commitment point)
    proof[42] ^= 0x01;

    // 3. Verify the tampered proof
    int verify_result = secp256k1_mpt_verify_same_plaintext(
            ctx, proof, &R1, &S1, &pub_1, &R2, &S2, &pub_2, tx_context_id);
    assert(verify_result == 0); // Should fail

    printf("Test passed!\n");
}

/**
 * Test 3: Verifying with different-amount ciphertexts (should fail).
 */
static void test_same_plaintext_wrong_ciphertext(const secp256k1_context* ctx) {
    unsigned char priv_1[32], priv_2[32];
    secp256k1_pubkey pub_1, pub_2;
    unsigned char r1[32], r2[32], r3[32];
    unsigned char tx_context_id[32];
    uint64_t amount_m1 = 123456;
    uint64_t amount_m2 = 777777; // A different amount

    secp256k1_pubkey R1, S1, R2, S2; // For amount m1
    secp256k1_pubkey R3, S3; // For amount m2
    unsigned char proof[261];

    printf("Running test: same plaintext proof (wrong ciphertext)...\n");

    // 1. Setup
    assert(secp256k1_elgamal_generate_keypair(ctx, priv_1, &pub_1) == 1);
    assert(secp256k1_elgamal_generate_keypair(ctx, priv_2, &pub_2) == 1);
    assert(get_random_scalar(ctx, r1) == 1);
    assert(get_random_scalar(ctx, r2) == 1);
    assert(get_random_scalar(ctx, r3) == 1);
    assert(get_random_scalar(ctx, tx_context_id) == 1);

    // 2. Create ciphertexts
    assert(secp256k1_elgamal_encrypt(ctx, &R1, &S1, &pub_1, amount_m1, r1) == 1); // C1(m1)
    assert(secp256k1_elgamal_encrypt(ctx, &R2, &S2, &pub_2, amount_m1, r2) == 1); // C2(m1)
    assert(secp256k1_elgamal_encrypt(ctx, &R3, &S3, &pub_2, amount_m2, r3) == 1); // C3(m2)

    // 3. Generate a VALID proof for C1 and C2 (which both encrypt m1)
    assert(secp256k1_mpt_prove_same_plaintext(
            ctx, proof, &R1, &S1, &pub_1, &R2, &S2, &pub_2,
            amount_m1, r1, r2, tx_context_id) == 1);

    // 4. Verify the proof, but swap C2 with C3 (which encrypts m2)
    int verify_result = secp256k1_mpt_verify_same_plaintext(
            ctx, proof, &R1, &S1, &pub_1, &R3, &S3, &pub_2, tx_context_id);
    assert(verify_result == 0); // Should fail

    printf("Test passed!\n");
}


// Main test runner
int main() {
    secp256k1_context* ctx = secp256k1_context_create(
            SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    assert(ctx != NULL);

    unsigned char seed[32];
    assert(RAND_bytes(seed, sizeof(seed)) == 1);
    assert(secp256k1_context_randomize(ctx, seed) == 1);

    // Run tests for this module
    test_same_plaintext_valid(ctx);
    test_same_plaintext_tampered_proof(ctx);
    test_same_plaintext_wrong_ciphertext(ctx);

    secp256k1_context_destroy(ctx);
    return 0;
}

#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <secp256k1.h>
#include <openssl/rand.h>
#include "secp256k1_mpt.h"

// Forward declarations for all test functions
static void test_key_generation(const secp256k1_context* ctx);
static void test_encryption(const secp256k1_context* ctx);
static void test_encryption_decryption_roundtrip(const secp256k1_context* ctx);
static void test_homomorphic_operations(const secp256k1_context* ctx);
static void test_zero_encryption(const secp256k1_context* ctx);
static void test_canonical_zero(const secp256k1_context* ctx);
static void test_verify_encryption(const secp256k1_context* ctx);

// Main test runner
int main() {
    secp256k1_context* ctx = secp256k1_context_create(
            SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);
    assert(ctx != NULL);

    unsigned char seed[32];
    assert(RAND_bytes(seed, sizeof(seed)) == 1);
    assert(secp256k1_context_randomize(ctx, seed) == 1);

    test_key_generation(ctx);
    test_encryption(ctx);
    test_encryption_decryption_roundtrip(ctx);
    test_homomorphic_operations(ctx);
    test_zero_encryption(ctx);
    test_canonical_zero(ctx);
    test_verify_encryption(ctx);

    secp256k1_context_destroy(ctx);
    return 0;
}

// --- Test Implementations ---

static void test_key_generation(const secp256k1_context* ctx) {
    unsigned char privkey[32];
    secp256k1_pubkey pubkey;
    printf("Running test: secp256k1_elgamal_generate_keypair...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
    printf("Test passed!\n");
}

static void test_encryption(const secp256k1_context* ctx) {
    unsigned char privkey[32], blinding_factor[32];
    secp256k1_pubkey pubkey, c1, c2, temp_pubkey;
    printf("Running test: secp256k1_elgamal_encrypt (smoke test)...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
    // Note: Reusing generate_keypair is a convenient way to get a random scalar
    assert(secp256k1_elgamal_generate_keypair(ctx, blinding_factor, &temp_pubkey) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, 12345, blinding_factor) == 1);
    printf("Test passed!\n");
}

static void test_encryption_decryption_roundtrip(const secp256k1_context* ctx) {
    unsigned char privkey[32], blinding_factor[32];
    secp256k1_pubkey pubkey, c1, c2, temp_pubkey;
    uint64_t original_amount = 10001;
    uint64_t decrypted_amount = 0;
    printf("Running test: encryption-decryption round trip...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
    assert(secp256k1_elgamal_generate_keypair(ctx, blinding_factor, &temp_pubkey) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, original_amount, blinding_factor) == 1);
    assert(secp256k1_elgamal_decrypt(ctx, &decrypted_amount, &c1, &c2, privkey) == 1);
    assert(original_amount == decrypted_amount);
    printf("Test passed!\n");
}

static void test_homomorphic_operations(const secp256k1_context* ctx) {
    unsigned char privkey[32];
    secp256k1_pubkey pubkey;
    uint64_t amount_a = 5000, amount_b = 1234;
    secp256k1_pubkey a_c1, a_c2, b_c1, b_c2, result_c1, result_c2;
    uint64_t decrypted_result;

    printf("Running test: homomorphic operations...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);

    {
        unsigned char k_a[32];
        secp256k1_pubkey temp_pubkey;
        assert(secp256k1_elgamal_generate_keypair(ctx, k_a, &temp_pubkey) == 1);
        assert(secp256k1_elgamal_encrypt(ctx, &a_c1, &a_c2, &pubkey, amount_a, k_a) == 1);
    }
    {
        unsigned char k_b[32];
        secp256k1_pubkey temp_pubkey;
        assert(secp256k1_elgamal_generate_keypair(ctx, k_b, &temp_pubkey) == 1);
        assert(secp256k1_elgamal_encrypt(ctx, &b_c1, &b_c2, &pubkey, amount_b, k_b) == 1);
    }

    assert(secp256k1_elgamal_add(ctx, &result_c1, &result_c2, &a_c1, &a_c2, &b_c1, &b_c2) == 1);
    assert(secp256k1_elgamal_decrypt(ctx, &decrypted_result, &result_c1, &result_c2, privkey) == 1);
    assert(decrypted_result == amount_a + amount_b);

    assert(secp256k1_elgamal_subtract(ctx, &result_c1, &result_c2, &a_c1, &a_c2, &b_c1, &b_c2) == 1);
    assert(secp256k1_elgamal_decrypt(ctx, &decrypted_result, &result_c1, &result_c2, privkey) == 1);
    assert(decrypted_result == amount_a - amount_b);
    printf("Test passed!\n");
}

static void test_zero_encryption(const secp256k1_context* ctx) {
    unsigned char privkey[32], blinding_factor[32];
    secp256k1_pubkey pubkey, c1, c2, temp_pubkey;
    uint64_t original_amount = 0;
    uint64_t decrypted_amount = 999; // Non-zero initial value
    printf("Running test: encrypting a random zero...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
    assert(secp256k1_elgamal_generate_keypair(ctx, blinding_factor, &temp_pubkey) == 1);
    assert(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, original_amount, blinding_factor) == 1);
    assert(secp256k1_elgamal_decrypt(ctx, &decrypted_amount, &c1, &c2, privkey) == 1);
    assert(original_amount == decrypted_amount);
    printf("Test passed!\n");
}

static void test_canonical_zero(const secp256k1_context* ctx) {
    unsigned char privkey[32];
    secp256k1_pubkey pubkey;
    secp256k1_pubkey c1_a, c2_a, c1_b, c2_b;
    uint64_t decrypted_amount = 999;

    // Use placeholder byte arrays for IDs (20 bytes for account, 24 for issuance)
    unsigned char account_id[20] = {1}; // Example ID
    unsigned char issuance_id[24] = {2}; // Example ID

    printf("Running test: canonical encrypted zero...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);

    // Generate it once
    assert(generate_canonical_encrypted_zero(ctx, &c1_a, &c2_a, &pubkey, account_id, issuance_id) == 1);

    // Generate it a second time with the same inputs
    assert(generate_canonical_encrypted_zero(ctx, &c1_b, &c2_b, &pubkey, account_id, issuance_id) == 1);

    // 1. Verify that it decrypts to zero
    assert(secp256k1_elgamal_decrypt(ctx, &decrypted_amount, &c1_a, &c2_a, privkey) == 1);
    assert(decrypted_amount == 0);

    // 2. Verify that the output is deterministic (both ciphertexts are identical)
    //    We compare the internal structs directly for this test, assuming determinism holds internally.
    assert(memcmp(&c1_a, &c1_b, sizeof(c1_a)) == 0);
    assert(memcmp(&c2_a, &c2_b, sizeof(c2_a)) == 0);

    printf("Test passed!\n");
}
static void test_verify_encryption(const secp256k1_context* ctx) {
    unsigned char privkey[32], blinding_factor[32];
    secp256k1_pubkey pubkey, c1, c2, temp_pubkey;
    uint64_t amount = 5000;
    uint64_t zero_amount = 0;

    printf("Running test: secp256k1_elgamal_verify_encryption...\n");
    assert(secp256k1_elgamal_generate_keypair(ctx, privkey, &pubkey) == 1);
    assert(secp256k1_elgamal_generate_keypair(ctx, blinding_factor, &temp_pubkey) == 1);

    /* 1. Test standard encryption verification */
    assert(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, amount, blinding_factor) == 1);
    assert(secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pubkey, amount, blinding_factor) == 1);

    /* 2. Test zero-value encryption verification (The special case we added) */
    assert(secp256k1_elgamal_encrypt(ctx, &c1, &c2, &pubkey, zero_amount, blinding_factor) == 1);
    assert(secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pubkey, zero_amount, blinding_factor) == 1);

    /* 3. Test detection of incorrect amount */
    assert(secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pubkey, amount + 1, blinding_factor) == 0);

    /* 4. Test detection of tampered ciphertext */
    c2 = c1; // Force a mismatch
    assert(secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pubkey, zero_amount, blinding_factor) == 0);

    printf("Test passed!\n");
}

/**
 * @file elgamal.c
 * @brief EC-ElGamal Encryption for Confidential Balances.
 *
 * This module implements additive homomorphic encryption using the ElGamal
 * scheme over the secp256k1 elliptic curve. It provides the core mechanism
 * for representing confidential balances and transferring value on the ledger.
 *
 * @details
 * **Encryption Scheme:**
 * Given a public key \f$ Q = sk \cdot G \f$ and a plaintext amount \f$ m \f$,
 * encryption with randomness \f$ r \f$ produces a ciphertext pair \f$ (C_1, C_2) \f$:
 * - \f$ C_1 = r \cdot G \f$ (Ephemeral public key)
 * - \f$ C_2 = m \cdot G + r \cdot Q \f$ (Masked amount)
 *
 * **Homomorphism:**
 * The scheme is additively homomorphic:
 * \f[ Enc(m_1) + Enc(m_2) = (C_{1,1}+C_{1,2}, C_{2,1}+C_{2,2}) = Enc(m_1 + m_2) \f]
 * This allows validators to update balances (e.g., add incoming transfers)
 * without decrypting them.
 *
 * **Decryption (Discrete Logarithm):**
 * Decryption involves two steps:
 * 1. Remove the mask: \f$ M = C_2 - sk \cdot C_1 = m \cdot G \f$.
 * 2. Recover \f$ m \f$ from \f$ M \f$: This requires solving the Discrete Logarithm
 * Problem (DLP) for \f$ m \f$. Since balances are 64-bit integers but typically
 * small in "human" terms, this implementation uses an optimized search
 * for ranges relevant to transaction processing (e.g., 0 to 1,000,000).
 *
 * **Canonical Zero:**
 * To ensure deterministic ledger state for empty accounts, a "Canonical Encrypted Zero"
 * is defined using randomness derived deterministically from the account ID and token ID.
 *
 * @see [Spec (ConfidentialMPT_20260201.pdf) Section 3.2.2] ElGamal Encryption
 */
#include "secp256k1_mpt.h"
#include <openssl/rand.h>
#include <openssl/sha.h>
#include <string.h>
#include <stdlib.h>

/* --- Internal Helpers --- */

static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* pk1, const secp256k1_pubkey* pk2) {
    return secp256k1_ec_pubkey_cmp(ctx, pk1, pk2) == 0;
}

static int compute_amount_point(const secp256k1_context* ctx, secp256k1_pubkey* mG, uint64_t amount) {
    unsigned char amount_scalar[32] = {0};
    int ret;
    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    ret = secp256k1_ec_pubkey_create(ctx, mG, amount_scalar);
    OPENSSL_cleanse(amount_scalar, 32); // Wipe scalar after use
    return ret;
}

/* --- Key Generation --- */

int secp256k1_elgamal_generate_keypair(
        const secp256k1_context* ctx,
        unsigned char* privkey,
        secp256k1_pubkey* pubkey)
{
    do {
        if (RAND_bytes(privkey, 32) != 1) return 0;
    } while (!secp256k1_ec_seckey_verify(ctx, privkey));

    if (!secp256k1_ec_pubkey_create(ctx, pubkey, privkey)) {
        OPENSSL_cleanse(privkey, 32); // Cleanup on failure
        return 0;
    }
    return 1;
}

/* --- Encryption --- */

int secp256k1_elgamal_encrypt(
        const secp256k1_context* ctx,
        secp256k1_pubkey* c1,
        secp256k1_pubkey* c2,
        const secp256k1_pubkey* pubkey_Q,
        uint64_t amount,
        const unsigned char* blinding_factor
) {
    secp256k1_pubkey S, mG;
    const secp256k1_pubkey* pts[2];

    /* 1. C1 = r * G */
    if (!secp256k1_ec_pubkey_create(ctx, c1, blinding_factor)) return 0;

    /* 2. S = r * Q (Shared Secret) */
    S = *pubkey_Q;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &S, blinding_factor)) return 0;

    /* 3. C2 = S + m*G */
    if (amount == 0) {
        *c2 = S; // m*G is infinity, so C2 = S
    } else {
        if (!compute_amount_point(ctx, &mG, amount)) return 0;
        pts[0] = &mG; pts[1] = &S;
        if (!secp256k1_ec_pubkey_combine(ctx, c2, pts, 2)) return 0;
    }

    return 1;
}

/* --- Decryption --- */

int secp256k1_elgamal_decrypt(
        const secp256k1_context* ctx,
        uint64_t* amount,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const unsigned char* privkey
) {
    secp256k1_pubkey S, M_target, current_M, G_point, next_M;
    const secp256k1_pubkey* pts[2];
    uint64_t i;
    unsigned char one[32] = {0}; one[31] = 1;

    /* 1. Recover Shared Secret: S = privkey * C1 */
    S = *c1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &S, privkey)) return 0;

    /* 2. Check for Amount = 0 (C2 == S) */
    /* This is much faster than doing point subtraction first */
    if (pubkey_equal(ctx, c2, &S)) {
        *amount = 0;
        return 1;
    }

    /* 3. Prepare Target: M_target = C2 - S */
    /* M_target = C2 + (-S) */
    if (!secp256k1_ec_pubkey_negate(ctx, &S)) return 0;
    pts[0] = c2; pts[1] = &S;
    if (!secp256k1_ec_pubkey_combine(ctx, &M_target, pts, 2)) return 0;

    /* 4. Brute Force Search (1 to 1,000,000) */
    /* Optimization: Use point comparison, no serialization inside loop */

    if (!secp256k1_ec_pubkey_create(ctx, &G_point, one)) return 0; // G
    current_M = G_point; // Start at 1*G

    for (i = 1; i <= 1000000; ++i) {
        // Fast comparison
        if (pubkey_equal(ctx, &current_M, &M_target)) {
            *amount = i;
            return 1;
        }

        // Increment: current_M = current_M + G
        pts[0] = &current_M; pts[1] = &G_point;
        if (!secp256k1_ec_pubkey_combine(ctx, &next_M, pts, 2)) return 0;
        current_M = next_M;
    }

    return 0; // Amount not found in range
}

/* --- Homomorphic Operations --- */

int secp256k1_elgamal_add(
        const secp256k1_context* ctx,
        secp256k1_pubkey* sum_c1,
        secp256k1_pubkey* sum_c2,
        const secp256k1_pubkey* a_c1,
        const secp256k1_pubkey* a_c2,
        const secp256k1_pubkey* b_c1,
        const secp256k1_pubkey* b_c2
) {
    const secp256k1_pubkey* pts[2];

    pts[0] = a_c1; pts[1] = b_c1;
    if (!secp256k1_ec_pubkey_combine(ctx, sum_c1, pts, 2)) return 0;

    pts[0] = a_c2; pts[1] = b_c2;
    if (!secp256k1_ec_pubkey_combine(ctx, sum_c2, pts, 2)) return 0;

    return 1;
}

int secp256k1_elgamal_subtract(
        const secp256k1_context* ctx,
        secp256k1_pubkey* diff_c1,
        secp256k1_pubkey* diff_c2,
        const secp256k1_pubkey* a_c1,
        const secp256k1_pubkey* a_c2,
        const secp256k1_pubkey* b_c1,
        const secp256k1_pubkey* b_c2
) {
    secp256k1_pubkey neg_b_c1 = *b_c1;
    secp256k1_pubkey neg_b_c2 = *b_c2;
    const secp256k1_pubkey* pts[2];

    if (!secp256k1_ec_pubkey_negate(ctx, &neg_b_c1)) return 0;
    if (!secp256k1_ec_pubkey_negate(ctx, &neg_b_c2)) return 0;

    pts[0] = a_c1; pts[1] = &neg_b_c1;
    if (!secp256k1_ec_pubkey_combine(ctx, diff_c1, pts, 2)) return 0;

    pts[0] = a_c2; pts[1] = &neg_b_c2;
    if (!secp256k1_ec_pubkey_combine(ctx, diff_c2, pts, 2)) return 0;

    return 1;
}

/* --- Canonical Encrypted Zero --- */

int generate_canonical_encrypted_zero(
        const secp256k1_context* ctx,
        secp256k1_pubkey* enc_zero_c1,
        secp256k1_pubkey* enc_zero_c2,
        const secp256k1_pubkey* pubkey,
        const unsigned char* account_id,     // 20 bytes
        const unsigned char* mpt_issuance_id // 24 bytes
) {
    unsigned char deterministic_scalar[32];
    unsigned char hash_input[51]; // 7 ("EncZero") + 20 + 24
    const char* domain = "EncZero";
    int ret;
    SHA256_CTX sha;

    // Build static buffer part
    memcpy(hash_input, domain, 7);
    memcpy(hash_input + 7, account_id, 20);
    memcpy(hash_input + 27, mpt_issuance_id, 24);

    /* Rejection sampling loop to ensure scalar is valid */
    do {
        SHA256(hash_input, 51, deterministic_scalar);

        // If invalid, re-hash the hash (standard chain method for determinism)
        // Or simply fail if strict canonical behavior is required.
        // Assuming rejection sampling is the intended design for safety:
        if (secp256k1_ec_seckey_verify(ctx, deterministic_scalar)) break;

        // Update input for next iteration to get new hash
        // (Note: The original code just looped SHA256 on same input which is static,
        // so it would loop forever if the first hash was invalid.
        // Fixed here by re-hashing the output if needed, though highly unlikely to fail).
        memcpy(hash_input, deterministic_scalar, 32);

    } while (1);

    ret = secp256k1_elgamal_encrypt(
            ctx,
            enc_zero_c1,
            enc_zero_c2,
            pubkey,
            0,
            deterministic_scalar
    );

    OPENSSL_cleanse(deterministic_scalar, 32); // Secure cleanup
    return ret;
}

/* --- Direct Verification (Convert) --- */

int secp256k1_elgamal_verify_encryption(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* c1,
        const secp256k1_pubkey* c2,
        const secp256k1_pubkey* pubkey_Q,
        uint64_t amount,
        const unsigned char* blinding_factor)
{
    secp256k1_pubkey expected_c1, expected_c2, mG, S;
    const secp256k1_pubkey* pts[2];

    /* 1. Verify C1 == r * G */
    if (!secp256k1_ec_pubkey_create(ctx, &expected_c1, blinding_factor)) return 0;
    if (!pubkey_equal(ctx, c1, &expected_c1)) return 0;

    /* 2. Verify C2 == r*Q + m*G */

    // S = r * Q
    S = *pubkey_Q;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &S, blinding_factor)) return 0;

    if (amount == 0) {
        expected_c2 = S;
    } else {
        if (!compute_amount_point(ctx, &mG, amount)) return 0;
        pts[0] = &mG; pts[1] = &S;
        if (!secp256k1_ec_pubkey_combine(ctx, &expected_c2, pts, 2)) return 0;
    }

    if (!pubkey_equal(ctx, c2, &expected_c2)) return 0;

    return 1;
}

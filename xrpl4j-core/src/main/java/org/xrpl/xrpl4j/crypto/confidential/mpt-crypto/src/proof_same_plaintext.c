/**
 * @file proof_same_plaintext.c
 * @brief Zero-Knowledge Proof of Plaintext Equality (1-to-1).
 *
 * This module implements a multi-statement Sigma protocol to prove that two
 * different ElGamal ciphertexts encrypt the **same** underlying plaintext amount,
 * potentially under different public keys and using different randomness.
 *
 * @details
 * **Statement:**
 * Given two ciphertexts \f$ (R_1, S_1) \f$ and \f$ (R_2, S_2) \f$ encrypted under
 * public keys \f$ P_1 \f$ and \f$ P_2 \f$ respectively, the prover demonstrates
 * knowledge of scalars \f$ m, r_1, r_2 \f$ such that:
 * 1. \f$ R_1 = r_1 \cdot G \f$ and \f$ S_1 = m \cdot G + r_1 \cdot P_1 \f$
 * 2. \f$ R_2 = r_2 \cdot G \f$ and \f$ S_2 = m \cdot G + r_2 \cdot P_2 \f$
 *
 * **Protocol Logic (Shared Nonce):**
 * To prove that \f$ m \f$ is identical in both ciphertexts without revealing it,
 * the prover uses a **shared random nonce** \f$ k_m \f$ for the amount commitment
 * across both logical branches of the proof.
 *
 * 1. **Commitments:**
 * - \f$ T_m = k_m \cdot G \f$ (Shared commitment to amount nonce)
 * - \f$ T_{r1,G} = k_{r1} \cdot G \f$, \f$ T_{r1,P1} = k_{r1} \cdot P_1 \f$
 * - \f$ T_{r2,G} = k_{r2} \cdot G \f$, \f$ T_{r2,P2} = k_{r2} \cdot P_2 \f$
 *
 * 2. **Challenge:**
 * \f$ e = H(\dots \parallel T_m \parallel \dots) \f$
 *
 * 3. **Responses:**
 * - \f$ s_m = k_m + e \cdot m \f$ (Shared response for amount)
 * - \f$ s_{r1} = k_{r1} + e \cdot r_1 \f$
 * - \f$ s_{r2} = k_{r2} + e \cdot r_2 \f$
 *
 * 4. **Verification:**
 * The verifier checks 4 equations. Crucially, the "Amount" equations for both
 * ciphertexts use the **same** \f$ s_m \f$ and \f$ T_m \f$, mathematically enforcing equality:
 * - \f$ s_m \cdot G + s_{r1} \cdot P_1 \stackrel{?}{=} T_m + T_{r1,P1} + e \cdot S_1 \f$
 * - \f$ s_m \cdot G + s_{r2} \cdot P_2 \stackrel{?}{=} T_m + T_{r2,P2} + e \cdot S_2 \f$
 *
 * **Security Context:**
 * This is used when transferring confidential tokens between accounts (re-encrypting
 * the sender's balance for the receiver) or updating keys, ensuring no value is
 * created or destroyed during the transformation.
 *
 * @see [Spec (ConfidentialMPT_20260106.pdf) Section 3.3.3] Proof of Equality of Plaintexts (Different Keys, Same Secret Amount)
 */
#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <stdlib.h>

/* --- Internal Helpers --- */

static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* pk1, const secp256k1_pubkey* pk2) {
    return secp256k1_ec_pubkey_cmp(ctx, pk1, pk2) == 0;
}

static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    do {
        if (RAND_bytes(scalar, 32) != 1) return 0;
    } while (!secp256k1_ec_seckey_verify(ctx, scalar));
    return 1;
}

/**
 * Builds the challenge hash input.
 */
static void build_same_plaintext_hash_input(
        const secp256k1_context* ctx,
        unsigned char* e_out,
        const secp256k1_pubkey* R1, const secp256k1_pubkey* S1, const secp256k1_pubkey* P1,
        const secp256k1_pubkey* R2, const secp256k1_pubkey* S2, const secp256k1_pubkey* P2,
        const secp256k1_pubkey* T_m,
        const secp256k1_pubkey* T_r1_G, const secp256k1_pubkey* T_r1_P1,
        const secp256k1_pubkey* T_r2_G, const secp256k1_pubkey* T_r2_P2,
        const unsigned char* tx_context_id)
{
    SHA256_CTX sha;
    unsigned char buf[33];
    unsigned char h[32];
    size_t len;
    const char* domain = "MPT_POK_SAME_PLAINTEXT_PROOF";

    SHA256_Init(&sha);
    SHA256_Update(&sha, domain, strlen(domain));

    /* Helper macro to serialize and update */
#define SER_AND_HASH(pk) do { \
        len = 33; \
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, pk, SECP256K1_EC_COMPRESSED); \
        SHA256_Update(&sha, buf, 33); \
    } while(0)

    // 6 Public Inputs
    SER_AND_HASH(R1); SER_AND_HASH(S1); SER_AND_HASH(P1);
    SER_AND_HASH(R2); SER_AND_HASH(S2); SER_AND_HASH(P2);

    // 5 Commitments
    SER_AND_HASH(T_m);
    SER_AND_HASH(T_r1_G); SER_AND_HASH(T_r1_P1);
    SER_AND_HASH(T_r2_G); SER_AND_HASH(T_r2_P2);

#undef SER_AND_HASH

    if (tx_context_id) {
        SHA256_Update(&sha, tx_context_id, 32);
    }

    SHA256_Final(h, &sha);
    secp256k1_mpt_scalar_reduce32(e_out, h);
}

/* --- Public API --- */

int secp256k1_mpt_prove_same_plaintext(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        const secp256k1_pubkey* R1, const secp256k1_pubkey* S1, const secp256k1_pubkey* P1,
        const secp256k1_pubkey* R2, const secp256k1_pubkey* S2, const secp256k1_pubkey* P2,
        uint64_t amount_m,
        const unsigned char* randomness_r1,
        const unsigned char* randomness_r2,
        const unsigned char* tx_context_id
) {
    unsigned char k_m[32], k_r1[32], k_r2[32];
    unsigned char m_scalar[32] = {0};
    unsigned char e[32];
    unsigned char s_m[32], s_r1[32], s_r2[32];
    unsigned char term[32];
    secp256k1_pubkey T_m, T_r1_G, T_r1_P1, T_r2_G, T_r2_P2;
    size_t len;
    int ok = 0;

    /* 1. Generate Randomness */
    if (!generate_random_scalar(ctx, k_m)) goto cleanup;
    if (!generate_random_scalar(ctx, k_r1)) goto cleanup;
    if (!generate_random_scalar(ctx, k_r2)) goto cleanup;

    /* 2. Commitments */
    if (!secp256k1_ec_pubkey_create(ctx, &T_m, k_m)) goto cleanup;

    if (!secp256k1_ec_pubkey_create(ctx, &T_r1_G, k_r1)) goto cleanup;
    T_r1_P1 = *P1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T_r1_P1, k_r1)) goto cleanup;

    if (!secp256k1_ec_pubkey_create(ctx, &T_r2_G, k_r2)) goto cleanup;
    T_r2_P2 = *P2;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &T_r2_P2, k_r2)) goto cleanup;

    /* 3. Challenge */
    build_same_plaintext_hash_input(ctx, e, R1, S1, P1, R2, S2, P2,
                                    &T_m, &T_r1_G, &T_r1_P1, &T_r2_G, &T_r2_P2,
                                    tx_context_id);

    /* 4. Responses */
    for (int i = 0; i < 8; ++i) m_scalar[31 - i] = (amount_m >> (i * 8)) & 0xFF;

    // s_m = k_m + e * m
    memcpy(s_m, k_m, 32);
    memcpy(term, m_scalar, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_m, term)) goto cleanup;

    // s_r1 = k_r1 + e * r1
    memcpy(s_r1, k_r1, 32);
    memcpy(term, randomness_r1, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_r1, term)) goto cleanup;

    // s_r2 = k_r2 + e * r2
    memcpy(s_r2, k_r2, 32);
    memcpy(term, randomness_r2, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_r2, term)) goto cleanup;

    /* 5. Serialize */
    unsigned char* ptr = proof_out;

    len = 33; if(!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T_m, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;
    len = 33; if(!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T_r1_G, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;
    len = 33; if(!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T_r1_P1, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;
    len = 33; if(!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T_r2_G, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;
    len = 33; if(!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &T_r2_P2, SECP256K1_EC_COMPRESSED)) goto cleanup; ptr += 33;

    memcpy(ptr, s_m, 32); ptr += 32;
    memcpy(ptr, s_r1, 32); ptr += 32;
    memcpy(ptr, s_r2, 32); ptr += 32;

    ok = 1;

    cleanup:
    OPENSSL_cleanse(k_m, 32);
    OPENSSL_cleanse(k_r1, 32);
    OPENSSL_cleanse(k_r2, 32);
    OPENSSL_cleanse(m_scalar, 32);
    return ok;
}

int secp256k1_mpt_verify_same_plaintext(
        const secp256k1_context* ctx,
        const unsigned char* proof,        // Caller MUST provide at least 261 bytes
        const secp256k1_pubkey* R1, const secp256k1_pubkey* S1, const secp256k1_pubkey* P1,
        const secp256k1_pubkey* R2, const secp256k1_pubkey* S2, const secp256k1_pubkey* P2,
        const unsigned char* tx_context_id
) {
    /* Fixed Size: 5 points (33) + 3 scalars (32) = 165 + 96 = 261 bytes */

    secp256k1_pubkey T_m, T_r1_G, T_r1_P1, T_r2_G, T_r2_P2;
    unsigned char s_m[32], s_r1[32], s_r2[32];
    unsigned char e[32];
    const unsigned char* ptr = proof;
    int ok = 0;

    secp256k1_pubkey LHS, RHS, term, SmG;
    const secp256k1_pubkey* pts[3];

    /* 1. Deserialize (Strict 261 bytes) */

    // Parse 5 Points (5 * 33 = 165 bytes)
    if (!secp256k1_ec_pubkey_parse(ctx, &T_m,    ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T_r1_G, ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T_r1_P1,ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T_r2_G, ptr, 33)) goto cleanup; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T_r2_P2,ptr, 33)) goto cleanup; ptr += 33;

    // Parse 3 Scalars (3 * 32 = 96 bytes)
    memcpy(s_m, ptr, 32); ptr += 32;
    memcpy(s_r1, ptr, 32); ptr += 32;
    memcpy(s_r2, ptr, 32); ptr += 32;

    // Sanity Check Scalars
    if (!secp256k1_ec_seckey_verify(ctx, s_m)) goto cleanup;
    if (!secp256k1_ec_seckey_verify(ctx, s_r1)) goto cleanup;
    if (!secp256k1_ec_seckey_verify(ctx, s_r2)) goto cleanup;

    /* 2. Challenge */
    build_same_plaintext_hash_input(ctx, e, R1, S1, P1, R2, S2, P2,
                                    &T_m, &T_r1_G, &T_r1_P1, &T_r2_G, &T_r2_P2,
                                    tx_context_id);

    /* 3. Verification Equations */

    // Precompute SmG (used in Eq 2 & 4)
    if (!secp256k1_ec_pubkey_create(ctx, &SmG, s_m)) goto cleanup;

    /* Eq 1: s_r1 * G == T_r1_G + e * R1 */
    if (!secp256k1_ec_pubkey_create(ctx, &LHS, s_r1)) goto cleanup;
    term = *R1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup;
    pts[0] = &T_r1_G; pts[1] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup;
    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    /* Eq 2: s_m * G + s_r1 * P1 == T_m + T_r1_P1 + e * S1 */
    term = *P1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, s_r1)) goto cleanup;
    pts[0] = &SmG; pts[1] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &LHS, pts, 2)) goto cleanup;

    term = *S1;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup;
    pts[0] = &T_m; pts[1] = &T_r1_P1; pts[2] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 3)) goto cleanup;
    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    /* Eq 3: s_r2 * G == T_r2_G + e * R2 */
    if (!secp256k1_ec_pubkey_create(ctx, &LHS, s_r2)) goto cleanup;
    term = *R2;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup;
    pts[0] = &T_r2_G; pts[1] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup;
    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    /* Eq 4: s_m * G + s_r2 * P2 == T_m + T_r2_P2 + e * S2 */
    term = *P2;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, s_r2)) goto cleanup;
    pts[0] = &SmG; pts[1] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &LHS, pts, 2)) goto cleanup;

    term = *S2;
    if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup;
    pts[0] = &T_m; pts[1] = &T_r2_P2; pts[2] = &term;
    if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 3)) goto cleanup;
    if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;

    ok = 1;

    cleanup:
    return ok;
}

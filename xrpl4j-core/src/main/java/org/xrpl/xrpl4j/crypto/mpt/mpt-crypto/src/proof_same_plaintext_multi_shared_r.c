/**
 * @file proof_same_plaintext_multi_shared_r.c
 * @brief Zero-Knowledge Proof of Plaintext Equality (1-to-N, Shared Randomness).
 *
 * This module implements an optimized multi-recipient Sigma protocol to prove that
 * \f$ N \f$ distinct ElGamal ciphertexts encrypt the **same** plaintext amount \f$ m \f$
 * using the **same** randomness \f$ r \f$, but under different public keys.
 *
 * @details
 * **Statement:**
 * Given a shared ephemeral key \f$ C_1 = r \cdot G \f$ and \f$ N \f$ components
 * \f$ C_{2,i} = m \cdot G + r \cdot P_i \f$ (where \f$ P_i \f$ is the public key for recipient \f$ i \f$),
 * the prover demonstrates knowledge of scalars \f$ m, r \f$ such that all equations hold.
 *
 * **Optimization:**
 * Unlike the general "Multi-Statement" proof (where \f$ r_i \f$ varies), this variant
 * enforces \f$ r_1 = r_2 = \dots = r_N = r \f$. This reduces the proof size significantly
 * because we only need one response scalar \f$ s_r \f$ for the randomness, rather than \f$ N \f$.
 *
 * **Protocol:**
 * 1. **Commitments:**
 * - \f$ T_r = k_r \cdot G \f$ (Commitment to shared randomness nonce)
 * - \f$ T_{m,i} = k_m \cdot G + k_r \cdot P_i \f$ (Commitment for each recipient)
 *
 * 2. **Challenge:**
 * \f$ e = H(\dots \parallel C_1 \parallel \{C_{2,i}, P_i\} \parallel T_r \parallel \{T_{m,i}\} \dots) \f$
 *
 * 3. **Responses:**
 * - \f$ s_m = k_m + e \cdot m \f$
 * - \f$ s_r = k_r + e \cdot r \f$
 *
 * 4. **Verification:**
 * - \f$ s_r \cdot G \stackrel{?}{=} T_r + e \cdot C_1 \f$
 * - For each \f$ i \f$: \f$ s_m \cdot G + s_r \cdot P_i \stackrel{?}{=} T_{m,i} + e \cdot C_{2,i} \f$
 *
 * **Security Context:**
 * This is used for broadcast-style transactions where the sender wants to prove to multiple
 * auditors or recipients that they are all receiving the exact same message/amount, efficiently.
 *
 * @see [Spec (ConfidentialMPT_20260201.pdf) Section 3.3.4] Proof of Equality of Plaintexts with Shared Randomness
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

size_t secp256k1_mpt_proof_equality_shared_r_size(size_t n_recipients) {
    // Tr (33) + N * Tm_i (33*N) + sm (32) + sr (32)
    return (33 * (n_recipients + 1)) + 64;
}

/*
 * Hash( Domain || C1 || {C2_i, Pk_i} || Tr || {Tm_i} || ContextID )
 */
static void compute_challenge_equality_shared_r(
        const secp256k1_context* ctx,
        unsigned char* e_out,
        size_t n,
        const secp256k1_pubkey* C1,
        const secp256k1_pubkey* C2_vec,
        const secp256k1_pubkey* Pk_vec,
        const secp256k1_pubkey* Tr,
        const secp256k1_pubkey* Tm_vec,
        const unsigned char* context_id
) {
    SHA256_CTX sha;
    unsigned char buf[33];
    unsigned char h[32];
    size_t len;
    size_t i;
    const char* domain = "MPT_POK_SAME_PLAINTEXT_SHARED_R";

    SHA256_Init(&sha);
    SHA256_Update(&sha, domain, strlen(domain));

    /* 1. Shared C1 */
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, C1, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    /* 2. Pairs {C2_i, Pk_i} */
    for (i = 0; i < n; i++) {
        len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, &C2_vec[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);
        len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, &Pk_vec[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);
    }

    /* 3. Commitment Tr */
    len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, Tr, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    /* 4. Commitments {Tm_i} */
    for (i = 0; i < n; i++) {
        len = 33; secp256k1_ec_pubkey_serialize(ctx, buf, &len, &Tm_vec[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);
    }

    /* 5. Transaction Context */
    if (context_id) {
        SHA256_Update(&sha, context_id, 32);
    }

    SHA256_Final(h, &sha);
    secp256k1_mpt_scalar_reduce32(e_out, h);
}

/* --- Public API --- */

int secp256k1_mpt_prove_equality_shared_r(
        const secp256k1_context* ctx,
        unsigned char* proof_out, // Caller MUST allocate secp256k1_mpt_proof_equality_shared_r_size(n)
        uint64_t amount,
        const unsigned char* r_shared,
        size_t n,
        const secp256k1_pubkey* C1,
        const secp256k1_pubkey* C2_vec,
        const secp256k1_pubkey* Pk_vec,
        const unsigned char* context_id
) {
    /* Local Variables */
    unsigned char k_m[32], k_r[32];
    unsigned char m_scalar[32] = {0};
    unsigned char e[32];
    unsigned char s_m[32], s_r[32];
    unsigned char term[32];

    secp256k1_pubkey Tr;
    secp256k1_pubkey* Tm_vec = NULL;
    int ok = 0;
    size_t i;
    unsigned char* ptr = proof_out;
    size_t len;

    /* 0. Validate Witness */
    if (!secp256k1_ec_seckey_verify(ctx, r_shared)) return 0;

    /* Allocate memory for commitments */
    if (n > 0) {
        Tm_vec = (secp256k1_pubkey*)malloc(sizeof(secp256k1_pubkey) * n);
        if (!Tm_vec) return 0;
    }

    /* 1. Prepare Witness */
    for (i = 0; i < 8; i++) {
        m_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }

    /* 2. Sample Random Nonces */
    if (!generate_random_scalar(ctx, k_m)) goto cleanup;
    if (!generate_random_scalar(ctx, k_r)) goto cleanup;

    /* 3. Compute Commitments */

    /* Tr = kr * G */
    if (!secp256k1_ec_pubkey_create(ctx, &Tr, k_r)) goto cleanup;

    /* Tm_i = km * G + kr * Pk_i */
    secp256k1_pubkey kmG;
    if (!secp256k1_ec_pubkey_create(ctx, &kmG, k_m)) goto cleanup;

    for (i = 0; i < n; i++) {
        secp256k1_pubkey krPk = Pk_vec[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &krPk, k_r)) goto cleanup; // kr * Pk

        const secp256k1_pubkey* pts[2] = {&kmG, &krPk};
        if (!secp256k1_ec_pubkey_combine(ctx, &Tm_vec[i], pts, 2)) goto cleanup;
    }

    /* 4. Compute Challenge */
    compute_challenge_equality_shared_r(ctx, e, n, C1, C2_vec, Pk_vec, &Tr, Tm_vec, context_id);

    /* 5. Compute Responses */

    /* s_m = k_m + e * m */
    memcpy(s_m, k_m, 32);
    memcpy(term, m_scalar, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_m, term)) goto cleanup;

    /* s_r = k_r + e * r */
    memcpy(s_r, k_r, 32);
    memcpy(term, r_shared, 32);
    if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup;
    if (!secp256k1_ec_seckey_tweak_add(ctx, s_r, term)) goto cleanup;

    /* 6. Serialize Proof */

    /* Serialize Tr */
    len = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &Tr, SECP256K1_EC_COMPRESSED)) goto cleanup;
    ptr += 33;

    /* Serialize Tm_i array */
    for (i = 0; i < n; i++) {
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &Tm_vec[i], SECP256K1_EC_COMPRESSED)) goto cleanup;
        ptr += 33;
    }

    /* Serialize Scalars */
    memcpy(ptr, s_m, 32); ptr += 32;
    memcpy(ptr, s_r, 32); ptr += 32;

    ok = 1;

    cleanup:
    // Wipe Secrets
    OPENSSL_cleanse(k_m, 32);
    OPENSSL_cleanse(k_r, 32);
    OPENSSL_cleanse(m_scalar, 32);

    // Wipe Intermediates
    OPENSSL_cleanse(term, 32);
    OPENSSL_cleanse(s_m, 32);
    OPENSSL_cleanse(s_r, 32);

    if (Tm_vec) free(Tm_vec);
    return ok;
}
int secp256k1_mpt_verify_equality_shared_r(
        const secp256k1_context* ctx,
        const unsigned char* proof,     // Caller MUST provide buffer of size: secp256k1_mpt_proof_equality_shared_r_size(n)
        size_t n,
        const secp256k1_pubkey* C1,
        const secp256k1_pubkey* C2_vec,
        const secp256k1_pubkey* Pk_vec,
        const unsigned char* context_id
) {
    /* Calculate expected size internally for strict checking later */
    size_t expected_len = secp256k1_mpt_proof_equality_shared_r_size(n);

    /* Local Variables */
    secp256k1_pubkey Tr;
    secp256k1_pubkey* Tm_vec = NULL;
    unsigned char s_m[32], s_r[32];
    unsigned char e[32];
    int ok = 0;
    size_t i;
    const unsigned char* ptr = proof;

    /* Allocate memory for commitments */
    Tm_vec = (secp256k1_pubkey*)malloc(sizeof(secp256k1_pubkey) * n);
    if (!Tm_vec) return 0;

    /* 1. Deserialize Proof */

    // Parse Tr (33 bytes)
    if (!secp256k1_ec_pubkey_parse(ctx, &Tr, ptr, 33)) goto cleanup;
    ptr += 33;

    // Parse N commitments (N * 33 bytes)
    for (i = 0; i < n; i++) {
        if (!secp256k1_ec_pubkey_parse(ctx, &Tm_vec[i], ptr, 33)) goto cleanup;
        ptr += 33;
    }

    // Parse Scalars (32 + 32 bytes)
    memcpy(s_m, ptr, 32); ptr += 32;
    memcpy(s_r, ptr, 32); ptr += 32;

    // Sanity check scalars
    if (!secp256k1_ec_seckey_verify(ctx, s_m)) goto cleanup;
    if (!secp256k1_ec_seckey_verify(ctx, s_r)) goto cleanup;

    /* 2. Challenge */
    compute_challenge_equality_shared_r(ctx, e, n, C1, C2_vec, Pk_vec, &Tr, Tm_vec, context_id);

    /* 3. Verification Equations */

    /* Eq 1: sr * G == Tr + e * C1 */
    {
        secp256k1_pubkey LHS, RHS;
        secp256k1_pubkey eC1 = *C1;

        if (!secp256k1_ec_pubkey_create(ctx, &LHS, s_r)) goto cleanup; // sr*G

        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &eC1, e)) goto cleanup; // e*C1
        const secp256k1_pubkey* pts[2] = {&Tr, &eC1};
        if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup; // Tr + e*C1

        if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
    }

    /* Eq 2: For each i, sm * G + sr * Pk_i == Tm_i + e * C2_i */
    {
        secp256k1_pubkey smG;
        if (!secp256k1_ec_pubkey_create(ctx, &smG, s_m)) goto cleanup; // Precompute sm*G

        for (i = 0; i < n; i++) {
            secp256k1_pubkey LHS, RHS;

            /* LHS = sm*G + sr*Pk_i */
            secp256k1_pubkey srPk = Pk_vec[i];
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &srPk, s_r)) goto cleanup;

            const secp256k1_pubkey* lhs_pts[2] = {&smG, &srPk};
            if (!secp256k1_ec_pubkey_combine(ctx, &LHS, lhs_pts, 2)) goto cleanup;

            /* RHS = Tm_i + e*C2_i */
            secp256k1_pubkey eC2 = C2_vec[i];
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &eC2, e)) goto cleanup;

            const secp256k1_pubkey* rhs_pts[2] = {&Tm_vec[i], &eC2};
            if (!secp256k1_ec_pubkey_combine(ctx, &RHS, rhs_pts, 2)) goto cleanup;

            if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
        }
    }

    // Strict Length Check: Ensure we read exactly what was expected
    if ((size_t)(ptr - proof) != expected_len) goto cleanup;

    ok = 1;

    cleanup:
    if (Tm_vec) free(Tm_vec);
    return ok;
}

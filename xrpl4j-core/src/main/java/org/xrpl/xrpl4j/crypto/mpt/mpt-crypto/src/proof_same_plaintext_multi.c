/**
 * @file proof_same_plaintext_multi.c
 * @brief Zero-Knowledge Proof of Plaintext Equality (1-to-N).
 *
 * This module implements a generalized multi-statement Sigma protocol to prove that
 * \f$ N \f$ distinct ElGamal ciphertexts all encrypt the **same** underlying
 * plaintext amount \f$ m \f$, using distinct randomness \f$ r_i \f$ for each.
 *
 * @details
 * **Statement:**
 * Given \f$ N \f$ ciphertexts \f$ (R_i, S_i) \f$ encrypted under public keys \f$ P_i \f$,
 * the prover demonstrates knowledge of scalars \f$ m \f$ and \f$ \{r_1, \dots, r_N\} \f$
 * such that for all \f$ i \in [1, N] \f$:
 * 1. \f$ R_i = r_i \cdot G \f$
 * 2. \f$ S_i = m \cdot G + r_i \cdot P_i \f$
 *
 * **Protocol (Shared Amount Nonce):**
 * The efficiency gain comes from reusing the random nonce for the amount (\f$ k_m \f$)
 * across all \f$ N \f$ proofs, tying them mathematically to the same value \f$ m \f$.
 *
 * 1. **Commitments:**
 * - \f$ T_m = k_m \cdot G \f$ (Shared commitment to amount nonce)
 * - For each \f$ i \f$:
 * - \f$ T_{r,G}^{(i)} = k_{r,i} \cdot G \f$
 * - \f$ T_{r,P}^{(i)} = k_{r,i} \cdot P_i \f$
 *
 * 2. **Challenge:**
 * \f[ e = H(\dots \parallel T_m \parallel \{T_{r,G}^{(i)}, T_{r,P}^{(i)}\}_{i=1}^N \parallel \dots) \f]
 *
 * 3. **Responses:**
 * - \f$ s_m = k_m + e \cdot m \f$ (Shared response for amount)
 * - For each \f$ i \f$: \f$ s_{r,i} = k_{r,i} + e \cdot r_i \f$
 *
 * 4. **Verification:**
 * For each \f$ i \in [1, N] \f$, the verifier checks:
 * - \f$ s_{r,i} \cdot G \stackrel{?}{=} T_{r,G}^{(i)} + e \cdot R_i \f$
 * - \f$ s_m \cdot G + s_{r,i} \cdot P_i \stackrel{?}{=} T_m + T_{r,P}^{(i)} + e \cdot S_i \f$
 *
 * **Security Context:**
 * This is crucial for "fan-out" transactions or auditing scenarios where a single value
 * must be proven correct against multiple encrypted destinations simultaneously, ensuring
 * consistency without revealing the value.
 *
 * @see [Spec (ConfidentialMPT_20260106.pdf) Section 3.3.4] Generalization for Multiple Ciphertexts
 */
#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <stdlib.h>

/* Helper for comparing public keys (from internal utils) */
static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* pk1, const secp256k1_pubkey* pk2) {
    return secp256k1_ec_pubkey_cmp(ctx, pk1, pk2) == 0;
}

static int generate_random_scalar(const secp256k1_context* ctx, unsigned char* scalar) {
    do {
        if (RAND_bytes(scalar, 32) != 1) return 0;
    } while (!secp256k1_ec_seckey_verify(ctx, scalar));
    return 1;
}

/*
 * Hash( Domain || {R_i, S_i, Pk_i} || Tm || {TrG_i, TrP_i} || TxID )
 */
static void compute_challenge_multi(
        const secp256k1_context* ctx,
        unsigned char* e_out,
        size_t n,
        const secp256k1_pubkey* R,
        const secp256k1_pubkey* S,
        const secp256k1_pubkey* Pk,
        const secp256k1_pubkey* Tm,
        const secp256k1_pubkey* TrG,
        const secp256k1_pubkey* TrP,
        const unsigned char* tx_id
) {
    SHA256_CTX sha;
    unsigned char buf[33];
    unsigned char h[32];
    size_t len;
    size_t i;
    const char* domain = "MPT_POK_SAME_PLAINTEXT_PROOF";

    SHA256_Init(&sha);
    SHA256_Update(&sha, domain, strlen(domain));

    /* 1. Public Inputs */
    for (i = 0; i < n; ++i) {
        len = 33;
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, &R[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);

        len = 33;
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, &S[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);

        len = 33;
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, &Pk[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);
    }

    /* 2. Commitments */
    len = 33;
    secp256k1_ec_pubkey_serialize(ctx, buf, &len, Tm, SECP256K1_EC_COMPRESSED);
    SHA256_Update(&sha, buf, 33);

    for (i = 0; i < n; ++i) {
        len = 33;
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, &TrG[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);

        len = 33;
        secp256k1_ec_pubkey_serialize(ctx, buf, &len, &TrP[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, buf, 33);
    }

    /* 3. Context */
    if (tx_id) {
        SHA256_Update(&sha, tx_id, 32);
    }

    SHA256_Final(h, &sha);
    secp256k1_mpt_scalar_reduce32(e_out, h);
}

/* --- Public API --- */

size_t secp256k1_mpt_prove_same_plaintext_multi_size(size_t n) {
    // (1 Tm + 2N Tr) * 33 + (1 sm + N sr) * 32
    return ((1 + 2 * n) * 33) + ((1 + n) * 32);
}

int secp256k1_mpt_prove_same_plaintext_multi(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        size_t* proof_len,
        uint64_t amount_m,
        size_t n,
        const secp256k1_pubkey* R,
        const secp256k1_pubkey* S,
        const secp256k1_pubkey* Pk,
        const unsigned char* r_array,
        const unsigned char* tx_id
) {
    size_t required_len = secp256k1_mpt_prove_same_plaintext_multi_size(n);
    if (!proof_len || *proof_len < required_len) {
        if (proof_len) *proof_len = required_len;
        return 0;
    }
    *proof_len = required_len;

    /* Heap Allocation to avoid Stack Overflow on large N */
    unsigned char* k_r_flat = NULL; // Stores n * 32 bytes
    secp256k1_pubkey* TrG = NULL;
    secp256k1_pubkey* TrP = NULL;

    unsigned char k_m[32];
    secp256k1_pubkey Tm;
    unsigned char e[32];
    unsigned char s_m[32];

    int ok = 0;
    size_t i;
    unsigned char* ptr = proof_out;

    /* Allocations */
    k_r_flat = (unsigned char*)malloc(n * 32);
    TrG = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    TrP = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));

    if (!k_r_flat || !TrG || !TrP) goto cleanup;

    /* 1. Generate Randomness & Commitments */

    // km -> Tm = km * G
    if (!generate_random_scalar(ctx, k_m)) goto cleanup;
    if (!secp256k1_ec_pubkey_create(ctx, &Tm, k_m)) goto cleanup;

    for (i = 0; i < n; i++) {
        unsigned char* kri = &k_r_flat[i * 32];

        // kri -> TrG = kri * G
        if (!generate_random_scalar(ctx, kri)) goto cleanup;
        if (!secp256k1_ec_pubkey_create(ctx, &TrG[i], kri)) goto cleanup;

        // TrP = kri * Pk_i
        TrP[i] = Pk[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &TrP[i], kri)) goto cleanup;
    }

    /* 2. Compute Challenge */
    compute_challenge_multi(ctx, e, n, R, S, Pk, &Tm, TrG, TrP, tx_id);

    /* 3. Compute Responses */

    // s_m = k_m + e * m
    {
        unsigned char m_scalar[32] = {0};
        // Convert uint64 to big-endian 32-byte
        for (i = 0; i < 8; ++i) m_scalar[31 - i] = (amount_m >> (i * 8)) & 0xFF;

        memcpy(s_m, k_m, 32);
        if (!secp256k1_ec_seckey_tweak_mul(ctx, m_scalar, e)) goto cleanup; // m*e
        if (!secp256k1_ec_seckey_tweak_add(ctx, s_m, m_scalar)) goto cleanup; // km + m*e
    }

    // Serialize Points first (Protocol Format)
    size_t len = 33;
    secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &Tm, SECP256K1_EC_COMPRESSED); ptr += 33;
    for(i=0; i<n; ++i) {
        secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &TrG[i], SECP256K1_EC_COMPRESSED); ptr += 33;
    }
    for(i=0; i<n; ++i) {
        secp256k1_ec_pubkey_serialize(ctx, ptr, &len, &TrP[i], SECP256K1_EC_COMPRESSED); ptr += 33;
    }

    // Serialize sm
    memcpy(ptr, s_m, 32); ptr += 32;

    // Calculate and Serialize sri = kri + e * ri
    for (i = 0; i < n; i++) {
        unsigned char s_ri[32];
        unsigned char term[32];

        memcpy(s_ri, &k_r_flat[i*32], 32);        // k_ri
        memcpy(term, &r_array[i*32], 32);         // r_i

        if (!secp256k1_ec_seckey_tweak_mul(ctx, term, e)) goto cleanup; // r*e
        if (!secp256k1_ec_seckey_tweak_add(ctx, s_ri, term)) goto cleanup; // k + r*e

        memcpy(ptr, s_ri, 32); ptr += 32;
    }

    ok = 1;

    cleanup:
    if (k_r_flat) {
        // Secure wipe of randomness
        OPENSSL_cleanse(k_r_flat, n * 32);
        free(k_r_flat);
    }
    OPENSSL_cleanse(k_m, 32);
    if (TrG) free(TrG);
    if (TrP) free(TrP);
    return ok;
}

int secp256k1_mpt_verify_same_plaintext_multi(
        const secp256k1_context* ctx,
        const unsigned char* proof,
        size_t proof_len,
        size_t n,
        const secp256k1_pubkey* R,
        const secp256k1_pubkey* S,
        const secp256k1_pubkey* Pk,
        const unsigned char* tx_id
) {
    if (proof_len != secp256k1_mpt_prove_same_plaintext_multi_size(n)) return 0;

    secp256k1_pubkey Tm;
    secp256k1_pubkey* TrG = NULL;
    secp256k1_pubkey* TrP = NULL;
    unsigned char s_m[32];
    unsigned char e[32];
    int ok = 0;
    size_t i;
    const unsigned char* ptr = proof;

    TrG = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    TrP = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    if (!TrG || !TrP) goto cleanup;

    /* 1. Deserialize */
    if (!secp256k1_ec_pubkey_parse(ctx, &Tm, ptr, 33)) goto cleanup; ptr += 33;

    for(i=0; i<n; ++i) {
        if (!secp256k1_ec_pubkey_parse(ctx, &TrG[i], ptr, 33)) goto cleanup; ptr += 33;
    }
    for(i=0; i<n; ++i) {
        if (!secp256k1_ec_pubkey_parse(ctx, &TrP[i], ptr, 33)) goto cleanup; ptr += 33;
    }

    memcpy(s_m, ptr, 32); ptr += 32;
    if (!secp256k1_ec_seckey_verify(ctx, s_m)) goto cleanup;

    /* 2. Recompute Challenge */
    compute_challenge_multi(ctx, e, n, R, S, Pk, &Tm, TrG, TrP, tx_id);

    /* 3. Verify Equations */

    /* Precompute s_m * G (Shared across all i) */
    secp256k1_pubkey SmG;
    if (!secp256k1_ec_pubkey_create(ctx, &SmG, s_m)) goto cleanup;

    for(i=0; i<n; ++i) {
        unsigned char s_ri[32];
        memcpy(s_ri, ptr, 32); ptr += 32;
        if (!secp256k1_ec_seckey_verify(ctx, s_ri)) goto cleanup;

        secp256k1_pubkey LHS, RHS, term;
        const secp256k1_pubkey* pts[3];

        /* --- Eq 1: s_ri * G == TrG_i + e * R_i --- */
        if (!secp256k1_ec_pubkey_create(ctx, &LHS, s_ri)) goto cleanup;

        term = R[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup;
        pts[0] = &TrG[i]; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 2)) goto cleanup;

        if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;


        /* --- Eq 2: s_m * G + s_ri * Pk_i == Tm + TrP_i + e * S_i --- */

        /* LHS = SmG + s_ri * Pk_i */
        term = Pk[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, s_ri)) goto cleanup;
        pts[0] = &SmG; pts[1] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &LHS, pts, 2)) goto cleanup;

        /* RHS = Tm + TrP_i + e * S_i */
        term = S[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, e)) goto cleanup;
        pts[0] = &Tm; pts[1] = &TrP[i]; pts[2] = &term;
        if (!secp256k1_ec_pubkey_combine(ctx, &RHS, pts, 3)) goto cleanup;

        if (!pubkey_equal(ctx, &LHS, &RHS)) goto cleanup;
    }

    if ((size_t)(ptr - proof) != proof_len) goto cleanup;
    ok = 1;

    cleanup:
    if (TrG) free(TrG);
    if (TrP) free(TrP);
    return ok;
}

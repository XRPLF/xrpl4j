/**
 * @file bulletproof_aggregated.c
 * @brief Aggregated Bulletproof Range Proofs (Logarithmic Size).
 *
 * This module implements non-interactive zero-knowledge range proofs based on
 * the Bulletproofs protocol (Bünz et al., 2018). It allows a prover to demonstrate
 * that a committed value lies within the range \f$ [0, 2^{64}) \f$ without revealing
 * the value itself.
 *
 * @details
 * **Protocol Overview:**
 * The implementation follows the standard single-value and aggregated Bulletproof logic:
 * 1. **Pedersen Commitment:** The value \f$ v \f$ is committed as \f$ V = v \cdot G + r \cdot H \f$.
 * 2. **Bit Decomposition:** The value is decomposed into 64 bits \f$ \mathbf{a}_L \f$.
 * 3. **Polynomial Commitment:** The prover commits to polynomials defining the range constraints.
 * 4. **Inner Product Argument (IPA):** A recursive argument reduces the proof size to
 * logarithmic complexity \f$ \mathcal{O}(\log n) \f$.
 *
 * **Aggregation:**
 * This implementation supports aggregating \f$ m \f$ proofs into a single verification
 * process. The total vector length is \f$ n = 64 \cdot m \f$. Aggregation significantly
 * reduces the on-chain footprint compared to \f$ m \f$ individual proofs.
 *
 * **Fiat-Shamir Transcript:**
 * The non-interactive challenge generation follows a strict dependency chain to ensure
 * binding and special soundness:
 * - \f$ \mathcal{T}_0 \f$: Domain Tag || ContextID || Value Commitments (\f$ V \f$)
 * - \f$ y, z \f$: Derived from \f$ \mathcal{T}_0 \parallel A \parallel S \f$
 * - \f$ x \f$: Derived from \f$ z \parallel T_1 \parallel T_2 \f$
 * - \f$ \mu \f$: Derived from \f$ x \f$ (for the IPA)
 *
 * **Dependencies:**
 * - Relies on `secp256k1` for elliptic curve arithmetic.
 * - Uses `SHA256` for the Fiat-Shamir transformation.
 *
 * @see [Spec (ConfidentialMPT_20260201.pdf) Section 3.3.6 Range Proof (using Bulletproofs)]
 */
#include "secp256k1_mpt.h"
#include <openssl/sha.h>
#include <openssl/rand.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <secp256k1.h>

/* Bit-size of each value proved in range */
#define BP_VALUE_BITS 64

/* Compute total vector length for aggregated Bulletproof */
#define BP_TOTAL_BITS(m) ((size_t)(BP_VALUE_BITS * (m)))

/* Compute IPA rounds = log2(total_bits) */
static inline size_t bp_ipa_rounds(size_t total_bits) {
    size_t r = 0;
    while (total_bits > 1) {
        total_bits >>= 1;
        r++;
    }
    return r;
}

/** Generates a secure 32-byte random scalar.
 * Returns 1 on success, 0 on failure.
 */
static int generate_random_scalar(
        const secp256k1_context* ctx,
        unsigned char* scalar_bytes)
{
    do {
        if (RAND_bytes(scalar_bytes, 32) != 1) {
            return 0; // Randomness failure
        }
    } while (secp256k1_ec_seckey_verify(ctx, scalar_bytes) != 1);
    return 1;
}

/**
 * Computes the point M = amount * G.
 * Internal helper used by commitment construction.
 */
static int compute_amount_point(
        const secp256k1_context* ctx,
        secp256k1_pubkey* mG,
        uint64_t amount)
{
    unsigned char amount_scalar[32] = {0};

    /* Zero amount is handled by the caller (no G term needed) */
    if (amount == 0) {
        return 0;
    }

    for (int i = 0; i < 8; ++i) {
        amount_scalar[31 - i] = (amount >> (i * 8)) & 0xFF;
    }
    return secp256k1_ec_pubkey_create(ctx, mG, amount_scalar);
}
/**
 * Safely adds a point to an accumulator (acc += term).
 * Handles uninitialized accumulators by assignment instead of addition.
 */
static int add_term(
        const secp256k1_context* ctx,
        secp256k1_pubkey* acc,
        int* acc_inited,
        const secp256k1_pubkey* term
) {
    if (!(*acc_inited)) {
        *acc = *term;
        *acc_inited = 1;
        return 1;
    } else {
        const secp256k1_pubkey* pts[2] = { acc, term };
        secp256k1_pubkey sum;
        if (!secp256k1_ec_pubkey_combine(ctx, &sum, pts, 2)) return 0;
        *acc = sum;
        return 1;
    }
}

/**
 * Computes modular subtraction of two scalars: res = a - b (mod q).
 */
static void secp256k1_mpt_scalar_sub(unsigned char *res,
                              const unsigned char *a,
                              const unsigned char *b)
{
    unsigned char neg_b[32];
    memcpy(neg_b, b, 32);
    secp256k1_mpt_scalar_negate(neg_b, neg_b);   /* neg_b = -b mod q */
    secp256k1_mpt_scalar_add(res, a, neg_b);     /* res = a + (-b) */
    OPENSSL_cleanse(neg_b, 32);
}
/**
 * Computes the modular dot product c = <a, b> = sum(a[i] * b[i]) mod q.
 * This function calculates the inner product of two scalar vectors.
 * ctx       The context.
 * out       Output 32-byte scalar (the inner product result).
 * a         Input scalar vector A (n * 32 bytes).
 * b         Input scalar vector B (n * 32 bytes).
 * n         The length of the vectors.
 * 1 on success, 0 on failure.
 */
int secp256k1_bulletproof_ipa_dot(
        const secp256k1_context* ctx,
        unsigned char* out,
        const unsigned char* a,
        const unsigned char* b,
        size_t n
) {
    unsigned char acc[32] = {0};
    unsigned char term[32];

    for (size_t i = 0; i < n; i++) {
        secp256k1_mpt_scalar_mul(term, a + i * 32, b + i * 32);
        secp256k1_mpt_scalar_add(acc, acc, term);
    }
    memcpy(out, acc, 32);
    return 1;
}
/**
 * Internal helper for multi-scalar multiplication.
 * Adds a point to the accumulator.
 */

int secp256k1_bulletproof_add_point_to_accumulator(
        const secp256k1_context* ctx,
        secp256k1_pubkey* acc,
        const secp256k1_pubkey* term)
{
    const secp256k1_pubkey* points[2] = {acc, term};
    secp256k1_pubkey temp_sum;

    if (secp256k1_ec_pubkey_combine(ctx, &temp_sum, points, 2) != 1) return 0;
    *acc = temp_sum;
    return 1;
}
/**
 * Computes Multiscalar Multiplication (MSM): R = sum(s[i] * P[i]).
 * ctx       The context.
 * r_out     Output point (the sum R).
 * points    Array of N input points (secp256k1_pubkey).
 * scalars   Flat array of N 32-byte scalars.
 * n         The number of terms (N).
 * return    1 on success, 0 on failure.
 * NOTE: This MSM is used only for Bulletproofs where all scalars are public.
 * It is NOT constant-time with respect to scalars and MUST NOT be used
 * for secret-key operations.
 */
int secp256k1_bulletproof_ipa_msm(
        const secp256k1_context* ctx,
        secp256k1_pubkey* r_out,
        const secp256k1_pubkey* points,
        const unsigned char* scalars,
        size_t n
) {
    secp256k1_pubkey acc;
    memset(&acc, 0, sizeof(acc));
    int initialized = 0;
    unsigned char zero[32] = {0};

    for (size_t i = 0; i < n; ++i) {
        if (memcmp(scalars + i * 32, zero, 32) == 0)
            continue;

        secp256k1_pubkey term = points[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, scalars + i * 32))
            return 0;

        if (!add_term(ctx, &acc, &initialized, &term))
            return 0;
    }

    /* All scalars zero → result is infinity (not representable here) */
    if (!initialized)
        return 0;

    *r_out = acc;
    return 1;
}
/* Try to add MSM(points, scalars) into acc.
 * If MSM is all-zero, do nothing and succeed.
 */
static int msm_try_add(
        const secp256k1_context* ctx,
        secp256k1_pubkey* acc,
        int* acc_inited,
        const secp256k1_pubkey* points,
        const unsigned char* scalars,
        size_t n
) {
    secp256k1_pubkey tmp;

    /* MSM returns 0 iff all scalars are zero.
     * In that case, we have nothing to add, so we return success (1). */
    if (!secp256k1_bulletproof_ipa_msm(ctx, &tmp, points, scalars, n)) {
        return 1;
    }
    return add_term(ctx, acc, acc_inited, &tmp);
}

/**
 * Computes component-wise: result[i] = a[i] * b[i] (Hadamard product)
 */
void scalar_vector_mul(const secp256k1_context* ctx, unsigned char res[][32],
                       unsigned char a[][32], unsigned char b[][32], size_t n) {
    for (size_t i = 0; i < n; i++) {
        secp256k1_mpt_scalar_mul(res[i], a[i], b[i]);
    }
}

/**
 * Computes component-wise: result[i] = a[i] + b[i]
 */
void scalar_vector_add(const secp256k1_context* ctx, unsigned char res[][32],
                       unsigned char a[][32], unsigned char b[][32], size_t n) {
    for (size_t i = 0; i < n; i++) {
        secp256k1_mpt_scalar_add(res[i], a[i], b[i]);
    }

}

/**
 * Fills a vector with powers of a scalar: [1, y, y^2, ..., y^{n-1}]
 */
void scalar_vector_powers(
        const secp256k1_context* ctx,
        unsigned char res[][32],
        const unsigned char* y,
        size_t n
) {
    if (n == 0) return;

    unsigned char one[32] = {0};
    one[31] = 1;
    memcpy(res[0], one, 32);

    for (size_t i = 1; i < n; i++) {
        secp256k1_mpt_scalar_mul(res[i], res[i-1], y);
    }
}
/**
 * Compute y^i for small i.
 */
static void scalar_pow_u32(
        const secp256k1_context* ctx,
        unsigned char y_pow_out[32],
        const unsigned char y[32],
        unsigned int i)
{
    (void)ctx;
    unsigned char one[32] = {0};
    one[31] = 1;
    memcpy(y_pow_out, one, 32);

    while (i--) {
        secp256k1_mpt_scalar_mul(y_pow_out, y_pow_out, y);
    }
}
/**
 * z_j2 = z^(j+2) for j = 0..m-1 (small exponent)
*/
static void compute_z_pows_j2(
        const secp256k1_context* ctx,
        unsigned char (*z_j2)[32], /* m x 32 */
        const unsigned char z[32],
        size_t m
) {
    for (size_t j = 0; j < m; j++) {
        scalar_pow_u32(ctx, z_j2[j], z, (unsigned int)(j + 2));
    }
}

/**
 * Point = Scalar * Point (using public API)
 */
static int secp256k1_bulletproof_point_scalar_mul(
        const secp256k1_context* ctx,
        secp256k1_pubkey* r_out,
        const secp256k1_pubkey* p_in,
        const unsigned char* s_scalar)
{
    *r_out = *p_in;
    return secp256k1_ec_pubkey_tweak_mul(ctx, r_out, s_scalar);
}

/**
 * Computes per-block y-power sums for aggregated Bulletproofs.
 * For block j (0-based):
 *   y_block_sum[j] = sum_{i=0}^{63} y^{64*j + i}
 * Also computes:
 *   two_sum = sum_{i=0}^{63} 2^i
 * These are used by the caller to construct delta(y, z).
 */
static void compute_delta_scalars(
        const secp256k1_context* ctx,
        unsigned char (*y_block_sum)[32], /* m blocks */
        unsigned char two_sum[32],
        const unsigned char y[32],
        size_t m
) {
    (void)ctx;

    unsigned char one[32] = {0};
    unsigned char y_pow[32];
    unsigned char two_pow[32];

    one[31] = 1;

    /* Compute two_sum = sum_{i=0}^{63} 2^i */
    memset(two_sum, 0, 32);
    memcpy(two_pow, one, 32);
    for (size_t i = 0; i < 64; i++) {
        secp256k1_mpt_scalar_add(two_sum, two_sum, two_pow);
        secp256k1_mpt_scalar_add(two_pow, two_pow, two_pow);
    }

    /* Compute y_block_sum[j] = sum_{i=0}^{63} y^{64j + i} */
    memcpy(y_pow, one, 32); /* y^0 */

    for (size_t j = 0; j < m; j++) {
        memset(y_block_sum[j], 0, 32);

        for (size_t i = 0; i < 64; i++) {
            secp256k1_mpt_scalar_add(y_block_sum[j], y_block_sum[j], y_pow);
            secp256k1_mpt_scalar_mul(y_pow, y_pow, y); /* advance y^k */
        }
    }
}
/**
 * Compare two secp256k1 public keys for equality.
 */
static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* a, const secp256k1_pubkey* b) {
    return secp256k1_ec_pubkey_cmp(ctx, a, b) == 0;
}
/**
 * u_flat and uinv_flat are arrays of length (rounds * 32):
 *   u_j     = u_flat    + 32*j
 *   u_j_inv = uinv_flat + 32*j
 */
int fold_generators(
        const secp256k1_context* ctx,
        secp256k1_pubkey* final_point,
        const secp256k1_pubkey* generators,
        const unsigned char* u_flat,
        const unsigned char* uinv_flat,
        size_t n,
        size_t rounds,
        int is_H   /* 0 = G folding, 1 = H folding */
) {
    /* n must be power-of-two and rounds must match log2(n) */
    if (n == 0 || (n & (n - 1)) != 0) return 0;
    if (((size_t)1 << rounds) != n) return 0;

    /* Allocate scalars for MSM: n * 32 bytes */
    unsigned char* s_flat = (unsigned char*)malloc(n * 32);
    if (!s_flat) return 0;

    unsigned char current_s[32];
    int ok = 0;

    for (size_t i = 0; i < n; i++) {
        /* current_s = 1 */
        memset(current_s, 0, 32);
        current_s[31] = 1;

        for (size_t j = 0; j < rounds; j++) {
            /* bit from MSB to LSB across 'rounds' bits */
            int bit = (int)((i >> (rounds - 1 - j)) & 1);

            const unsigned char* uj    = u_flat    + 32 * j;
            const unsigned char* ujinv = uinv_flat + 32 * j;

            if (!is_H) {
                /* G folding: bit 0 -> u_inv, bit 1 -> u */
                secp256k1_mpt_scalar_mul(current_s, current_s, bit ? uj : ujinv);
            } else {
                /* H folding: bit 0 -> u, bit 1 -> u_inv */
                secp256k1_mpt_scalar_mul(current_s, current_s, bit ? ujinv : uj);
            }
        }

        memcpy(s_flat + (i * 32), current_s, 32);
    }

    ok = secp256k1_bulletproof_ipa_msm(ctx, final_point, generators, s_flat, n);

    OPENSSL_cleanse(current_s, 32);
    OPENSSL_cleanse(s_flat, n * 32);
    free(s_flat);

    return ok;
}
/*
 * Apply verifier-side IPA updates to P for `rounds` rounds.
 * Update rule per round i:
 *   P <- P + (u_i^2) * L_i + (u_i^{-2}) * R_i
 * u_flat / uinv_flat are (rounds * 32)-byte arrays:
 *   u_i    = u_flat    + 32*i
 *   u_iinv = uinv_flat + 32*i
 */
int apply_ipa_folding_to_P(
        const secp256k1_context* ctx,
        secp256k1_pubkey* P,
        const secp256k1_pubkey* L_vec,
        const secp256k1_pubkey* R_vec,
        const unsigned char* u_flat,
        const unsigned char* uinv_flat,
        size_t rounds
) {
    unsigned char u_sq[32], uinv_sq[32];
    secp256k1_pubkey tL, tR;
    const secp256k1_pubkey* pts[3];

    for (size_t i = 0; i < rounds; i++) {
        const unsigned char* ui    = u_flat    + 32 * i;
        const unsigned char* uiinv = uinv_flat + 32 * i;

        /* u_sq = u_i^2, uinv_sq = (u_i^{-1})^2 = u_i^{-2} */
        secp256k1_mpt_scalar_mul(u_sq, ui, ui);
        secp256k1_mpt_scalar_mul(uinv_sq, uiinv, uiinv);

        /* tL = (u_i^2) * L_i */
        tL = L_vec[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tL, u_sq)) {
            OPENSSL_cleanse(u_sq, 32);
            OPENSSL_cleanse(uinv_sq, 32);
            return 0;
        }

        /* tR = (u_i^{-2}) * R_i */
        tR = R_vec[i];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tR, uinv_sq)) {
            OPENSSL_cleanse(u_sq, 32);
            OPENSSL_cleanse(uinv_sq, 32);
            return 0;
        }

        /* P <- P + tL + tR */
        pts[0] = P;
        pts[1] = &tL;
        pts[2] = &tR;

        secp256k1_pubkey newP;
        if (!secp256k1_ec_pubkey_combine(ctx, &newP, pts, 3)) {
            OPENSSL_cleanse(u_sq, 32);
            OPENSSL_cleanse(uinv_sq, 32);
            return 0;
        }
        *P = newP;

    }

    OPENSSL_cleanse(u_sq, 32);
    OPENSSL_cleanse(uinv_sq, 32);
    return 1;
}

/**
 * Computes the cross-term commitments L and R.
 * L = <a_L, G_R> + <b_R, H_L> + c_L * ux * g
 * R = <a_R, G_L> + <b_L, H_R> + c_R * ux * g
 * ctx       The context.
 * L         Output: Commitment point L_j.
 * R         Output: Commitment point R_j.
 * half_n    Length of the input vector halves.
 * g         The blinding generator point (Pk_base in our case).
 * return    1 on success, 0 on failure.
 */

int secp256k1_bulletproof_ipa_compute_LR(
        const secp256k1_context* ctx,
        secp256k1_pubkey* L,
        secp256k1_pubkey* R,
        const unsigned char* a_L,
        const unsigned char* a_R,
        const unsigned char* b_L,
        const unsigned char* b_R,
        const secp256k1_pubkey* G_L,
        const secp256k1_pubkey* G_R,
        const secp256k1_pubkey* H_L,
        const secp256k1_pubkey* H_R,
        const secp256k1_pubkey* U,
        const unsigned char* ux,
        size_t half_n
) {
    unsigned char cL[32], cR[32];
    unsigned char cLux[32], cRux[32];
    unsigned char zero[32] = {0};

    secp256k1_pubkey acc, term;
    int acc_inited; /* Tracks if acc contains a valid point */

    /* cL = <a_L, b_R>, cR = <a_R, b_L> */
    if (!secp256k1_bulletproof_ipa_dot(ctx, cL, a_L, b_R, half_n)) return 0;
    if (!secp256k1_bulletproof_ipa_dot(ctx, cR, a_R, b_L, half_n)) return 0;

    /* ---------------- L Calculation ---------------- */
    acc_inited = 0;

    /* Try adding terms. correct logic updates acc_inited to 1 */
    if (!msm_try_add(ctx, &acc, &acc_inited, G_R, a_L, half_n)) goto cleanup;
    if (!msm_try_add(ctx, &acc, &acc_inited, H_L, b_R, half_n)) goto cleanup;

    secp256k1_mpt_scalar_mul(cLux, cL, ux);
    if (memcmp(cLux, zero, 32) != 0) {
        term = *U;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, cLux)) goto cleanup;
        if (!add_term(ctx, &acc, &acc_inited, &term)) goto cleanup;
    }

    /* Check initialization explicitly */
    if (acc_inited == 0) {
        /* L resulted in Infinity. This is theoretically possible but invalid for serialization.
         * We cannot proceed. */
        goto cleanup;
    }
    *L = acc;

    /* ---------------- R Calculation ---------------- */
    acc_inited = 0;

    if (!msm_try_add(ctx, &acc, &acc_inited, G_L, a_R, half_n)) goto cleanup;
    if (!msm_try_add(ctx, &acc, &acc_inited, H_R, b_L, half_n)) goto cleanup;

    secp256k1_mpt_scalar_mul(cRux, cR, ux);
    if (memcmp(cRux, zero, 32) != 0) {
        term = *U;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &term, cRux)) goto cleanup;
        if (!add_term(ctx, &acc, &acc_inited, &term)) goto cleanup;
    }

    if (acc_inited == 0) {
        goto cleanup;
    }
    *R = acc;

    OPENSSL_cleanse(cL, 32);
    OPENSSL_cleanse(cR, 32);
    OPENSSL_cleanse(cLux, 32);
    OPENSSL_cleanse(cRux, 32);
    return 1;

    cleanup:
    OPENSSL_cleanse(cL, 32);
    OPENSSL_cleanse(cR, 32);
    OPENSSL_cleanse(cLux, 32);
    OPENSSL_cleanse(cRux, 32);
    return 0;
}
/**
 * One IPA compression step (in-place).
 *
 * Input vectors are length (2*half_n):
 *   a[0..2*half_n-1], b[0..2*half_n-1],
 *   G[0..2*half_n-1], H[0..2*half_n-1]
 *
 * After return, the first half contains folded vectors:
 *   a'[0..half_n-1], b'[0..half_n-1], G'[0..half_n-1], H'[0..half_n-1]
 *
 * Formulas (matching prover/verifier conventions):
 *   a'[i] = aL[i]*x + aR[i]*x_inv
 *   b'[i] = bL[i]*x_inv + bR[i]*x
 *   G'[i] = GL[i]*x_inv + GR[i]*x
 *   H'[i] = HL[i]*x + HR[i]*x_inv
 */
int secp256k1_bulletproof_ipa_compress_step(
        const secp256k1_context* ctx,
        unsigned char* a,
        unsigned char* b,
        secp256k1_pubkey* G,
        secp256k1_pubkey* H,
        size_t half_n,
        const unsigned char* x,
        const unsigned char* x_inv
) {
    size_t i;
    int ok = 0;

    unsigned char t1[32], t2[32];
    secp256k1_pubkey left, right;
    const secp256k1_pubkey* pts[2];

    if (ctx == NULL || a == NULL || b == NULL || G == NULL || H == NULL) return 0;
    if (half_n == 0) return 0;

    /* x and x_inv must be valid non-zero scalars */
    if (secp256k1_ec_seckey_verify(ctx, x) != 1) return 0;
    if (secp256k1_ec_seckey_verify(ctx, x_inv) != 1) return 0;

    for (i = 0; i < half_n; ++i) {
        unsigned char* aL = a + (i * 32);
        unsigned char* aR = a + ((i + half_n) * 32);

        unsigned char* bL = b + (i * 32);
        unsigned char* bR = b + ((i + half_n) * 32);

        /* a'[i] = aL*x + aR*x_inv */
        secp256k1_mpt_scalar_mul(t1, aL, x);
        secp256k1_mpt_scalar_mul(t2, aR, x_inv);
        secp256k1_mpt_scalar_add(aL, t1, t2);

        /* b'[i] = bL*x_inv + bR*x */
        secp256k1_mpt_scalar_mul(t1, bL, x_inv);
        secp256k1_mpt_scalar_mul(t2, bR, x);
        secp256k1_mpt_scalar_add(bL, t1, t2);

        /* G'[i] = GL*x_inv + GR*x */
        left  = G[i];
        right = G[i + half_n];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &left,  x_inv)) goto cleanup;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &right, x))     goto cleanup;
        pts[0] = &left; pts[1] = &right;
        if (!secp256k1_ec_pubkey_combine(ctx, &G[i], pts, 2))   goto cleanup;

        /* H'[i] = HL*x + HR*x_inv */
        left  = H[i];
        right = H[i + half_n];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &left,  x))     goto cleanup;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &right, x_inv)) goto cleanup;
        pts[0] = &left; pts[1] = &right;
        if (!secp256k1_ec_pubkey_combine(ctx, &H[i], pts, 2))   goto cleanup;
    }

    ok = 1;

    cleanup:
    OPENSSL_cleanse(t1, 32);
    OPENSSL_cleanse(t2, 32);
    return ok;
}
static int scalar_is_zero(const unsigned char s[32]) {
    unsigned char z[32] = {0};
    return memcmp(s, z, 32) == 0;
}

/*
 * ux is the fixed IPA binding scalar.
 *
 * It MUST be derived exactly once from:
 *     ux = H(commit_inp || <a,b>)
 *
 * and reused consistently throughout the IPA:
 *   - L/R cross-term construction
 *   - final (a·b·ux)·g term
 *
 * It MUST NOT depend on per-round challenges (u_i),
 * and MUST be identical for prover and verifier.
 */
int derive_ipa_binding_challenge(
        const secp256k1_context* ctx,
        unsigned char* ux_out,
        const unsigned char* commit_inp_32,
        const unsigned char* dot_32)
{
    unsigned char hash_input[64];
    unsigned char hash_output[32];

    /* 1. Build hash input = commit_inp || dot */
    memcpy(hash_input, commit_inp_32, 32);
    memcpy(hash_input + 32, dot_32, 32);

    /* 2. Hash */
    SHA256(hash_input, 64, hash_output);

    /* 3. Reduce hash to a valid scalar */
    /* CRITICAL: Wraps the 32-byte random string into the curve order */
    secp256k1_mpt_scalar_reduce32(ux_out, hash_output);

    /* * 4. Verify (Sanity check)
     * Reduce32 guarantees the value is < Order.
     * This checks for the virtually impossible case where hash is exactly 0.
     */
    if (secp256k1_ec_seckey_verify(ctx, ux_out) != 1) {
        return 0;
    }

    return 1;
}
/**
 * Derive u = H(last_challenge || L || R) reduced to a valid scalar.
 * IMPORTANT: use the SAME exact logic in verifier.
 */
int derive_ipa_round_challenge(
        const secp256k1_context* ctx,
        unsigned char u_out[32],
        const unsigned char last_challenge[32],
        const secp256k1_pubkey* L,
        const secp256k1_pubkey* R)
{
    unsigned char L_ser[33], R_ser[33];
    size_t len = 33;
    SHA256_CTX sha;
    unsigned char hash[32];

    if (!secp256k1_ec_pubkey_serialize(ctx, L_ser, &len, L, SECP256K1_EC_COMPRESSED)) return 0;
    len = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, R_ser, &len, R, SECP256K1_EC_COMPRESSED)) return 0;

    SHA256_Init(&sha);
    SHA256_Update(&sha, last_challenge, 32);
    SHA256_Update(&sha, L_ser, 33);
    SHA256_Update(&sha, R_ser, 33);
    SHA256_Final(hash, &sha);
    secp256k1_mpt_scalar_reduce32(hash, hash);
    memcpy(u_out, hash, 32);

    /* Reject invalid scalar (0 or >= group order). */
    if (secp256k1_ec_seckey_verify(ctx, u_out) != 1) return 0;

    return 1;
}
/**
 * Runs the Inner Product Argument (IPA) prover.
 * Recursively folds vectors G, H, a, and b into a single final term,
 * producing log2(n) pairs of cross-term commitments (stored in L_out/R_out).
 * Returns 1 on success, 0 on failure.
 */
int secp256k1_bulletproof_run_ipa_prover(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* g,
        secp256k1_pubkey* G_vec,
        secp256k1_pubkey* H_vec,
        unsigned char* a_vec,
        unsigned char* b_vec,
        size_t n,
        const unsigned char ipa_transcript_id[32],
        const unsigned char ux_scalar[32],
        secp256k1_pubkey* L_out,
        secp256k1_pubkey* R_out,
        size_t max_rounds,
        size_t* rounds_out,
        unsigned char a_final[32],
        unsigned char b_final[32]
) {
    size_t rounds = 0;
    size_t cur_n = n;
    int ok = 0;

    unsigned char u_scalar[32], u_inv[32];
    unsigned char last_challenge[32];

    /* Validate n is power of 2 */
    if (n == 0 || (n & (n - 1)) != 0) return 0;

    /* rounds = log2(n) */
    while (cur_n > 1) { cur_n >>= 1; rounds++; }
    cur_n = n;

    /* Bounds check (CRITICAL for aggregated proofs) */
    if (rounds > max_rounds) return 0;

    /* Seed transcript */
    memcpy(last_challenge, ipa_transcript_id, 32);

    for (size_t r = 0; r < rounds; ++r) {
        size_t half_n = cur_n >> 1;
        secp256k1_pubkey Lr, Rr;

        /* 1) Compute cross-term commitments Lr, Rr */
        if (!secp256k1_bulletproof_ipa_compute_LR(
                ctx, &Lr, &Rr,
                a_vec, a_vec + half_n * 32,
                b_vec, b_vec + half_n * 32,
                G_vec, G_vec + half_n,
                H_vec, H_vec + half_n,
                g,
                ux_scalar,
                half_n
        )) goto cleanup;

        /* 2) Store L/R */
        L_out[r] = Lr;
        R_out[r] = Rr;

        /* 3) Fiat–Shamir round challenge u_r */
        if (!derive_ipa_round_challenge(ctx, u_scalar, last_challenge, &Lr, &Rr))
            goto cleanup;

        /* 4) u_r^{-1} */
        secp256k1_mpt_scalar_inverse(u_inv, u_scalar);
        if (!secp256k1_ec_seckey_verify(ctx, u_inv)) goto cleanup;

        /* 5) Update transcript chaining state */
        memcpy(last_challenge, u_scalar, 32);

        /* 6) Fold vectors in-place */
        if (!secp256k1_bulletproof_ipa_compress_step(
                ctx, a_vec, b_vec, G_vec, H_vec, half_n, u_scalar, u_inv
        )) goto cleanup;

        cur_n = half_n;
    }

    /* Final folded scalars */
    memcpy(a_final, a_vec, 32);
    memcpy(b_final, b_vec, 32);

    if (rounds_out) *rounds_out = rounds;
    ok = 1;

    cleanup:
    OPENSSL_cleanse(u_scalar, 32);
    OPENSSL_cleanse(u_inv, 32);
    return ok;
}

/*
 * Verifies a Bulletproof Inner Product Argument (IPA).
 *
 * Given:
 *   - the original generator vectors G_vec and H_vec,
 *   - the prover’s cross-term commitments L_i and R_i,
 *   - the final folded scalars a_final and b_final,
 *   - the binding scalar ux,
 *   - and the initial commitment P,
 *
 * this function re-derives all Fiat–Shamir challenges u_i from the transcript
 * and reconstructs the folded generators G_f and H_f implicitly.
 *
 * Verification checks that the folded commitment P' equals:
 *
 *     P' = a_final * G_f
 *        + b_final * H_f
 *        + (a_final * b_final * ux) * U
 *
 * where G_f and H_f are obtained by folding G_vec and H_vec using the challenges
 * u_i and their inverses, and P' is obtained by applying the same folding
 * operations to P using the L_i and R_i commitments.
 *
 * All group operations avoid explicit construction of the point at infinity,
 * which is not representable via the libsecp256k1 public-key API.
 */
static int ipa_verify_explicit(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* G_vec,     /* original G generators (length n) */
        const secp256k1_pubkey* H_vec,     /* original H generators (length n) */
        const secp256k1_pubkey* U,
        const secp256k1_pubkey* P_in,      /* initial P */
        const secp256k1_pubkey* L_vec,     /* length = rounds */
        const secp256k1_pubkey* R_vec,     /* length = rounds */
        size_t n,                          /* total vector length (64*m) */
        const unsigned char a_final[32],
        const unsigned char b_final[32],
        const unsigned char ux[32],
        const unsigned char ipa_transcript_id[32]
) {
    secp256k1_pubkey P = *P_in;
    secp256k1_pubkey Gf, Hf, RHS, tmp;
    int RHS_inited = 0;
    int ok = 0;

    /* --- derive rounds --- */
    if (n == 0 || (n & (n - 1)) != 0)
        return 0;

    size_t rounds = bp_ipa_rounds(n);

    /* --- allocate u / u_inv --- */
    unsigned char* u_flat    = (unsigned char*)malloc(rounds * 32);
    unsigned char* uinv_flat = (unsigned char*)malloc(rounds * 32);
    if (!u_flat || !uinv_flat)
        goto cleanup_alloc;

    unsigned char last[32];
    memcpy(last, ipa_transcript_id, 32);

    /* ---- 1. Re-derive u_i ---- */
    for (size_t i = 0; i < rounds; i++) {
        unsigned char* ui    = u_flat    + 32 * i;
        unsigned char* uiinv = uinv_flat + 32 * i;

        if (!derive_ipa_round_challenge(ctx, ui, last, &L_vec[i], &R_vec[i]))
            goto cleanup;

        secp256k1_mpt_scalar_inverse(uiinv, ui);
        if (!secp256k1_ec_seckey_verify(ctx, uiinv))
            goto cleanup;

        memcpy(last, ui, 32);
    }

    /* ---- 2. Fold generators ---- */
    if (!fold_generators(ctx, &Gf, G_vec, u_flat, uinv_flat, n, rounds, 0))
        goto cleanup;

    if (!fold_generators(ctx, &Hf, H_vec, u_flat, uinv_flat, n, rounds, 1))
        goto cleanup;

    /* ---- 3. RHS = a*Gf + b*Hf + (a*b*ux)*U ---- */

    if (!scalar_is_zero(a_final)) {
        tmp = Gf;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, a_final))
            goto cleanup;
        if (!add_term(ctx, &RHS, &RHS_inited, &tmp))
            goto cleanup;
    }

    if (!scalar_is_zero(b_final)) {
        tmp = Hf;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, b_final))
            goto cleanup;
        if (!add_term(ctx, &RHS, &RHS_inited, &tmp))
            goto cleanup;
    }

    {
        unsigned char ab[32], ab_ux[32];
        secp256k1_mpt_scalar_mul(ab, a_final, b_final);
        secp256k1_mpt_scalar_mul(ab_ux, ab, ux);

        if (!scalar_is_zero(ab_ux)) {
            tmp = *U;
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, ab_ux))
                goto cleanup;
            if (!add_term(ctx, &RHS, &RHS_inited, &tmp))
                goto cleanup;
        }

        OPENSSL_cleanse(ab, 32);
        OPENSSL_cleanse(ab_ux, 32);
    }

    if (!RHS_inited)
        goto cleanup;

    /* ---- 4. Fold P using L/R ---- */
    if (!apply_ipa_folding_to_P(
            ctx, &P, L_vec, R_vec, u_flat, uinv_flat, rounds))
        goto cleanup;

    /* ---- 5. Compare P and RHS ---- */
    if (pubkey_equal(ctx, &P, &RHS))
        ok = 1;

    cleanup:
    OPENSSL_cleanse(u_flat, rounds * 32);
    OPENSSL_cleanse(uinv_flat, rounds * 32);

    cleanup_alloc:
    free(u_flat);
    free(uinv_flat);
    return ok;
}
/**
 * Phase 1, Step 3 (Aggregated):
 * Compute al, ar, sl, sr vectors for ONE value block inside an aggregated proof.
 *
 * The caller is responsible for:
 *   - allocating al/ar/sl/sr of length (BP_VALUE_BITS * m)
 *   - calling this once per value with block_index = j
 *
 * Block layout:
 *   bits for value j occupy indices:
 *     [BP_VALUE_BITS * j .. BP_VALUE_BITS * j + BP_VALUE_BITS - 1]
 */
int secp256k1_bulletproof_compute_vectors_block(
        const secp256k1_context* ctx,
        uint64_t value,
        size_t block_index,              /* j-th value */
        unsigned char* al,               /* length = BP_TOTAL_BITS(m) * 32 */
        unsigned char* ar,
        unsigned char* sl,
        unsigned char* sr
) {
    const size_t offset = BP_VALUE_BITS * block_index;
    int ok = 1;

    /* Scalars */
    unsigned char one[32] = {0};
    unsigned char minus_one[32];
    unsigned char zero[32] = {0};

    one[31] = 1;
    memcpy(minus_one, one, 32);
    secp256k1_mpt_scalar_negate(minus_one, minus_one);

    /* ---- 1. Encode value bits into al/ar ---- */
    for (size_t i = 0; i < BP_VALUE_BITS; i++) {
        size_t idx = offset + i;

        if ((value >> i) & 1) {
            /* bit = 1  => al = 1, ar = 0 */
            memcpy(al + idx * 32, one, 32);
            memcpy(ar + idx * 32, zero, 32);
        } else {
            /* bit = 0  => al = 0, ar = -1 */
            memcpy(al + idx * 32, zero, 32);
            memcpy(ar + idx * 32, minus_one, 32);
        }
    }

    /* ---- 2. Generate random blinding vectors sl/sr ---- */
    for (size_t i = 0; i < BP_VALUE_BITS; i++) {
        size_t idx = offset + i;

        if (!generate_random_scalar(ctx, sl + idx * 32)) {
            ok = 0;
            goto cleanup;
        }
        if (!generate_random_scalar(ctx, sr + idx * 32)) {
            ok = 0;
            goto cleanup;
        }
    }

    return 1;

    cleanup:
    /* Wipe only the affected block */
    OPENSSL_cleanse(al + offset * 32, BP_VALUE_BITS * 32);
    OPENSSL_cleanse(ar + offset * 32, BP_VALUE_BITS * 32);
    OPENSSL_cleanse(sl + offset * 32, BP_VALUE_BITS * 32);
    OPENSSL_cleanse(sr + offset * 32, BP_VALUE_BITS * 32);
    return 0;
}
/**
 * Computes the Pedersen Commitment: C = value*G + blinding_factor*Pk_base.
 */
int secp256k1_bulletproof_create_commitment(
        const secp256k1_context* ctx,
        secp256k1_pubkey* commitment_C,
        uint64_t value,
        const unsigned char* blinding_factor,
        const secp256k1_pubkey* pk_base
) {
    secp256k1_pubkey G_term, Pk_term;
    const secp256k1_pubkey* points_to_add[2];
    int v_is_zero = (value == 0);

    /* 1. Compute r * Pk_base (The Blinding Term) */
    Pk_term = *pk_base;
    if (secp256k1_ec_pubkey_tweak_mul(ctx, &Pk_term, blinding_factor) != 1) return 0;

    /* 2. Handle Value Term */
    if (v_is_zero) {
        /* If v=0, C = 0*G + r*H = r*H.
           We skip G_term entirely because libsecp cannot represent infinity. */
        *commitment_C = Pk_term;
        return 1;
    }

    /* 3. Compute v * G (The Value Term) */
    if (!compute_amount_point(ctx, &G_term, value)) return 0;

    /* 4. Combine: C = v*G + r*Pk_base */
    points_to_add[0] = &G_term;
    points_to_add[1] = &Pk_term;
    if (secp256k1_ec_pubkey_combine(ctx, commitment_C, points_to_add, 2) != 1) return 0;

    return 1;
}

/**
 * Generates an aggregated Bulletproof for m values.
 *
 * This function constructs a range proof asserting that all m values are within
 * the [0, 2^64) range. The proof is serialized into proof_out.
 *
 * Inputs:
 * - values: Array of m 64-bit integers to prove.
 * - blindings_flat: Array of m 32-byte blinding factors (one per value).
 * - m: Number of values to aggregate (must be a power of 2).
 * - pk_base: Generator H used for the commitments (C = vG + rH).
 * - context_id: Optional 32-byte unique ID to bind the proof to a context.
 *
 * Outputs:
 * - proof_out: Buffer to receive the serialized proof.
 * - proof_len: On input, size of proof_out. On output, actual proof size.
 *
 * Returns 1 on success, 0 on failure.
 */
int secp256k1_bulletproof_prove_agg(
        const secp256k1_context* ctx,
        unsigned char* proof_out,
        size_t* proof_len,
        const uint64_t* values,
        const unsigned char* blindings_flat,
        size_t m,
        const secp256k1_pubkey* pk_base,
        const unsigned char* context_id
) {
    /* ---- 0. Dimensions ---- */
    const size_t n = BP_TOTAL_BITS(m);          /* 64*m */
    const size_t rounds = bp_ipa_rounds(n);     /* log2(64*m) */

    /* 64*m must be power-of-two -> m must be power-of-two */
    if (m == 0) return 0;
    if ((n & (n - 1)) != 0) return 0;

    /* Proof length = 4*33 + 2*rounds*33 + 5*32 */
    const size_t proof_size = 292 + 66 * rounds;
    if (proof_len) *proof_len = proof_size;

    int ok = 0;

    /* ---- 1. Allocate vectors ---- */
    secp256k1_pubkey* G_vec = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* H_vec = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* H_prime = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    unsigned char* al = (unsigned char*)malloc(n * 32);
    unsigned char* ar = (unsigned char*)malloc(n * 32);
    unsigned char* sl = (unsigned char*)malloc(n * 32);
    unsigned char* sr = (unsigned char*)malloc(n * 32);
    unsigned char* l_vec = (unsigned char*)malloc(n * 32);
    unsigned char* r_vec = (unsigned char*)malloc(n * 32);
    unsigned char* r1_vec = (unsigned char*)malloc(n * 32);

    secp256k1_pubkey* L_vec = (secp256k1_pubkey*)malloc(rounds * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* R_vec = (secp256k1_pubkey*)malloc(rounds * sizeof(secp256k1_pubkey));

    unsigned char* y_powers = (unsigned char*)malloc(n * 32); /* y^i */
    unsigned char* z_j2 = (unsigned char*)malloc(m * 32);     /* z^(j+2) */

    if (!G_vec || !H_vec || !H_prime ||
        !al || !ar || !sl || !sr || !l_vec || !r_vec || !r1_vec ||
        !L_vec || !R_vec ||
        !y_powers || !z_j2) {
        goto cleanup;
    }

    /* ---- 2. Scalars / points ---- */
    secp256k1_pubkey A, S, T1, T2, U;

    unsigned char alpha[32], rho[32];
    unsigned char tau1[32], tau2[32];
    unsigned char t1[32], t2[32];
    unsigned char t_hat[32], tau_x[32], mu[32];
    unsigned char a_final[32], b_final[32];

    unsigned char y[32], z[32], x[32];
    unsigned char z_sq[32], z_neg[32], x_sq[32];
    unsigned char ux_scalar[32];
    unsigned char ipa_transcript[32];

    unsigned char one[32] = {0}; one[31] = 1;
    unsigned char minus_one[32]; secp256k1_mpt_scalar_negate(minus_one, one);
    unsigned char zero[32] = {0};

    /* ---- 3. Generator vectors ---- */
    if (!secp256k1_mpt_get_generator_vector(ctx, G_vec, n, (const unsigned char*)"G", 1)) goto cleanup;
    if (!secp256k1_mpt_get_generator_vector(ctx, H_vec, n, (const unsigned char*)"H", 1)) goto cleanup;
    {
        secp256k1_pubkey U_arr[1];
        if (!secp256k1_mpt_get_generator_vector(ctx, U_arr, 1, (const unsigned char*)"BP_U", 4)) goto cleanup;
        U = U_arr[0];
    }

    /* ---- 4. Bit-decomposition for m values into al/ar (concat) + random sl/sr ---- */
    for (size_t j = 0; j < m; j++) {
        uint64_t v = values[j];
        for (size_t i = 0; i < BP_VALUE_BITS; i++) {
            const size_t k = j * BP_VALUE_BITS + i; /* 0..n-1 */
            unsigned char* al_k = al + 32*k;
            unsigned char* ar_k = ar + 32*k;
            unsigned char* sl_k = sl + 32*k;
            unsigned char* sr_k = sr + 32*k;

            if ((v >> i) & 1) {
                memcpy(al_k, one, 32);
                memset(ar_k, 0, 32);
            } else {
                memset(al_k, 0, 32);
                memcpy(ar_k, minus_one, 32);
            }

            if (!generate_random_scalar(ctx, sl_k)) goto cleanup;
            if (!generate_random_scalar(ctx, sr_k)) goto cleanup;
        }
    }

    if (!generate_random_scalar(ctx, alpha)) goto cleanup;
    if (!generate_random_scalar(ctx, rho)) goto cleanup;

    /* ---- 5. Commitments A and S ----
     * A = alpha*Base + <al,G> + <ar,H>
     * S = rho*Base   + <sl,G> + <sr,H>
     */
    {
        secp256k1_pubkey tG, tH, tB;
        const secp256k1_pubkey* pts[3];

        if (!secp256k1_bulletproof_ipa_msm(ctx, &tG, G_vec, al, n)) goto cleanup;
        if (!secp256k1_bulletproof_ipa_msm(ctx, &tH, H_vec, ar, n)) goto cleanup;
        tB = *pk_base;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tB, alpha)) goto cleanup;
        pts[0] = &tB; pts[1] = &tG; pts[2] = &tH;
        if (!secp256k1_ec_pubkey_combine(ctx, &A, pts, 3)) goto cleanup;

        if (!secp256k1_bulletproof_ipa_msm(ctx, &tG, G_vec, sl, n)) goto cleanup;
        if (!secp256k1_bulletproof_ipa_msm(ctx, &tH, H_vec, sr, n)) goto cleanup;
        tB = *pk_base;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tB, rho)) goto cleanup;
        pts[0] = &tB; pts[1] = &tG; pts[2] = &tH;
        if (!secp256k1_ec_pubkey_combine(ctx, &S, pts, 3)) goto cleanup;
    }

    /* ---- 6. Fiat–Shamir y,z ---- */
    {
        unsigned char A_ser[33], S_ser[33];
        size_t len = 33;
        SHA256_CTX sha;

        if (!secp256k1_ec_pubkey_serialize(ctx, A_ser, &len, &A, SECP256K1_EC_COMPRESSED)) goto cleanup;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, S_ser, &len, &S, SECP256K1_EC_COMPRESSED)) goto cleanup;

        /* --- START OF TRANSCRIPT --- */
        SHA256_Init(&sha);

        /* 1. Domain Separation  */
        SHA256_Update(&sha, "MPT_BULLETPROOF_RANGE", 21);

        /* 2. Transaction Context [cite: 431] */
        if (context_id) SHA256_Update(&sha, context_id, 32);

        /* 3. Statement: Value Commitments*/
        for (size_t i = 0; i < m; i++) {
            secp256k1_pubkey V_temp;
            unsigned char V_ser[33];
            size_t v_len = 33;
            /* Reconstruct commitment for the transcript */
            if (!secp256k1_bulletproof_create_commitment(ctx, &V_temp, values[i], blindings_flat + 32*i, pk_base)) goto cleanup;
            if (!secp256k1_ec_pubkey_serialize(ctx, V_ser, &v_len, &V_temp, SECP256K1_EC_COMPRESSED)) goto cleanup;
            SHA256_Update(&sha, V_ser, 33);
        }


        SHA256_Update(&sha, A_ser, 33);
        SHA256_Update(&sha, S_ser, 33);

        SHA256_Final(y, &sha);
        secp256k1_mpt_scalar_reduce32(y, y);

        /* To match spec H(T1 || y), we continue from the previous state (or re-hash) */
        SHA256_Init(&sha);
        SHA256_Update(&sha, "MPT_BULLETPROOF_RANGE", 21);
        if (context_id) SHA256_Update(&sha, context_id, 32);

        for (size_t i = 0; i < m; i++) {
            /* ... (re-add commitments) ... */
            secp256k1_pubkey V_temp;
            unsigned char V_ser[33];
            size_t v_len = 33;
            secp256k1_bulletproof_create_commitment(ctx, &V_temp, values[i], blindings_flat + 32*i, pk_base);
            secp256k1_ec_pubkey_serialize(ctx, V_ser, &v_len, &V_temp, SECP256K1_EC_COMPRESSED);
            SHA256_Update(&sha, V_ser, 33);
        }
        SHA256_Update(&sha, A_ser, 33);
        SHA256_Update(&sha, S_ser, 33);
        SHA256_Update(&sha, y, 32);
        SHA256_Final(z, &sha);
        secp256k1_mpt_scalar_reduce32(z, z);
        memcpy(z_neg, z, 32);
        secp256k1_mpt_scalar_negate(z_neg, z_neg);
    }

    /* ---- 7. Aggregated polynomial setup ---- */

/* y_powers[k] = y^k */
    {
        unsigned char ypow[32];
        memcpy(ypow, one, 32);

        for (size_t k = 0; k < n; k++) {
            memcpy(y_powers + 32*k, ypow, 32);
            secp256k1_mpt_scalar_mul(ypow, ypow, y);
        }
        OPENSSL_cleanse(ypow, 32);
    }

/* z_j2[j] = z^(j+2) */
    compute_z_pows_j2(ctx, (unsigned char (*)[32])z_j2, z, m);

/* l0, r0, r1 */
    for (size_t block = 0; block < m; block++) {
        const unsigned char* zblk = z_j2 + 32*block; /* z^(block+2) */

        for (size_t i = 0; i < BP_VALUE_BITS; i++) {
            size_t k = block * BP_VALUE_BITS + i;

            unsigned char* l0 = l_vec  + 32*k;
            unsigned char* r0 = r_vec  + 32*k;
            unsigned char* r1 = r1_vec + 32*k;

            const unsigned char* al_k = al + 32*k;
            const unsigned char* ar_k = ar + 32*k;
            const unsigned char* sr_k = sr + 32*k;
            const unsigned char* yk   = y_powers + 32*k;

            unsigned char two_i[32] = {0};
            two_i[31 - (i >> 3)] = (unsigned char)(1u << (i & 7));

            /* l0 = aL - z */
            secp256k1_mpt_scalar_add(l0, al_k, z_neg);

            /* r0 = y^k * (aR + z) + z^(block+2) * 2^i */
            {
                unsigned char tmp1[32], tmp2[32];

                /* tmp1 = aR + z */
                secp256k1_mpt_scalar_add(tmp1, ar_k, z);

                /* r0 = y^k * tmp1 */
                secp256k1_mpt_scalar_mul(r0, tmp1, yk);

                /* tmp2 = z^(block+2) * 2^i */
                secp256k1_mpt_scalar_mul(tmp2, zblk, two_i);

                /* r0 += tmp2 */
                secp256k1_mpt_scalar_add(r0, r0, tmp2);

                OPENSSL_cleanse(tmp1, 32);
                OPENSSL_cleanse(tmp2, 32);
            }

            /* r1 = sR * y^k */
            secp256k1_mpt_scalar_mul(r1, sr_k, yk);
        }
    }

/* t1 = <l0, r1> + <l1, r0> */
    {
        unsigned char dot1[32], dot2[32];
        if (!secp256k1_bulletproof_ipa_dot(ctx, dot1, l_vec, r1_vec, n)) goto cleanup;
        if (!secp256k1_bulletproof_ipa_dot(ctx, dot2, sl,    r_vec,  n)) goto cleanup;
        secp256k1_mpt_scalar_add(t1, dot1, dot2);
        OPENSSL_cleanse(dot1, 32);
        OPENSSL_cleanse(dot2, 32);
    }

/* t2 = <l1, r1> */
    if (!secp256k1_bulletproof_ipa_dot(ctx, t2, sl, r1_vec, n)) goto cleanup;

/* Make sure these exist before T1/T2 */
    if (!generate_random_scalar(ctx, tau1)) goto cleanup;
    if (!generate_random_scalar(ctx, tau2)) goto cleanup;

/* ---- 8. Commit T1, T2 ---- */
/* T1 = t1*G + tau1*Base   where G = G_vec[0] */
    {
        secp256k1_pubkey tG, tB;
        const secp256k1_pubkey* pts[2];

        if (memcmp(t1, zero, 32) == 0) goto cleanup; /* prototype guard */
        /* tG = t1 * (curve base generator) */
        if (!secp256k1_ec_pubkey_create(ctx, &tG, t1)) goto cleanup;


        tB = *pk_base;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tB, tau1)) goto cleanup;

        pts[0] = &tG; pts[1] = &tB;
        if (!secp256k1_ec_pubkey_combine(ctx, &T1, pts, 2)) goto cleanup;
    }

/* T2 = t2*G + tau2*Base */
    {
        secp256k1_pubkey tG, tB;
        const secp256k1_pubkey* pts[2];

        if (memcmp(t2, zero, 32) == 0) goto cleanup; /* prototype guard */
        /* tG = t2 * (curve base generator) */
        if (!secp256k1_ec_pubkey_create(ctx, &tG, t2)) goto cleanup;


        tB = *pk_base;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tB, tau2)) goto cleanup;

        pts[0] = &tG; pts[1] = &tB;
        if (!secp256k1_ec_pubkey_combine(ctx, &T2, pts, 2)) goto cleanup;
    }

/* ---- 9. Challenge x ---- */
/* x = H(context_id || A || S || y || z || T1 || T2) */
    {
        unsigned char A_ser[33], S_ser[33], T1_ser[33], T2_ser[33];
        size_t len = 33;
        SHA256_CTX sha;

        if (!secp256k1_ec_pubkey_serialize(ctx, A_ser,  &len, &A,  SECP256K1_EC_COMPRESSED)) goto cleanup;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, S_ser,  &len, &S,  SECP256K1_EC_COMPRESSED)) goto cleanup;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, T1_ser, &len, &T1, SECP256K1_EC_COMPRESSED)) goto cleanup;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, T2_ser, &len, &T2, SECP256K1_EC_COMPRESSED)) goto cleanup;

        SHA256_Init(&sha);
        if (context_id) SHA256_Update(&sha, context_id, 32);
        SHA256_Update(&sha, A_ser,  33);
        SHA256_Update(&sha, S_ser,  33);
        SHA256_Update(&sha, y,      32);
        SHA256_Update(&sha, z,      32);
        SHA256_Update(&sha, T1_ser, 33);
        SHA256_Update(&sha, T2_ser, 33);
        SHA256_Final(x, &sha);

        secp256k1_mpt_scalar_reduce32(x, x);
        if (memcmp(x, zero, 32) == 0) goto cleanup; /* avoid infinity later */
    }

    /* ---- 10. Evaluate l(x), r(x), t_hat ---- */
    for (size_t k = 0; k < n; k++) {
        unsigned char tmp[32];

        /* l = l0 + sL*x */
        secp256k1_mpt_scalar_mul(tmp, sl + 32*k, x);
        secp256k1_mpt_scalar_add(l_vec + 32*k, l_vec + 32*k, tmp);

        /* r = r0 + r1*x */
        secp256k1_mpt_scalar_mul(tmp, r1_vec + 32*k, x);
        secp256k1_mpt_scalar_add(r_vec + 32*k, r_vec + 32*k, tmp);

        OPENSSL_cleanse(tmp, 32);
    }

    if (!secp256k1_bulletproof_ipa_dot(ctx, t_hat, l_vec, r_vec, n)) goto cleanup;

    /* ---- 11. tau_x and mu (aggregation changes tau_x) ---- */
    secp256k1_mpt_scalar_mul(x_sq, x, x);

    /* tau_x = tau2*x^2 + tau1*x + sum_j z^(j+2) * blinding_j */
    secp256k1_mpt_scalar_mul(tau_x, tau2, x_sq);
    {
        unsigned char tmp[32];
        secp256k1_mpt_scalar_mul(tmp, tau1, x);
        secp256k1_mpt_scalar_add(tau_x, tau_x, tmp);

        /* + sum_j z^(j+2) * r_j */
        for (size_t j = 0; j < m; j++) {
            unsigned char add[32];
            secp256k1_mpt_scalar_mul(add, z_j2 + 32*j, blindings_flat + 32*j);
            secp256k1_mpt_scalar_add(tau_x, tau_x, add);
            OPENSSL_cleanse(add, 32);
        }

        OPENSSL_cleanse(tmp, 32);
    }

    /* mu = alpha + rho*x */
    {
        unsigned char tmp[32];
        secp256k1_mpt_scalar_mul(tmp, rho, x);
        secp256k1_mpt_scalar_add(mu, alpha, tmp);
        OPENSSL_cleanse(tmp, 32);
    }

    /* ---- 12. IPA transcript + ux (binding), and H' normalization ---- */

/* 12a. Build a stable IPA transcript seed (32 bytes).
 *
 * IMPORTANT:
 *  - Prover and verifier MUST hash the exact same bytes in the exact same order.
 *  - Use only public elements that both sides already know.
 *  - Do NOT depend on internal intermediate buffers.
 *
 * Minimal, safe choice: context_id || A||S||T1||T2 || y||z||x || t_hat
 * (All points are serialized compressed 33 bytes.)
 */
    {
        unsigned char A_ser[33], S_ser[33], T1_ser[33], T2_ser[33];
        size_t ser_len;
        SHA256_CTX sha;

        ser_len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, A_ser, &ser_len, &A,  SECP256K1_EC_COMPRESSED)) goto cleanup;
        ser_len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, S_ser, &ser_len, &S,  SECP256K1_EC_COMPRESSED)) goto cleanup;
        ser_len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, T1_ser, &ser_len, &T1, SECP256K1_EC_COMPRESSED)) goto cleanup;
        ser_len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, T2_ser, &ser_len, &T2, SECP256K1_EC_COMPRESSED)) goto cleanup;

        SHA256_Init(&sha);

        /* Domain/context binding */
        if (context_id) SHA256_Update(&sha, context_id, 32);

        /* Outer commitments */
        SHA256_Update(&sha, A_ser, 33);
        SHA256_Update(&sha, S_ser, 33);
        SHA256_Update(&sha, T1_ser, 33);
        SHA256_Update(&sha, T2_ser, 33);

        /* Outer challenges */
        SHA256_Update(&sha, y, 32);
        SHA256_Update(&sha, z, 32);
        SHA256_Update(&sha, x, 32);

        /* Bind to t_hat as well (public scalar) */
        SHA256_Update(&sha, t_hat, 32);

        SHA256_Final(ipa_transcript, &sha);
    }

/* 12b. Derive u_x = H(ipa_transcript || t_hat) reduced to scalar. */
    if (!derive_ipa_binding_challenge(ctx, ux_scalar, ipa_transcript, t_hat)) goto cleanup;

/* 12c. Normalize H: H'[k] = H[k] * y^{-k}.
 *
 * NOTE:
 *  - Requires y != 0.
 *  - If y==0 (mod q), abort (cannot invert).
 */
    {
        unsigned char y_inv[32];
        unsigned char y_inv_pow[32]; /* (y^{-1})^k */
        if (memcmp(y, zero, 32) == 0) goto cleanup;

        secp256k1_mpt_scalar_inverse(y_inv, y);
        memcpy(y_inv_pow, one, 32);

        for (size_t k = 0; k < n; k++) {
            H_prime[k] = H_vec[k];
            /* H_prime[k] = H_vec[k] * (y^{-1})^k */
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &H_prime[k], y_inv_pow)) goto cleanup;
            secp256k1_mpt_scalar_mul(y_inv_pow, y_inv_pow, y_inv);
        }

        OPENSSL_cleanse(y_inv, 32);
        OPENSSL_cleanse(y_inv_pow, 32);
    }

/* 12d. Run IPA prover */
    {
        size_t rounds_used = 0;

        if (!secp256k1_bulletproof_run_ipa_prover(
                ctx,
                &U,             /* binding generator point */
                G_vec,          /* G */
                H_prime,        /* H' */
                l_vec,          /* l(x) scalars (flat 32*n) */
                r_vec,          /* r(x) scalars (flat 32*n) */
                n,
                ipa_transcript, /* 32-byte seed */
                ux_scalar,      /* u_x scalar */
                L_vec,
                R_vec,
                rounds,         /* max_rounds = log2(n) */
                &rounds_used,
                a_final,
                b_final
        )) goto cleanup;

        if (rounds_used != rounds) goto cleanup;
    }

    /* ---- 13. Serialize (uses rounds) ---- */
    {
        const size_t expected = 292 + 66 * rounds; /* 4*33 + 2*rounds*33 + 5*32 */

        /* Standard pattern: query size only */
        if (proof_out == NULL) {
            if (proof_len) *proof_len = expected;
            ok = 1;
            goto cleanup;
        }

        if (proof_len == NULL) goto cleanup;

        if (*proof_len < expected) {
            *proof_len = expected;
            goto cleanup; /* not enough space */
        }

        unsigned char* ptr = proof_out;
        size_t ser_len;

#define SER_PT(P) do { \
        ser_len = 33; \
        if (!secp256k1_ec_pubkey_serialize(ctx, ptr, &ser_len, &(P), SECP256K1_EC_COMPRESSED)) goto cleanup; \
        if (ser_len != 33) goto cleanup; \
        ptr += 33; \
    } while (0)

        SER_PT(A); SER_PT(S); SER_PT(T1); SER_PT(T2);

        for (size_t r = 0; r < rounds; r++) SER_PT(L_vec[r]);
        for (size_t r = 0; r < rounds; r++) SER_PT(R_vec[r]);

        memcpy(ptr, a_final, 32); ptr += 32;
        memcpy(ptr, b_final, 32); ptr += 32;
        memcpy(ptr, t_hat, 32);   ptr += 32;
        memcpy(ptr, tau_x, 32);   ptr += 32;
        memcpy(ptr, mu, 32);      ptr += 32;

#undef SER_PT

        /* Final sanity */
        if ((size_t)(ptr - proof_out) != expected) goto cleanup;

        *proof_len = expected;
    }

    ok = 1;

    cleanup:

    /* wipe sensitive scalars; free buffers */
    if (al) OPENSSL_cleanse(al, n * 32);
    if (ar) OPENSSL_cleanse(ar, n * 32);
    if (sl) OPENSSL_cleanse(sl, n * 32);
    if (sr) OPENSSL_cleanse(sr, n * 32);
    if (l_vec) OPENSSL_cleanse(l_vec, n * 32);
    if (r_vec) OPENSSL_cleanse(r_vec, n * 32);
    if (r1_vec) OPENSSL_cleanse(r1_vec, n * 32);

    OPENSSL_cleanse(alpha, 32);
    OPENSSL_cleanse(rho, 32);
    OPENSSL_cleanse(tau1, 32);
    OPENSSL_cleanse(tau2, 32);
    OPENSSL_cleanse(t1, 32);
    OPENSSL_cleanse(t2, 32);
    OPENSSL_cleanse(t_hat, 32);
    OPENSSL_cleanse(tau_x, 32);
    OPENSSL_cleanse(mu, 32);

    OPENSSL_cleanse(y, 32);
    OPENSSL_cleanse(z, 32);
    OPENSSL_cleanse(x, 32);
    OPENSSL_cleanse(z_sq, 32);
    OPENSSL_cleanse(z_neg, 32);
    OPENSSL_cleanse(x_sq, 32);
    OPENSSL_cleanse(ux_scalar, 32);
    OPENSSL_cleanse(ipa_transcript, 32);

    if (G_vec) { OPENSSL_cleanse(G_vec, n * sizeof(secp256k1_pubkey)); free(G_vec); }
    if (H_vec) { OPENSSL_cleanse(H_vec, n * sizeof(secp256k1_pubkey)); free(H_vec); }
    if (H_prime) { OPENSSL_cleanse(H_prime, n * sizeof(secp256k1_pubkey)); free(H_prime); }

    if (al) free(al);
    if (ar) free(ar);
    if (sl) free(sl);
    if (sr) free(sr);
    if (l_vec) free(l_vec);
    if (r_vec) free(r_vec);
    if (r1_vec) free(r1_vec);

    if (L_vec) { OPENSSL_cleanse(L_vec, rounds * sizeof(secp256k1_pubkey)); free(L_vec); }
    if (R_vec) { OPENSSL_cleanse(R_vec, rounds * sizeof(secp256k1_pubkey)); free(R_vec); }

    if (y_powers) { OPENSSL_cleanse(y_powers, n * 32); free(y_powers); }
    if (z_j2) { OPENSSL_cleanse(z_j2, m * 32); free(z_j2); }

    return ok;
}
/**
 * Verifies an aggregated Bulletproof range proof for m commitments.
 *
 * Checks that the values committed in `commitment_C_vec` are all within the
 * [0, 2^64) range.
 *
 * Usage Notes:
 * - The generator vectors G_vec and H_vec must have length n = 64 * m.
 * - The commitment array `commitment_C_vec` must contain m elements.
 * (For a single proof where m=1, pass a pointer to the single commitment).
 * - To bind commitments to the proof transcript, include them in the
 * `context_id` hash before calling this function.
 *
 * Serialized Proof Format:
 * - A, S, T1, T2       (4 * 33 bytes)
 * - L_vec              (rounds * 33 bytes)
 * - R_vec              (rounds * 33 bytes)
 * - a, b               (2 * 32 bytes)
 * - t_hat, tau_x, mu   (3 * 32 bytes)
 *
 * Total Size: 292 + (66 * rounds) bytes, where rounds = log2(64 * m).
 *
 * Returns 1 if valid, 0 otherwise.
 */

int secp256k1_bulletproof_verify_agg(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* G_vec,              /* length n = 64*m */
        const secp256k1_pubkey* H_vec,              /* length n = 64*m */
        const unsigned char* proof,
        size_t proof_len,
        const secp256k1_pubkey* commitment_C_vec,   /* length m */
        size_t m,
        const secp256k1_pubkey* pk_base,
        const unsigned char* context_id
) {

    if (!ctx || !G_vec || !H_vec || !proof || !commitment_C_vec || !pk_base) return 0;
    if (m == 0) return 0;
    /* Aggregation requires n = 64*m to be power-of-two => m must be power-of-two. */
    if ((m & (m - 1)) != 0) return 0;

    const size_t n = BP_TOTAL_BITS(m);     /* 64*m */
    const size_t rounds = bp_ipa_rounds(n);

    /* Proof length is dynamic in aggregated mode */
    const size_t expected_len = 292 + 66 * rounds;
    if (proof_len != expected_len) return 0;

    /* --- Unpack proof --- */
    secp256k1_pubkey A, S, T1, T2;
    secp256k1_pubkey U;

    secp256k1_pubkey* L_vec = (secp256k1_pubkey*)malloc(rounds * sizeof(secp256k1_pubkey));
    secp256k1_pubkey* R_vec = (secp256k1_pubkey*)malloc(rounds * sizeof(secp256k1_pubkey));
    if (!L_vec || !R_vec) { free(L_vec); free(R_vec); return 0; }

    unsigned char a_final[32], b_final[32];
    unsigned char t_hat[32], tau_x[32], mu[32];

    unsigned char y[32], z[32], x[32];
    unsigned char ux_scalar[32];
    unsigned char z_sq[32];

    const unsigned char* ptr = proof;

    if (!secp256k1_ec_pubkey_parse(ctx, &A, ptr, 33)) goto fail; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &S, ptr, 33)) goto fail; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T1, ptr, 33)) goto fail; ptr += 33;
    if (!secp256k1_ec_pubkey_parse(ctx, &T2, ptr, 33)) goto fail; ptr += 33;

    for (size_t i = 0; i < rounds; i++) {
        if (!secp256k1_ec_pubkey_parse(ctx, &L_vec[i], ptr, 33)) goto fail;
        ptr += 33;
    }

    for (size_t i = 0; i < rounds; i++) {
        if (!secp256k1_ec_pubkey_parse(ctx, &R_vec[i], ptr, 33)) goto fail;
        ptr += 33;
    }

    memcpy(a_final, ptr, 32); ptr += 32;
    memcpy(b_final, ptr, 32); ptr += 32;
    memcpy(t_hat,   ptr, 32); ptr += 32;
    memcpy(tau_x,   ptr, 32); ptr += 32;
    memcpy(mu,      ptr, 32); ptr += 32;

    /* Basic scalar validity */
    if (!secp256k1_ec_seckey_verify(ctx, a_final)) goto fail;
    if (!secp256k1_ec_seckey_verify(ctx, b_final)) goto fail;
    if (!secp256k1_ec_seckey_verify(ctx, t_hat))   goto fail;
    if (!secp256k1_ec_seckey_verify(ctx, tau_x))   goto fail;
    if (!secp256k1_ec_seckey_verify(ctx, mu))      goto fail;

    /* Derive U */
    {
        secp256k1_pubkey U_arr[1];
        if (!secp256k1_mpt_get_generator_vector(ctx, U_arr, 1, (const unsigned char*)"BP_U", 4)) goto fail;
        U = U_arr[0];
    }

    /* --- Fiat–Shamir: y,z --- */
    unsigned char A_ser[33], S_ser[33], T1_ser[33], T2_ser[33];
    size_t slen = 33;
    SHA256_CTX sha;

    if (!secp256k1_ec_pubkey_serialize(ctx, A_ser, &slen, &A, SECP256K1_EC_COMPRESSED)) goto fail;
    slen = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, S_ser, &slen, &S, SECP256K1_EC_COMPRESSED)) goto fail;
    slen = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, T1_ser, &slen, &T1, SECP256K1_EC_COMPRESSED)) goto fail;
    slen = 33;
    if (!secp256k1_ec_pubkey_serialize(ctx, T2_ser, &slen, &T2, SECP256K1_EC_COMPRESSED)) goto fail;

    SHA256_Init(&sha);

    /* 1. Domain Separation */
    SHA256_Update(&sha, "MPT_BULLETPROOF_RANGE", 21);

    /* 2. Transaction Context */
    if (context_id) SHA256_Update(&sha, context_id, 32);

    /* 3. Value Commitments (Inputs to Verify) */
    for (size_t i = 0; i < m; i++) {
        unsigned char C_ser[33];
        size_t c_len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, C_ser, &c_len, &commitment_C_vec[i], SECP256K1_EC_COMPRESSED)) goto fail;
        SHA256_Update(&sha, C_ser, 33);
    }

    /* 4. A, S */
    SHA256_Update(&sha, A_ser, 33);
    SHA256_Update(&sha, S_ser, 33);
    SHA256_Final(y, &sha);
    secp256k1_mpt_scalar_reduce32(y, y);

    /* Generate z (Hash T1 || y) */
    SHA256_Init(&sha);
    SHA256_Update(&sha, "MPT_BULLETPROOF_RANGE", 21);
    if (context_id) SHA256_Update(&sha, context_id, 32);
    for (size_t i = 0; i < m; i++) {
        unsigned char C_ser[33];
        size_t c_len = 33;
        secp256k1_ec_pubkey_serialize(ctx, C_ser, &c_len, &commitment_C_vec[i], SECP256K1_EC_COMPRESSED);
        SHA256_Update(&sha, C_ser, 33);
    }
    SHA256_Update(&sha, A_ser, 33);
    SHA256_Update(&sha, S_ser, 33);
    SHA256_Update(&sha, y, 32);
    SHA256_Final(z, &sha);
    secp256k1_mpt_scalar_reduce32(z, z);

    if (!secp256k1_ec_seckey_verify(ctx, y) || !secp256k1_ec_seckey_verify(ctx, z)) goto fail;

    /* Powers */
    unsigned char* y_powers      = (unsigned char*)malloc(n * 32);
    unsigned char* y_inv_powers  = (unsigned char*)malloc(n * 32);
    if (!y_powers || !y_inv_powers) { free(y_powers); free(y_inv_powers); goto fail; }

    unsigned char y_inv[32];
    scalar_vector_powers(ctx, (unsigned char (*)[32])y_powers, y, n);
    secp256k1_mpt_scalar_inverse(y_inv, y);
    scalar_vector_powers(ctx, (unsigned char (*)[32])y_inv_powers, y_inv, n);

    /* --- Fiat–Shamir: x --- */
    SHA256_Init(&sha);
    if (context_id) SHA256_Update(&sha, context_id, 32);
    SHA256_Update(&sha, A_ser,  33);
    SHA256_Update(&sha, S_ser,  33);
    SHA256_Update(&sha, y,      32);
    SHA256_Update(&sha, z,      32);
    SHA256_Update(&sha, T1_ser, 33);
    SHA256_Update(&sha, T2_ser, 33);
    SHA256_Final(x, &sha);
    secp256k1_mpt_scalar_reduce32(x, x);

    if (!secp256k1_ec_seckey_verify(ctx, x)) { free(y_powers); free(y_inv_powers); goto fail; }

    /* z^2 */
    secp256k1_mpt_scalar_mul(z_sq, z, z);

    /* =========================================================================
     * Step 3: Verify polynomial identity:
     *   t_hat*G + tau_x*H == (sum_j z^(j+2) * V_j) + delta(y,z)*G + x*T1 + x^2*T2
     * ========================================================================= */

    /* --- delta(y,z) for aggregation --- */
    unsigned char (*y_block_sum)[32] = (unsigned char (*)[32])malloc(m * 32);
    if (!y_block_sum) { free(y_powers); free(y_inv_powers); goto fail; }

    unsigned char two_sum[32];
    compute_delta_scalars(ctx, y_block_sum, two_sum, y, m);

    unsigned char delta[32] = {0};
    unsigned char sum_y_all[32] = {0};

/* sum_y_all = sum_{k=0}^{n-1} y^k = sum_j y_block_sum[j] */
    for (size_t j = 0; j < m; j++) {
        secp256k1_mpt_scalar_add(sum_y_all, sum_y_all, y_block_sum[j]);
    }

/* delta += (z - z^2) * sum_y_all */
    {
        unsigned char z_minus_z2[32], tmp[32];
        secp256k1_mpt_scalar_sub(z_minus_z2, z, z_sq);
        secp256k1_mpt_scalar_mul(tmp, z_minus_z2, sum_y_all);
        secp256k1_mpt_scalar_add(delta, delta, tmp);
        OPENSSL_cleanse(z_minus_z2, 32);
        OPENSSL_cleanse(tmp, 32);
    }

/* delta -= sum_{j=0}^{m-1} z^(j+3) * two_sum */
    for (size_t j = 0; j < m; j++) {
        unsigned char z_j3[32], tmp[32];
        scalar_pow_u32(ctx, z_j3, z, (unsigned int)(j + 3));
        secp256k1_mpt_scalar_mul(tmp, z_j3, two_sum);
        secp256k1_mpt_scalar_negate(tmp, tmp);
        secp256k1_mpt_scalar_add(delta, delta, tmp);
        OPENSSL_cleanse(z_j3, 32);
        OPENSSL_cleanse(tmp, 32);
    }

    OPENSSL_cleanse(sum_y_all, 32);


    /* LHS = t_hat*G + tau_x*Base */
    secp256k1_pubkey LHS;
    {
        unsigned char zero32[32] = {0};
        int have_t = 0, have_tau = 0;
        secp256k1_pubkey tG, tauH;

        if (memcmp(t_hat, zero32, 32) != 0) {
            if (!secp256k1_ec_pubkey_create(ctx, &tG, t_hat)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
            have_t = 1;
        }
        if (memcmp(tau_x, zero32, 32) != 0) {
            tauH = *pk_base;
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tauH, tau_x)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
            have_tau = 1;
        }
        if (have_t && have_tau) {
            const secp256k1_pubkey* pts[2] = { &tG, &tauH };
            if (!secp256k1_ec_pubkey_combine(ctx, &LHS, pts, 2)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
        } else if (have_t) {
            LHS = tG;
        } else if (have_tau) {
            LHS = tauH;
        } else {
            free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail;
        }
    }

    /* RHS = (sum_j z^(j+2) V_j) + delta*G + x*T1 + x^2*T2 */
    secp256k1_pubkey RHS;
    {
        secp256k1_pubkey acc, tmpP;
        int inited = 0;
        unsigned char zero32[32] = {0};

        /* sum_j z^(j+2) V_j */
        for (size_t j = 0; j < m; j++) {
            unsigned char z_j2[32];
            scalar_pow_u32(ctx, z_j2, z, (unsigned int)(j + 2));

            if (memcmp(z_j2, zero32, 32) != 0) {
                tmpP = commitment_C_vec[j];
                if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmpP, z_j2)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
                if (!add_term(ctx, &acc, &inited, &tmpP)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
            }
            OPENSSL_cleanse(z_j2, 32);
        }

        /* + delta*G */
        if (!scalar_is_zero(delta)) {
            secp256k1_pubkey deltaG;
            if (!secp256k1_ec_pubkey_create(ctx, &deltaG, delta)) goto fail;
            if (!add_term(ctx, &acc, &inited, &deltaG)) goto fail;
        }



        /* + x*T1 */
        if (memcmp(x, zero32, 32) != 0) {
            tmpP = T1;
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmpP, x)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
            if (!add_term(ctx, &acc, &inited, &tmpP)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
        }

        /* + x^2*T2 */
        unsigned char x_sq[32];
        secp256k1_mpt_scalar_mul(x_sq, x, x);
        if (memcmp(x_sq, zero32, 32) != 0) {
            tmpP = T2;
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmpP, x_sq)) { OPENSSL_cleanse(x_sq,32); free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
            if (!add_term(ctx, &acc, &inited, &tmpP)) { OPENSSL_cleanse(x_sq,32); free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
        }
        OPENSSL_cleanse(x_sq, 32);

       // if (!inited) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
        RHS = acc;

    }

    if (!pubkey_equal(ctx, &LHS, &RHS)) {
        printf("[VERIFY] Step3 polynomial identity failed\n");
        free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail;
    }

    /* =========================================================================
     * Step 4: Build P and Verify IPA
     * ========================================================================= */

    unsigned char ipa_transcript_id[32];
    {
        SHA256_CTX sha;
        unsigned char A_ser[33], S_ser[33], T1_ser[33], T2_ser[33];
        size_t len = 33;

        if (!secp256k1_ec_pubkey_serialize(ctx, A_ser, &len, &A,  SECP256K1_EC_COMPRESSED)) goto fail;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, S_ser, &len, &S,  SECP256K1_EC_COMPRESSED)) goto fail;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, T1_ser,&len, &T1, SECP256K1_EC_COMPRESSED)) goto fail;
        len = 33;
        if (!secp256k1_ec_pubkey_serialize(ctx, T2_ser,&len, &T2, SECP256K1_EC_COMPRESSED)) goto fail;



        SHA256_Init(&sha);
        if (context_id) SHA256_Update(&sha, context_id, 32);
        SHA256_Update(&sha, A_ser, 33);
        SHA256_Update(&sha, S_ser, 33);
        SHA256_Update(&sha, T1_ser,33);
        SHA256_Update(&sha, T2_ser,33);
        SHA256_Update(&sha, y, 32);
        SHA256_Update(&sha, z, 32);
        SHA256_Update(&sha, x, 32);
        SHA256_Update(&sha, t_hat, 32);
        SHA256_Final(ipa_transcript_id, &sha);
    }

    if (!derive_ipa_binding_challenge(ctx, ux_scalar, ipa_transcript_id, t_hat))
        goto fail;


    secp256k1_pubkey P = A;

    /* P += x*S */
    {
        secp256k1_pubkey xS = S;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &xS, x)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }


        secp256k1_pubkey newP;
        const secp256k1_pubkey* pts[2] = { &P, &xS };
        if (!secp256k1_ec_pubkey_combine(ctx, &newP, pts, 2)) goto fail;
        P = newP;

    }

    /* P += sum_{k=0}^{n-1} [ (-z)*G_k + ( z*y^k + z^(block+2)*z^2*2^i ) * (y^{-k}*H_k) ] */
    unsigned char neg_z[32];
    memcpy(neg_z, z, 32);
    if (!secp256k1_ec_seckey_negate(ctx, neg_z)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }

    for (size_t j = 0; j < m; j++) {
        unsigned char z_j2[32];
        scalar_pow_u32(ctx, z_j2, z, (unsigned int)(j + 2));

        for (size_t i = 0; i < 64; i++) {
            const size_t k = j * 64 + i;

            /* ---- Gi term: (-z) * G_k ---- */
            if (!scalar_is_zero(neg_z)) {
                secp256k1_pubkey Gi = G_vec[k];
                if (!secp256k1_ec_pubkey_tweak_mul(ctx, &Gi, neg_z)) goto fail;

                secp256k1_pubkey newP;
                const secp256k1_pubkey* pts2[2] = { &P, &Gi };
                if (!secp256k1_ec_pubkey_combine(ctx, &newP, pts2, 2)) goto fail;
                P = newP;
            }

            /* ---- Hi term: termH * (y^{-k} * H_k) ---- */
            unsigned char termH[32], tmp[32];
            unsigned char two_i[32] = {0};

            secp256k1_mpt_scalar_mul(termH, z, (const unsigned char*)(y_powers + 32*k));

            two_i[31 - (i / 8)] = (unsigned char)(1u << (i % 8));
            secp256k1_mpt_scalar_mul(tmp, z_j2, two_i);
            secp256k1_mpt_scalar_add(termH, termH, tmp);

            if (!scalar_is_zero(termH)) {
                secp256k1_pubkey Hi = H_vec[k];

                /* Hi = (y^{-k} * H_k) */
                if (!secp256k1_ec_pubkey_tweak_mul(ctx, &Hi, (const unsigned char*)(y_inv_powers + 32*k)))
                    goto fail;

                /* Hi = termH * Hi */
                if (!secp256k1_ec_pubkey_tweak_mul(ctx, &Hi, termH))
                    goto fail;

                secp256k1_pubkey newP;
                const secp256k1_pubkey* pts2[2] = { &P, &Hi };
                if (!secp256k1_ec_pubkey_combine(ctx, &newP, pts2, 2)) goto fail;
                P = newP;
            }

            OPENSSL_cleanse(tmp, 32);
            OPENSSL_cleanse(termH, 32);
        }

        OPENSSL_cleanse(z_j2, 32);
    }


    /* P += (t_hat * ux) * U */
    {
        unsigned char t_hat_ux[32];
        unsigned char zero32[32] = {0};
        secp256k1_mpt_scalar_mul(t_hat_ux, t_hat, ux_scalar);

        if (memcmp(t_hat_ux, zero32, 32) != 0) {
            secp256k1_pubkey Q = U;
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &Q, t_hat_ux)) { OPENSSL_cleanse(t_hat_ux,32); free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
            const secp256k1_pubkey* pts2[2] = { &P, &Q };
            secp256k1_pubkey newP;
            if (!secp256k1_ec_pubkey_combine(ctx, &newP, pts2, 2)) goto fail;
            P = newP;

        }
        OPENSSL_cleanse(t_hat_ux, 32);
    }

    /* P -= mu*pk_base */
    {
        unsigned char neg_mu[32];
        memcpy(neg_mu, mu, 32);
        if (!secp256k1_ec_seckey_negate(ctx, neg_mu)) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }

        secp256k1_pubkey mu_term = *pk_base;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &mu_term, neg_mu)) { OPENSSL_cleanse(neg_mu,32); free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }
        OPENSSL_cleanse(neg_mu, 32);

        const secp256k1_pubkey* pts2[2] = { &P, &mu_term };
        secp256k1_pubkey newP;
        if (!secp256k1_ec_pubkey_combine(ctx, &newP, pts2, 2)) goto fail;
        P = newP;

    }

    /* Build Hprime = y^{-k} * H_k (length n) */
    secp256k1_pubkey* Hprime = (secp256k1_pubkey*)malloc(n * sizeof(secp256k1_pubkey));
    if (!Hprime) { free(y_block_sum); free(y_powers); free(y_inv_powers); goto fail; }

    for (size_t k = 0; k < n; k++) {
        Hprime[k] = H_vec[k];
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &Hprime[k], (const unsigned char*)(y_inv_powers + 32 * k))) {
            free(Hprime);
            free(y_block_sum); free(y_powers); free(y_inv_powers);
            goto fail;
        }
    }
    /* IPA verify */

    int ok = ipa_verify_explicit(
            ctx,
            G_vec,
            Hprime,
            &U,
            &P,
            L_vec,
            R_vec,
            n,              /* <-- add this */
            a_final,
            b_final,
            ux_scalar,
            ipa_transcript_id
    );


    free(Hprime);
    free(y_block_sum);
    free(y_powers);
    free(y_inv_powers);
    free(L_vec);
    free(R_vec);

    return ok ? 1 : 0;

    fail:
    free(L_vec);
    free(R_vec);
    return 0;
}

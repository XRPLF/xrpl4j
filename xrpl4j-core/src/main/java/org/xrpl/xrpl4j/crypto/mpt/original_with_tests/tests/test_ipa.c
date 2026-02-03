#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <secp256k1.h>
#include <openssl/rand.h>
#include <openssl/sha.h>
#include "secp256k1_mpt.h"

#define N_BITS 64
/* log2(64) = 6 rounds */
#define IPA_ROUNDS 6

/* ---- Helper Macros ---- */
static int scalar_is_zero(const unsigned char s[32]) {
    unsigned char z[32] = {0};
    return memcmp(s, z, 32) == 0;
}

/* ---- Forward declarations from your bulletproof_aggregated.c implementation ---- */

/** NOTE: These signatures must match exactly what is in bulletproof_aggregated.c */

extern int secp256k1_bulletproof_run_ipa_prover(
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
);

/* These helpers are needed for the manual verification logic in this test */
extern int derive_ipa_round_challenge(
        const secp256k1_context* ctx,
        unsigned char u_out[32],
        const unsigned char last_challenge[32],
        const secp256k1_pubkey* L,
        const secp256k1_pubkey* R
);

extern int fold_generators(
        const secp256k1_context* ctx,
        secp256k1_pubkey* final_point,
        const secp256k1_pubkey* generators,
        const unsigned char* u_flat,
        const unsigned char* uinv_flat,
        size_t n,
        size_t rounds,
        int is_H
);

extern int apply_ipa_folding_to_P(
        const secp256k1_context* ctx,
        secp256k1_pubkey* P,
        const secp256k1_pubkey* L_vec,
        const secp256k1_pubkey* R_vec,
        const unsigned char* u_flat,
        const unsigned char* uinv_flat,
        size_t rounds
);

extern int secp256k1_bulletproof_ipa_dot(
        const secp256k1_context* ctx,
        unsigned char* out,
        const unsigned char* a,
        const unsigned char* b,
        size_t n
);

/* ---- Test Utils ---- */

static void random_scalar(const secp256k1_context* ctx, unsigned char s[32]) {
    do { RAND_bytes(s, 32); }
    while (!secp256k1_ec_seckey_verify(ctx, s));
}

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
        secp256k1_pubkey sum;
        const secp256k1_pubkey* pts[2] = { acc, term };
        if (!secp256k1_ec_pubkey_combine(ctx, &sum, pts, 2)) return 0;
        *acc = sum;
        return 1;
    }
}

static int pubkey_equal(const secp256k1_context* ctx, const secp256k1_pubkey* a, const secp256k1_pubkey* b) {
    return secp256k1_ec_pubkey_cmp(ctx, a, b) == 0;
}

static int test_ipa_verify_explicit(
        const secp256k1_context* ctx,
        const secp256k1_pubkey* G_vec,
        const secp256k1_pubkey* H_vec,
        const secp256k1_pubkey* U,
        const secp256k1_pubkey* P_in,
        const secp256k1_pubkey* L_vec,
        const secp256k1_pubkey* R_vec,
        size_t n,
        const unsigned char a_final[32],
        const unsigned char b_final[32],
        const unsigned char ux[32],
        const unsigned char ipa_transcript_id[32]
) {
    secp256k1_pubkey P = *P_in;
    secp256k1_pubkey Gf, Hf, RHS, tmp;
    int RHS_inited = 0;
    int ok = 0;
    size_t i;

    /* 1. Calculate Rounds */
    size_t rounds = 0;
    size_t tmp_n = n;
    while (tmp_n > 1) { tmp_n >>= 1; rounds++; }

    /* 2. Allocate and Derive Challenges */
    unsigned char* u_flat    = (unsigned char*)malloc(rounds * 32);
    unsigned char* uinv_flat = (unsigned char*)malloc(rounds * 32);
    unsigned char last[32];

    if (!u_flat || !uinv_flat) goto cleanup;

    memcpy(last, ipa_transcript_id, 32);

    for (i = 0; i < rounds; i++) {
        unsigned char* ui    = u_flat    + 32 * i;
        unsigned char* uiinv = uinv_flat + 32 * i;

        if (!derive_ipa_round_challenge(ctx, ui, last, &L_vec[i], &R_vec[i]))
            goto cleanup;

        secp256k1_mpt_scalar_inverse(uiinv, ui);
        memcpy(last, ui, 32);
    }

    /* 3. Fold Generators */
    if (!fold_generators(ctx, &Gf, G_vec, u_flat, uinv_flat, n, rounds, 0)) goto cleanup;
    if (!fold_generators(ctx, &Hf, H_vec, u_flat, uinv_flat, n, rounds, 1)) goto cleanup;

    /* 4. Compute RHS = a*Gf + b*Hf + (a*b*ux)*U */
    if (!scalar_is_zero(a_final)) {
        tmp = Gf;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, a_final)) goto cleanup;
        add_term(ctx, &RHS, &RHS_inited, &tmp);
    }

    if (!scalar_is_zero(b_final)) {
        tmp = Hf;
        if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, b_final)) goto cleanup;
        add_term(ctx, &RHS, &RHS_inited, &tmp);
    }

    {
        unsigned char ab[32], ab_ux[32];
        secp256k1_mpt_scalar_mul(ab, a_final, b_final);
        secp256k1_mpt_scalar_mul(ab_ux, ab, ux);

        if (!scalar_is_zero(ab_ux)) {
            tmp = *U;
            if (!secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, ab_ux)) goto cleanup;
            add_term(ctx, &RHS, &RHS_inited, &tmp);
        }
    }

    if (!RHS_inited) goto cleanup;

    /* 5. Fold P */
    if (!apply_ipa_folding_to_P(ctx, &P, L_vec, R_vec, u_flat, uinv_flat, rounds))
        goto cleanup;

    /* 6. Compare */
    if (pubkey_equal(ctx, &P, &RHS)) {
        ok = 1;
    }

    cleanup:
    if (u_flat) free(u_flat);
    if (uinv_flat) free(uinv_flat);
    return ok;
}

int main(void) {
    secp256k1_context* ctx =
            secp256k1_context_create(SECP256K1_CONTEXT_SIGN | SECP256K1_CONTEXT_VERIFY);

    printf("[IPA TEST] Setup...\n");

    /* 1. Generators */
    secp256k1_pubkey G[N_BITS], H[N_BITS], U;
    if (!secp256k1_mpt_get_generator_vector(ctx, G, N_BITS, (unsigned char*)"G", 1)) return 1;
    if (!secp256k1_mpt_get_generator_vector(ctx, H, N_BITS, (unsigned char*)"H", 1)) return 1;
    if (!secp256k1_mpt_get_generator_vector(ctx, &U, 1, (unsigned char*)"BP_U", 4)) return 1;

    /* Copy for verifier (since prover folds in-place) */
    secp256k1_pubkey G0[N_BITS], H0[N_BITS];
    memcpy(G0, G, sizeof(G));
    memcpy(H0, H, sizeof(H));

    /* 2. Random Vectors a, b */
    unsigned char a_vec[N_BITS * 32];
    unsigned char b_vec[N_BITS * 32];
    for (int i = 0; i < N_BITS; i++) {
        random_scalar(ctx, &a_vec[i*32]);
        random_scalar(ctx, &b_vec[i*32]);
    }

    /* 3. Dot Product */
    unsigned char dot[32];
    secp256k1_bulletproof_ipa_dot(ctx, dot, a_vec, b_vec, N_BITS);

    /* 4. Transcript & Binding Challenge */
    unsigned char ipa_transcript_id[32];
    SHA256((unsigned char*)"IPA_TEST", 8, ipa_transcript_id);

    unsigned char ux[32];
    {
        SHA256_CTX sha;
        SHA256_Init(&sha);
        SHA256_Update(&sha, ipa_transcript_id, 32);
        SHA256_Update(&sha, dot, 32);
        SHA256_Final(ux, &sha);
        /* Reduce is handled in real code, here just check verify */
        secp256k1_mpt_scalar_reduce32(ux, ux);
    }

    /* 5. Build Initial Commitment P = <a,G> + <b,H> + <a,b>*ux*U */
    secp256k1_pubkey P, tmp;
    int P_inited = 0;

    for (int i = 0; i < N_BITS; i++) {
        unsigned char* ai = &a_vec[i*32];
        unsigned char* bi = &b_vec[i*32];

        if (!scalar_is_zero(ai)) {
            tmp = G0[i];
            secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, ai);
            add_term(ctx, &P, &P_inited, &tmp);
        }
        if (!scalar_is_zero(bi)) {
            tmp = H0[i];
            secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, bi);
            add_term(ctx, &P, &P_inited, &tmp);
        }
    }

    /* Add Binding Term: (<a,b>*ux)*U */
    unsigned char dot_ux[32];
    secp256k1_mpt_scalar_mul(dot_ux, dot, ux);
    if (!scalar_is_zero(dot_ux)) {
        tmp = U;
        secp256k1_ec_pubkey_tweak_mul(ctx, &tmp, dot_ux);
        add_term(ctx, &P, &P_inited, &tmp);
    }
    assert(P_inited);

    /* 6. Run Prover */
    printf("[IPA TEST] Running Prover...\n");
    secp256k1_pubkey L[IPA_ROUNDS], R[IPA_ROUNDS];
    unsigned char a_final[32], b_final[32];
    size_t rounds_out = 0;

    int res = secp256k1_bulletproof_run_ipa_prover(
            ctx,
            &U,
            G, H,
            a_vec, b_vec, /* These get folded in-place */
            N_BITS,
            ipa_transcript_id,
            ux,
            L, R,
            IPA_ROUNDS,
            &rounds_out,
            a_final, b_final
    );
    assert(res == 1);
    assert(rounds_out == IPA_ROUNDS);

    /* 7. Run Verifier */
    printf("[IPA TEST] Running Verifier...\n");
    int ok = test_ipa_verify_explicit(
            ctx,
            G0, H0,
            &U,
            &P,
            L, R,
            N_BITS,
            a_final, b_final,
            ux,
            ipa_transcript_id
    );

    printf("[IPA TEST] Result: %s\n", ok ? "PASSED" : "FAILED");
    assert(ok == 1);

    secp256k1_context_destroy(ctx);
    return 0;
}

#include "test_utils.h"

#include <algorithm>
#include <iostream>
#include <vector>
#include <secp256k1_mpt.h>
#include <utility/mpt_utility.h>

// helper to create mock accounts and issuance IDs
template <typename T>
T create_mock_id(uint8_t fill) {
    T mock;
    std::fill(std::begin(mock.bytes), std::end(mock.bytes), fill);
    return mock;
}

// Internal usage only for verifying bullet proof for convert back.
static int
mpt_compute_convert_back_remainder(
    uint8_t const commitment[kMPT_PEDERSEN_COMMIT_SIZE],
    uint64_t amount,
    uint8_t out_rem[kMPT_PEDERSEN_COMMIT_SIZE])
{
    secp256k1_context const* ctx = mpt_secp256k1_context();

    secp256k1_pubkey pc_balance;
    if (secp256k1_ec_pubkey_parse(ctx, &pc_balance, commitment, kMPT_PEDERSEN_COMMIT_SIZE) != 1)
        return -1;

    // Convert amount to 32-byte big-endian scalar
    uint8_t scalar[32] = {0};
    for (int i = 0; i < 8; ++i) {
        scalar[31 - i] = static_cast<uint8_t>(amount >> (i * 8));
    }

    // Calculate mG and negate it to get -mG
    secp256k1_pubkey mG;
    if (secp256k1_ec_pubkey_create(ctx, &mG, scalar) != 1)
        return -1;

    if (secp256k1_ec_pubkey_negate(ctx, &mG) != 1)
        return -1;

    // Calculate pc_rem = pc_balance - mG
    secp256k1_pubkey const* summands[2] = {&pc_balance, &mG};
    secp256k1_pubkey pc_rem;
    if (secp256k1_ec_pubkey_combine(ctx, &pc_rem, summands, 2) != 1)
        return -1;

    size_t out_len = kMPT_PEDERSEN_COMMIT_SIZE;
    return (secp256k1_ec_pubkey_serialize(ctx, out_rem, &out_len, &pc_rem, SECP256K1_EC_COMPRESSED) == 1) ? 0 : -1;
}

// Internal usage only for verifying bullet proof for confidential send.
static int
compute_send_remainder(
    secp256k1_context const* ctx,
    uint8_t const balance_commitment[kMPT_PEDERSEN_COMMIT_SIZE],
    uint8_t const amount_commitment[kMPT_PEDERSEN_COMMIT_SIZE],
    secp256k1_pubkey* out_rem)
{
    secp256k1_pubkey pc_balance;
    if (secp256k1_ec_pubkey_parse(ctx, &pc_balance, balance_commitment, kMPT_PEDERSEN_COMMIT_SIZE) != 1)
        return -1;

    secp256k1_pubkey pc_amount;
    if (secp256k1_ec_pubkey_parse(ctx, &pc_amount, amount_commitment, kMPT_PEDERSEN_COMMIT_SIZE) != 1)
        return -1;

    // Negate PC_amount point to get -PC_amount
    if (secp256k1_ec_pubkey_negate(ctx, &pc_amount) != 1)
        return -1;

    // Compute pc_rem = pc_balance + (-pc_amount)
    secp256k1_pubkey const* summands[2] = {&pc_balance, &pc_amount};
    if (secp256k1_ec_pubkey_combine(ctx, out_rem, summands, 2) != 1)
        return -1;

    return 0;
}

// Internal use only for verifying aggregated bullet proofs.
static int
mpt_verify_aggregated_bulletproof(
    uint8_t const* proof,
    size_t proof_len,
    std::vector<uint8_t const*> const& compressed_commitments,
    uint8_t const context_hash[kMPT_HALF_SHA_SIZE])
{
    size_t const m = compressed_commitments.size();
    if (m == 0 || (m & (m - 1)) != 0)
        return -1;

    secp256k1_context const* ctx = mpt_secp256k1_context();

    std::vector<secp256k1_pubkey> commitments(m);
    for (size_t i = 0; i < m; ++i) {
        if (secp256k1_ec_pubkey_parse(ctx, &commitments[i],
            compressed_commitments[i], kMPT_PEDERSEN_COMMIT_SIZE) != 1)
            return -1;
    }

    size_t const n = 64 * m;
    std::vector<secp256k1_pubkey> G_vec(n);
    std::vector<secp256k1_pubkey> H_vec(n);

    if (secp256k1_mpt_get_generator_vector(ctx, G_vec.data(), n, (unsigned char const*)"G", 1) != 1)
        return -1;

    if (secp256k1_mpt_get_generator_vector(ctx, H_vec.data(), n, (unsigned char const*)"H", 1) != 1)
        return -1;

    secp256k1_pubkey pk_base;
    if (secp256k1_mpt_get_h_generator(ctx, &pk_base) != 1)
        return -1;

    int const res = secp256k1_bulletproof_verify_agg(
        ctx,
        G_vec.data(),
        H_vec.data(),
        proof,
        proof_len,
        commitments.data(),
        m,
        &pk_base,
        context_hash);

    return (res == 1) ? 0 : -1;
}

void
test_encryption_decryption()
{
    uint8_t priv[kMPT_PRIVKEY_SIZE];
    uint8_t pub[kMPT_PUBKEY_SIZE];
    uint8_t bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t ciphertext[kMPT_ELGAMAL_TOTAL_SIZE];
    EXPECT(mpt_generate_keypair(priv, pub) == 0);

    std::vector<uint64_t> test_amounts = {
        0, 1, 1000,
        // todo: due to the lib's current limitation, large numbers
        // are not supported yet. We need to add them back once the limitation is fixed.
        // 123456789,
        // 10000000000ULL
    };

    for (uint64_t original_amount : test_amounts)
    {
        uint64_t decrypted_amount = 0;

        EXPECT(mpt_generate_blinding_factor(bf) == 0);
        EXPECT(mpt_encrypt_amount(original_amount, pub, bf, ciphertext) == 0);
        EXPECT(mpt_decrypt_amount(ciphertext, priv, &decrypted_amount) == 0);
        EXPECT(decrypted_amount == original_amount);
    }
}

void
test_mpt_confidential_convert()
{
    // Setup mock account, issuance and transaction details
    account_id acc = create_mock_id<account_id>(0xAA);
    mpt_issuance_id issuance = create_mock_id<mpt_issuance_id>(0xBB);
    uint32_t seq = 12345;
    uint64_t convert_amount = 750;

    uint8_t priv[kMPT_PRIVKEY_SIZE];
    uint8_t pub[kMPT_PUBKEY_SIZE];
    uint8_t bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t ciphertext[kMPT_ELGAMAL_TOTAL_SIZE];
    uint8_t tx_hash[kMPT_HALF_SHA_SIZE];
    uint8_t proof[kMPT_SCHNORR_PROOF_SIZE];

    EXPECT(mpt_generate_keypair(priv, pub) == 0);
    EXPECT(mpt_generate_blinding_factor(bf) == 0);
    EXPECT(mpt_encrypt_amount(convert_amount, pub, bf, ciphertext) == 0);

    EXPECT(mpt_get_convert_context_hash(acc, seq, issuance, convert_amount, tx_hash) == 0);
    EXPECT(mpt_get_convert_proof(pub, priv, tx_hash, proof) == 0);

    secp256k1_context const* ctx = mpt_secp256k1_context();

    secp256k1_pubkey c1, c2, pk;
    EXPECT(secp256k1_ec_pubkey_parse(ctx, &c1, ciphertext, kMPT_ELGAMAL_CIPHER_SIZE) == 1);
    EXPECT(
        secp256k1_ec_pubkey_parse(
            ctx, &c2, ciphertext + kMPT_ELGAMAL_CIPHER_SIZE, kMPT_ELGAMAL_CIPHER_SIZE) == 1);

    EXPECT(secp256k1_ec_pubkey_parse(ctx, &pk, pub, kMPT_PUBKEY_SIZE) == 1);
    EXPECT(secp256k1_elgamal_verify_encryption(ctx, &c1, &c2, &pk, convert_amount, bf) == 1);
    EXPECT(secp256k1_mpt_pok_sk_verify(ctx, proof, &pk, tx_hash) == 1);
}

void
test_mpt_confidential_send()
{
    // Setup mock account, issuance and transaction details
    account_id sender_acc = create_mock_id<account_id>(0x11);
    account_id dest_acc = create_mock_id<account_id>(0x22);
    mpt_issuance_id issuance = create_mock_id<mpt_issuance_id>(0xBB);
    uint32_t seq = 54321;
    uint64_t amount_to_send = 100;
    uint64_t prev_balance = 2000;
    uint32_t version = 1;

    // Generate Keypairs for all parties
    uint8_t sender_priv[kMPT_PRIVKEY_SIZE], sender_pub[kMPT_PUBKEY_SIZE];
    uint8_t dest_priv[kMPT_PRIVKEY_SIZE], dest_pub[kMPT_PUBKEY_SIZE];
    uint8_t issuer_priv[kMPT_PRIVKEY_SIZE], issuer_pub[kMPT_PUBKEY_SIZE];

    EXPECT(mpt_generate_keypair(sender_priv, sender_pub) == 0);
    EXPECT(mpt_generate_keypair(dest_priv, dest_pub) == 0);
    EXPECT(mpt_generate_keypair(issuer_priv, issuer_pub) == 0);

    // Encrypt for all recipients (using same shared blinding factor for link proof)
    uint8_t shared_bf[kMPT_BLINDING_FACTOR_SIZE];
    EXPECT(mpt_generate_blinding_factor(shared_bf) == 0);

    uint8_t sender_ct[kMPT_ELGAMAL_TOTAL_SIZE];
    uint8_t dest_ct[kMPT_ELGAMAL_TOTAL_SIZE];
    uint8_t issuer_ct[kMPT_ELGAMAL_TOTAL_SIZE];

    EXPECT(mpt_encrypt_amount(amount_to_send, sender_pub, shared_bf, sender_ct) == 0);
    EXPECT(mpt_encrypt_amount(amount_to_send, dest_pub, shared_bf, dest_ct) == 0);
    EXPECT(mpt_encrypt_amount(amount_to_send, issuer_pub, shared_bf, issuer_ct) == 0);

    // Prepare recipients that is expected by the confidential send proof function
    std::vector<mpt_confidential_recipient> recipients;
    auto add_recipient = [&](uint8_t* p, uint8_t* c) {
        mpt_confidential_recipient r;
        std::copy(p, p + kMPT_PUBKEY_SIZE, r.pubkey);
        std::copy(c, c + kMPT_ELGAMAL_TOTAL_SIZE, r.encrypted_amount);
        recipients.push_back(r);
    };
    add_recipient(sender_pub, sender_ct);
    add_recipient(dest_pub, dest_ct);
    add_recipient(issuer_pub, issuer_ct);

    // Generate pedersen commitments for amount and balance
    uint8_t amount_bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t amount_comm[kMPT_PEDERSEN_COMMIT_SIZE];
    EXPECT(mpt_generate_blinding_factor(amount_bf) == 0);
    EXPECT(mpt_get_pedersen_commitment(amount_to_send, amount_bf, amount_comm) == 0);

    uint8_t balance_bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t balance_comm[kMPT_PEDERSEN_COMMIT_SIZE];
    EXPECT(mpt_generate_blinding_factor(balance_bf) == 0);
    EXPECT(mpt_get_pedersen_commitment(prev_balance, balance_bf, balance_comm) == 0);

    // Generate context hash for the transaction
    uint8_t send_ctx_hash[kMPT_HALF_SHA_SIZE];
    EXPECT(
        mpt_get_send_context_hash(sender_acc, seq, issuance, dest_acc, version, send_ctx_hash) ==
        0);

    // Prepare pedersen proof params for both amount and balance linkage proofs
    mpt_pedersen_proof_params amt_params;
    amt_params.amount = amount_to_send;
    std::copy(amount_bf, amount_bf + kMPT_BLINDING_FACTOR_SIZE, amt_params.blinding_factor);
    std::copy(amount_comm, amount_comm + kMPT_PEDERSEN_COMMIT_SIZE, amt_params.pedersen_commitment);
    std::copy(sender_ct, sender_ct + kMPT_ELGAMAL_TOTAL_SIZE, amt_params.encrypted_amount);

    mpt_pedersen_proof_params bal_params;
    bal_params.amount = prev_balance;
    std::copy(balance_bf, balance_bf + kMPT_BLINDING_FACTOR_SIZE, bal_params.blinding_factor);
    std::copy(balance_comm, balance_comm + kMPT_PEDERSEN_COMMIT_SIZE, bal_params.pedersen_commitment);

    uint8_t prev_bal_bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t prev_bal_ct[kMPT_ELGAMAL_TOTAL_SIZE];
    EXPECT(mpt_generate_blinding_factor(prev_bal_bf) == 0);
    EXPECT(mpt_encrypt_amount(prev_balance, sender_pub, prev_bal_bf, prev_bal_ct) == 0);
    std::copy(prev_bal_ct, prev_bal_ct + kMPT_ELGAMAL_TOTAL_SIZE, bal_params.encrypted_amount);

    // Generate the confidential send proof
    size_t proof_len = get_confidential_send_proof_size(recipients.size());
    std::vector<uint8_t> proof(proof_len);

    int result = mpt_get_confidential_send_proof(
        sender_priv,
        amount_to_send,
        recipients.data(),
        3,
        shared_bf,
        send_ctx_hash,
        &amt_params,
        &bal_params,
        proof.data(),
        &proof_len);

    EXPECT(result == 0);

    // The rest of code in this function is to verify the proof
    // we just generated, simulating what a verifier would do in rippled.
    secp256k1_context const* ctx = mpt_secp256k1_context();
    size_t current_offset = 0;

    // Verify multi-ciphertext equality
    size_t n_recipients = recipients.size();
    size_t sizeEquality = get_multi_ciphertext_equality_proof_size(n_recipients);

    std::vector<secp256k1_pubkey> r_list(n_recipients);
    std::vector<secp256k1_pubkey> s_list(n_recipients);
    std::vector<secp256k1_pubkey> pk_list(n_recipients);

    for (size_t i = 0; i < n_recipients; ++i)
    {
        EXPECT(mpt_make_ec_pair(recipients[i].encrypted_amount, r_list[i], s_list[i]));
        EXPECT(secp256k1_ec_pubkey_parse(ctx, &pk_list[i], recipients[i].pubkey, kMPT_PUBKEY_SIZE) == 1);
    }

    EXPECT(
        secp256k1_mpt_verify_same_plaintext_multi(
            ctx,
            proof.data() + current_offset,
            sizeEquality,
            n_recipients,
            r_list.data(),
            s_list.data(),
            pk_list.data(),
            send_ctx_hash) == 1);

    current_offset += sizeEquality;

    // Verify amount pedersen linkage
    secp256k1_pubkey pk, amt_pcm;
    secp256k1_pubkey amt_c1, amt_c2;

    EXPECT(secp256k1_ec_pubkey_parse(ctx, &pk, sender_pub, kMPT_PUBKEY_SIZE) == 1);
    EXPECT(secp256k1_ec_pubkey_parse(ctx, &amt_pcm, amount_comm, kMPT_PEDERSEN_COMMIT_SIZE) == 1);

    EXPECT(mpt_make_ec_pair(sender_ct, amt_c1, amt_c2));
    EXPECT(
        secp256k1_elgamal_pedersen_link_verify(
            ctx, proof.data() + current_offset, &amt_c1, &amt_c2, &pk, &amt_pcm, send_ctx_hash) ==
        1);

    current_offset += kMPT_PEDERSEN_LINK_SIZE;

    // Verify balance pedersen linkage
    secp256k1_pubkey bal_pcm;
    secp256k1_pubkey bal_c1, bal_c2;

    EXPECT(secp256k1_ec_pubkey_parse(ctx, &bal_pcm, balance_comm, kMPT_PEDERSEN_COMMIT_SIZE) == 1);
    EXPECT(mpt_make_ec_pair(prev_bal_ct, bal_c1, bal_c2));

    EXPECT(
        secp256k1_elgamal_pedersen_link_verify(
            ctx, proof.data() + current_offset, &pk, &bal_c2, &bal_c1, &bal_pcm, send_ctx_hash) ==
        1);

    current_offset += kMPT_PEDERSEN_LINK_SIZE;

    // Verify Range Proof
    secp256k1_pubkey rem_pcm;
    EXPECT(compute_send_remainder(ctx, balance_comm, amount_comm, &rem_pcm) == 0);

    std::vector<secp256k1_pubkey> bulletproof_commitments = { amt_pcm, rem_pcm };

    size_t const n = 64 * 2;
    std::vector<secp256k1_pubkey> g_vec(n);
    std::vector<secp256k1_pubkey> h_vec(n);

    EXPECT(secp256k1_mpt_get_generator_vector(ctx, g_vec.data(), n, (unsigned char const*)"G", 1) == 1);
    EXPECT(secp256k1_mpt_get_generator_vector(ctx, h_vec.data(), n, (unsigned char const*)"H", 1) == 1);

    secp256k1_pubkey h_gen;
    EXPECT(secp256k1_mpt_get_h_generator(ctx, &h_gen) == 1);

    size_t bp_size = kMPT_DOUBLE_BULLETPROOF_SIZE;
    EXPECT(
        secp256k1_bulletproof_verify_agg(
            ctx,
            g_vec.data(),
            h_vec.data(),
            proof.data() + current_offset,
            bp_size,
            bulletproof_commitments.data(),
            2, // m = 2
            &h_gen,
            send_ctx_hash) == 1);

    current_offset += bp_size;

    // Verify the entire generated proof was consumed
    EXPECT(current_offset == proof_len);
}

void
test_mpt_convert_back()
{
    // Setup mock account, issuance and transaction details
    account_id acc = create_mock_id<account_id>(0x55);
    mpt_issuance_id issuance = create_mock_id<mpt_issuance_id>(0xEE);
    uint32_t seq = 98765;
    uint64_t current_balance = 5000;
    uint64_t amount_to_convert_back = 1000;
    uint32_t version = 2;

    uint8_t priv[kMPT_PRIVKEY_SIZE], pub[kMPT_PUBKEY_SIZE];
    EXPECT(mpt_generate_keypair(priv, pub) == 0);

    // Mock spending confidential balance.
    // This is the ElGamal ciphertext currently stored on-chain.
    uint8_t bal_bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t spending_bal_ct[kMPT_ELGAMAL_TOTAL_SIZE];
    EXPECT(mpt_generate_blinding_factor(bal_bf) == 0);
    EXPECT(mpt_encrypt_amount(current_balance, pub, bal_bf, spending_bal_ct) == 0);

    // Generate context hash
    uint8_t context_hash[kMPT_HALF_SHA_SIZE];
    EXPECT(
        mpt_get_convert_back_context_hash(
            acc, seq, issuance, amount_to_convert_back, version, context_hash) == 0);

    // Generate pedersen commitments for current balance
    uint8_t pcm_bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t pcm_comm[kMPT_PEDERSEN_COMMIT_SIZE];
    EXPECT(mpt_generate_blinding_factor(pcm_bf) == 0);
    EXPECT(mpt_get_pedersen_commitment(current_balance, pcm_bf, pcm_comm) == 0);

    // Prepare pedersen proof params
    mpt_pedersen_proof_params pc_params;
    pc_params.amount = current_balance;
    std::copy(pcm_bf, pcm_bf + kMPT_BLINDING_FACTOR_SIZE, pc_params.blinding_factor);
    std::copy(pcm_comm, pcm_comm + kMPT_PEDERSEN_COMMIT_SIZE, pc_params.pedersen_commitment);
    std::copy(spending_bal_ct, spending_bal_ct + kMPT_ELGAMAL_TOTAL_SIZE, pc_params.encrypted_amount);

    // Generate proof
    uint8_t proof[kMPT_PEDERSEN_LINK_SIZE + kMPT_SINGLE_BULLETPROOF_SIZE];
    int result = mpt_get_convert_back_proof(priv, pub, context_hash, amount_to_convert_back, &pc_params, proof);

    EXPECT(result == 0);

    // The rest of code in this function is to verify the proof
    // we just generated, simulating what a verifier would do in rippled.
    secp256k1_context const* ctx = mpt_secp256k1_context();

    // Vefify balance pedersen linkage
    secp256k1_pubkey c1, c2, pk, pcm;
    EXPECT(mpt_make_ec_pair(pc_params.encrypted_amount, c1, c2));
    EXPECT(secp256k1_ec_pubkey_parse(ctx, &pk, pub, kMPT_PUBKEY_SIZE) == 1);
    EXPECT(secp256k1_ec_pubkey_parse(ctx, &pcm, pcm_comm, kMPT_PEDERSEN_COMMIT_SIZE) == 1);

    int verify_link_result =
        secp256k1_elgamal_pedersen_link_verify(ctx, proof, &pk, &c2, &c1, &pcm, context_hash);
    EXPECT(verify_link_result == 1);

    // Vefify range proof
    uint8_t derived_pc_rem[kMPT_PEDERSEN_COMMIT_SIZE];
    EXPECT(mpt_compute_convert_back_remainder(pcm_comm, amount_to_convert_back, derived_pc_rem) == 0);

    uint8_t const* bp_ptr = proof + kMPT_PEDERSEN_LINK_SIZE;
    std::vector<uint8_t const*> commitments = { derived_pc_rem };

    EXPECT(mpt_verify_aggregated_bulletproof(bp_ptr, kMPT_SINGLE_BULLETPROOF_SIZE,
           commitments, context_hash) == 0);
}

void
test_mpt_clawback()
{
    // Setup mock account, issuance and transaction details
    account_id issuer_acc = create_mock_id<account_id>(0x11);
    account_id holder_acc = create_mock_id<account_id>(0x22);
    mpt_issuance_id issuance = create_mock_id<mpt_issuance_id>(0xCC);

    uint32_t seq = 200;
    uint64_t claw_amount = 500;

    uint8_t issuer_priv[kMPT_PRIVKEY_SIZE], issuer_pub[kMPT_PUBKEY_SIZE];
    EXPECT(mpt_generate_keypair(issuer_priv, issuer_pub) == 0);

    // Generate context hash
    uint8_t context_hash[kMPT_HALF_SHA_SIZE];
    EXPECT(
        mpt_get_clawback_context_hash(
            issuer_acc, seq, issuance, claw_amount, holder_acc, context_hash) == 0);

    // Mock holder's "sfIssuerEncryptedBalance"
    uint8_t bf[kMPT_BLINDING_FACTOR_SIZE];
    uint8_t issuer_encrypted_bal[kMPT_ELGAMAL_TOTAL_SIZE];
    EXPECT(mpt_generate_blinding_factor(bf) == 0);
    EXPECT(mpt_encrypt_amount(claw_amount, issuer_pub, bf, issuer_encrypted_bal) == 0);

    // Generate proof
    uint8_t proof[kMPT_EQUALITY_PROOF_SIZE];
    int result = mpt_get_clawback_proof(
        issuer_priv, issuer_pub, context_hash, claw_amount, issuer_encrypted_bal, proof);
    EXPECT(result == 0);

    // The rest of code in this function is to verify the proof
    // we just generated, simulating what a verifier would do in rippled.
    secp256k1_context const* ctx = mpt_secp256k1_context();
    secp256k1_pubkey c1, c2, pk;

    EXPECT(mpt_make_ec_pair(issuer_encrypted_bal, c1, c2));
    EXPECT(secp256k1_ec_pubkey_parse(ctx, &pk, issuer_pub, kMPT_PUBKEY_SIZE) == 1);

    int verify_result =
        secp256k1_equality_plaintext_verify(ctx, proof, &pk, &c2, &c1, claw_amount, context_hash);

    EXPECT(verify_result == 1);
}

int
main()
{
    test_encryption_decryption();
    test_mpt_confidential_convert();
    test_mpt_confidential_send();
    test_mpt_convert_back();
    test_mpt_clawback();

    std::cout << "\n[SUCCESS] All assertions passed!" << std::endl;

    return 0;
}

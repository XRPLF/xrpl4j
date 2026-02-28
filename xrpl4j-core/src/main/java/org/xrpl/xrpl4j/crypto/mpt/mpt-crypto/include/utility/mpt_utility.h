#ifndef MPT_UTILITY_H
#define MPT_UTILITY_H

#include <secp256k1.h>
#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

// XRPL Transaction Types, the number MUST match rippled's definitions
#define ttCONFIDENTIAL_MPT_CONVERT 85
#define ttCONFIDENTIAL_MPT_MERGE_INBOX 86
#define ttCONFIDENTIAL_MPT_CONVERT_BACK 87
#define ttCONFIDENTIAL_MPT_SEND 88
#define ttCONFIDENTIAL_MPT_CLAWBACK 89

// General crypto primitive sizes in bytes
#define kMPT_HALF_SHA_SIZE 32
#define kMPT_PUBKEY_SIZE 33
#define kMPT_PRIVKEY_SIZE 32
#define kMPT_BLINDING_FACTOR_SIZE 32

// Gamal & Pedersen primitive sizes in bytes
#define kMPT_ELGAMAL_CIPHER_SIZE 33
#define kMPT_ELGAMAL_TOTAL_SIZE 66
#define kMPT_PEDERSEN_COMMIT_SIZE 33

// Proof sizes in bytes
#define kMPT_SCHNORR_PROOF_SIZE 65
#define kMPT_EQUALITY_PROOF_SIZE 98
#define kMPT_PEDERSEN_LINK_SIZE 195
#define kMPT_SINGLE_BULLETPROOF_SIZE 688
#define kMPT_DOUBLE_BULLETPROOF_SIZE 754

// Field sizes in bytes for context hash
#define kMPT_TYPE_SIZE 2
#define kMPT_ACCOUNT_ID_SIZE 20
#define kMPT_SEQUENCE_SIZE 4
#define kMPT_ISSUANCE_ID_SIZE 24
#define kMPT_AMOUNT_SIZE 8
#define kMPT_VERSION_SIZE 4

// Context hash sizes
#define kMPT_COMMON_HASH_SIZE                                     \
    (kMPT_TYPE_SIZE + kMPT_ACCOUNT_ID_SIZE + kMPT_SEQUENCE_SIZE + \
     kMPT_ISSUANCE_ID_SIZE)                                                // 50 bytes
#define kMPT_CONVERT_HASH_SIZE (kMPT_COMMON_HASH_SIZE + kMPT_AMOUNT_SIZE)  // 58 bytes
#define kMPT_SEND_HASH_SIZE \
    (kMPT_COMMON_HASH_SIZE + kMPT_ACCOUNT_ID_SIZE + kMPT_VERSION_SIZE)  // 74 bytes
#define kMPT_CONVERT_BACK_HASH_SIZE \
    (kMPT_COMMON_HASH_SIZE + kMPT_AMOUNT_SIZE + kMPT_VERSION_SIZE)  // 62 bytes
#define kMPT_CLAWBACK_HASH_SIZE \
    (kMPT_COMMON_HASH_SIZE + kMPT_AMOUNT_SIZE + kMPT_ACCOUNT_ID_SIZE)  // 78 bytes

/**
 * @brief Represents a unique 24-byte MPT issuance ID.
 */
typedef struct
{
    uint8_t bytes[kMPT_ISSUANCE_ID_SIZE];
} mpt_issuance_id;

/**
 * @brief Represents a 20-byte account ID.
 */
typedef struct
{
    uint8_t bytes[kMPT_ACCOUNT_ID_SIZE];
} account_id;

/**
 * @brief Represents a recipient in a Confidential Send transaction.
 * Contains a pubkey and an encrypted amount.
 */
struct mpt_confidential_recipient
{
    uint8_t pubkey[kMPT_PUBKEY_SIZE];
    uint8_t encrypted_amount[kMPT_ELGAMAL_TOTAL_SIZE];
};

/**
 * @brief Parameters required to generate a Pedersen Linkage Proof.
 */
struct mpt_pedersen_proof_params
{
    /**
     * @brief The 64-byte Pedersen commitment.
     */
    uint8_t pedersen_commitment[kMPT_PEDERSEN_COMMIT_SIZE];

    /**
     * @brief The actual numeric value being committed.
     */
    uint64_t amount;

    /**
     * @brief The 66-byte buffer containing the encrypted amount.
     */
    uint8_t encrypted_amount[kMPT_ELGAMAL_TOTAL_SIZE];

    /**
     * @brief The 32-byte secret random value used to blind the Pedersen commitment.
     */
    uint8_t blinding_factor[kMPT_BLINDING_FACTOR_SIZE];
};

secp256k1_context*
mpt_secp256k1_context();

/**
 * @brief Context Hash for ConfidentialMPTConvert.
 */
int
mpt_get_convert_context_hash(
    account_id account,
    uint32_t sequence,
    mpt_issuance_id issuanceID,
    uint64_t amount,
    uint8_t out_hash[kMPT_HALF_SHA_SIZE]);

/**
 * @brief Context Hash for ConfidentialMPTConvertBack.
 */
int
mpt_get_convert_back_context_hash(
    account_id account,
    uint32_t sequence,
    mpt_issuance_id issuanceID,
    uint64_t amount,
    uint32_t version,
    uint8_t out_hash[kMPT_HALF_SHA_SIZE]);

/**
 * @brief Context Hash for ConfidentialMPTSend.
 */
int
mpt_get_send_context_hash(
    account_id account,
    uint32_t sequence,
    mpt_issuance_id issuanceID,
    account_id destination,
    uint32_t version,
    uint8_t out_hash[kMPT_HALF_SHA_SIZE]);

/**
 * @brief Context Hash for ConfidentialMPTClawback.
 */
int
mpt_get_clawback_context_hash(
    account_id account,
    uint32_t sequence,
    mpt_issuance_id issuanceID,
    uint64_t amount,
    account_id holder,
    uint8_t out_hash[kMPT_HALF_SHA_SIZE]);

/**
 * @brief Calculates the size of the Multi-Ciphertext Equality Proof.
 */
size_t
get_multi_ciphertext_equality_proof_size(size_t n_recipients);

/**
 * @brief Calculates the total size for a ConfidentialMPTSend proof.
 */
size_t
get_confidential_send_proof_size(size_t n_recipients);

/* ============================================================================
 * Key & Ciphertext Utilities
 * ============================================================================ */

/**
 * @brief Parses a 66-byte buffer into two internal secp256k1 public keys.
 * @param buffer [in] 66-byte buffer containing two points.
 * @param out1   [out] First internal public key (C1).
 * @param out2   [out] Second internal public key (C2).
 * @return true on success, false if parsing fails.
 */
bool
mpt_make_ec_pair(
    uint8_t const buffer[kMPT_ELGAMAL_TOTAL_SIZE],
    secp256k1_pubkey& out1,
    secp256k1_pubkey& out2);

/**
 * @brief Serializes two internal secp256k1 public keys into a 66-byte buffer.
 * @param in1   [in] Internal format of the first point (C1).
 * @param in2   [in] Internal format of the second point (C2).
 * @param out   [out] 66-byte buffer to write the serialized points.
 * @return true if both points were valid and successfully serialized, false otherwise.
 */
bool
mpt_serialize_ec_pair(
    secp256k1_pubkey const& in1,
    secp256k1_pubkey const& in2,
    uint8_t out[kMPT_ELGAMAL_TOTAL_SIZE]);

/**
 * @brief Generates a new Secp256k1 ElGamal keypair.
 * @param out_privkey [out] A 32-byte buffer for private key.
 * @param out_pubkey  [out] A 33-byte buffer for public key.
 * @return 0 on success, -1 on failure.
 */
int
mpt_generate_keypair(uint8_t* out_privkey, uint8_t* out_pubkey);

/**
 * @brief Generates a 32-byte blinding factor.
 * @param out_factor [out] A 32-byte buffer to store the blinding factor.
 * @return 0 on success, -1 on failure.
 */
int
mpt_generate_blinding_factor(uint8_t out_factor[kMPT_BLINDING_FACTOR_SIZE]);

/**
 * @brief Encrypts an uint64 amount using an ElGamal public key.
 * @param amount           [in]  The integer value to encrypt.
 * @param pubkey           [in]  The 33-byte public key.
 * @param blinding_factor  [in]  The 32-byte random blinding factor (scalar r).
 * @param out_ciphertext   [out] A 66-byte buffer to store the resulting ciphertext (C1, C2).
 * @return 0 on success, -1 on failure.
 */
int
mpt_encrypt_amount(
    uint64_t amount,
    uint8_t const pubkey[kMPT_PUBKEY_SIZE],
    uint8_t const blinding_factor[kMPT_BLINDING_FACTOR_SIZE],
    uint8_t out_ciphertext[kMPT_ELGAMAL_TOTAL_SIZE]);

/**
 * @brief Decrypts an MPT amount from a ciphertext pair.
 * @param ciphertext [in]  A 66-byte buffer containing the two points (C1, C2).
 * @param privkey    [in]  The 32-byte private key.
 * @param out_amount [out] Pointer to store the decrypted uint64_t amount.
 * @return 0 on success, -1 on failure.
 */
int
mpt_decrypt_amount(
    uint8_t const ciphertext[kMPT_ELGAMAL_TOTAL_SIZE],
    uint8_t const privkey[kMPT_PRIVKEY_SIZE],
    uint64_t* out_amount);

/* ============================================================================
 * Proof Generation
 * ============================================================================ */

/**
 * @brief Generates a Schnorr Proof of Knowledge for a Confidential MPT conversion.
 *
 * This proof is used in 'ConfidentialMPTConvert' transactions to prove the
 * sender possesses the private key associated with the account, binding it
 * to the specific transaction via the ctx_hash.
 *
 * @param pubkey    [in]  33-byte public key of the account.
 * @param privkey   [in]  32-byte private key of the account.
 * @param ctx_hash  [in]  32-byte hash of the transaction (challenge).
 * @param out_proof [out] 65-byte buffer to store the Schnorr proof.
 * @return 0 on success, -1 on failure.
 */
int
mpt_get_convert_proof(
    uint8_t const pubkey[kMPT_PUBKEY_SIZE],
    uint8_t const privkey[kMPT_PRIVKEY_SIZE],
    uint8_t const ctx_hash[kMPT_HALF_SHA_SIZE],
    uint8_t out_proof[kMPT_SCHNORR_PROOF_SIZE]);

/**
 * @brief Computes a Pedersen Commitment point for Confidential MPT.
 * @param amount           [in]  The 64-bit unsigned integer value to commit.
 * @param blinding_factor  [in]  A 32-byte secret scalar (rho) used to hide the amount.
 * @param out_commitment   [out] A 33-byte buffer to store the commitment
 */
int
mpt_get_pedersen_commitment(
    uint64_t amount,
    uint8_t const blinding_factor[kMPT_BLINDING_FACTOR_SIZE],
    uint8_t out_commitment[kMPT_PEDERSEN_COMMIT_SIZE]);

/**
 * @brief Generates a ZK linkage proof between an ElGamal ciphertext and a Pedersen commitment.
 * @param pubkey              [in] 33-byte public key of the sender.
 * @param blinding_factor     [in] 32-byte blinding factor used for the ElGamal encryption.
 * @param context_hash        [in] 32-byte hash of the transaction context.
 * @param params              [in] Struct containing commitment, amount, and ciphertext.
 * @param out                 [out] Buffer of exactly 195 bytes to store the proof.
 * @return 0 on success, -1 on failure.
 */
int
mpt_get_amount_linkage_proof(
    uint8_t const pubkey[kMPT_PUBKEY_SIZE],
    uint8_t const blinding_factor[kMPT_BLINDING_FACTOR_SIZE],
    uint8_t const context_hash[kMPT_HALF_SHA_SIZE],
    mpt_pedersen_proof_params const* params,
    uint8_t out[kMPT_PEDERSEN_LINK_SIZE]);

/**
 * @brief Generates a ZK linkage proof for the sender's balance.
 * @param priv                [in] 32-byte private key of the sender.
 * @param pub                 [in] 33-byte public key of the sender.
 * @param context_hash        [in] 32-byte hash of the transaction context.
 * @param params              [in] Struct containing commitment, amount, and ciphertext.
 * @param out                 [out] Buffer of exactly 195 bytes to store the proof.
 * @return 0 on success, -1 on failure.
 */
int
mpt_get_balance_linkage_proof(
    uint8_t const priv[kMPT_PRIVKEY_SIZE],
    uint8_t const pub[kMPT_PUBKEY_SIZE],
    uint8_t const context_hash[kMPT_HALF_SHA_SIZE],
    mpt_pedersen_proof_params const* params,
    uint8_t out[kMPT_PEDERSEN_LINK_SIZE]);

/**
 * @brief Generates proof for ConfidentialMPTSend.
 * @param priv.             [in] The sender's 32-byte private key.
 * @param amount            [in] The amount being sent.
 * @param recipients        [in] List of recipients (Sender, Dest, Issuer).
 * @param n_recipients      [in] Number of recipients in the list.
 * @param tx_blinding_factor [in] The ElGamal 'r' used for the transaction.
 * @param context_hash      [in] The 32-byte context hash.
 * @param amount_params     [in] Linkage params for the transaction amount.
 * @param balance_params    [in] Linkage params for the sender's balance.
 * @param out_proof         [out] Pointer to the buffer to be filled with the hex/bytes.
 * @param out_len           [in/out] In: Size of the buffer. Out: Actual bytes written.
 * @return 0 on success, -1 on failure (e.g., buffer too small or math error).
 */
int
mpt_get_confidential_send_proof(
    uint8_t const priv[kMPT_PRIVKEY_SIZE],
    uint64_t amount,
    mpt_confidential_recipient const* recipients,
    size_t n_recipients,
    uint8_t const tx_blinding_factor[kMPT_BLINDING_FACTOR_SIZE],
    uint8_t const context_hash[kMPT_HALF_SHA_SIZE],
    mpt_pedersen_proof_params const* amount_params,
    mpt_pedersen_proof_params const* balance_params,
    uint8_t* out_proof,
    size_t* out_len);

/**
 * @brief Generates proof for ConfidentialMPTConvertBack.
 * @param priv          [in] The holder's 32-byte private key.
 * @param pub           [in] The holder's 33-byte public key.
 * @param context_hash  [in] The 32-byte context hash binding the proof to the transaction.
 * @param amount        [in] The amount to convert back.
 * @param params        [in] Pedersen commitment parameters.
 * @param out_proof     [out] The 883-byte buffer to be filled with the Pedersen linkage proof and range proof.
 * @return 0 on success, -1 on failure (e.g., math error or invalid parameters).
 */
int
mpt_get_convert_back_proof(
    uint8_t const priv[kMPT_PRIVKEY_SIZE],
    uint8_t const pub[kMPT_PUBKEY_SIZE],
    uint8_t const context_hash[kMPT_HALF_SHA_SIZE],
    uint64_t const amount,
    mpt_pedersen_proof_params const* params,
    uint8_t out_proof[kMPT_PEDERSEN_LINK_SIZE + kMPT_SINGLE_BULLETPROOF_SIZE]);

/**
 * @brief Generates proof for ConfidentialMPTClawback.
 * @param priv              [in] The issuer's 32-byte private key.
 * @param pub               [in] The issuer's 33-byte public key.
 * @param context_hash      [in] The 32-byte context hash binding the proof to the transaction.
 * @param amount            [in] The plaintext amount to be clawed back.
 * @param encrypted_amount  [in] The 66-byte sfIssuerEncryptedBalance blob from the ledger.
 * @param out_proof         [out] The 98-byte buffer to be filled with the equality proof.
 * @return 0 on success, -1 on failure (e.g., math error or invalid ciphertext).
 */
int
mpt_get_clawback_proof(
    uint8_t const priv[kMPT_PRIVKEY_SIZE],
    uint8_t const pub[kMPT_PUBKEY_SIZE],
    uint8_t const context_hash[kMPT_HALF_SHA_SIZE],
    uint64_t const amount,
    uint8_t const encrypted_amount[kMPT_ELGAMAL_TOTAL_SIZE],
    uint8_t out_proof[kMPT_EQUALITY_PROOF_SIZE]);

#ifdef __cplusplus
}
#endif
#endif

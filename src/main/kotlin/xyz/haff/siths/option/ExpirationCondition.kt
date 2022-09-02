package xyz.haff.siths.option

/**
 * To manage in which cases this expiration should be set
 */
enum class ExpirationCondition {
    /**
     * Only if no expiration exists
     */
    NX,

    /**
     * Only if expiration exists
     */
    XX,

    /**
     * Only if the new expiry is after than the current one
     */
    GT, // Only if the new expiry is greater than the current one

    /**
     * Only if the new expiry is before the current one
     */
    LT,
}
package xyz.haff.siths.option

/**
 * To manage if an operation is to be taken based on whether a previous value existed
 */
enum class ExistenceCondition {
    /**
     * Only if it didn't exist
     */
    NX,

    /**
     * Only if it existed
     */
    XX
}
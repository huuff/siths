package xyz.haff.siths.option

// TODO: Document this so that these comments appear in the javadoc (kdoc?)
enum class ExpirationCondition {
    NX, // Only if no expiration exists
    XX, // Only if expiration exists
    GT, // Only if the new expiry is greater than the current one
    LT, // Only if the new expiry is smaller than the current one
}
package leia.logic

// The rule matches and the
data class LogAppend(
    val sinkDescription: SinkDescription,
    val request: IncomingRequest,
    val receipt: Receipt
) : Match()

sealed class Result {
    abstract fun combine(other: Result): Result
}

// The incoming request doesn't match this particular rule
object NoMatch : Result() {
    override fun combine(other: Result): Result = other
}

// A match is a result that matches all the rules. It dominates all other rules
sealed class Match : Result() {
    // Match + Match = Undefined
    // Match + NoMatch = Match
    // Match + ErrorMatch = Match
    override fun combine(other: Result) = when (other) {
        is ErrorMatch -> this
        is Match -> throw UnsupportedOperationException("we cant handle this yet")
        else -> this
    }
}

// An error match would have matched if some additional criteria had been met,
// such as the presence of authentication headers.
sealed class ErrorMatch : Match() {
    override fun combine(other: Result): Match =
        when (other) {
            is Match -> other
            is ErrorMatch -> throw UnsupportedOperationException("we cant handle this yet")
            is NoMatch -> this
        }
}

// The rule matches but authorization is missing but is required
data class NotAuthorized(val triedAuthMethods: List<String>) : ErrorMatch()

// The rule matches but authorization presented is invalid
object Forbidden : ErrorMatch()

// The rule matches but CORS is not allowed for that host.
object CorsNotAllowed : ErrorMatch()

// A CorsPreflight alllowed is actually an error match as it is overridden by
// actual matches event if it results in a 200
object CorsPreflightAllowed : ErrorMatch()

// Validation of request body as JSON failed
object JsonValidationFailed : ErrorMatch()

// Validation of JSON schema failed
object JsonSchemaInvalid : ErrorMatch()

// HTTP method is not allowed
object MethodNotAllowed : ErrorMatch()

// Leia health check
object LeiaHealthCheck : ErrorMatch()
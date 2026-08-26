package cl.gus.labs.fakestore.analytics

/**
 * Who a given event is for.
 *
 * An event declares the audiences it belongs to; a tracker declares the audiences it serves.
 * The dispatcher delivers an event to a tracker when the two sets intersect. Routing therefore
 * lives in one place instead of being re-implemented as an `if` at the top of every vendor.
 *
 * Replace these entries with the taxonomy the product actually uses. Keep the set small: a
 * taxonomy nobody can explain in one sentence per entry will be applied inconsistently.
 */
enum class Audience {
    /** Product and behavioural analytics: funnels, retention, feature usage. */
    Product,

    /** Attribution, campaigns, lifecycle messaging. */
    Marketing,

    /** Diagnostics and performance signals aimed at the engineering team. */
    Engineering,

    /** Customer-experience and voice-of-customer tooling: surveys, session replay. */
    Experience,
}

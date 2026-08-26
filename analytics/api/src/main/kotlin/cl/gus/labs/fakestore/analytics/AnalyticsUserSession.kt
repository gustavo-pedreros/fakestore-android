package cl.gus.labs.fakestore.analytics

/**
 * Identity, as opposed to events.
 *
 * Every method has a no-op default so a vendor implements only what its SDK supports. This is
 * deliberate: the alternative — a central manager that decides "Firebase gets identify, the
 * survey tool does not" — puts each vendor's capabilities in a file that vendor does not own,
 * and every new vendor has to edit it.
 */
interface AnalyticsUserSession {
    fun identify(user: UserIdentity) {}
    fun clear() {}
}

/**
 * What is known about the current user.
 *
 * Add fields the product actually sends and no more: everything here reaches third parties.
 * Anything personally identifying should be justified per vendor, not added because it was
 * available.
 */
data class UserIdentity(
    val userId: String,
    val traits: Map<String, Any> = emptyMap(),
)

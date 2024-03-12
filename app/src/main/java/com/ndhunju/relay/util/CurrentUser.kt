package com.ndhunju.relay.util

import com.ndhunju.relay.service.UserSettingsPersistService

/**
 * Provides API to interact with currently active/logged [User].
 */
interface CurrentUser {

    var user: User
    fun isUserSignedIn(): Boolean
}

/**
 * Implements [CurrentUser] such that any changes to [CurrentUser.user] are persisted permanently
 */
class PersistableCurrentUserImpl(
    private val userSettingsPersistService: UserSettingsPersistService? = null
): CurrentUser {

    override var user: User = User()
        get() {
            val user = userSettingsPersistService?.retrieve() ?: User()
            user.setOnUserUpdatedListener(this::onUserUpdated)
            return user
        }

        set(value) {
            field = value
            userSettingsPersistService?.save(value)
        }

    override fun isUserSignedIn(): Boolean {
        return user.isRegistered && user.id.isNotEmpty()
    }

    private fun onUserUpdated() {
        // Update this on the persistent storage too
        userSettingsPersistService?.save(user)
    }

}

data class User(
    val id: String = "",
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val isRegistered: Boolean = false,
    var encryptionKey: String? = null,
    // TODO: Nikesh - Should we merge User and Child class?
    // Below fields are relevant when current user is functioning as a child user
    val parentUserIds: MutableList<String> = mutableListOf(),
    val parentUserEmails: MutableList<String> = mutableListOf(),
    // Below fields are relevant when current user is functioning as a parent user
    val childUsers: MutableList<User> = mutableListOf()
) {
    @Transient
    private var onUserUpdated: (() -> Unit)? = null

    fun setOnUserUpdatedListener(onUserUpdated: (() -> Unit)? = null) {
        this.onUserUpdated = onUserUpdated
    }

    /**
     * Updates related filed with passed [parentUsers]
     */
    fun updateParentUser(parentUsers: List<User>) {
        parentUserIds.clear()
        parentUserEmails.clear()
        parentUsers.forEach { parentUser ->
            if (parentUser.email != null) {
                parentUserIds.add(parentUser.id)
                parentUserEmails.add(parentUser.email)
            }
        }
        onUserUpdated?.invoke()
    }

    /**
     * Adds passed [encryptionKey] to [childUserEmail] given that [childUserEmail] is already
     * in [childUserEmails] list. Otherwise, returns false
     */
    fun addEncryptionKeyOfChild(childUserEmail: String, encryptionKey: String): Boolean {
        childUsers.forEach { childUser ->
            if (childUser.email == childUserEmail) {
                childUser.encryptionKey = encryptionKey
                onUserUpdated?.invoke()
                return true
            }
            
        }
        return false
    }

    fun getEncryptionKey(childUserId: String): String? {
        childUsers.forEach { childUser ->
            if (childUser.id == childUserId) {
                return childUser.encryptionKey
            }
        }

        return null
    }
}
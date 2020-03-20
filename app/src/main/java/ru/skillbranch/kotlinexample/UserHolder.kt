package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(fullName: String, email: String, password: String): User {

        // return User.makeUser(fullName = fullName, email = email, password = password).also { user -> map[user.login] = user }

        val user = User.makeUser(fullName = fullName, email = email, password = password)
        if (!map.containsKey(user.login)) {
            map[user.login] = user
        } else {
            throw IllegalArgumentException("A user with this email already exists")
        }
        return user
    }

    fun registerUserByPhone(fullName: String, rawPhone: String): User {

        // return User.makeUser(fullName = fullName, rawPhone = rawPhone).also { user -> map[user.login] = user }

        val user = User.makeUser(fullName = fullName, phone = rawPhone)
        if (map.containsKey(user.login)) {
            throw IllegalArgumentException("A user with this phone already exists")
        } else {
            if (user.login.isPhoneNumber()) {
                map[user.login] = user
            } else {
                throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
            }
        }
        return user
    }

    fun loginUser(login: String, password: String): String? {
        val preparedLogin = if (login.isPhoneNumber()) {
            login.toPhoneNumber()
        } else {
            login.trim()
        }
        return map[preparedLogin]?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String) {
        if (login.isPhoneNumber()) {
            val user = map[login.toPhoneNumber()]
            user?.changeAccessCodeAndSendItToUser()
        }
    }

    fun importUsers(list: List<String>): List<User> {
        val result = ArrayList<User>()
        for (item in list.withIndex()) {
            val parts = item.value.split(";")
            if (parts.size < 4) {
                throw IllegalArgumentException("Insufficient fields in item with index ${item.index}, at least 4 fields separated by semicolon expected")
            } else {
                val fullName = parts[0]
                val email = parts[1].trim().ifEmpty { null }
                val rawPhone = parts[3].trim().ifEmpty { null }
                val isValidEmail = (email != null)
                val isValidPhone = (rawPhone != null && rawPhone.isPhoneNumber())
                if (!isValidEmail && !isValidPhone) {
                    throw IllegalArgumentException("Email and phone are both empty or incorrect")
                } else {
                    val saltAndHash = parts[2].split(":")
                    var salt: String? = null
                    var hash: String? = null
                    when (saltAndHash.size) {
                        0 -> {}
                        2 -> {
                            salt = saltAndHash[0].ifEmpty { null }
                            hash = saltAndHash[1].ifEmpty { null }
                        }
                        else -> throw IllegalArgumentException("Incorrect format of salt and hash, must be two strings separated by colon")
                    }
                    if ((salt == null && hash != null) || (salt != null && hash == null)) {
                        throw IllegalArgumentException("Incorrect format of salt and hash, both must be null or non-null")
                    } else {
                        if (email != null && (salt == null && hash == null)) {
                            throw IllegalArgumentException("Salt and hash must not be null when email is specified")
                        } else {
                            val user = User.makeUserFromCsv(
                                fullName = fullName,
                                email = email,
                                salt = salt,
                                hash = hash,
                                phone = rawPhone
                            )
                            if (map.containsKey(user.login)) {
                                if (user.login.isPhoneNumber()) {
                                    throw IllegalArgumentException("A user with this phone already exists")
                                } else {
                                    throw IllegalArgumentException("A user with this email already exists")
                                }
                            } else {
                                result.add(user)
                                map[user.login] = user
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }
}

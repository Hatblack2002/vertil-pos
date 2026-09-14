package com.vertil.pos.security

import com.vertil.pos.data.dao.UserDao
import com.vertil.pos.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mindrot.jbcrypt.BCrypt

/**
 * AuthenticationManager — autenticación local con bcrypt.
 *
 * Nunca almacena contraseñas en texto plano.
 */
class AuthenticationManager(private val userDao: UserDao) {

    private val _current = MutableStateFlow<Session?>(null)
    val current: StateFlow<Session?> = _current.asStateFlow()

    suspend fun login(username: String, password: String): LoginResult {
        val user = userDao.getByUsername(username.trim())
            ?: return LoginResult.Failure("Usuario no encontrado")
        if (!BCrypt.checkpw(password, user.passwordHash)) {
            return LoginResult.Failure("Contraseña incorrecta")
        }
        val session = Session(userId = user.id, username = user.username, role = Role.valueOf(user.role), fullName = user.fullName)
        _current.value = session
        return LoginResult.Success(session)
    }

    fun logout() {
        _current.value = null
    }

    fun hasPermission(permission: Permission): Boolean {
        val s = _current.value ?: return false
        return RolePermissions.can(s.role, permission)
    }

    fun requirePermission(permission: Permission) {
        if (!hasPermission(permission)) {
            throw SecurityException("Permiso denegado: $permission (requerido por rol ${_current.value?.role})")
        }
    }

    suspend fun createUser(username: String, password: String, fullName: String, role: Role): Long {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt(12))
        return userDao.insert(UserEntity(
            username = username.trim(),
            passwordHash = hash,
            fullName = fullName,
            role = role.name
        ))
    }

    suspend fun changePassword(userId: Long, newPassword: String) {
        val user = userDao.getById(userId) ?: return
        val hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12))
        userDao.update(user.copy(passwordHash = hash))
    }
}

data class Session(
    val userId: Long,
    val username: String,
    val role: Role,
    val fullName: String
)

sealed class LoginResult {
    data class Success(val session: Session) : LoginResult()
    data class Failure(val message: String) : LoginResult()
}

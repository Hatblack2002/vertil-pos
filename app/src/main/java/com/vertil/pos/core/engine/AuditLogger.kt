package com.vertil.pos.core.engine

import com.vertil.pos.data.dao.AuditDao
import com.vertil.pos.data.entity.AuditLogEntity
import com.vertil.pos.security.AuthenticationManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.put

/**
 * AuditLogger — registra operaciones sensibles.
 * Append-only: la UI no puede modificar/eliminar entradas.
 */
class AuditLogger(
    private val auditDao: AuditDao,
    private val auth: AuthenticationManager
) {
    private val json = Json { encodeDefaults = true }

    suspend fun log(action: String, entity: String? = null, entityId: String? = null, details: Map<String, Any?> = emptyMap()) {
        val session = auth.current.value
        val detailsJson = if (details.isEmpty()) null else {
            val obj = JsonObject(
                details.mapValues { (_, v) -> JsonPrimitive(v?.toString() ?: "") }
            )
            json.encodeToString(JsonObject.serializer(), obj)
        }
        auditDao.insert(AuditLogEntity(
            userId = session?.userId,
            username = session?.username,
            action = action,
            entity = entity,
            entityId = entityId,
            details = detailsJson
        ))
    }
}

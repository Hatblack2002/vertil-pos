package com.vertil.pos.backup

import com.vertil.pos.data.db.PosDatabase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.io.File

/**
 * BackupService — export/import de toda la DB a JSON.
 * Formato: vertil-pos-backup-v1.
 */
class BackupService(private val db: PosDatabase) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun export(targetFile: File): Boolean {
        return try {
            val catDao = db.categoryDao()
            val prodDao = db.productDao()
            val supDao = db.supplierDao()
            val cusDao = db.customerDao()
            val usrDao = db.userDao()
            val saleDao = db.saleDao()
            val saleItemDao = db.saleItemDao()
            val invDao = db.inventoryDao()
            val cashDao = db.cashDao()
            val audDao = db.auditDao()
            val setDao = db.settingsDao()
            val sales = saleDao.getByDateRange(0, System.currentTimeMillis())
            val sessions = cashDao.observeRecentSessions().first()

            val root = buildJsonObject {
                put("format", "vertil-pos-backup-v1")
                put("version", 1)
                put("createdAt", System.currentTimeMillis())
                put("categories", catDao.getAll().toJsonArray())
                put("products", prodDao.search("").toJsonArray())
                put("suppliers", supDao.observeAll().first().toJsonArray())
                put("customers", cusDao.observeAll().first().toJsonArray())
                put("users", usrDao.observeAll().first().toJsonArray())
                put("sales", sales.toJsonArray())
                put("saleItems", sales.flatMap { saleItemDao.getBySale(it.id) }.toJsonArray())
                put("inventoryMovements", invDao.observeRecent().first().toJsonArray())
                put("cashSessions", sessions.toJsonArray())
                put("cashMovements", sessions.flatMap { cashDao.getMovements(it.id) }.toJsonArray())
                put("auditLogs", audDao.observeRecent().first().toJsonArray())
                put("settings", setDao.getAll().toJsonArray())
            }
            targetFile.writeText(json.encodeToString(JsonObject.serializer(), root))
            true
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }
    }

    suspend fun import(sourceFile: File, overwrite: Boolean): ImportResult {
        return try {
            val root = json.parseToJsonElement(sourceFile.readText()).jsonObject
            val format = root["format"]?.jsonPrimitive?.content
            if (format != "vertil-pos-backup-v1") {
                return ImportResult.Failure("Formato no soportado: $format")
            }
            if (!overwrite) return ImportResult.Failure("Confirmación requerida")

            val catDao = db.categoryDao()
            val prodDao = db.productDao()
            val supDao = db.supplierDao()
            val cusDao = db.customerDao()
            val usrDao = db.userDao()
            val saleDao = db.saleDao()
            val saleItemDao = db.saleItemDao()
            val invDao = db.inventoryDao()
            val cashDao = db.cashDao()
            val audDao = db.auditDao()
            val setDao = db.settingsDao()

            root["categories"]?.jsonArray?.forEach { catDao.insert(it.jsonObject.toCategory()) }
            root["products"]?.jsonArray?.forEach { prodDao.insert(it.jsonObject.toProduct()) }
            root["suppliers"]?.jsonArray?.forEach { supDao.insert(it.jsonObject.toSupplier()) }
            root["customers"]?.jsonArray?.forEach { cusDao.insert(it.jsonObject.toCustomer()) }
            root["users"]?.jsonArray?.forEach { usrDao.insert(it.jsonObject.toUser()) }
            root["sales"]?.jsonArray?.forEach { saleDao.insert(it.jsonObject.toSale()) }
            root["saleItems"]?.jsonArray?.forEach { saleItemDao.insertAll(listOf(it.jsonObject.toSaleItem())) }
            root["inventoryMovements"]?.jsonArray?.forEach { invDao.insert(it.jsonObject.toInventoryMovement()) }
            root["cashSessions"]?.jsonArray?.forEach { cashDao.insert(it.jsonObject.toCashSession()) }
            root["cashMovements"]?.jsonArray?.forEach { cashDao.insertMovement(it.jsonObject.toCashMovement()) }
            root["auditLogs"]?.jsonArray?.forEach { audDao.insert(it.jsonObject.toAuditLog()) }
            root["settings"]?.jsonArray?.forEach { setDao.set(it.jsonObject.toSetting()) }

            ImportResult.Success(
                products = root["products"]?.jsonArray?.size ?: 0,
                sales = root["sales"]?.jsonArray?.size ?: 0,
                users = root["users"]?.jsonArray?.size ?: 0
            )
        } catch (t: Throwable) {
            ImportResult.Failure("Import fallido: ${t.message}")
        }
    }

    // === Helpers ===
    private fun Any?.toJsonElement(): JsonPrimitive = when (this) {
        null -> JsonPrimitive(null as String?)
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        else -> JsonPrimitive(this.toString())
    }

    private fun List<Any>.toJsonArray(): JsonArray = buildJsonArray {
        forEach { item ->
            when (item) {
                is com.vertil.pos.data.entity.CategoryEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.ProductEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.SupplierEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.CustomerEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.UserEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.SaleEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.SaleItemEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.InventoryMovementEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.CashSessionEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.CashMovementEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.AuditLogEntity -> add(item.toJsonObject())
                is com.vertil.pos.data.entity.AppSettingsEntity -> add(item.toJsonObject())
            }
        }
    }

    private fun com.vertil.pos.data.entity.CategoryEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("name", name); put("color", color); put("createdAt", createdAt)
    }
    private fun JsonObject.toCategory(): com.vertil.pos.data.entity.CategoryEntity =
        com.vertil.pos.data.entity.CategoryEntity(
            id = this["id"]!!.jsonPrimitive.long,
            name = this["name"]!!.jsonPrimitive.content,
            color = this["color"]?.jsonPrimitive?.content,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.ProductEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("barcode", barcode); put("sku", sku); put("name", name)
        put("description", description); put("categoryId", categoryId); put("brand", brand)
        put("purchasePriceCents", purchasePriceCents); put("salePriceCents", salePriceCents)
        put("stock", stock); put("minimumStock", minimumStock); put("maximumStock", maximumStock)
        put("unit", unit); put("imageUri", imageUri); put("supplierId", supplierId)
        put("active", active); put("createdAt", createdAt); put("updatedAt", updatedAt)
    }
    private fun JsonObject.toProduct(): com.vertil.pos.data.entity.ProductEntity =
        com.vertil.pos.data.entity.ProductEntity(
            id = this["id"]!!.jsonPrimitive.long,
            barcode = this["barcode"]?.jsonPrimitive?.content,
            sku = this["sku"]?.jsonPrimitive?.content,
            name = this["name"]!!.jsonPrimitive.content,
            description = this["description"]?.jsonPrimitive?.content,
            categoryId = this["categoryId"]?.jsonPrimitive?.longOrNull,
            brand = this["brand"]?.jsonPrimitive?.content,
            purchasePriceCents = this["purchasePriceCents"]!!.jsonPrimitive.long,
            salePriceCents = this["salePriceCents"]!!.jsonPrimitive.long,
            stock = this["stock"]!!.jsonPrimitive.double,
            minimumStock = this["minimumStock"]!!.jsonPrimitive.double,
            maximumStock = this["maximumStock"]!!.jsonPrimitive.double,
            unit = this["unit"]!!.jsonPrimitive.content,
            imageUri = this["imageUri"]?.jsonPrimitive?.content,
            supplierId = this["supplierId"]?.jsonPrimitive?.longOrNull,
            active = this["active"]!!.jsonPrimitive.boolean,
            createdAt = this["createdAt"]!!.jsonPrimitive.long,
            updatedAt = this["updatedAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.SupplierEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("name", name); put("phone", phone); put("email", email)
        put("address", address); put("notes", notes); put("createdAt", createdAt)
    }
    private fun JsonObject.toSupplier(): com.vertil.pos.data.entity.SupplierEntity =
        com.vertil.pos.data.entity.SupplierEntity(
            id = this["id"]!!.jsonPrimitive.long,
            name = this["name"]!!.jsonPrimitive.content,
            phone = this["phone"]?.jsonPrimitive?.content,
            email = this["email"]?.jsonPrimitive?.content,
            address = this["address"]?.jsonPrimitive?.content,
            notes = this["notes"]?.jsonPrimitive?.content,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.CustomerEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("name", name); put("phone", phone); put("email", email)
        put("address", address); put("notes", notes); put("createdAt", createdAt)
    }
    private fun JsonObject.toCustomer(): com.vertil.pos.data.entity.CustomerEntity =
        com.vertil.pos.data.entity.CustomerEntity(
            id = this["id"]!!.jsonPrimitive.long,
            name = this["name"]!!.jsonPrimitive.content,
            phone = this["phone"]?.jsonPrimitive?.content,
            email = this["email"]?.jsonPrimitive?.content,
            address = this["address"]?.jsonPrimitive?.content,
            notes = this["notes"]?.jsonPrimitive?.content,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.UserEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("username", username); put("passwordHash", passwordHash)
        put("fullName", fullName); put("role", role); put("active", active); put("createdAt", createdAt)
    }
    private fun JsonObject.toUser(): com.vertil.pos.data.entity.UserEntity =
        com.vertil.pos.data.entity.UserEntity(
            id = this["id"]!!.jsonPrimitive.long,
            username = this["username"]!!.jsonPrimitive.content,
            passwordHash = this["passwordHash"]!!.jsonPrimitive.content,
            fullName = this["fullName"]!!.jsonPrimitive.content,
            role = this["role"]!!.jsonPrimitive.content,
            active = this["active"]!!.jsonPrimitive.boolean,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.SaleEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("number", number); put("userId", userId)
        put("customerId", customerId); put("cashSessionId", cashSessionId)
        put("subtotalCents", subtotalCents); put("discountCents", discountCents)
        put("taxCents", taxCents); put("totalCents", totalCents); put("paymentMethod", paymentMethod)
        put("receivedCents", receivedCents); put("changeCents", changeCents); put("status", status)
        put("notes", notes); put("createdAt", createdAt)
    }
    private fun JsonObject.toSale(): com.vertil.pos.data.entity.SaleEntity =
        com.vertil.pos.data.entity.SaleEntity(
            id = this["id"]!!.jsonPrimitive.long,
            number = this["number"]!!.jsonPrimitive.content,
            userId = this["userId"]!!.jsonPrimitive.long,
            customerId = this["customerId"]?.jsonPrimitive?.longOrNull,
            cashSessionId = this["cashSessionId"]?.jsonPrimitive?.longOrNull,
            subtotalCents = this["subtotalCents"]!!.jsonPrimitive.long,
            discountCents = this["discountCents"]!!.jsonPrimitive.long,
            taxCents = this["taxCents"]!!.jsonPrimitive.long,
            totalCents = this["totalCents"]!!.jsonPrimitive.long,
            paymentMethod = this["paymentMethod"]!!.jsonPrimitive.content,
            receivedCents = this["receivedCents"]!!.jsonPrimitive.long,
            changeCents = this["changeCents"]!!.jsonPrimitive.long,
            status = this["status"]!!.jsonPrimitive.content,
            notes = this["notes"]?.jsonPrimitive?.content,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.SaleItemEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("saleId", saleId); put("productId", productId); put("productName", productName)
        put("barcode", barcode); put("quantity", quantity); put("unitPriceCents", unitPriceCents)
        put("discountCents", discountCents); put("subtotalCents", subtotalCents)
    }
    private fun JsonObject.toSaleItem(): com.vertil.pos.data.entity.SaleItemEntity =
        com.vertil.pos.data.entity.SaleItemEntity(
            id = this["id"]!!.jsonPrimitive.long,
            saleId = this["saleId"]!!.jsonPrimitive.long,
            productId = this["productId"]!!.jsonPrimitive.long,
            productName = this["productName"]!!.jsonPrimitive.content,
            barcode = this["barcode"]?.jsonPrimitive?.content,
            quantity = this["quantity"]!!.jsonPrimitive.double,
            unitPriceCents = this["unitPriceCents"]!!.jsonPrimitive.long,
            discountCents = this["discountCents"]!!.jsonPrimitive.long,
            subtotalCents = this["subtotalCents"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.InventoryMovementEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("productId", productId); put("quantity", quantity); put("type", type)
        put("reason", reason); put("userId", userId); put("reference", reference); put("createdAt", createdAt)
    }
    private fun JsonObject.toInventoryMovement(): com.vertil.pos.data.entity.InventoryMovementEntity =
        com.vertil.pos.data.entity.InventoryMovementEntity(
            id = this["id"]!!.jsonPrimitive.long,
            productId = this["productId"]!!.jsonPrimitive.long,
            quantity = this["quantity"]!!.jsonPrimitive.double,
            type = this["type"]!!.jsonPrimitive.content,
            reason = this["reason"]?.jsonPrimitive?.content,
            userId = this["userId"]!!.jsonPrimitive.long,
            reference = this["reference"]?.jsonPrimitive?.content,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.CashSessionEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("userId", userId); put("openingBalanceCents", openingBalanceCents)
        put("closingBalanceCents", closingBalanceCents); put("expectedBalanceCents", expectedBalanceCents)
        put("differenceCents", differenceCents); put("openTime", openTime); put("closeTime", closeTime)
        put("status", status)
    }
    private fun JsonObject.toCashSession(): com.vertil.pos.data.entity.CashSessionEntity =
        com.vertil.pos.data.entity.CashSessionEntity(
            id = this["id"]!!.jsonPrimitive.long,
            userId = this["userId"]!!.jsonPrimitive.long,
            openingBalanceCents = this["openingBalanceCents"]!!.jsonPrimitive.long,
            closingBalanceCents = this["closingBalanceCents"]?.jsonPrimitive?.longOrNull,
            expectedBalanceCents = this["expectedBalanceCents"]?.jsonPrimitive?.longOrNull,
            differenceCents = this["differenceCents"]?.jsonPrimitive?.longOrNull,
            openTime = this["openTime"]!!.jsonPrimitive.long,
            closeTime = this["closeTime"]?.jsonPrimitive?.longOrNull,
            status = this["status"]!!.jsonPrimitive.content
        )

    private fun com.vertil.pos.data.entity.CashMovementEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("cashSessionId", cashSessionId); put("type", type)
        put("amountCents", amountCents); put("reason", reason); put("saleId", saleId)
        put("userId", userId); put("createdAt", createdAt)
    }
    private fun JsonObject.toCashMovement(): com.vertil.pos.data.entity.CashMovementEntity =
        com.vertil.pos.data.entity.CashMovementEntity(
            id = this["id"]!!.jsonPrimitive.long,
            cashSessionId = this["cashSessionId"]!!.jsonPrimitive.long,
            type = this["type"]!!.jsonPrimitive.content,
            amountCents = this["amountCents"]!!.jsonPrimitive.long,
            reason = this["reason"]?.jsonPrimitive?.content,
            saleId = this["saleId"]?.jsonPrimitive?.longOrNull,
            userId = this["userId"]!!.jsonPrimitive.long,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.AuditLogEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("id", id); put("userId", userId); put("username", username); put("action", action)
        put("entity", entity); put("entityId", entityId); put("details", details); put("createdAt", createdAt)
    }
    private fun JsonObject.toAuditLog(): com.vertil.pos.data.entity.AuditLogEntity =
        com.vertil.pos.data.entity.AuditLogEntity(
            id = this["id"]!!.jsonPrimitive.long,
            userId = this["userId"]?.jsonPrimitive?.longOrNull,
            username = this["username"]?.jsonPrimitive?.content,
            action = this["action"]!!.jsonPrimitive.content,
            entity = this["entity"]?.jsonPrimitive?.content,
            entityId = this["entityId"]?.jsonPrimitive?.content,
            details = this["details"]?.jsonPrimitive?.content,
            createdAt = this["createdAt"]!!.jsonPrimitive.long
        )

    private fun com.vertil.pos.data.entity.AppSettingsEntity.toJsonObject(): JsonObject = buildJsonObject {
        put("key", key); put("value", value)
    }
    private fun JsonObject.toSetting(): com.vertil.pos.data.entity.AppSettingsEntity =
        com.vertil.pos.data.entity.AppSettingsEntity(
            key = this["key"]!!.jsonPrimitive.content,
            value = this["value"]!!.jsonPrimitive.content
        )
}

sealed class ImportResult {
    data class Success(val products: Int, val sales: Int, val users: Int) : ImportResult()
    data class Failure(val message: String) : ImportResult()
}

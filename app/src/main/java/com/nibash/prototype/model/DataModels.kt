package com.nibash.prototype.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class FloorAddition(
    val floorNumber: Int, // 0 for Ground, 1 for 1st, etc.
    val type: String // "Parking Only", "Single Flat", "Regular"
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("floorNumber", floorNumber)
        obj.put("type", type)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): FloorAddition {
            return FloorAddition(
                floorNumber = obj.getInt("floorNumber"),
                type = obj.getString("type")
            )
        }
    }
}

data class FlatStyle(
    val roomSuffix: String, // e.g. "01" (to match rooms ending in 01 or 1)
    val bedrooms: Int,
    val bathrooms: Int,
    val attachedBaths: Int,
    val openBaths: Int,
    val balconies: Int,
    val kitchens: Int
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("roomSuffix", roomSuffix)
        obj.put("bedrooms", bedrooms)
        obj.put("bathrooms", bathrooms)
        obj.put("attachedBaths", attachedBaths)
        obj.put("openBaths", openBaths)
        obj.put("balconies", balconies)
        obj.put("kitchens", kitchens)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): FlatStyle {
            return FlatStyle(
                roomSuffix = obj.getString("roomSuffix"),
                bedrooms = obj.optInt("bedrooms", 2),
                bathrooms = obj.optInt("bathrooms", 1),
                attachedBaths = obj.optInt("attachedBaths", 0),
                openBaths = obj.optInt("openBaths", 1),
                balconies = obj.optInt("balconies", 1),
                kitchens = obj.optInt("kitchens", 1)
            )
        }
    }
}

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String, // e.g. "101", "A01"
    val name: String,
    val occupation: String,
    val phone: String,
    val familyMembers: Int,
    val startDate: String = "2024-01",
    val isActive: Boolean = true
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("roomId", roomId)
        obj.put("name", name)
        obj.put("occupation", occupation)
        obj.put("phone", phone)
        obj.put("familyMembers", familyMembers)
        obj.put("startDate", startDate)
        obj.put("isActive", isActive)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Tenant {
            return Tenant(
                id = obj.optString("id", UUID.randomUUID().toString()),
                roomId = obj.getString("roomId"),
                name = obj.getString("name"),
                occupation = obj.optString("occupation", ""),
                phone = obj.optString("phone", ""),
                familyMembers = obj.optInt("familyMembers", 1),
                startDate = obj.optString("startDate", "2024-01"),
                isActive = obj.optBoolean("isActive", true)
            )
        }
    }
}

data class Payment(
    val id: String = UUID.randomUUID().toString(),
    val tenantId: String,
    val roomId: String,
    val month: String, // e.g., "July 2024", "June 2024"
    val amountPaid: Double,
    val amountDue: Double,
    val advancePaid: Double,
    val isPaid: Boolean,
    val paidDate: String = ""
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("tenantId", tenantId)
        obj.put("roomId", roomId)
        obj.put("month", month)
        obj.put("amountPaid", amountPaid)
        obj.put("amountDue", amountDue)
        obj.put("advancePaid", advancePaid)
        obj.put("isPaid", isPaid)
        obj.put("paidDate", paidDate)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Payment {
            return Payment(
                id = obj.optString("id", UUID.randomUUID().toString()),
                tenantId = obj.getString("tenantId"),
                roomId = obj.getString("roomId"),
                month = obj.getString("month"),
                amountPaid = obj.optDouble("amountPaid", 0.0),
                amountDue = obj.optDouble("amountDue", 0.0),
                advancePaid = obj.optDouble("advancePaid", 0.0),
                isPaid = obj.optBoolean("isPaid", false),
                paidDate = obj.optString("paidDate", "")
            )
        }
    }
}

data class Building(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val floorsCount: Int,
    val systemType: String, // "Flats" or "Individual Rooms"
    val roomsPerFloor: Int,
    val floorAdditions: List<FloorAddition> = emptyList(),
    val numberingSystem: String, // "Numeric (101, 102)" or "Alphabetic (A01, A02)"
    val flatStyles: List<FlatStyle> = emptyList(),
    val rules: List<String> = emptyList(),
    val rentType: String, // "Universal" or "Set of Flats"
    val universalRent: Double = 0.0,
    val customRents: Map<String, Double> = emptyMap(), // suffix to rent amount (e.g. "01" -> 15000.0)
    val customRoomRents: Map<String, Double> = emptyMap(), // specific room to rent (e.g. "101" -> 16000.0)
    val excludedRooms: List<String> = emptyList(), // rooms excluded from lists, not fit for rent, not calculated anywhere else
    val tenants: List<Tenant> = emptyList(),
    val payments: List<Payment> = emptyList()
) {
    // Generate all room numbers based on floors, systemType, additions, and numberingSystem
    fun generateRoomNumbers(includeExcluded: Boolean = false): List<String> {
        val rooms = mutableListOf<String>()
        for (f in 0 until floorsCount) {
            val addition = floorAdditions.find { it.floorNumber == f }
            if (addition?.type == "Parking Only") {
                continue // ground floor or other floor dedicated to parking
            }

            val count = if (addition?.type == "Single Flat") {
                1
            } else {
                roomsPerFloor
            }

            for (r in 1..count) {
                val roomNum = if (numberingSystem.contains("Alphabetic")) {
                    val letter = ('A'.code + f).toChar()
                    String.format("%c%02d", letter, r)
                } else {
                    // Numeric system, floor starts at f
                    // Floor 0: 001, 002 (if no parking), Floor 1: 101, 102, etc.
                    String.format("%d%02d", f, r)
                }
                rooms.add(roomNum)
            }
        }
        return if (includeExcluded) rooms else rooms.filter { it !in excludedRooms }
    }

    // Get style of a room
    fun getStyleForRoom(roomNum: String): FlatStyle {
        // Find if matches any suffix
        for (style in flatStyles) {
            if (roomNum.endsWith(style.roomSuffix)) {
                return style
            }
        }
        // Return default style
        return FlatStyle(
            roomSuffix = "default",
            bedrooms = 2,
            bathrooms = 1,
            attachedBaths = 0,
            openBaths = 1,
            balconies = 1,
            kitchens = 1
        )
    }

    // Get Rent for room
    fun getRentForRoom(roomNum: String): Double {
        // Check specific room custom rent first
        customRoomRents[roomNum]?.let { return it }

        if (rentType == "Universal") {
            return universalRent
        }
        for ((suffix, rent) in customRents) {
            if (roomNum.endsWith(suffix)) {
                return rent
            }
        }
        // Fallback default
        return universalRent.takeIf { it > 0 } ?: 10000.0
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("floorsCount", floorsCount)
        obj.put("systemType", systemType)
        obj.put("roomsPerFloor", roomsPerFloor)

        val additionsArr = JSONArray()
        floorAdditions.forEach { additionsArr.put(it.toJson()) }
        obj.put("floorAdditions", additionsArr)

        obj.put("numberingSystem", numberingSystem)

        val stylesArr = JSONArray()
        flatStyles.forEach { stylesArr.put(it.toJson()) }
        obj.put("flatStyles", stylesArr)

        val rulesArr = JSONArray()
        rules.forEach { rulesArr.put(it) }
        obj.put("rules", rulesArr)

        obj.put("rentType", rentType)
        obj.put("universalRent", universalRent)

        val customRentsObj = JSONObject()
        customRents.forEach { (k, v) -> customRentsObj.put(k, v) }
        obj.put("customRents", customRentsObj)

        val customRoomRentsObj = JSONObject()
        customRoomRents.forEach { (k, v) -> customRoomRentsObj.put(k, v) }
        obj.put("customRoomRents", customRoomRentsObj)

        val excludedRoomsArr = JSONArray()
        excludedRooms.forEach { excludedRoomsArr.put(it) }
        obj.put("excludedRooms", excludedRoomsArr)

        val tenantsArr = JSONArray()
        tenants.forEach { tenantsArr.put(it.toJson()) }
        obj.put("tenants", tenantsArr)

        val paymentsArr = JSONArray()
        payments.forEach { paymentsArr.put(it.toJson()) }
        obj.put("payments", paymentsArr)

        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): Building {
            val additionsList = mutableListOf<FloorAddition>()
            val additionsArr = obj.optJSONArray("floorAdditions")
            if (additionsArr != null) {
                for (i in 0 until additionsArr.length()) {
                    additionsList.add(FloorAddition.fromJson(additionsArr.getJSONObject(i)))
                }
            }

            val stylesList = mutableListOf<FlatStyle>()
            val stylesArr = obj.optJSONArray("flatStyles")
            if (stylesArr != null) {
                for (i in 0 until stylesArr.length()) {
                    stylesList.add(FlatStyle.fromJson(stylesArr.getJSONObject(i)))
                }
            }

            val rulesList = mutableListOf<String>()
            val rulesArr = obj.optJSONArray("rules")
            if (rulesArr != null) {
                for (i in 0 until rulesArr.length()) {
                    rulesList.add(rulesArr.getString(i))
                }
            }

            val customRentsMap = mutableMapOf<String, Double>()
            val customRentsObj = obj.optJSONObject("customRents")
            if (customRentsObj != null) {
                customRentsObj.keys().forEach { key ->
                    customRentsMap[key] = customRentsObj.getDouble(key)
                }
            }

            val customRoomRentsMap = mutableMapOf<String, Double>()
            val customRoomRentsObj = obj.optJSONObject("customRoomRents")
            if (customRoomRentsObj != null) {
                customRoomRentsObj.keys().forEach { key ->
                    customRoomRentsMap[key] = customRoomRentsObj.getDouble(key)
                }
            }

            val excludedRoomsList = mutableListOf<String>()
            val excludedRoomsArr = obj.optJSONArray("excludedRooms")
            if (excludedRoomsArr != null) {
                for (i in 0 until excludedRoomsArr.length()) {
                    excludedRoomsList.add(excludedRoomsArr.getString(i))
                }
            }

            val tenantsList = mutableListOf<Tenant>()
            val tenantsArr = obj.optJSONArray("tenants")
            if (tenantsArr != null) {
                for (i in 0 until tenantsArr.length()) {
                    tenantsList.add(Tenant.fromJson(tenantsArr.getJSONObject(i)))
                }
            }

            val paymentsList = mutableListOf<Payment>()
            val paymentsArr = obj.optJSONArray("payments")
            if (paymentsArr != null) {
                for (i in 0 until paymentsArr.length()) {
                    paymentsList.add(Payment.fromJson(paymentsArr.getJSONObject(i)))
                }
            }

            return Building(
                id = obj.optString("id", UUID.randomUUID().toString()),
                name = obj.getString("name"),
                floorsCount = obj.getInt("floorsCount"),
                systemType = obj.getString("systemType"),
                roomsPerFloor = obj.getInt("roomsPerFloor"),
                floorAdditions = additionsList,
                numberingSystem = obj.getString("numberingSystem"),
                flatStyles = stylesList,
                rules = rulesList,
                rentType = obj.getString("rentType"),
                universalRent = obj.optDouble("universalRent", 0.0),
                customRents = customRentsMap,
                customRoomRents = customRoomRentsMap,
                excludedRooms = excludedRoomsList,
                tenants = tenantsList,
                payments = paymentsList
            )
        }
    }
}

object DummyDataProvider {
    fun getDummyBuildings(): List<Building> {
        return emptyList()
    }
}

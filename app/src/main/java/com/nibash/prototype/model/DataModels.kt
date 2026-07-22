package com.nibash.prototype.model

import java.util.UUID

data class FloorAddition(
    val floorNumber: Int, // 0 for Ground, 1 for 1st, etc.
    val type: String // "Parking Only", "Single Flat", "Regular"
)

data class FlatStyle(
    val roomSuffix: String, // e.g. "01" (to match rooms ending in 01 or 1)
    val bedrooms: Int,
    val bathrooms: Int,
    val attachedBaths: Int,
    val openBaths: Int,
    val balconies: Int,
    val kitchens: Int
)

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val roomId: String, // e.g. "101", "A01"
    val name: String,
    val occupation: String,
    val phone: String,
    val familyMembers: Int,
    val startDate: String = "2024-01",
    val isActive: Boolean = true
)

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
)

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
    val tenants: List<Tenant> = emptyList(),
    val payments: List<Payment> = emptyList()
) {
    // Generate all room numbers based on floors, systemType, additions, and numberingSystem
    fun generateRoomNumbers(): List<String> {
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
        return rooms
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
}

object DummyDataProvider {
    fun getDummyBuildings(): List<Building> {
        // Generate building 1: Nibash Tower
        val flatStyles1 = listOf(
            FlatStyle("01", 3, 2, 1, 1, 2, 1),
            FlatStyle("02", 2, 1, 0, 1, 1, 1)
        )
        val customRents1 = mapOf(
            "01" to 18000.0,
            "02" to 12000.0
        )
        val floorAdditions1 = listOf(
            FloorAddition(0, "Parking Only"),
            FloorAddition(4, "Single Flat") // penthouse
        )
        val rules1 = listOf(
            "No pets allowed",
            "No rooftop access",
            "Gate closes at 9:00 PM",
            "Parking fee for vehicles apply"
        )

        val tenant1 = Tenant(roomId = "101", name = "Rahat Kabir", occupation = "Software Engineer", phone = "+8801712345678", familyMembers = 3)
        val tenant2 = Tenant(roomId = "102", name = "Sultana Yasmin", occupation = "Banker", phone = "+8801811223344", familyMembers = 2)
        val tenant3 = Tenant(roomId = "201", name = "Monirul Islam", occupation = "Business Owner", phone = "+8801511998877", familyMembers = 4)

        val payments1 = listOf(
            Payment(tenantId = tenant1.id, roomId = "101", month = "July 2024", amountPaid = 18000.0, amountDue = 0.0, advancePaid = 0.0, isPaid = true, paidDate = "2024-07-02"),
            Payment(tenantId = tenant1.id, roomId = "101", month = "June 2024", amountPaid = 18000.0, amountDue = 0.0, advancePaid = 0.0, isPaid = true, paidDate = "2024-06-03"),
            Payment(tenantId = tenant2.id, roomId = "102", month = "July 2024", amountPaid = 0.0, amountDue = 12000.0, advancePaid = 0.0, isPaid = false),
            Payment(tenantId = tenant2.id, roomId = "102", month = "June 2024", amountPaid = 12000.0, amountDue = 0.0, advancePaid = 2000.0, isPaid = true, paidDate = "2024-06-05"),
            Payment(tenantId = tenant3.id, roomId = "201", month = "July 2024", amountPaid = 18000.0, amountDue = 0.0, advancePaid = 0.0, isPaid = true, paidDate = "2024-07-01")
        )

        val building1 = Building(
            id = "b1",
            name = "Nibash Tower",
            floorsCount = 5,
            systemType = "Flats",
            roomsPerFloor = 2,
            floorAdditions = floorAdditions1,
            numberingSystem = "Numeric (101, 102)",
            flatStyles = flatStyles1,
            rules = rules1,
            rentType = "Set of Flats",
            customRents = customRents1,
            universalRent = 15000.0,
            tenants = listOf(tenant1, tenant2, tenant3),
            payments = payments1
        )

        // Building 2: Dream Villa
        val tenant4 = Tenant(roomId = "A01", name = "Sajib Hasan", occupation = "Doctor", phone = "+8801911224455", familyMembers = 3)
        val building2 = Building(
            id = "b2",
            name = "Dream Villa",
            floorsCount = 3,
            systemType = "Individual Rooms",
            roomsPerFloor = 3,
            floorAdditions = emptyList(), // No parking floor, rooms on ground floor
            numberingSystem = "Alphabetic (A01, A02)",
            rules = listOf("No pets allowed", "Gate closes at 10:00 PM"),
            rentType = "Universal",
            universalRent = 8000.0,
            tenants = listOf(tenant4),
            payments = listOf(
                Payment(tenantId = tenant4.id, roomId = "A01", month = "July 2024", amountPaid = 8000.0, amountDue = 0.0, advancePaid = 1000.0, isPaid = true, paidDate = "2024-07-05")
            )
        )

        return listOf(building1, building2)
    }
}

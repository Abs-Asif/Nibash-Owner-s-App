package com.nibash.prototype

import com.nibash.prototype.model.Building
import com.nibash.prototype.model.FlatStyle
import com.nibash.prototype.model.FloorAddition
import com.nibash.prototype.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NibashOwnerTest {

    @Test
    fun testRoomGeneration_NumericWithParkingAndSingleFlat() {
        val floorAdditions = listOf(
            FloorAddition(0, "Parking Only"),
            FloorAddition(3, "Single Flat")
        )
        val building = Building(
            name = "Test Tower",
            floorsCount = 4,
            systemType = "Flats",
            roomsPerFloor = 2,
            floorAdditions = floorAdditions,
            numberingSystem = "Numeric (101, 102)",
            rentType = "Universal",
            universalRent = 10000.0
        )

        val rooms = building.generateRoomNumbers()

        // Floor 0: Parking (0 rooms)
        // Floor 1: Regular (2 rooms: 101, 102)
        // Floor 2: Regular (2 rooms: 201, 202)
        // Floor 3: Single Flat (1 room: 301)
        val expectedRooms = listOf("101", "102", "201", "202", "301")
        assertEquals(expectedRooms, rooms)
    }

    @Test
    fun testRoomGeneration_Alphabetic() {
        val building = Building(
            name = "Alpha Villa",
            floorsCount = 3,
            systemType = "Individual Rooms",
            roomsPerFloor = 2,
            floorAdditions = emptyList(),
            numberingSystem = "Alphabetic (A01, A02)",
            rentType = "Universal",
            universalRent = 8000.0
        )

        val rooms = building.generateRoomNumbers()

        // Floor 0 (A): A01, A02
        // Floor 1 (B): B01, B02
        // Floor 2 (C): C01, C02
        val expectedRooms = listOf("A01", "A02", "B01", "B02", "C01", "C02")
        assertEquals(expectedRooms, rooms)
    }

    @Test
    fun testRentForRoom_UniversalVsCustom() {
        // Universal rent
        val buildingUniversal = Building(
            name = "Uni House",
            floorsCount = 2,
            systemType = "Flats",
            roomsPerFloor = 2,
            numberingSystem = "Numeric (101, 102)",
            rentType = "Universal",
            universalRent = 12000.0
        )
        assertEquals(12000.0, buildingUniversal.getRentForRoom("101"), 0.0)
        assertEquals(12000.0, buildingUniversal.getRentForRoom("202"), 0.0)

        // Custom rents by suffix matching
        val customRents = mapOf(
            "01" to 15000.0,
            "02" to 11000.0
        )
        val buildingCustom = Building(
            name = "Custom House",
            floorsCount = 2,
            systemType = "Flats",
            roomsPerFloor = 2,
            numberingSystem = "Numeric (101, 102)",
            rentType = "Set of Flats",
            customRents = customRents,
            universalRent = 10000.0
        )
        assertEquals(15000.0, buildingCustom.getRentForRoom("101"), 0.0)
        assertEquals(11000.0, buildingCustom.getRentForRoom("102"), 0.0)
        assertEquals(15000.0, buildingCustom.getRentForRoom("201"), 0.0)
        assertEquals(11000.0, buildingCustom.getRentForRoom("202"), 0.0)
    }

    @Test
    fun testTenantSearchLogic() {
        val tenants = listOf(
            Tenant(roomId = "101", name = "Asif Karim", phone = "017112233", occupation = "Engineer", familyMembers = 2),
            Tenant(roomId = "102", name = "Kamal Hossain", phone = "018223344", occupation = "Merchant", familyMembers = 4),
            Tenant(roomId = "201", name = "Sultana Begum", phone = "015998877", occupation = "Teacher", familyMembers = 3)
        )

        // Search by name substring (case-insensitive)
        val searchByName = tenants.filter { it.name.contains("karim", ignoreCase = true) }
        assertEquals(1, searchByName.size)
        assertEquals("Asif Karim", searchByName.first().name)

        // Search by phone suffix
        val searchByPhone = tenants.filter { it.phone.contains("33") }
        val names = searchByPhone.map { it.name }
        assertTrue(names.contains("Asif Karim"))
        assertTrue(names.contains("Kamal Hossain"))
        assertEquals(2, names.size)
    }

    @Test
    fun testRoomLayoutStyles() {
        val customStyles = listOf(
            FlatStyle("01", 3, 2, 1, 1, 2, 1),
            FlatStyle("02", 2, 1, 0, 1, 1, 1)
        )
        val building = Building(
            name = "Style Tower",
            floorsCount = 2,
            systemType = "Flats",
            roomsPerFloor = 2,
            numberingSystem = "Numeric (101, 102)",
            rentType = "Universal",
            flatStyles = customStyles
        )

        val style101 = building.getStyleForRoom("101")
        val style102 = building.getStyleForRoom("102")

        assertEquals(3, style101.bedrooms)
        assertEquals(2, style101.bathrooms)
        assertEquals(1, style101.attachedBaths)

        assertEquals(2, style102.bedrooms)
        assertEquals(1, style102.bathrooms)
        assertEquals(0, style102.attachedBaths)
    }
}

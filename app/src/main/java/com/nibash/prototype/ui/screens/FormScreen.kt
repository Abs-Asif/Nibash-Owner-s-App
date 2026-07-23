package com.nibash.prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nibash.prototype.model.Building
import com.nibash.prototype.model.FlatStyle
import com.nibash.prototype.model.FloorAddition
import com.nibash.prototype.ui.components.BuildingCanvas

@Composable
fun FormScreen(
    onBuildingCreated: (Building) -> Unit,
    onBackToHome: () -> Unit
) {
    // Current step in the form wizard (1 to 9)
    var currentStep by remember { mutableStateOf(1) }

    // Building states
    var buildingName by remember { mutableStateOf("") }
    var floorsCountStr by remember { mutableStateOf("4") }
    val floorsCount = floorsCountStr.toIntOrNull() ?: 0

    var systemType by remember { mutableStateOf("Flats") } // "Flats" or "Individual Rooms"
    var roomsPerFloorStr by remember { mutableStateOf("2") }
    val roomsPerFloor = roomsPerFloorStr.toIntOrNull() ?: 1

    // Floor additions: floor index to type ("Regular", "Parking Only", "Single Flat")
    var floorAdditionsMap by remember { mutableStateOf(mapOf<Int, String>()) }

    var numberingSystem by remember { mutableStateOf("Numeric (101, 102)") } // or "Alphabetic (A01, A02)"

    // Flat styles for each suffix (default style customized)
    var style01Bedrooms by remember { mutableStateOf("3") }
    var style01Bathrooms by remember { mutableStateOf("2") }
    var style01Attached by remember { mutableStateOf("1") }
    var style01Open by remember { mutableStateOf("1") }
    var style01Balcony by remember { mutableStateOf("2") }
    var style01Kitchen by remember { mutableStateOf("1") }

    var style02Bedrooms by remember { mutableStateOf("2") }
    var style02Bathrooms by remember { mutableStateOf("1") }
    var style02Attached by remember { mutableStateOf("0") }
    var style02Open by remember { mutableStateOf("1") }
    var style02Balcony by remember { mutableStateOf("1") }
    var style02Kitchen by remember { mutableStateOf("1") }

    // Rules
    val defaultRules = listOf(
        "No pets allowed",
        "No rooftop access",
        "Gate closes at 9:00 PM",
        "No parking for cycles",
        "Parking fee for vehicles apply",
        "No loud music after 10 PM",
        "No bachelors allowed",
        "Garbage cleaning fee: 500 BDT"
    )
    var selectedRules by remember { mutableStateOf(setOf<String>()) }

    // Rent
    var rentType by remember { mutableStateOf("Universal") } // "Universal" or "Set of Flats"
    var universalRentStr by remember { mutableStateOf("15000") }
    var rent01Str by remember { mutableStateOf("18000") }
    var rent02Str by remember { mutableStateOf("12000") }

    // Map state variables into floor additions list dynamically
    val floorAdditions = remember(floorAdditionsMap) {
        floorAdditionsMap.map { FloorAddition(it.key, it.value) }
    }

    // Intercept back presses to move back a step in form instead of quitting the app!
    BackHandler {
        if (currentStep > 1) {
            currentStep--
        } else {
            onBackToHome()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        // --- TOP HALF: LIVE 3D ANIMATION VIEW ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BuildingCanvas(
                modifier = Modifier.fillMaxSize(),
                floorsCount = floorsCount,
                roomsPerFloor = roomsPerFloor,
                floorAdditions = floorAdditions
            )

            // Step Label Overlay
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xCC111827)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "Live 3D Preview (Step $currentStep/9)",
                    color = Color(0xFF38BDF8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick cancel button
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
                    .background(Color(0x55000000), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
        }

        // --- DIVIDER LINE WITH BACKWARDS AND FORWARDS CONTROLS ---
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        ) {
            val isStepValid = remember(
                currentStep, buildingName, floorsCountStr, systemType, roomsPerFloorStr,
                style01Bedrooms, style01Bathrooms, style01Attached, style01Open, style01Balcony, style01Kitchen,
                style02Bedrooms, style02Bathrooms, style02Attached, style02Open, style02Balcony, style02Kitchen,
                rentType, universalRentStr, rent01Str, rent02Str
            ) {
                when (currentStep) {
                    1 -> buildingName.isNotBlank()
                    2 -> {
                        val count = floorsCountStr.toIntOrNull()
                        count != null && count in 1..50
                    }
                    3 -> systemType.isNotBlank()
                    4 -> {
                        val count = roomsPerFloorStr.toIntOrNull()
                        count != null && count in 1..20
                    }
                    5 -> true
                    6 -> numberingSystem.isNotBlank()
                    7 -> {
                        val s1Bed = style01Bedrooms.toIntOrNull()
                        val s1Bath = style01Bathrooms.toIntOrNull()
                        val s1Att = style01Attached.toIntOrNull()
                        val s1Opn = style01Open.toIntOrNull()
                        val s1Bal = style01Balcony.toIntOrNull()
                        val s1Kit = style01Kitchen.toIntOrNull()

                        val s2Bed = style02Bedrooms.toIntOrNull()
                        val s2Bath = style02Bathrooms.toIntOrNull()
                        val s2Att = style02Attached.toIntOrNull()
                        val s2Opn = style02Open.toIntOrNull()
                        val s2Bal = style02Balcony.toIntOrNull()
                        val s2Kit = style02Kitchen.toIntOrNull()

                        s1Bed != null && s1Bed >= 0 && s1Bath != null && s1Bath >= 0 &&
                        s1Att != null && s1Att >= 0 && s1Opn != null && s1Opn >= 0 &&
                        s1Bal != null && s1Bal >= 0 && s1Kit != null && s1Kit >= 0 &&
                        s2Bed != null && s2Bed >= 0 && s2Bath != null && s2Bath >= 0 &&
                        s2Att != null && s2Att >= 0 && s2Opn != null && s2Opn >= 0 &&
                        s2Bal != null && s2Bal >= 0 && s2Kit != null && s2Kit >= 0
                    }
                    8 -> true
                    9 -> {
                        if (rentType == "Universal") {
                            val rent = universalRentStr.toDoubleOrNull()
                            rent != null && rent >= 0.0
                        } else {
                            val r1 = rent01Str.toDoubleOrNull()
                            val r2 = rent02Str.toDoubleOrNull()
                            r1 != null && r1 >= 0.0 && r2 != null && r2 >= 0.0
                        }
                    }
                    else -> true
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Forward / Backward control bar at the top part of the bottom form
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { if (currentStep > 1) currentStep-- else onBackToHome() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF2D2D44)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Step",
                            tint = Color.White
                        )
                    }

                    // Question/Step Title text
                    Text(
                        text = when (currentStep) {
                            1 -> "Building Identity"
                            2 -> "Building Floors"
                            3 -> "Layout System"
                            4 -> "Rooms Per Floor"
                            5 -> "Floor Additions"
                            6 -> "Numbering Logic"
                            7 -> "Flat Styles & Layout"
                            8 -> "Building Rules"
                            9 -> "Rent Rates"
                            else -> "Nibash Form"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (currentStep < 9) {
                        FilledIconButton(
                            enabled = isStepValid,
                            onClick = { currentStep++ },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF6366F1),
                                disabledContainerColor = Color(0xFF2D2D44)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Step",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Submit Button
                        FilledIconButton(
                            enabled = isStepValid,
                            onClick = {
                                // Assemble the building
                                val styles = listOf(
                                    FlatStyle(
                                        roomSuffix = "01",
                                        bedrooms = style01Bedrooms.toIntOrNull() ?: 3,
                                        bathrooms = style01Bathrooms.toIntOrNull() ?: 2,
                                        attachedBaths = style01Attached.toIntOrNull() ?: 1,
                                        openBaths = style01Open.toIntOrNull() ?: 1,
                                        balconies = style01Balcony.toIntOrNull() ?: 2,
                                        kitchens = style01Kitchen.toIntOrNull() ?: 1
                                    ),
                                    FlatStyle(
                                        roomSuffix = "02",
                                        bedrooms = style02Bedrooms.toIntOrNull() ?: 2,
                                        bathrooms = style02Bathrooms.toIntOrNull() ?: 1,
                                        attachedBaths = style02Attached.toIntOrNull() ?: 0,
                                        openBaths = style02Open.toIntOrNull() ?: 1,
                                        balconies = style02Balcony.toIntOrNull() ?: 1,
                                        kitchens = style02Kitchen.toIntOrNull() ?: 1
                                    )
                                )

                                val customRents = mapOf(
                                    "01" to (rent01Str.toDoubleOrNull() ?: 15000.0),
                                    "02" to (rent02Str.toDoubleOrNull() ?: 12000.0)
                                )

                                val newBuilding = Building(
                                    name = buildingName.ifBlank { "New Building Prototype" },
                                    floorsCount = floorsCount,
                                    systemType = systemType,
                                    roomsPerFloor = roomsPerFloor,
                                    floorAdditions = floorAdditions,
                                    numberingSystem = numberingSystem,
                                    flatStyles = styles,
                                    rules = selectedRules.toList(),
                                    rentType = rentType,
                                    universalRent = universalRentStr.toDoubleOrNull() ?: 10000.0,
                                    customRents = customRents
                                )
                                onBuildingCreated(newBuilding)
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF10B981),
                                disabledContainerColor = Color(0xFF2D2D44)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Complete building",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Smooth linear progress indicator
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / 9f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFF6366F1),
                    trackColor = Color(0xFF2D2D44)
                )

                // Scrollable container for the interactive form inputs
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (currentStep) {
                            1 -> StepBuildingName(
                                buildingName = buildingName,
                                onNameChange = { buildingName = it }
                            )
                            2 -> StepFloorsCount(
                                floorsCountStr = floorsCountStr,
                                onFloorsChange = { floorsCountStr = it }
                            )
                            3 -> StepSystemType(
                                systemType = systemType,
                                onSystemTypeChange = { systemType = it }
                            )
                            4 -> StepRoomsPerFloor(
                                roomsCountStr = roomsPerFloorStr,
                                onRoomsCountChange = { roomsPerFloorStr = it }
                            )
                            5 -> StepFloorAdditions(
                                floorsCount = floorsCount,
                                floorAdditionsMap = floorAdditionsMap,
                                onAdditionsChange = { floorAdditionsMap = it }
                            )
                            6 -> StepNumberingSystem(
                                numberingSystem = numberingSystem,
                                onNumberingChange = { numberingSystem = it }
                            )
                            7 -> StepFlatStyles(
                                suffix01Bedrooms = style01Bedrooms,
                                suffix01Bathrooms = style01Bathrooms,
                                suffix01Attached = style01Attached,
                                suffix01Open = style01Open,
                                suffix01Balcony = style01Balcony,
                                suffix01Kitchen = style01Kitchen,
                                onStyle01Change = { bed, bath, att, opn, bal, kit ->
                                    style01Bedrooms = bed
                                    style01Bathrooms = bath
                                    style01Attached = att
                                    style01Open = opn
                                    style01Balcony = bal
                                    style01Kitchen = kit
                                },
                                suffix02Bedrooms = style02Bedrooms,
                                suffix02Bathrooms = style02Bathrooms,
                                suffix02Attached = style02Attached,
                                suffix02Open = style02Open,
                                suffix02Balcony = style02Balcony,
                                suffix02Kitchen = style02Kitchen,
                                onStyle02Change = { bed, bath, att, opn, bal, kit ->
                                    style02Bedrooms = bed
                                    style02Bathrooms = bath
                                    style02Attached = att
                                    style02Open = opn
                                    style02Balcony = bal
                                    style02Kitchen = kit
                                }
                            )
                            8 -> StepRulesList(
                                availableRules = defaultRules,
                                selectedRules = selectedRules,
                                onRulesChange = { selectedRules = it }
                            )
                            9 -> StepRentConfig(
                                rentType = rentType,
                                onRentTypeChange = { rentType = it },
                                universalRentStr = universalRentStr,
                                onUniversalRentChange = { universalRentStr = it },
                                rent01Str = rent01Str,
                                onRent01Change = { rent01Str = it },
                                rent02Str = rent02Str,
                                onRent02Change = { rent02Str = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- FORMS SUB-SCREENS FOR EACH QUESTION ---

@Composable
fun StepBuildingName(buildingName: String, onNameChange: (String) -> Unit) {
    Text(
        text = "What is the name of your building?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    OutlinedTextField(
        value = buildingName,
        onValueChange = onNameChange,
        label = { Text("Building Name") },
        placeholder = { Text("e.g. Nibash Tower, Paradise Villa") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = Color(0xFF4B5563)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun StepFloorsCount(floorsCountStr: String, onFloorsChange: (String) -> Unit) {
    Text(
        text = "How many floors are in the building?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    OutlinedTextField(
        value = floorsCountStr,
        onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
                onFloorsChange(it)
            }
        },
        label = { Text("Number of floors") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = Color(0xFF4B5563)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun StepSystemType(systemType: String, onSystemTypeChange: (String) -> Unit) {
    Text(
        text = "Is this building partitioned into complete Flats or Individual Rooms?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onSystemTypeChange("Flats") }
                .border(
                    width = 2.dp,
                    color = if (systemType == "Flats") Color(0xFF6366F1) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C3E))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Flats System", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Multi-room apartment spaces", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onSystemTypeChange("Individual Rooms") }
                .border(
                    width = 2.dp,
                    color = if (systemType == "Individual Rooms") Color(0xFF6366F1) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C3E))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Room System", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Single room hostels/units", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun StepRoomsPerFloor(roomsCountStr: String, onRoomsCountChange: (String) -> Unit) {
    Text(
        text = "How many units are typically on each floor?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    OutlinedTextField(
        value = roomsCountStr,
        onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
                onRoomsCountChange(it)
            }
        },
        label = { Text("Units per floor") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = Color(0xFF4B5563)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun StepFloorAdditions(
    floorsCount: Int,
    floorAdditionsMap: Map<Int, String>,
    onAdditionsChange: (Map<Int, String>) -> Unit
) {
    Text(
        text = "Floor Customizations & Exceptions",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
    Text(
        text = "You can set ground floor for Parking, or designate specific floors as Single Flat penthouses.",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    for (f in 0 until floorsCount) {
        val currentType = floorAdditionsMap[f] ?: "Regular"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF2A2A3D), RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (f == 0) "Ground Floor (0)" else "Floor $f",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Regular", "Parking Only", "Single Flat").forEach { type ->
                    val isSelected = currentType == type
                    Text(
                        text = type,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF1E1E2E))
                            .clickable {
                                val newMap = floorAdditionsMap.toMutableMap()
                                newMap[f] = type
                                onAdditionsChange(newMap)
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StepNumberingSystem(numberingSystem: String, onNumberingChange: (String) -> Unit) {
    Text(
        text = "Select room numbering code structure:",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    listOf("Numeric (101, 102, 103)", "Alphabetic (A01, A02, B01)").forEach { system ->
        val isSelected = numberingSystem.startsWith(system.substring(0, 3))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(
                    if (isSelected) Color(0xFF2C2C4E) else Color(0xFF1F1F2F),
                    RoundedCornerShape(8.dp)
                )
                .clickable { onNumberingChange(system) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onNumberingChange(system) },
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6366F1))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = system,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun StepFlatStyles(
    suffix01Bedrooms: String,
    suffix01Bathrooms: String,
    suffix01Attached: String,
    suffix01Open: String,
    suffix01Balcony: String,
    suffix01Kitchen: String,
    onStyle01Change: (String, String, String, String, String, String) -> Unit,
    suffix02Bedrooms: String,
    suffix02Bathrooms: String,
    suffix02Attached: String,
    suffix02Open: String,
    suffix02Balcony: String,
    suffix02Kitchen: String,
    onStyle02Change: (String, String, String, String, String, String) -> Unit
) {
    Text(
        text = "Customize Rooms & Flats Layout Styles",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
    Text(
        text = "Define room layouts based on room suffix sets (e.g. all 101/201/301 rooms):",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    // --- SECTION FOR *01 SUFFIX ---
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3D)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Style for suffix *01 [e.g. 101, 201, 301]", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = suffix01Bedrooms,
                    onValueChange = { onStyle01Change(it, suffix01Bathrooms, suffix01Attached, suffix01Open, suffix01Balcony, suffix01Kitchen) },
                    label = { Text("Beds", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix01Bathrooms,
                    onValueChange = { onStyle01Change(suffix01Bedrooms, it, suffix01Attached, suffix01Open, suffix01Balcony, suffix01Kitchen) },
                    label = { Text("Baths", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix01Attached,
                    onValueChange = { onStyle01Change(suffix01Bedrooms, suffix01Bathrooms, it, suffix01Open, suffix01Balcony, suffix01Kitchen) },
                    label = { Text("Attach", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = suffix01Open,
                    onValueChange = { onStyle01Change(suffix01Bedrooms, suffix01Bathrooms, suffix01Attached, it, suffix01Balcony, suffix01Kitchen) },
                    label = { Text("Open B.", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix01Balcony,
                    onValueChange = { onStyle01Change(suffix01Bedrooms, suffix01Bathrooms, suffix01Attached, suffix01Open, it, suffix01Kitchen) },
                    label = { Text("Balcony", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix01Kitchen,
                    onValueChange = { onStyle01Change(suffix01Bedrooms, suffix01Bathrooms, suffix01Attached, suffix01Open, suffix01Balcony, it) },
                    label = { Text("Kitchen", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
            }
        }
    }

    // --- SECTION FOR *02 SUFFIX ---
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Style for suffix *02 [e.g. 102, 202, 302]", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = suffix02Bedrooms,
                    onValueChange = { onStyle02Change(it, suffix02Bathrooms, suffix02Attached, suffix02Open, suffix02Balcony, suffix02Kitchen) },
                    label = { Text("Beds", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix02Bathrooms,
                    onValueChange = { onStyle02Change(suffix02Bedrooms, it, suffix02Attached, suffix02Open, suffix02Balcony, suffix02Kitchen) },
                    label = { Text("Baths", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix02Attached,
                    onValueChange = { onStyle02Change(suffix02Bedrooms, suffix02Bathrooms, it, suffix02Open, suffix02Balcony, suffix02Kitchen) },
                    label = { Text("Attach", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = suffix02Open,
                    onValueChange = { onStyle02Change(suffix02Bedrooms, suffix02Bathrooms, suffix02Attached, it, suffix02Balcony, suffix02Kitchen) },
                    label = { Text("Open B.", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix02Balcony,
                    onValueChange = { onStyle02Change(suffix02Bedrooms, suffix02Bathrooms, suffix02Attached, suffix02Open, it, suffix02Kitchen) },
                    label = { Text("Balcony", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
                OutlinedTextField(
                    value = suffix02Kitchen,
                    onValueChange = { onStyle02Change(suffix02Bedrooms, suffix02Bathrooms, suffix02Attached, suffix02Open, suffix02Balcony, it) },
                    label = { Text("Kitchen", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                )
            }
        }
    }
}

@Composable
fun StepRulesList(
    availableRules: List<String>,
    selectedRules: Set<String>,
    onRulesChange: (Set<String>) -> Unit
) {
    Text(
        text = "Select rules for the building:",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    availableRules.forEach { rule ->
        val isChecked = selectedRules.contains(rule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF232338), RoundedCornerShape(8.dp))
                .clickable {
                    val newSet = selectedRules.toMutableSet()
                    if (isChecked) newSet.remove(rule) else newSet.add(rule)
                    onRulesChange(newSet)
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    val newSet = selectedRules.toMutableSet()
                    if (isChecked) newSet.remove(rule) else newSet.add(rule)
                    onRulesChange(newSet)
                },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = rule, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun StepRentConfig(
    rentType: String,
    onRentTypeChange: (String) -> Unit,
    universalRentStr: String,
    onUniversalRentChange: (String) -> Unit,
    rent01Str: String,
    onRent01Change: (String) -> Unit,
    rent02Str: String,
    onRent02Change: (String) -> Unit
) {
    Text(
        text = "Rent Pricing Structure",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
    Text(
        text = "Choose whether all rooms have the same rent or if rent varies by room suffix:",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onRentTypeChange("Universal") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (rentType == "Universal") Color(0xFF6366F1) else Color(0xFF2A2A3D)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Universal Rent", fontSize = 11.sp)
        }

        Button(
            onClick = { onRentTypeChange("Set of Flats") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (rentType == "Set of Flats") Color(0xFF6366F1) else Color(0xFF2A2A3D)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("By Suffix (*01/*02)", fontSize = 11.sp)
        }
    }

    if (rentType == "Universal") {
        OutlinedTextField(
            value = universalRentStr,
            onValueChange = onUniversalRentChange,
            label = { Text("Rent Amount (BDT/month)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rent01Str,
                onValueChange = onRent01Change,
                label = { Text("Rent for *01 Flats (BDT/month)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = rent02Str,
                onValueChange = onRent02Change,
                label = { Text("Rent for *02 Flats (BDT/month)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

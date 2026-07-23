package com.nibash.prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

    // Dynamic flat styles based on roomsPerFloor
    var dynamicFlatStyles by remember(roomsPerFloor) {
        val count = roomsPerFloor.coerceIn(1, 20)
        mutableStateOf(
            (1..count).map { r ->
                val suffix = String.format("%02d", r)
                FlatStyle(
                    roomSuffix = suffix,
                    bedrooms = if (systemType == "Individual Rooms") 1 else 2,
                    bathrooms = 1,
                    attachedBaths = 0,
                    openBaths = 1,
                    balconies = 1,
                    kitchens = if (systemType == "Individual Rooms") 0 else 1
                )
            }
        )
    }

    // Dynamic rent inputs based on roomsPerFloor
    var dynamicCustomRentsMap by remember(roomsPerFloor) {
        val count = roomsPerFloor.coerceIn(1, 20)
        mutableStateOf(
            (1..count).associate { r ->
                val suffix = String.format("%02d", r)
                suffix to "12000"
            }
        )
    }

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

    // Rent Type
    var rentType by remember { mutableStateOf("Universal") } // "Universal" or "Set of Flats"
    var universalRentStr by remember { mutableStateOf("15000") }

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
            .background(Color(0xFF0B0B14))
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopStart)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "Live 3D Preview (Step $currentStep/9)",
                    color = Color(0xFF818CF8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick cancel button
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color(0x55000000), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
        }

        // --- BOTTOM HALF: INPUT WIZARD ---
        Card(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
        ) {
            val isStepValid = remember(
                currentStep, buildingName, floorsCountStr, systemType, roomsPerFloorStr,
                dynamicFlatStyles, rentType, universalRentStr, dynamicCustomRentsMap
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
                        // Check if all fields in all styles are valid positive integers
                        dynamicFlatStyles.all { style ->
                            style.bedrooms >= 0 && style.bathrooms >= 0 &&
                            style.attachedBaths >= 0 && style.openBaths >= 0 &&
                            style.balconies >= 0 && style.kitchens >= 0
                        }
                    }
                    8 -> true
                    9 -> {
                        if (rentType == "Universal") {
                            val rent = universalRentStr.toDoubleOrNull()
                            rent != null && rent >= 0.0
                        } else {
                            // Check if all custom rents in map are valid non-negative numbers
                            dynamicCustomRentsMap.values.all { rStr ->
                                val r = rStr.toDoubleOrNull()
                                r != null && r >= 0.0
                            }
                        }
                    }
                    else -> true
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Forward / Backward control bar at the top part of the bottom form
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { if (currentStep > 1) currentStep-- else onBackToHome() },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF222238)
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
                            1 -> "Property Identity"
                            2 -> "Property Floors"
                            3 -> "Layout Architecture"
                            4 -> "Units Per Floor"
                            5 -> "Exception Additions"
                            6 -> "Numbering System"
                            7 -> "Flat Styles & Layout"
                            8 -> "House Rules"
                            9 -> "Rent Pricing Plan"
                            else -> "Property Form"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )

                    if (currentStep < 9) {
                        FilledIconButton(
                            enabled = isStepValid,
                            onClick = { currentStep++ },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF6366F1),
                                disabledContainerColor = Color(0xFF222238)
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
                                val customRentsParsed = dynamicCustomRentsMap.mapValues {
                                    it.value.toDoubleOrNull() ?: 12000.0
                                }

                                val newBuilding = Building(
                                    name = buildingName.ifBlank { "New Premium Nibash" },
                                    floorsCount = floorsCount,
                                    systemType = systemType,
                                    roomsPerFloor = roomsPerFloor,
                                    floorAdditions = floorAdditions,
                                    numberingSystem = numberingSystem,
                                    flatStyles = dynamicFlatStyles,
                                    rules = selectedRules.toList(),
                                    rentType = rentType,
                                    universalRent = universalRentStr.toDoubleOrNull() ?: 15000.0,
                                    customRents = customRentsParsed
                                )
                                onBuildingCreated(newBuilding)
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF10B981),
                                disabledContainerColor = Color(0xFF222238)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Create Property",
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
                        .padding(bottom = 20.dp)
                        .clip(CircleShape),
                    color = Color(0xFF6366F1),
                    trackColor = Color(0xFF222238)
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
                            7 -> StepFlatStylesDynamic(
                                flatStyles = dynamicFlatStyles,
                                onStylesChange = { dynamicFlatStyles = it }
                            )
                            8 -> StepRulesList(
                                availableRules = defaultRules,
                                selectedRules = selectedRules,
                                onRulesChange = { selectedRules = it }
                            )
                            9 -> StepRentConfigDynamic(
                                rentType = rentType,
                                onRentTypeChange = { rentType = it },
                                universalRentStr = universalRentStr,
                                onUniversalRentChange = { universalRentStr = it },
                                customRentsMap = dynamicCustomRentsMap,
                                onCustomRentsMapChange = { dynamicCustomRentsMap = it }
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
        text = "What is the name of your property?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp),
        fontWeight = FontWeight.Medium
    )
    OutlinedTextField(
        value = buildingName,
        onValueChange = onNameChange,
        label = { Text("Property Name") },
        placeholder = { Text("e.g. Nibash Tower, Silver Villa") },
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
        text = "How many floors are in this building?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp),
        fontWeight = FontWeight.Medium
    )
    OutlinedTextField(
        value = floorsCountStr,
        onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
                onFloorsChange(it)
            }
        },
        label = { Text("Number of floors (1 - 50)") },
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

    val count = floorsCountStr.toIntOrNull()
    if (count != null && count !in 1..50) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enter a logical floor count between 1 and 50.",
            color = Color(0xFFEF4444),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StepSystemType(systemType: String, onSystemTypeChange: (String) -> Unit) {
    Text(
        text = "Is this building partitioned into complete Flats or Individual Rooms?",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp),
        fontWeight = FontWeight.Medium
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
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Flats System", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Multi-room spaces", color = Color.Gray, fontSize = 11.sp)
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onSystemTypeChange("Individual Rooms") }
                .border(
                    width = 2.dp,
                    color = if (systemType == "Individual Rooms") Color(0xFF6366F1) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Room System", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hostels / Units", color = Color.Gray, fontSize = 11.sp)
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
        modifier = Modifier.padding(bottom = 12.dp),
        fontWeight = FontWeight.Medium
    )
    OutlinedTextField(
        value = roomsCountStr,
        onValueChange = {
            if (it.isEmpty() || it.toIntOrNull() != null) {
                onRoomsCountChange(it)
            }
        },
        label = { Text("Units per floor (1 - 20)") },
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

    val count = roomsCountStr.toIntOrNull()
    if (count != null && count !in 1..20) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please enter a count between 1 and 20.",
            color = Color(0xFFEF4444),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
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
        text = "Set ground floor for Parking, or designate specific floors as Single Flat penthouses.",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    for (f in 0 until floorsCount) {
        val currentType = floorAdditionsMap[f] ?: "Regular"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF1C1C2C), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (f == 0) "Ground Floor (0)" else "Floor $f",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF6366F1) else Color(0xFF141424))
                            .clickable {
                                val newMap = floorAdditionsMap.toMutableMap()
                                newMap[f] = type
                                onAdditionsChange(newMap)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
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
        modifier = Modifier.padding(bottom = 12.dp),
        fontWeight = FontWeight.Medium
    )
    listOf("Numeric (101, 102, 103)", "Alphabetic (A01, A02, B01)").forEach { system ->
        val isSelected = numberingSystem.startsWith(system.substring(0, 3))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(
                    if (isSelected) Color(0xFF1E1E2F) else Color(0xFF141424),
                    RoundedCornerShape(12.dp)
                )
                .clickable { onNumberingChange(system) }
                .padding(16.dp)
                .border(1.dp, if (isSelected) Color(0xFF6366F1) else Color.Transparent, RoundedCornerShape(12.dp)),
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
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StepFlatStylesDynamic(
    flatStyles: List<FlatStyle>,
    onStylesChange: (List<FlatStyle>) -> Unit
) {
    Text(
        text = "Customize Suffix Layout Styles",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
    Text(
        text = "Define room layouts dynamically based on your rooms per floor suffixes (e.g., all *01, *02 units):",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    flatStyles.forEachIndexed { index, style ->
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2C)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Layout Style for suffix *${style.roomSuffix}",
                    color = Color(0xFF818CF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var bedStr by remember(style) { mutableStateOf(style.bedrooms.toString()) }
                    var bathStr by remember(style) { mutableStateOf(style.bathrooms.toString()) }
                    var attachedStr by remember(style) { mutableStateOf(style.attachedBaths.toString()) }

                    OutlinedTextField(
                        value = bedStr,
                        onValueChange = {
                            bedStr = it
                            val value = it.toIntOrNull() ?: 0
                            val updated = style.copy(bedrooms = value)
                            val newList = flatStyles.toMutableList()
                            newList[index] = updated
                            onStylesChange(newList)
                        },
                        label = { Text("Bedrooms", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = bathStr,
                        onValueChange = {
                            bathStr = it
                            val value = it.toIntOrNull() ?: 0
                            val updated = style.copy(bathrooms = value)
                            val newList = flatStyles.toMutableList()
                            newList[index] = updated
                            onStylesChange(newList)
                        },
                        label = { Text("Bathrooms", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = attachedStr,
                        onValueChange = {
                            attachedStr = it
                            val value = it.toIntOrNull() ?: 0
                            val updated = style.copy(attachedBaths = value)
                            val newList = flatStyles.toMutableList()
                            newList[index] = updated
                            onStylesChange(newList)
                        },
                        label = { Text("Attached Baths", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var openStr by remember(style) { mutableStateOf(style.openBaths.toString()) }
                    var balconyStr by remember(style) { mutableStateOf(style.balconies.toString()) }
                    var kitchenStr by remember(style) { mutableStateOf(style.kitchens.toString()) }

                    OutlinedTextField(
                        value = openStr,
                        onValueChange = {
                            openStr = it
                            val value = it.toIntOrNull() ?: 0
                            val updated = style.copy(openBaths = value)
                            val newList = flatStyles.toMutableList()
                            newList[index] = updated
                            onStylesChange(newList)
                        },
                        label = { Text("Open Baths", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = balconyStr,
                        onValueChange = {
                            balconyStr = it
                            val value = it.toIntOrNull() ?: 0
                            val updated = style.copy(balconies = value)
                            val newList = flatStyles.toMutableList()
                            newList[index] = updated
                            onStylesChange(newList)
                        },
                        label = { Text("Balconies", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = kitchenStr,
                        onValueChange = {
                            kitchenStr = it
                            val value = it.toIntOrNull() ?: 0
                            val updated = style.copy(kitchens = value)
                            val newList = flatStyles.toMutableList()
                            newList[index] = updated
                            onStylesChange(newList)
                        },
                        label = { Text("Kitchens", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )
                }
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
        text = "Select rules for this property:",
        color = Color.LightGray,
        fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 12.dp),
        fontWeight = FontWeight.Medium
    )

    availableRules.forEach { rule ->
        val isChecked = selectedRules.contains(rule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color(0xFF1C1C2C), RoundedCornerShape(12.dp))
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
fun StepRentConfigDynamic(
    rentType: String,
    onRentTypeChange: (String) -> Unit,
    universalRentStr: String,
    onUniversalRentChange: (String) -> Unit,
    customRentsMap: Map<String, String>,
    onCustomRentsMapChange: (Map<String, String>) -> Unit
) {
    Text(
        text = "Rent Pricing Structure",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )
    Text(
        text = "Choose whether all rooms have a universal rent rate or vary dynamically by room suffix:",
        color = Color.LightGray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { onRentTypeChange("Universal") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (rentType == "Universal") Color(0xFF6366F1) else Color(0xFF1E1E2F)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Universal Rent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = { onRentTypeChange("Set of Flats") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (rentType == "Set of Flats") Color(0xFF6366F1) else Color(0xFF1E1E2F)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("By Room Suffix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            customRentsMap.keys.sorted().forEach { suffix ->
                val currentVal = customRentsMap[suffix] ?: ""
                OutlinedTextField(
                    value = currentVal,
                    onValueChange = { newVal ->
                        val newMap = customRentsMap.toMutableMap()
                        newMap[suffix] = newVal
                        onCustomRentsMapChange(newMap)
                    },
                    label = { Text("Rent for *${suffix} Flats (BDT/month)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

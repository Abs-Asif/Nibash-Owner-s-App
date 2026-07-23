package com.nibash.prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.nibash.prototype.model.Payment
import com.nibash.prototype.model.Tenant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    building: Building,
    onBack: () -> Unit,
    onUpdateBuilding: (Building) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddTenantDialog by remember { mutableStateOf(false) }
    var showLogPaymentDialog by remember { mutableStateOf(false) }
    var selectedRoomForDetails by remember { mutableStateOf<String?>(null) }
    var preselectedRoomToAddTenant by remember { mutableStateOf<String?>(null) }

    // Navigation interceptor
    BackHandler {
        onBack()
    }

    // Tenant Search calculation (case-insensitive)
    val filteredTenants = remember(searchQuery, building.tenants) {
        if (searchQuery.isBlank()) {
            building.tenants
        } else {
            building.tenants.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
            }
        }
    }

    val roomNumbers = remember(building) { building.generateRoomNumbers(includeExcluded = false) }
    val excludedRooms = building.excludedRooms

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B14))
    ) {
        // --- DETAIL TOP BAR ---
        TopAppBar(
            title = {
                Column {
                    Text(text = building.name, fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                    Text(
                        text = "${building.floorsCount} Floors • ${roomNumbers.size} Active Units" +
                                if (excludedRooms.isNotEmpty()) " (${excludedRooms.size} Excluded)" else "",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141424)),
            actions = {
                // Future expansion
            }
        )

        // --- BODY ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // --- SECTION 1: SEARCH TENANTS ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Tenant (by Name or Phone)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF4B5563)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            if (searchQuery.isNotBlank()) {
                Text(
                    text = "Search Results (${filteredTenants.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (filteredTenants.isEmpty()) {
                    Text("No tenants found matching '$searchQuery'", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                } else {
                    filteredTenants.forEach { tenant ->
                        TenantSearchResultCard(tenant = tenant, onRemove = {
                            // Remove Tenant action
                            val newTenants = building.tenants.filter { it.id != tenant.id }
                            val newPayments = building.payments.filter { it.tenantId != tenant.id }
                            onUpdateBuilding(building.copy(tenants = newTenants, payments = newPayments))
                        })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- SECTION 2: COMPACT ROOM LIST (NO RENT) ---
            Text(
                text = "Building Units (Compact List)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Legends
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Occupied", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF6366F1), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vacant", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (roomNumbers.isEmpty()) {
                        Text(
                            text = "No active rooms in this building.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // Compact List representation showing ONLY room number and room detail (no rent directly displayed)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            roomNumbers.forEach { roomNum ->
                                val tenant = building.tenants.find { it.roomId == roomNum }
                                val isOccupied = tenant != null
                                val style = building.getStyleForRoom(roomNum)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1C1C2C), RoundedCornerShape(12.dp))
                                        .clickable { selectedRoomForDetails = roomNum }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(if (isOccupied) Color(0xFF10B981) else Color(0xFF6366F1), CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "Unit $roomNum",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                            // Room detail (Beds, Baths, Kitchen, Balconies etc)
                                            Text(
                                                text = "${style.bedrooms} Bed • ${style.bathrooms} Bath • ${style.balconies} Balcony • ${style.kitchens} Kitchen",
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isOccupied) "Occupied" else "Vacant",
                                            color = if (isOccupied) Color(0xFF10B981) else Color(0xFF6366F1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(
                                                    if (isOccupied) Color(0x1110B981) else Color(0x116366F1),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Details",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION 3: QUICK ACTIONS ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddTenantDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Tenant", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showLogPaymentDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Rent", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 4: EXCLUDED ROOMS LEDGER ---
            if (excludedRooms.isNotEmpty()) {
                Text(
                    text = "Excluded Units (In Animation Only)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        excludedRooms.forEach { roomNum ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFF221525), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Block, contentDescription = "Excluded", tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Unit $roomNum", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Text(
                                    text = "Re-include",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            val updatedExcluded = excludedRooms.filter { it != roomNum }
                                            onUpdateBuilding(building.copy(excludedRooms = updatedExcluded))
                                        }
                                        .background(Color(0x1138BDF8), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- SECTION 5: RENT RECORD BOOK ---
            Text(
                text = "Rent Record Ledger (Payments)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (building.payments.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "No rent payments recorded yet.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                building.payments.reversed().forEach { payment ->
                    val tenant = building.tenants.find { it.id == payment.tenantId }
                    PaymentRecordRow(payment = payment, tenantName = tenant?.name ?: "Unknown Tenant")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECTION 6: RULES LIST ---
            if (building.rules.isNotEmpty()) {
                Text(
                    text = "Building Codes & Rules",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        building.rules.forEach { rule ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = rule, color = Color.LightGray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: ROOM DETAILS (CUSTOM RENT & EXCLUDE OPTIONS) ---
    if (selectedRoomForDetails != null) {
        val roomNum = selectedRoomForDetails!!
        val tenant = building.tenants.find { it.roomId == roomNum }
        val isOccupied = tenant != null
        val style = building.getStyleForRoom(roomNum)

        // Rent configuration variables in Dialog
        var customRentInput by remember(roomNum) {
            val existingCustom = building.customRoomRents[roomNum]
            val initialVal = existingCustom ?: building.getRentForRoom(roomNum)
            mutableStateOf(initialVal.toInt().toString())
        }

        AlertDialog(
            onDismissRequest = { selectedRoomForDetails = null },
            title = {
                Text(
                    text = "Unit $roomNum Management",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            containerColor = Color(0xFF141424),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Layout style info
                    Text("Unit Specifications", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2C)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("• Bedrooms: ${style.bedrooms}", color = Color.LightGray, fontSize = 13.sp)
                            Text("• Bathrooms: ${style.bathrooms} (Attached: ${style.attachedBaths}, Open: ${style.openBaths})", color = Color.LightGray, fontSize = 13.sp)
                            Text("• Balconies: ${style.balconies}", color = Color.LightGray, fontSize = 13.sp)
                            Text("• Kitchens: ${style.kitchens}", color = Color.LightGray, fontSize = 13.sp)
                        }
                    }

                    // Rent Management Section
                    Text("Rent Management Plan", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customRentInput,
                            onValueChange = { customRentInput = it },
                            label = { Text("Set Custom Room Rent (BDT/month)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Info indicator about active rent
                        val parsedRent = customRentInput.toDoubleOrNull() ?: 0.0
                        if (parsedRent > 0.0) {
                            Text(
                                text = "Active Room Rent: ${parsedRent.toInt()} BDT",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tenant Info Section
                    Text("Tenant Occupancy Status", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (isOccupied) {
                        val t = tenant!!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x1510B981)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Tenant: ${t.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Occupation: ${t.occupation}", color = Color.LightGray, fontSize = 12.sp)
                                Text("Phone: ${t.phone}", color = Color.White, fontSize = 12.sp)
                                Text("Family Size: ${t.familyMembers} persons", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x156366F1)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF6366F1), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "This unit is currently vacant.",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exclude option button
                    Button(
                        onClick = {
                            // Exclude room from list (active rental pool)
                            val updatedExcluded = (building.excludedRooms + roomNum).distinct()
                            // Evict active tenant if any
                            val updatedTenants = building.tenants.filter { it.roomId != roomNum }
                            val updatedPayments = building.payments.filter { it.roomId != roomNum }

                            // Update custom rents map as well to remove this room if wanted
                            val updatedCustomRents = building.customRoomRents.toMutableMap()
                            updatedCustomRents.remove(roomNum)

                            onUpdateBuilding(
                                building.copy(
                                    excludedRooms = updatedExcluded,
                                    tenants = updatedTenants,
                                    payments = updatedPayments,
                                    customRoomRents = updatedCustomRents
                                )
                            )
                            selectedRoomForDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                    ) {
                        Text("Exclude Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Save custom rent button
                    Button(
                        enabled = customRentInput.toDoubleOrNull() != null && (customRentInput.toDoubleOrNull() ?: 0.0) >= 0.0,
                        onClick = {
                            val newRent = customRentInput.toDoubleOrNull() ?: 0.0
                            val updatedCustomRents = building.customRoomRents.toMutableMap()
                            updatedCustomRents[roomNum] = newRent

                            onUpdateBuilding(
                                building.copy(customRoomRents = updatedCustomRents)
                            )
                            selectedRoomForDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Save Rent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isOccupied) {
                        TextButton(
                            onClick = {
                                // Evict tenant
                                val newTenants = building.tenants.filter { it.id != tenant!!.id }
                                val newPayments = building.payments.filter { it.tenantId != tenant!!.id }
                                onUpdateBuilding(building.copy(tenants = newTenants, payments = newPayments))
                                selectedRoomForDetails = null
                            }
                        ) {
                            Text("Evict", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(
                            onClick = {
                                preselectedRoomToAddTenant = roomNum
                                selectedRoomForDetails = null
                                showAddTenantDialog = true
                            }
                        ) {
                            Text("Add Tenant", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(onClick = { selectedRoomForDetails = null }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            }
        )
    }

    // --- DIALOG: ADD TENANT ---
    if (showAddTenantDialog) {
        val vacantRooms = remember(building.tenants, roomNumbers) {
            roomNumbers.filter { room -> building.tenants.none { it.roomId == room } }
        }

        var name by remember { mutableStateOf("") }
        var occupation by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var familyCountStr by remember { mutableStateOf("3") }
        var selectedRoom by remember { mutableStateOf(preselectedRoomToAddTenant ?: vacantRooms.firstOrNull() ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddTenantDialog = false
                preselectedRoomToAddTenant = null
            },
            title = { Text("Add Tenant to Property", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF141424),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tenant Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = familyCountStr,
                        onValueChange = { familyCountStr = it },
                        label = { Text("Number of Family Members") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Room Selection dropdown alternative
                    Text("Select Flat / Room:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (vacantRooms.isEmpty()) {
                        Text(
                            text = "No vacant rooms available in this building!",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(vacantRooms) { room ->
                                val isSelected = selectedRoom == room
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFF6366F1) else Color(0xFF1C1C2C),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedRoom = room }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(text = room, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val isFamilyCountValid = familyCountStr.toIntOrNull() != null && (familyCountStr.toIntOrNull() ?: 0) >= 1
                Button(
                    enabled = vacantRooms.isNotEmpty() && name.isNotBlank() && selectedRoom.isNotBlank() && isFamilyCountValid,
                    onClick = {
                        if (name.isNotBlank() && selectedRoom.isNotBlank()) {
                            val newTenant = Tenant(
                                roomId = selectedRoom,
                                name = name,
                                occupation = occupation,
                                phone = phone,
                                familyMembers = familyCountStr.toIntOrNull() ?: 1
                            )
                            val newTenants = building.tenants + newTenant
                            onUpdateBuilding(building.copy(tenants = newTenants))
                            showAddTenantDialog = false
                            preselectedRoomToAddTenant = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        disabledContainerColor = Color(0xFF222238)
                    )
                ) {
                    Text("Add Tenant", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddTenantDialog = false
                    preselectedRoomToAddTenant = null
                }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // --- DIALOG: LOG RENT PAYMENT ---
    if (showLogPaymentDialog) {
        var selectedTenantId by remember { mutableStateOf(building.tenants.firstOrNull()?.id ?: "") }
        val selectedTenant = building.tenants.find { it.id == selectedTenantId }
        val correspondingRoom = selectedTenant?.roomId ?: ""

        var month by remember { mutableStateOf("July 2024") }
        val standardRentAmount = if (correspondingRoom.isNotBlank()) building.getRentForRoom(correspondingRoom) else 10000.0

        var amountPaidStr by remember { mutableStateOf(standardRentAmount.toInt().toString()) }
        var advancePaidStr by remember { mutableStateOf("0") }
        var isPaid by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showLogPaymentDialog = false },
            title = { Text("Log Monthly Rent", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF141424),
            text = {
                if (building.tenants.isEmpty()) {
                    Text("No active tenants to log rent for! Please add a tenant first.", color = Color.LightGray)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text("Select Tenant:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(building.tenants) { tenant ->
                                val isSelected = selectedTenantId == tenant.id
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFF10B981) else Color(0xFF1C1C2C),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedTenantId = tenant.id
                                            val tRoom = tenant.roomId
                                            amountPaidStr = building.getRentForRoom(tRoom).toInt().toString()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "${tenant.name} (${tenant.roomId})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = month,
                            onValueChange = { month = it },
                            label = { Text("Month & Year") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = amountPaidStr,
                            onValueChange = { amountPaidStr = it },
                            label = { Text("Amount Paid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = advancePaidStr,
                            onValueChange = { advancePaidStr = it },
                            label = { Text("Advance Paid (if any)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isPaid,
                                onCheckedChange = { isPaid = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as fully paid", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                if (building.tenants.isNotEmpty()) {
                    val paidVal = amountPaidStr.toDoubleOrNull()
                    val advVal = advancePaidStr.toDoubleOrNull()
                    val isPaymentInputValid = paidVal != null && paidVal >= 0.0 && advVal != null && advVal >= 0.0

                    Button(
                        enabled = isPaymentInputValid,
                        onClick = {
                            if (isPaymentInputValid) {
                                val pVal = paidVal!!
                                val aVal = advVal!!
                                val dueVal = (standardRentAmount - pVal).coerceAtLeast(0.0)

                                val newPayment = Payment(
                                    tenantId = selectedTenantId,
                                    roomId = correspondingRoom,
                                    month = month,
                                    amountPaid = pVal,
                                    amountDue = dueVal,
                                    advancePaid = aVal,
                                    isPaid = isPaid,
                                    paidDate = if (isPaid) "2024-07-10" else ""
                                )

                                val newPayments = building.payments + newPayment
                                onUpdateBuilding(building.copy(payments = newPayments))
                                showLogPaymentDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color(0xFF222238)
                        )
                    ) {
                        Text("Save Record", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogPaymentDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun TenantSearchResultCard(tenant: Tenant, onRemove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2C)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = tenant.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(text = "Room: ${tenant.roomId} • ${tenant.occupation}", color = Color.LightGray, fontSize = 12.sp)
                Text(text = "Phone: ${tenant.phone}", color = Color(0xFF6366F1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun PaymentRecordRow(payment: Payment, tenantName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2C)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "$tenantName (${payment.roomId})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text(text = "Month: ${payment.month}", color = Color.Gray, fontSize = 11.sp)
                if (payment.advancePaid > 0.0) {
                    Text(text = "Advance: ${payment.advancePaid.toInt()} BDT", color = Color(0xFFA7F3D0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (payment.isPaid) {
                    Text(text = "${payment.amountPaid.toInt()} BDT", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(text = "PAID", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 10.sp)
                } else {
                    Text(text = "Due: ${payment.amountDue.toInt()} BDT", color = Color(0xFFEF4444), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(text = "UNPAID", color = Color(0xFFEF4444), fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
        }
    }
}

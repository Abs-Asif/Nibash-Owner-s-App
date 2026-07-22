package com.nibash.prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

    // Navigation interceptor
    BackHandler {
        onBack()
    }

    // Tenant Search calculation (2.3)
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

    val roomNumbers = remember(building) { building.generateRoomNumbers() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        // --- DETAIL TOP BAR ---
        TopAppBar(
            title = {
                Column {
                    Text(text = building.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text(text = "${building.floorsCount} Floors • ${roomNumbers.size} Units", color = Color.Gray, fontSize = 12.sp)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E2E)),
            actions = {
                // Delete/Reset options if wanted
            }
        )

        // --- BODY ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- SECTION 1: SEARCH TENANTS (2.3) ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Tenant (by Name or Phone)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF4B5563)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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

            // --- SECTION 2: DYNAMIC FLAT/ROOM GRID ---
            Text(
                text = "Building Flats / Rooms",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Let's list flat boxes inside a row wrapping flow
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E2E), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // Legends
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF10B981), RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Occupied", color = Color.Gray, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF6366F1), RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vacant", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                // Grid layout inside column
                val chunkSize = 4
                val chunks = roomNumbers.chunked(chunkSize)
                chunks.forEach { rowRooms ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowRooms.forEach { roomNum ->
                            val tenant = building.tenants.find { it.roomId == roomNum }
                            val isOccupied = tenant != null
                            val rentAmount = building.getRentForRoom(roomNum)
                            val style = building.getStyleForRoom(roomNum)

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        // Just quick trigger dialog or overlay to show style details
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOccupied) Color(0xFF065F46) else Color(0xFF312E81)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = roomNum, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text(text = "${rentAmount.toInt()} B", color = Color.LightGray, fontSize = 10.sp)
                                    Text(text = "${style.bedrooms}B / ${style.bathrooms}T", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                        }
                        // Pad empty space in row
                        if (rowRooms.size < chunkSize) {
                            for (i in 0 until (chunkSize - rowRooms.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 3: QUICK ACTIONS ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddTenantDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Tenant", fontSize = 12.sp)
                }

                Button(
                    onClick = { showLogPaymentDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Rent", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 4: RENT RECORD BOOK (2.2) ---
            Text(
                text = "Rent Record Ledger (Payments)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (building.payments.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
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

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 5: RULES LIST ---
            if (building.rules.isNotEmpty()) {
                Text(
                    text = "Building Codes & Rules",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        building.rules.forEach { rule ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = rule, color = Color.LightGray, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: ADD TENANT (2.1) ---
    if (showAddTenantDialog) {
        var name by remember { mutableStateOf("") }
        var occupation by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var familyCountStr by remember { mutableStateOf("3") }
        var selectedRoom by remember { mutableStateOf(roomNumbers.firstOrNull() ?: "") }
        var isDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddTenantDialog = false },
            title = { Text("Add Tenant to Flat", color = Color.White) },
            containerColor = Color(0xFF1E1E2E),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tenant Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    OutlinedTextField(
                        value = familyCountStr,
                        onValueChange = { familyCountStr = it },
                        label = { Text("Number of Family Members") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                    )

                    // Room Selection dropdown alternative
                    Text("Select Flat/Room:", color = Color.Gray, fontSize = 12.sp)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(roomNumbers) { room ->
                            val isSelected = selectedRoom == room
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) Color(0xFF6366F1) else Color(0xFF2C2C3E),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedRoom = room }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = room, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
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
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("Add Tenant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTenantDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // --- DIALOG: LOG RENT PAYMENT (2.2) ---
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
            title = { Text("Log Monthly Rent", color = Color.White) },
            containerColor = Color(0xFF1E1E2E),
            text = {
                if (building.tenants.isEmpty()) {
                    Text("No active tenants to log rent for! Please add a tenant first.", color = Color.LightGray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Tenant:", color = Color.Gray, fontSize = 12.sp)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(building.tenants) { tenant ->
                                val isSelected = selectedTenantId == tenant.id
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFF10B981) else Color(0xFF2C2C3E),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            selectedTenantId = tenant.id
                                            val tRoom = tenant.roomId
                                            amountPaidStr = building.getRentForRoom(tRoom).toInt().toString()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "${tenant.name} (${tenant.roomId})", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = month,
                            onValueChange = { month = it },
                            label = { Text("Month & Year") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                        )

                        OutlinedTextField(
                            value = amountPaidStr,
                            onValueChange = { amountPaidStr = it },
                            label = { Text("Amount Paid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
                        )

                        OutlinedTextField(
                            value = advancePaidStr,
                            onValueChange = { advancePaidStr = it },
                            label = { Text("Advance Paid (if any)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray)
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
                    Button(
                        onClick = {
                            val paidVal = amountPaidStr.toDoubleOrNull() ?: 0.0
                            val advVal = advancePaidStr.toDoubleOrNull() ?: 0.0
                            val dueVal = (standardRentAmount - paidVal).coerceAtLeast(0.0)

                            val newPayment = Payment(
                                tenantId = selectedTenantId,
                                roomId = correspondingRoom,
                                month = month,
                                amountPaid = paidVal,
                                amountDue = dueVal,
                                advancePaid = advVal,
                                isPaid = isPaid,
                                paidDate = if (isPaid) "2024-07-10" else ""
                            )

                            val newPayments = building.payments + newPayment
                            onUpdateBuilding(building.copy(payments = newPayments))
                            showLogPaymentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Save Record")
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C3E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                Text(text = "Phone: ${tenant.phone}", color = Color(0xFF6366F1), fontSize = 11.sp)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252538)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                    Text(text = "Advance: ${payment.advancePaid.toInt()} BDT", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (payment.isPaid) {
                    Text(text = "${payment.amountPaid.toInt()} BDT", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "PAID", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 10.sp)
                } else {
                    Text(text = "Due: ${payment.amountDue.toInt()} BDT", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "UNPAID", color = Color(0xFFEF4444), fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
        }
    }
}

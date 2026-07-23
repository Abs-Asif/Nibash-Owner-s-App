package com.nibash.prototype.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nibash.prototype.model.Building
import com.nibash.prototype.ui.components.BuildingCanvas

@Composable
fun HomeScreen(
    buildings: List<Building>,
    onBuildingClick: (String) -> Unit,
    onAddBuildingClick: () -> Unit
) {
    // Stats calculation
    val totalBuildings = buildings.size
    val totalRooms = buildings.sumOf { it.generateRoomNumbers().size }
    val totalTenants = buildings.sumOf { it.tenants.size }
    val vacantRooms = (totalRooms - totalTenants).coerceAtLeast(0)
    val vacancyRatePercent = if (totalRooms > 0) (vacantRooms * 100) / totalRooms else 0

    val totalRentCollected = buildings.sumOf { b ->
        b.payments.filter { it.isPaid }.sumOf { it.amountPaid }
    }
    val totalRentDue = buildings.sumOf { b ->
        b.payments.filter { !it.isPaid }.sumOf { it.amountDue }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B14)) // Super dark, rich premium slate
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0B0B14))
                    )
                )
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nibash Owner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Dynamic 3D Property Manager",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA5B4FC),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Decorative user icon or active dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
            }
        }

        // --- DASHBOARD CONTAINER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // --- CAROUSEL TILES (BUILDINGS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your 3D Properties",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (buildings.isNotEmpty()) {
                    Text(
                        text = "$totalBuildings Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF818CF8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (buildings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151522)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0x116366F1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No properties found!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the plus button below to create your custom building. It will be rendered live in high-fidelity isometric 3D.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(buildings) { building ->
                        BuildingCarouselCard(
                            building = building,
                            onClick = { onBuildingClick(building.id) }
                        )
                    }
                }
            }

            // --- STATS OVERVIEW CARD ---
            Text(
                text = "Operational Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatGridItem(
                    title = "Total Buildings",
                    value = "$totalBuildings",
                    icon = Icons.Default.Business,
                    iconColor = Color(0xFF818CF8),
                    modifier = Modifier.weight(1f)
                )

                StatGridItem(
                    title = "Rooms / Flats",
                    value = "$totalRooms",
                    icon = Icons.Default.Room,
                    iconColor = Color(0xFF34D399),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatGridItem(
                    title = "Active Tenants",
                    value = "$totalTenants",
                    icon = Icons.Default.Groups,
                    iconColor = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                )

                StatGridItem(
                    title = "Vacancy Rate",
                    value = "$vacancyRatePercent%",
                    icon = Icons.Default.Apartment,
                    iconColor = Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rent Financial Summary Card (Modern Glassmorphic Ledger Card)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
                    .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Financial Ledger Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Overview of this month's rental income status.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x1A10B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Rent Collected", color = Color.Gray, fontSize = 11.sp)
                                Text("${totalRentCollected.toInt()} BDT", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x1AEF4444), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MoneyOff, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Outstanding Due", color = Color.Gray, fontSize = 11.sp)
                                Text("${totalRentDue.toInt()} BDT", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val totalCalculatedRent = totalRentCollected + totalRentDue
                    val collectedRatio = if (totalCalculatedRent > 0) (totalRentCollected.toFloat() / totalCalculatedRent.toFloat()) else 0f

                    LinearProgressIndicator(
                        progress = { collectedRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFF2C2C3E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Collection Efficiency",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(collectedRatio * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // --- HUGE PLUS BUTTON (FAB-like button) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onAddBuildingClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0x33FFFFFF), CircleShape),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Property",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun BuildingCarouselCard(
    building: Building,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
        modifier = Modifier
            .size(300.dp) // Square that takes a lot of space
            .clickable { onClick() }
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Isometric Building Representation Mini Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0F0F1A))
            ) {
                // RENDER DYNAMIC 3D BUILDING CANVAS INSIDE EACH CAROUSEL CARD
                BuildingCanvas(
                    modifier = Modifier.fillMaxSize(),
                    floorsCount = building.floorsCount,
                    roomsPerFloor = building.roomsPerFloor,
                    floorAdditions = building.floorAdditions
                )

                // Small indicator of floor counts
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color(0xFF6366F1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${building.floorsCount} Floors",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom text area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C2C))
                    .padding(16.dp)
            ) {
                Text(
                    text = building.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${building.systemType} layout",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Rooms: ${building.generateRoomNumbers().size} | Tenants: ${building.tenants.size}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF2C2C4E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Manage",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatGridItem(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141424)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
        }
    }
}

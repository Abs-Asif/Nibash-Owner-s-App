package com.nibash.prototype.ui.screens

import androidx.compose.foundation.background
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
            .background(Color(0xFF0F0F1A))
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F0F1A))
                    )
                )
                .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
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
                    text = "Property & Tenant Management Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA5B4FC)
                )
            }
        }

        // --- DASHBOARD CONTAINER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // --- CAROUSEL TILES (BUILDINGS) ---
            Text(
                text = "Your Properties",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (buildings.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No buildings found!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the plus button below to create your first custom building with isometric 3D live previews.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
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
                text = "Overview Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatGridItem(
                    title = "Total Buildings",
                    value = "$totalBuildings",
                    icon = Icons.Default.Business,
                    iconColor = Color(0xFF818CF8),
                    modifier = Modifier.weight(1f)
                )

                StatGridItem(
                    title = "Flats / Rooms",
                    value = "$totalRooms",
                    icon = Icons.Default.Room,
                    iconColor = Color(0xFF34D399),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // Rent Financial Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B2F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Rent Book Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("Total Collected", color = Color.Gray, fontSize = 11.sp)
                                Text("${totalRentCollected.toInt()} BDT", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoneyOff, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("Outstanding Due", color = Color.Gray, fontSize = 11.sp)
                                Text("${totalRentDue.toInt()} BDT", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- HUGE PLUS BUTTON (FAB-like button) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onAddBuildingClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier
            .width(240.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Isometric Building Representation Mini Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF2E2E3E), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(48.dp)
                )
                // Small indicator of floor counts
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color(0xFF4F46E5), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        text = "${building.floorsCount} Floors",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = building.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = "${building.systemType} layout",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Flats: ${building.generateRoomNumbers().size}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Tenants: ${building.tenants.size}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Manage building",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(18.dp)
                )
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

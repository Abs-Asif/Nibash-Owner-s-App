package com.nibash.prototype.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nibash.prototype.model.FloorAddition
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BuildingCanvas(
    modifier: Modifier = Modifier,
    floorsCount: Int,
    roomsPerFloor: Int,
    floorAdditions: List<FloorAddition> = emptyList()
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E1E2C)) // Dark premium space background
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (floorsCount <= 0) {
                // Draw a simple grid or ground plot if no building floors yet
                drawGroundPlot(size.width, size.height)
                return@Canvas
            }

            val centerX = size.width / 2f
            val centerY = size.height - 80f // near the bottom

            // Slabs dimensions based on room count
            // "When the user adds flat or room number, the building will thicken, but never too much."
            // We cap roomsPerFloor to make sure it doesn't overflow.
            val thicknessMultiplier = (roomsPerFloor.coerceIn(1, 10) - 1) * 12f
            val widthX = 100f + thicknessMultiplier // Left side depth
            val lengthY = 100f + thicknessMultiplier // Right side width
            val floorHeight = 65f

            // Draw Ground plot first
            drawGroundPlot(size.width, size.height)

            // Draw each floor from bottom to top
            for (f in 0 until floorsCount) {
                val zBottom = f * floorHeight
                val zTop = (f + 1) * floorHeight

                val addition = floorAdditions.find { it.floorNumber == f }
                val isParking = addition?.type == "Parking Only"
                val isSingleFlat = addition?.type == "Single Flat"

                if (isParking) {
                    // Draw parking columns instead of solid walls, with a concrete slab ceiling
                    drawParkingFloor(
                        centerX = centerX,
                        centerY = centerY,
                        zBottom = zBottom,
                        zTop = zTop,
                        width = widthX,
                        length = lengthY
                    )
                } else {
                    // Draw regular solid floor
                    drawSolidFloor(
                        centerX = centerX,
                        centerY = centerY,
                        zBottom = zBottom,
                        zTop = zTop,
                        width = widthX,
                        length = lengthY,
                        roomsCount = if (isSingleFlat == true) 1 else roomsPerFloor
                    )
                }

                // Draw Floor level marker tag next to building
                drawFloorTag(
                    centerX = centerX,
                    centerY = centerY,
                    zBottom = zBottom,
                    zTop = zTop,
                    width = widthX
                )
            }

            // Draw Roof elements on top floor
            val roofZ = floorsCount * floorHeight
            drawRoofDecoration(
                centerX = centerX,
                centerY = centerY,
                z = roofZ,
                width = widthX,
                length = lengthY
            )
        }
    }
}

// Helper to convert 3D isometric points to 2D screen coordinates
// x is depth (left), y is width (right), z is height (upwards)
private fun isoProject(
    x: Float,
    y: Float,
    z: Float,
    centerX: Float,
    centerY: Float
): Pair<Float, Float> {
    val angleRad = 30f * (Math.PI / 180f).toFloat()
    val cos30 = cos(angleRad)
    val sin30 = sin(angleRad)

    val screenX = centerX + (y - x) * cos30
    val screenY = centerY - (y + x) * sin30 - z
    return Pair(screenX, screenY)
}

private fun DrawScope.drawGroundPlot(canvasWidth: Float, canvasHeight: Float) {
    val centerX = canvasWidth / 2f
    val centerY = canvasHeight - 80f

    val p1 = isoProject(-160f, -160f, 0f, centerX, centerY)
    val p2 = isoProject(-160f, 160f, 0f, centerX, centerY)
    val p3 = isoProject(160f, 160f, 0f, centerX, centerY)
    val p4 = isoProject(160f, -160f, 0f, centerX, centerY)

    val path = Path().apply {
        moveTo(p1.first, p1.second)
        lineTo(p2.first, p2.second)
        lineTo(p3.first, p3.second)
        lineTo(p4.first, p4.second)
        close()
    }

    // Soft concrete ground
    drawPath(path, Color(0xFF2C2C3D))
    drawPath(path, Color(0xFF4E4E63), style = Stroke(width = 2f))
}

private fun DrawScope.drawSolidFloor(
    centerX: Float,
    centerY: Float,
    zBottom: Float,
    zTop: Float,
    width: Float,
    length: Float,
    roomsCount: Int
) {
    // 3D corners
    // Left Face: (0,0) to (width,0)
    // Right Face: (0,0) to (0,length)
    // Top slab at zTop

    val leftColor = Color(0xFF4A5568) // Dark grey
    val rightColor = Color(0xFF718096) // Light grey
    val topColor = Color(0xFFE2E8F0) // Off white ceiling

    // --- LEFT WALL FACE ---
    val l1 = isoProject(0f, 0f, zBottom, centerX, centerY)
    val l2 = isoProject(width, 0f, zBottom, centerX, centerY)
    val l3 = isoProject(width, 0f, zTop, centerX, centerY)
    val l4 = isoProject(0f, 0f, zTop, centerX, centerY)

    val leftPath = Path().apply {
        moveTo(l1.first, l1.second)
        lineTo(l2.first, l2.second)
        lineTo(l3.first, l3.second)
        lineTo(l4.first, l4.second)
        close()
    }
    drawPath(leftPath, leftColor)

    // --- RIGHT WALL FACE ---
    val r1 = isoProject(0f, 0f, zBottom, centerX, centerY)
    val r2 = isoProject(0f, length, zBottom, centerX, centerY)
    val r3 = isoProject(0f, length, zTop, centerX, centerY)
    val r4 = isoProject(0f, 0f, zTop, centerX, centerY)

    val rightPath = Path().apply {
        moveTo(r1.first, r1.second)
        lineTo(r2.first, r2.second)
        lineTo(r3.first, r3.second)
        lineTo(r4.first, r4.second)
        close()
    }
    drawPath(rightPath, rightColor)

    // --- CEILING / TOP SLAB ---
    val t1 = isoProject(0f, 0f, zTop, centerX, centerY)
    val t2 = isoProject(width, 0f, zTop, centerX, centerY)
    val t3 = isoProject(width, length, zTop, centerX, centerY)
    val t4 = isoProject(0f, length, zTop, centerX, centerY)

    val topPath = Path().apply {
        moveTo(t1.first, t1.second)
        lineTo(t2.first, t2.second)
        lineTo(t3.first, t3.second)
        lineTo(t4.first, t4.second)
        close()
    }
    drawPath(topPath, topColor)

    // Outline
    drawPath(leftPath, Color(0xFF2D3748), style = Stroke(width = 1.5f))
    drawPath(rightPath, Color(0xFF2D3748), style = Stroke(width = 1.5f))
    drawPath(topPath, Color(0xFFCBD5E1), style = Stroke(width = 1.5f))

    // --- DRAW WINDOWS (represents rooms) ---
    // Right wall windows
    val winWidth = 14f
    val winHeight = 20f
    val winColor = Color(0xFF63B3ED) // Beautiful glowing cyan

    val itemsCount = roomsCount.coerceIn(1, 6)
    for (i in 0 until itemsCount) {
        val segmentY = length / (itemsCount + 1)
        val windowY = (i + 1) * segmentY

        // Window points
        val wp1 = isoProject(0f, windowY - winWidth/2, zBottom + 20f, centerX, centerY)
        val wp2 = isoProject(0f, windowY + winWidth/2, zBottom + 20f, centerX, centerY)
        val wp3 = isoProject(0f, windowY + winWidth/2, zBottom + 20f + winHeight, centerX, centerY)
        val wp4 = isoProject(0f, windowY - winWidth/2, zBottom + 20f + winHeight, centerX, centerY)

        val winPath = Path().apply {
            moveTo(wp1.first, wp1.second)
            lineTo(wp2.first, wp2.second)
            lineTo(wp3.first, wp3.second)
            lineTo(wp4.first, wp4.second)
            close()
        }
        drawPath(winPath, winColor)
        drawPath(winPath, Color.White, style = Stroke(width = 1f))
    }

    // Left wall windows (matching symmetry or simple design)
    for (i in 0 until itemsCount) {
        val segmentX = width / (itemsCount + 1)
        val windowX = (i + 1) * segmentX

        val wp1 = isoProject(windowX - winWidth/2, 0f, zBottom + 20f, centerX, centerY)
        val wp2 = isoProject(windowX + winWidth/2, 0f, zBottom + 20f, centerX, centerY)
        val wp3 = isoProject(windowX + winWidth/2, 0f, zBottom + 20f + winHeight, centerX, centerY)
        val wp4 = isoProject(windowX - winWidth/2, 0f, zBottom + 20f + winHeight, centerX, centerY)

        val winPath = Path().apply {
            moveTo(wp1.first, wp1.second)
            lineTo(wp2.first, wp2.second)
            lineTo(wp3.first, wp3.second)
            lineTo(wp4.first, wp4.second)
            close()
        }
        drawPath(winPath, Color(0xFFFEE2E2)) // soft warm light
        drawPath(winPath, Color.White, style = Stroke(width = 1f))
    }
}

private fun DrawScope.drawParkingFloor(
    centerX: Float,
    centerY: Float,
    zBottom: Float,
    zTop: Float,
    width: Float,
    length: Float
) {
    // Parking has concrete columns at corners instead of walls, showing empty space.
    val columnColor = Color(0xFF4A5568)
    val ceilingColor = Color(0xFFCBD5E1)

    // Corner pillar coordinates
    val corners = listOf(
        Pair(0f, 0f),
        Pair(width, 0f),
        Pair(0f, length),
        Pair(width, length),
        Pair(width/2f, 0f),
        Pair(0f, length/2f)
    )

    val pillarSize = 10f

    // Draw pillars
    for (corner in corners) {
        val cx = corner.first
        val cy = corner.second

        // Draw pillar cylinder/box
        val p1 = isoProject(cx - pillarSize/2, cy - pillarSize/2, zBottom, centerX, centerY)
        val p4 = isoProject(cx - pillarSize/2, cy - pillarSize/2, zTop, centerX, centerY)

        // Draw basic column lines for 3D effect
        drawLine(columnColor, start = androidx.compose.ui.geometry.Offset(p1.first, p1.second), end = androidx.compose.ui.geometry.Offset(p4.first, p4.second), strokeWidth = pillarSize)
    }

    // Draw ceiling slab
    val t1 = isoProject(0f, 0f, zTop, centerX, centerY)
    val t2 = isoProject(width, 0f, zTop, centerX, centerY)
    val t3 = isoProject(width, length, zTop, centerX, centerY)
    val t4 = isoProject(0f, length, zTop, centerX, centerY)

    val topPath = Path().apply {
        moveTo(t1.first, t1.second)
        lineTo(t2.first, t2.second)
        lineTo(t3.first, t3.second)
        lineTo(t4.first, t4.second)
        close()
    }
    drawPath(topPath, ceilingColor)
    drawPath(topPath, Color(0xFF4A5568), style = Stroke(width = 1.5f))

    // Simple "PARKING" sign on slab or empty car silhouette inside columns
    val carP1 = isoProject(width/2f, length/2f, zBottom, centerX, centerY)
    drawCircle(Color(0xFF3182CE), radius = 6f, center = androidx.compose.ui.geometry.Offset(carP1.first, carP1.second))
}

private fun DrawScope.drawRoofDecoration(
    centerX: Float,
    centerY: Float,
    z: Float,
    width: Float,
    length: Float
) {
    // Rooftop water tank or access cabin
    val cabinW = width * 0.4f
    val cabinL = length * 0.4f
    val cabinH = 25f

    val zBottom = z
    val zTop = z + cabinH

    val rColor = Color(0xFF319795) // Teal accent for roof cabin

    // Left face
    val l1 = isoProject(0f, 0f, zBottom, centerX, centerY)
    val l2 = isoProject(cabinW, 0f, zBottom, centerX, centerY)
    val l3 = isoProject(cabinW, 0f, zTop, centerX, centerY)
    val l4 = isoProject(0f, 0f, zTop, centerX, centerY)
    val leftPath = Path().apply {
        moveTo(l1.first, l1.second)
        lineTo(l2.first, l2.second)
        lineTo(l3.first, l3.second)
        lineTo(l4.first, l4.second)
        close()
    }
    drawPath(leftPath, rColor)

    // Right face
    val r1 = isoProject(0f, 0f, zBottom, centerX, centerY)
    val r2 = isoProject(0f, cabinL, zBottom, centerX, centerY)
    val r3 = isoProject(0f, cabinL, zTop, centerX, centerY)
    val r4 = isoProject(0f, 0f, zTop, centerX, centerY)
    val rightPath = Path().apply {
        moveTo(r1.first, r1.second)
        lineTo(r2.first, r2.second)
        lineTo(r3.first, r3.second)
        lineTo(r4.first, r4.second)
        close()
    }
    drawPath(rightPath, Color(0xFF2C7A7B))

    // Outline
    drawPath(leftPath, Color.White, style = Stroke(width = 1f))
    drawPath(rightPath, Color.White, style = Stroke(width = 1f))
}

private fun DrawScope.drawFloorTag(
    centerX: Float,
    centerY: Float,
    zBottom: Float,
    zTop: Float,
    width: Float
) {
    // Draws a line from the floor edge outwards to the left, and a marker dot.
    // The leftmost corner of the floor is (width, 0, zMid)
    val zMid = (zBottom + zTop) / 2f
    val edgePt = isoProject(width, 0f, zMid, centerX, centerY)

    val lineLength = 50f
    val endX = edgePt.first - lineLength
    val endY = edgePt.second - 10f

    // Draw indicator line
    drawLine(
        color = Color(0x99FFFFFF),
        start = androidx.compose.ui.geometry.Offset(edgePt.first, edgePt.second),
        end = androidx.compose.ui.geometry.Offset(endX, endY),
        strokeWidth = 1.5f
    )

    // Small indicator dot
    drawCircle(
        color = Color(0xFFF6AD55),
        radius = 4f,
        center = androidx.compose.ui.geometry.Offset(edgePt.first, edgePt.second)
    )

    // Label is handled overlay or simple shape. In high performance Canvas drawing, we can draw a circle or block.
    // We can also overlay actual composables for perfect text rendering, which is much cleaner than Canvas native text paint.
    // So we will keep the connector dot/line here.
}

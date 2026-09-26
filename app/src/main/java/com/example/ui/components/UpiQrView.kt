package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun UpiQrView(
    data: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color(0xFF0F172A)
) {
    // Generate 25x25 grid representation based on data payload
    val grid = remember(data) {
        generateQrGrid(data, 25)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, codeColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = grid.size
            val cellSize = size.width / gridSize

            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (grid[r][c]) {
                        drawRect(
                            color = codeColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }

        // Center UPI Badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, codeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₹",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = codeColor
            )
        }
    }
}

private fun generateQrGrid(content: String, size: Int): Array<BooleanArray> {
    val grid = Array(size) { BooleanArray(size) { false } }

    // Finder patterns in corners (7x7)
    drawFinderPattern(grid, 0, 0)
    drawFinderPattern(grid, size - 7, 0)
    drawFinderPattern(grid, 0, size - 7)

    // Timing lines
    for (i in 7 until size - 7) {
        grid[6][i] = (i % 2 == 0)
        grid[i][6] = (i % 2 == 0)
    }

    // Alignment pattern
    val alignCenter = size - 7
    if (alignCenter > 7) {
        for (r in -2..2) {
            for (c in -2..2) {
                if (r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0)) {
                    grid[alignCenter + r][alignCenter + c] = true
                }
            }
        }
    }

    // Deterministic pseudo-random payload filling based on content
    var hash = content.hashCode().absoluteValue
    val bytes = content.toByteArray()
    var byteIdx = 0

    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip finder patterns
            if (isReservedArea(r, c, size)) continue

            // Combine byte data with coordinate hash
            val b = if (bytes.isNotEmpty()) bytes[byteIdx % bytes.size].toInt() else hash
            byteIdx++
            val bit = (((r * 31 + c * 17 + b + hash) % 3) == 0) || (((r + c + (b % 7)) % 2) == 0)
            grid[r][c] = bit
        }
    }

    return grid
}

private fun drawFinderPattern(grid: Array<BooleanArray>, startR: Int, startC: Int) {
    for (r in 0 until 7) {
        for (c in 0 until 7) {
            if (r == 0 || r == 6 || c == 0 || c == 6) {
                grid[startR + r][startC + c] = true
            } else if (r in 2..4 && c in 2..4) {
                grid[startR + r][startC + c] = true
            } else {
                grid[startR + r][startC + c] = false
            }
        }
    }
}

private fun isReservedArea(r: Int, c: Int, size: Int): Boolean {
    // Top-left finder + separator
    if (r <= 7 && c <= 7) return true
    // Bottom-left finder + separator
    if (r >= size - 8 && c <= 7) return true
    // Top-right finder + separator
    if (r <= 7 && c >= size - 8) return true
    // Center logo area (approx 7x7)
    val mid = size / 2
    if (r in (mid - 3)..(mid + 3) && c in (mid - 3)..(mid + 3)) return true
    return false
}

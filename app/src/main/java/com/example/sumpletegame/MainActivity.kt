package com.example.sumpletegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import java.lang.Double.min
import kotlin.random.Random

// -------------------- DATA CLASSES --------------------
data class Cell(
    val row: Int,
    val col: Int,
    val isSolution: Boolean = false,
    var backgroundColor: Color = Color.Gray
)

data class TotalSums(val rowSums: List<Int>, val columnSums: List<Int>)

data class GameSetUp(
    val grid: List<List<Int>>,
    val cells: List<List<Cell>>,
    val totalSums: TotalSums,
    val victorySet: List<Int>
)

// -------------------- CONSTANTS --------------------
private val CellSize = 56.dp
private val SumCellSize = 56.dp

// -------------------- PARENT --------------------
@Composable
fun ParentComposable() {
    var toggleIsOn by remember { mutableStateOf(true) }
    var isSolutionCorrect by remember { mutableStateOf(true) }
    var gameFinished by remember { mutableStateOf(false) }


    // Game state
    var gameSetup by remember { mutableStateOf(generateSumpleteGrid(7)) }
    var clickedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var erasedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Grid + sums scrollable area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            SumpleteGame(
                toggleIsOn = toggleIsOn,
                size = 7,
                onGameEnd = { correct, finished ->
                    isSolutionCorrect = correct
                    gameFinished = finished
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        GameStatus(isSolutionCorrect = isSolutionCorrect, gameFinished = gameFinished)
        Spacer(modifier = Modifier.height(12.dp))
        ToggleScreen(toggleIsOn = toggleIsOn, onToggleChanged = { toggleIsOn = it })

        // ---- Restart Button ----
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                // Reinitialize everything
                gameSetup = generateSumpleteGrid(7)
                clickedCells = emptySet()
                erasedCells = emptySet()
                isSolutionCorrect = true
                gameFinished = false
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Restart Game (in dev)")
        }
    }
}

// -------------------- GAME LOGIC --------------------
fun generateSumpleteGrid(size: Int): GameSetUp {
    val grid = List(size) { List(size) { Random.nextInt(1, 10) } }
    val cells = List(size) { row ->
        List(size) { col ->
            Cell(row, col, Random.nextBoolean())
        }
    }
    val rowSums = List(size) { row ->
        cells[row].filter { it.isSolution }.sumOf { grid[it.row][it.col] }
    }
    val colSums = List(size) { col ->
        cells.map { it[col] }.filter { it.isSolution }.sumOf { grid[it.row][it.col] }
    }
    val victorySet = List(size) { row ->
        cells[row].filter { it.isSolution }.map { size * it.row + it.col }
    }.flatten()

    return GameSetUp(grid, cells, TotalSums(rowSums, colSums), victorySet)
}

// -------------------- GAME UI --------------------
@Composable
fun SumpleteGame(
    toggleIsOn: Boolean,
    size: Int = 4,
    onGameEnd: (Boolean, Boolean) -> Unit
) {
    val (grid, cells, sums, victorySet) = remember { generateSumpleteGrid(size) }
    val (rowSums, colSums) = sums
    var clickedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var erasedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isSolutionCorrect by remember { mutableStateOf(true) }
    var gameFinished by remember { mutableStateOf(false) }

    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    LaunchedEffect(isSolutionCorrect, gameFinished) {
        onGameEnd(isSolutionCorrect, gameFinished)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Sumplete Game",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        BoxWithConstraints {
            val gridHeight = this.maxHeight - SumCellSize // reserve space for bottom column sums
            val gridWidth = this.maxWidth
            Row {
                // Scrollable grid
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(gridHeight)
                        .verticalScroll(verticalScroll)
                ) {
                    Column(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                        // Grid cells
                        for (i in grid.indices) {
                            Row {
                                for (j in grid[i].indices) {
                                    val index = size * i + j
                                    val cell = cells[i][j]
                                    val backgroundColor =
                                        when {
                                            clickedCells.contains(index) && cell.isSolution -> Color(0xFF4CAF50)
                                            erasedCells.contains(index) && !cell.isSolution -> Color.White
                                            (clickedCells.contains(index) && !cell.isSolution) ||
                                                    (erasedCells.contains(index) && cell.isSolution) -> Color(0xFFE53935)
                                            else -> Color(0xFF9E9E9E)
                                        }

                                    Box(
                                        modifier = Modifier
                                            .size(CellSize)
                                            .padding(2.dp)
                                            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                                            .clickable(enabled = !clickedCells.contains(index)) {
                                                clickedCells =
                                                    if (!clickedCells.contains(index) && toggleIsOn) {
                                                        clickedCells + index
                                                    } else clickedCells

                                                erasedCells =
                                                    if (!erasedCells.contains(index) && !toggleIsOn) {
                                                        erasedCells + index
                                                    } else erasedCells

                                                if ((!cell.isSolution && toggleIsOn) || (cell.isSolution && !toggleIsOn)) {
                                                    isSolutionCorrect = false
                                                    gameFinished = true
                                                }

                                                if (victorySet.sorted() == clickedCells.sorted()) {
                                                    gameFinished = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val textValue = grid[i][j].toString()
                                        Text(
                                            textValue,
                                            fontSize = if (textValue.length > 1) 16.sp else 18.sp,
                                            color = Color.White,//if (backgroundColor == Color.White) Color.Black else Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Row sums (scrollable vertically)
                Column(
                    modifier = Modifier
                        .height(gridHeight)
                        .verticalScroll(verticalScroll)
                ) {
                    for (rowSum in rowSums) {
                        Box(
                            modifier = Modifier
                                .size(SumCellSize)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                rowSum.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

// Bottom column sums (fixed vertically, scrolls horizontally)
            Box(
                modifier = Modifier
                    //.align(Alignment.BottomEnd)
                    .width(gridWidth-SumCellSize)//supposing cells are squares
                    .offset(y = min(gridHeight,SumCellSize*size))
                    .horizontalScroll(horizontalScroll)
            ) {
                Row {
                    for (colSum in colSums) {
                        Box(
                            modifier = Modifier
                                .size(SumCellSize)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                colSum.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------- STATUS --------------------
@Composable
fun GameStatus(isSolutionCorrect: Boolean, gameFinished: Boolean) {
    val statusText = when {
        isSolutionCorrect && !gameFinished -> " "
        isSolutionCorrect && gameFinished -> "🎉 Victory!!"
        else -> "❌ Defeat"
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            statusText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSolutionCorrect) Color(0xFF4CAF50) else Color(0xFFE53935)
        )
    }
}

// -------------------- TOGGLE --------------------
@Composable
fun ToggleScreen(toggleIsOn: Boolean, onToggleChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Erase", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = toggleIsOn,
            onCheckedChange = onToggleChanged
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text("Click", fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

// -------------------- PREVIEW & MAIN --------------------
@Preview(showBackground = true)
@Composable
fun PreviewSumpleteGame() {
    ParentComposable()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ParentComposable()
            }
        }
    }
}

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
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlin.math.min

// -------------------- DATA CLASSES --------------------
data class Cell(
    val row: Int,
    val col: Int,
    val isSolution: Boolean = false
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

    // Game level
    //var size = 12
    var gridSizeInUse by remember { mutableStateOf(12) }              // active grid size
    var pendingGridSize by remember { mutableStateOf(gridSizeInUse) } // dropdown selection

    // Game setup
    var gameSetup by remember { mutableStateOf(generateSumpleteGrid(gridSizeInUse)) }

    // Game state
    var toggleIsOn by remember { mutableStateOf(true) }
    var isSolutionCorrect by remember { mutableStateOf(true) }
    var gameFinished by remember { mutableStateOf(false) }
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
                gameSetup = gameSetup,
                clickedCells = clickedCells,
                erasedCells = erasedCells,
                isSolutionCorrect = isSolutionCorrect,
                gameFinished = gameFinished,
                toggleIsOn = toggleIsOn,
                size = gridSizeInUse,
                onClickedCellsChanged = { clickedCells = it },
                onErasedCellsChanged = { erasedCells = it },
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


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown only updates pendingGridSize
            DropdownMenuExample(
                selectedSize = pendingGridSize,
                onSizeSelected = { newSize -> pendingGridSize = newSize }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Restart button applies pendingGridSize
            Button(onClick = {
                gridSizeInUse = pendingGridSize
                gameSetup = generateSumpleteGrid(gridSizeInUse)
                clickedCells = emptySet()
                erasedCells = emptySet()
                isSolutionCorrect = true
                gameFinished = false
            }) {
                Text("New Game")
            }
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
    gameSetup: GameSetUp,
    clickedCells: Set<Int>,
    erasedCells: Set<Int>,
    isSolutionCorrect: Boolean,
    gameFinished: Boolean,
    toggleIsOn: Boolean,
    size: Int,
    onClickedCellsChanged: (Set<Int>) -> Unit,
    onErasedCellsChanged: (Set<Int>) -> Unit,
    onGameEnd: (Boolean, Boolean) -> Unit
) {
    val (grid, cells, sums, victorySet) = gameSetup
    val (rowSums, colSums) = sums

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


            val gridHeight = min(this.maxHeight.value-SumCellSize.value,size*CellSize.value).dp
            val gridWidth = min(this.maxWidth.value-SumCellSize.value,size*CellSize.value).dp

            Row {
                // Scrollable grid
                Box(
                    modifier = Modifier
                        .width(gridWidth)
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
                                                var newClicked = clickedCells
                                                var newErased = erasedCells
                                                var newCorrect = isSolutionCorrect
                                                var newFinished = gameFinished

                                                if (!clickedCells.contains(index) && toggleIsOn) {
                                                    newClicked = newClicked + index
                                                    onClickedCellsChanged(newClicked)
                                                } else if (!erasedCells.contains(index) && !toggleIsOn) {
                                                    newErased = newErased + index
                                                    onErasedCellsChanged(newErased)
                                                }

                                                if ((!cell.isSolution && toggleIsOn) || (cell.isSolution && !toggleIsOn)) {
                                                    newCorrect = false
                                                    newFinished = true
                                                }

                                                if (victorySet.sorted() == newClicked.sorted()) {
                                                    newFinished = true
                                                }

                                                onGameEnd(newCorrect, newFinished)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val textValue = grid[i][j].toString()
                                        Text(
                                            textValue,
                                            fontSize = if (textValue.length > 1) 16.sp else 18.sp,
                                            color = Color.White, //Always white
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
                        .width(gridWidth)
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
                    .height(gridHeight)
                    .width(gridWidth)
                    .offset(y = gridHeight)
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


@Composable
fun DropdownMenuExample(selectedSize: Int, onSizeSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text("Grid size: $selectedSize")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (i in 3..15) {
                DropdownMenuItem(
                    text = { Text(i.toString()) },
                    onClick = {
                        onSizeSelected(i)
                        expanded = false
                    }
                )
            }
        }
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

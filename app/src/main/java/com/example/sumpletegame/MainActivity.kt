package com.example.sumpletegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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


data class Cell(val row: Int, val col: Int, val isSolution: Boolean = false, var backgroundColor: Color =  Color.Gray)
data class totalSums(val rowSums: List<Int>, val columnSums: List<Int>)
data class GameSetUp(val grid: List<List<Int>>, val cells: List<List<Cell>>, val totalSums: totalSums, val victorySet: List<Int>)

@Composable
fun ParentComposable() {
    var toggleIsOn by remember { mutableStateOf(true) }

    SumpleteGame(toggleIsOn=toggleIsOn,5)
    MaterialTheme {
        ToggleScreen(toggleIsOn=toggleIsOn,
            onToggleChanged = { toggleIsOn = it })
    }
}
//NEXT STEPS



fun generateSumpleteGrid(size: Int): GameSetUp {
    val grid = List(size) { List(size) { Random.nextInt(1, 10) } }
    val cells = List(size) { row ->
        List(size) { col ->
            Cell(row, col, Random.nextBoolean())
        }
    }
    val rowSums = List(size) { row -> cells[row].filter { it.isSolution }.sumOf { grid[it.row][it.col] } }
    val colSums = List(size) { col -> cells.map { it[col] }.filter { it.isSolution }.sumOf { grid[it.row][it.col] } }
    val victorySet=List(size) { row -> cells[row].filter { it.isSolution }.map{ size*it.row+it.col} }.flatten()
    return GameSetUp(grid, cells, totalSums(rowSums, colSums),victorySet)
}

@Composable
fun SumpleteGame(toggleIsOn: Boolean, size: Int = 4) {
    val (grid, cells, sums,victorySet) = remember { generateSumpleteGrid(size) }
    val (rowSums, colSums) = sums
    val isSolutionCorrect = remember { mutableStateOf(true) }
    val gameFinished = remember { mutableStateOf(false) }
    var clickedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var erasedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sumplete Game", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Column {
            for (i in grid.indices) {
                Row {
                    for (j in grid[i].indices) {
                        val index=size*i+j//Take care if the grid is not a square
                        val cell = cells[i][j]
                        var backgroundColor = if (clickedCells.contains(index) && cell.isSolution) {Color.Green }
                        else if (erasedCells.contains(index) &&   !cell.isSolution){Color.White}
                        else if ((clickedCells.contains(index) && !cell.isSolution)||(erasedCells.contains(index) && cell.isSolution)){Color.Red}
                        else Color.Gray

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable(enabled = !clickedCells.contains(index)) {
                                    //cell.clicked = true
                                    clickedCells = if(!clickedCells.contains(index) && toggleIsOn) {
                                        clickedCells + index
                                    }
                                    else
                                    {clickedCells}
                                    erasedCells = if(!erasedCells.contains(index) && !toggleIsOn) {
                                        erasedCells + index
                                    }
                                    else
                                    {erasedCells}
                                    if ((!cell.isSolution && toggleIsOn) || (cell.isSolution && !toggleIsOn)) {
                                        isSolutionCorrect.value = false
                                        gameFinished.value = true
                                    }
                                    if (victorySet.sorted()==clickedCells.sorted()) {//it remains to detect the victory in the game
                                        gameFinished.value = true
                                    }
                                }
                                .background(backgroundColor, shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(grid[i][j].toString(), fontSize = 20.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(rowSums[i].toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    //println(victorySet.sorted())
                    //println(clickedCells.sorted())
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                for (colSum in colSums) {
                    Text(colSum.toString()+"     ", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(if (isSolutionCorrect.value && !gameFinished.value)"You are on the way to win" else if (isSolutionCorrect.value && gameFinished.value)"Victory!!" else "Defeat", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isSolutionCorrect.value) Color.Green else Color.Red)
    }
}


//fun checkSolution(grid: Array<IntArray>, rowSums: IntArray, colSums: IntArray, clickedCells: List<Cell>): Boolean {
//   val expectedCells = mutableSetOf<Cell>()
//  for (i in grid.indices) {
//    for (j in grid[i].indices) {
//       if (rowSums[i] > 0 && colSums[j] > 0 && grid[i][j] <= rowSums[i] && grid[i][j] <= colSums[j]) {
//          expectedCells.add(Cell(i, j))
//     }
// }
// }
// return clickedCells.toSet() == expectedCells
//}


@Composable
fun ToggleScreen(toggleIsOn: Boolean, onToggleChanged: (Boolean) -> Unit) {

    Box( modifier = Modifier

        .fillMaxSize()
        .padding(80.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            //Spacer(modifier = Modifier.height(2000.dp))

            Text("Erase",
                fontSize = 28.sp )

            Spacer(modifier = Modifier.width(12.dp))


            Switch(
                checked = toggleIsOn,
                onCheckedChange = onToggleChanged
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Label always visible on the right
            Text("Click",
                fontSize = 28.sp )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSumpleteGame() {
    ParentComposable()
}

// Launch the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParentComposable()
        }
    }
}


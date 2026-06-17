package ru.slovodel.scorekeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import ru.slovodel.scorekeeper.ui.GameController
import ru.slovodel.scorekeeper.ui.SlovodelApp
import ru.slovodel.scorekeeper.ui.theme.SlovodelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val controller = remember { GameController(applicationContext) }
            SlovodelTheme {
                SlovodelApp(controller)
            }
        }
    }
}

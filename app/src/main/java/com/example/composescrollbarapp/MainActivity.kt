package com.example.composescrollbarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composescrollbarapp.ui.theme.ComposeScrollBarAppTheme
import com.justin.composescrollbar.CustomScrollBar
import com.justin.composescrollbar.ThumbProperties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeScrollBarAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val itemList: List<Int> = (1..100).toList()
                    val listState = rememberLazyListState()
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(state = listState) {
                            items(itemList) { item ->
                                ColumnItem(item)
                            }
                        }
                        CustomScrollBar(
                            listState = listState,
                            thumbProperties = ThumbProperties(
                                thumbColor = Color.Cyan,
                                thumbSelectedColor = Color.Blue,
                            ),
                        ){
                            Box(modifier = Modifier
                                .size(60.dp)
                                .background(Color.Red)) {
                                
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ColumnItem(values: Int) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textAlign = TextAlign.Center,
            text = "Current Value is : $values"
        )
    }
}
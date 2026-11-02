package com.teddolap.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F7F1)).padding(24.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E7A4C)), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("TED-Dolap", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Evdeki israfa son. Uygulama telefonda basariyla calisiyor!", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

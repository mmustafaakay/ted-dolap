package com.teddolap.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val GreenPrimary = Color(0xFF1E7A4C)
val GreenSoft = Color(0xFFDCEFE2)
val WarnOrange = Color(0xFFB86F05)
val BadRed = Color(0xFFBE3D2A)
val AppBg = Color(0xFFF3F7F1)

data class DolapUrun(val ad: String, val miktar: String, val kalanGun: Int, val dolap: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("ted_dolap_prefs", Context.MODE_PRIVATE) }
            
            var apiKey by remember { mutableStateOf(prefs.getString("gemini_key", "") ?: "") }
            var selectedTab by remember { mutableStateOf(0) } // 0: Ana Sayfa, 1: AI Şef, 2: Ayarlar

            var urunler by remember {
                mutableStateOf(
                    listOf(
                        DolapUrun("Süt (1L)", "1 adet", 1, "Buzdolabı"),
                        DolapUrun("Domates", "4 adet", 2, "Buzdolabı"),
                        DolapUrun("Yumurta", "6 adet", 4, "Buzdolabı"),
                        DolapUrun("Yoğurt", "1 kova", 3, "Buzdolabı"),
                        DolapUrun("Bayat Ekmek", "3 dilim", 1, "Mutfak")
                    )
                )
            }

            Scaffold(
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Kitchen, contentDescription = null) },
                            label = { Text("Dolabım") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                            label = { Text("Yapay Zekâ") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Ayarlar") }
                        )
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBg)
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> HomeTab(urunler = urunler, onUrunGuncelle = { urunler = it })
                        1 -> AiChefTab(apiKey = apiKey, urunler = urunler, onGoSettings = { selectedTab = 2 })
                        2 -> SettingsTab(
                            currentKey = apiKey,
                            onSaveKey = { newKey ->
                                apiKey = newKey
                                prefs.edit().putString("gemini_key", newKey).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}

// 1. DOLABIM SEKME EKRANI
@Composable
fun HomeTab(urunler: List<DolapUrun>, onUrunGuncelle: (List<DolapUrun>) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column {
                Text("TED-Dolap", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                Text("Evdeki israfa son · Aile Takibi", fontSize = 13.sp, color = Color.Gray)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Son 30 Günde Kurtarılan", color = Color.White.copy(0.85f), fontSize = 12.sp)
                        Text("540 ₺", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Text("12 ürün zamanında değerlendirildi", color = Color.White.copy(0.9f), fontSize = 12.sp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(64.dp).background(Color.White.copy(0.2f), RoundedCornerShape(32.dp))
                    ) {
                        Text("%92", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
        item {
            Text("Önce Bunları Tüket (Tarihi Yaklaşanlar)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(urunler) { urun ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(urun.ad, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${urun.dolap} · ${urun.miktar}", color = Color.Gray, fontSize = 12.sp)
                        }
                        val color = if (urun.kalanGun <= 2) BadRed else WarnOrange
                        Text("${urun.kalanGun} gün kaldı", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onUrunGuncelle(urunler.filter { it != urun }) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSoft, contentColor = GreenPrimary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Değerlendirdim", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onUrunGuncelle(urunler.filter { it != urun }) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9E1DB), contentColor = BadRed),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Çöpe Gitti", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 2. YAPAY ZEKÂ ŞEF SEKME EKRANI (GEMINI ENTEGRASYONU)
@Composable
fun AiChefTab(apiKey: String, urunler: List<DolapUrun>, onGoSettings: () -> Unit) {
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Yapay Zekâ İsraf Savaşçısı", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
        Text(
            "Tarihi yaklaşan ürünlerinizi analiz eder ve israfı önleyecek pratik ev yemekleri önerir.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        if (apiKey.isBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("API Anahtarı Gerekli", fontWeight = FontWeight.Bold, color = WarnOrange)
                    Text("Gemini yapay zekâsını kullanabilmek için Ayarlar ekranından API anahtarınızı girin.", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onGoSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = WarnOrange)
                    ) {
                        Text("Ayarlara Git")
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = ""
                    responseText = ""

                    scope.launch {
                        try {
                            val prompt = """
                                Sen gıda israfını önleyen usta bir Türk aşçısısın.
                                Dolabımda tarihi yaklaşan şu ürünler var:
                                ${urunler.joinToString("\n") { "- ${it.ad} (${it.miktar}), ${it.kalanGun} gün kaldı" }}
                                
                                Lütfen bu ürünleri kurtaracak, evdeki temel malzemelerle (tuz, yağ, un, soğan vs.) yapılabilecek 2 pratik yemek veya tatlı öner.
                                Her tarif için:
                                1. Yemek Adı
                                2. Hangi tarihi yaklaşan ürünleri değerlendirdiği
                                3. 3-4 adımda pratik hazırlanışı
                                Samimi ve teşvik edici bir dille yaz.
                            """.trimIndent()

                            val result = withContext(Dispatchers.IO) {
                                val model = GenerativeModel(
                                    modelName = "gemini-1.5-flash",
                                    apiKey = apiKey.trim()
                                )
                                model.generateContent(prompt).text ?: "Yanıt alınamadı."
                            }
                            responseText = result
                        } catch (e: Exception) {
                            errorMessage = "Hata oluştu: ${e.localizedMessage ?: "Bilinmeyen hata. Anahtarınızı kontrol edin."}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Şef Düşünüyor...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bugün Ne Pişirsem? (Gemini'ye Sor)")
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = BadRed, fontSize = 13.sp)
            }

            if (responseText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        item {
                            Text(responseText, fontSize = 14.sp, lineHeight = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

// 3. AYARLAR SEKME EKRANI (GÜVENLİ API KEY GİRİŞİ)
@Composable
fun SettingsTab(currentKey: String, onSaveKey: (String) -> Unit) {
    var keyInput by remember { mutableStateOf(currentKey) }
    var isSaved by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ayarlar", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Google Gemini API Anahtarı", fontWeight = FontWeight.Bold)
                Text(
                    "Anahtarınız sadece telefonunuzda güvenle saklanır, asla GitHub veya üçüncü taraf sunuculara gitmez.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        isSaved = false
                    },
                    label = { Text("API Anahtarı (AIzaSy...)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        onSaveKey(keyInput.trim())
                        isSaved = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Anahtarı Kaydet")
                }

                if (isSaved) {
                    Text("✓ API Anahtarı başarıyla kaydedildi!", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Ücretsiz Gemini API Anahtarı Nasıl Alınır?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("1. aistudio.google.com adresine gidin.", fontSize = 12.sp, color = Color.DarkGray)
                Text("2. Google hesabınızla giriş yapıp 'Get API key' deyin.", fontSize = 12.sp, color = Color.DarkGray)
                Text("3. Aldığınız anahtarı yukarıdaki kutuya yapıştırıp kaydedin.", fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

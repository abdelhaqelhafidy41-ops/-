package com.example.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.NoorMuslimApp
import com.example.data.local.entity.WidgetConfigEntity
import com.example.data.local.preferences.CalculationMethod
import com.example.ui.theme.NoorMuslimTheme
import kotlinx.coroutines.launch

class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            NoorMuslimTheme {
                Scaffold { innerPadding ->
                    WidgetConfigureScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSaveConfig = { cityName, lat, lon, method ->
                            saveAndFinish(cityName, lat, lon, method)
                        }
                    )
                }
            }
        }
    }

    private fun saveAndFinish(cityName: String, lat: Double, lon: Double, method: CalculationMethod) {
        val app = application as? NoorMuslimApp
        val container = app?.container
        val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

        coroutineScope.launch {
            container?.widgetConfigRepository?.saveConfig(
                WidgetConfigEntity(
                    appWidgetId = appWidgetId,
                    widgetType = "prayer_configured",
                    cityName = cityName,
                    latitude = lat,
                    longitude = lon,
                    calculationMethod = method.name,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            WidgetUpdateCoordinator.updateAllWidgets(this@WidgetConfigureActivity)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}

data class CityOption(val name: String, val lat: Double, val lon: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigureScreen(
    modifier: Modifier = Modifier,
    onSaveConfig: (cityName: String, lat: Double, lon: Double, method: CalculationMethod) -> Unit
) {
    val cities = listOf(
        CityOption("مكة المكرمة", 21.4225, 39.8262),
        CityOption("المدينة المنورة", 24.4672, 39.6111),
        CityOption("الرياض", 24.7136, 46.6753),
        CityOption("القاهرة", 30.0444, 31.2357),
        CityOption("القدس الشريف", 31.7683, 35.2137),
        CityOption("إسطنبول", 41.0082, 28.9784),
        CityOption("دبي", 25.2048, 55.2708),
        CityOption("عمان", 31.9539, 35.9106)
    )

    var selectedCity by remember { mutableStateOf(cities[0]) }
    var selectedMethod by remember { mutableStateOf(CalculationMethod.UMM_AL_QURA) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "تخصيص ويدجت الصلاة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "اختر المدينة وطريقة الحساب المفضلة لهذا الويدجت",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // City Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "المدينة المختارة",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                cities.forEach { city ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCity == city,
                            onClick = { selectedCity = city }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = city.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculation Method Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "طريقة الحساب",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                CalculationMethod.values().forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = method.labelArabic, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSaveConfig(selectedCity.name, selectedCity.lat, selectedCity.lon, selectedMethod)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("widget_save_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حفظ وإضافة الويدجت", fontWeight = FontWeight.Bold)
        }
    }
}

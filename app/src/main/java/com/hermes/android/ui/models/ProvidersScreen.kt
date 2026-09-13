package com.hermes.android.ui.models

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.android.backend.TermuxDetector
import com.hermes.android.backend.TermuxServiceStatus
import com.hermes.android.data.models.ProviderConfig
import com.hermes.android.data.models.ProviderRepository
import com.hermes.android.data.models.ProviderType
import com.hermes.android.ui.theme.HermesTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen() {
    val context = LocalContext.current
    val repository = remember { ProviderRepository(context) }
    val detector = remember { TermuxDetector(context) }
    val providers by repository.providers.collectAsState()
    val activeModelId by repository.activeModelId.collectAsState()
    val activeProviderId by repository.activeProviderId.collectAsState()
    val termuxStatus by detector.status.collectAsState()

    val scope = rememberCoroutineScope()
    var showAddProviderDialog by remember { mutableStateOf(false) }
    var targetProviderForCustomModel by remember { mutableStateOf<String?>(null) }
    var customModelInput by remember { mutableStateOf("") }
    var fetchingProviderId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        detector.probe()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProviderDialog = true },
                containerColor = HermesTokens.Emerald,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Provider")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "AI Providers & Models",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HermesTokens.TextPrimary
                        )
                    )
                    Text(
                        "Connect freely to any provider or local model without limits",
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Termux Connection Banner
            TermuxStatusCard(
                status = termuxStatus,
                onRefresh = { scope.launch { detector.probe() } },
                onOpenTermux = { detector.openTermux() }
            )

            if (statusMessage.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(HermesTokens.RadiusS),
                    color = HermesTokens.CardElevated,
                    border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        statusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.Amber),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Providers List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        activeModelId = activeModelId,
                        isCurrentProvider = activeProviderId == provider.id,
                        isFetching = fetchingProviderId == provider.id,
                        onSelectModel = { modelId ->
                            repository.setActiveModel(provider.id, modelId)
                            statusMessage = "Selected model: $modelId"
                        },
                        onAddCustomModel = {
                            targetProviderForCustomModel = provider.id
                            customModelInput = ""
                        },
                        onFetchFromApi = {
                            scope.launch {
                                fetchingProviderId = provider.id
                                statusMessage = "Fetching models from ${provider.name}…"
                                val res = repository.fetchModelsFromEndpoint(provider.id)
                                fetchingProviderId = null
                                res.onSuccess {
                                    statusMessage = "Loaded ${it.size} models from ${provider.name} ✓"
                                }.onFailure {
                                    statusMessage = "Failed: ${it.message}"
                                }
                            }
                        },
                        onDelete = {
                            repository.deleteProvider(provider.id)
                        }
                    )
                }
            }
        }
    }

    // Add Custom Model Dialog
    if (targetProviderForCustomModel != null) {
        AlertDialog(
            onDismissRequest = { targetProviderForCustomModel = null },
            title = { Text("Add Custom Model") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter any model identifier supported by this provider (no limits):",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customModelInput,
                        onValueChange = { customModelInput = it },
                        placeholder = { Text("e.g. gpt-4o, claude-3-7-sonnet, qwen2.5-coder") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(HermesTokens.RadiusS)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customModelInput.isNotBlank()) {
                            repository.addCustomModel(targetProviderForCustomModel!!, customModelInput.trim())
                            repository.setActiveModel(targetProviderForCustomModel!!, customModelInput.trim())
                            statusMessage = "Added & activated custom model: $customModelInput"
                            targetProviderForCustomModel = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)
                ) {
                    Text("Add & Select", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { targetProviderForCustomModel = null }) { Text("Cancel") }
            }
        )
    }

    // Add Provider Dialog
    if (showAddProviderDialog) {
        AddProviderDialog(
            onDismiss = { showAddProviderDialog = false },
            onAdd = { name, baseUrl, apiKey, type ->
                val p = repository.addProvider(name, baseUrl, apiKey, type)
                statusMessage = "Added provider: ${p.name}"
                showAddProviderDialog = false
            }
        )
    }
}

@Composable
private fun TermuxStatusCard(
    status: TermuxServiceStatus,
    onRefresh: () -> Unit,
    onOpenTermux: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HermesTokens.RadiusM),
        colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark),
        border = BorderStroke(1.dp, HermesTokens.BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                is TermuxServiceStatus.Active -> HermesTokens.Emerald.copy(alpha = 0.2f)
                                is TermuxServiceStatus.InstalledIdle -> HermesTokens.Amber.copy(alpha = 0.2f)
                                else -> HermesTokens.CardElevated
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = null,
                        tint = when (status) {
                            is TermuxServiceStatus.Active -> HermesTokens.Emerald
                            is TermuxServiceStatus.InstalledIdle -> HermesTokens.Amber
                            else -> HermesTokens.TextMuted
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Termux Environment",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = HermesTokens.TextPrimary
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        val badgeText = when (status) {
                            is TermuxServiceStatus.Active -> "LINKED"
                            is TermuxServiceStatus.InstalledIdle -> "INSTALLED"
                            TermuxServiceStatus.Checking -> "CHECKING"
                            TermuxServiceStatus.NotInstalled -> "NOT FOUND"
                        }
                        val badgeColor = when (status) {
                            is TermuxServiceStatus.Active -> HermesTokens.Emerald
                            is TermuxServiceStatus.InstalledIdle -> HermesTokens.Amber
                            else -> HermesTokens.TextMuted
                        }
                        Surface(
                            shape = RoundedCornerShape(HermesTokens.RadiusPill),
                            color = badgeColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        when (status) {
                            is TermuxServiceStatus.Active -> "${status.serviceName} listening on ${status.endpoint}"
                            is TermuxServiceStatus.InstalledIdle -> "Termux is installed. Tap Launch to start Hermes."
                            TermuxServiceStatus.Checking -> "Probing local ports…"
                            TermuxServiceStatus.NotInstalled -> "Install Termux to run Hermes locally on-device."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                    )
                }
            }

            Row {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = HermesTokens.TextMuted)
                }
                if (status is TermuxServiceStatus.InstalledIdle) {
                    Button(
                        onClick = onOpenTermux,
                        colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.CardElevated)
                    ) {
                        Text("Launch", style = MaterialTheme.typography.labelSmall, color = HermesTokens.Emerald)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: ProviderConfig,
    activeModelId: String,
    isCurrentProvider: Boolean,
    isFetching: Boolean,
    onSelectModel: (String) -> Unit,
    onAddCustomModel: () -> Unit,
    onFetchFromApi: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HermesTokens.RadiusM),
        colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark),
        border = BorderStroke(
            1.dp,
            if (isCurrentProvider) HermesTokens.Emerald.copy(alpha = 0.7f) else HermesTokens.BorderSubtle
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (provider.isTermuxLocal) Icons.Default.Terminal else Icons.Default.Cloud,
                        contentDescription = null,
                        tint = if (provider.isTermuxLocal) HermesTokens.Amber else HermesTokens.Cyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        provider.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = HermesTokens.TextPrimary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = HermesTokens.Emerald,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        IconButton(onClick = onFetchFromApi, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Fetch Models",
                                tint = HermesTokens.TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (!provider.isTermuxLocal) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = HermesTokens.Error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Text(
                provider.baseUrl,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = HermesTokens.TextMuted,
                    fontSize = 11.sp
                )
            )

            Spacer(Modifier.height(10.dp))

            // Models Row
            Text(
                "MODELS (${provider.models.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = HermesTokens.TextMuted,
                    fontSize = 10.sp
                )
            )

            Spacer(Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(provider.models, key = { it.id }) { model ->
                    val isSelected = isCurrentProvider && activeModelId == model.id
                    Surface(
                        shape = RoundedCornerShape(HermesTokens.RadiusPill),
                        color = if (isSelected) HermesTokens.Emerald else HermesTokens.CardElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HermesTokens.Emerald else HermesTokens.BorderSubtle
                        ),
                        modifier = Modifier.clickable { onSelectModel(model.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                model.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.Black else HermesTokens.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Add custom model pill
                item {
                    Surface(
                        shape = RoundedCornerShape(HermesTokens.RadiusPill),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, HermesTokens.Amber.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onAddCustomModel() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = HermesTokens.Amber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "+ Custom Model",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = HermesTokens.Amber,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProviderDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, baseUrl: String, apiKey: String, type: ProviderType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProviderType.OPENAI_COMPATIBLE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Provider") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider Name") },
                    placeholder = { Text("e.g. My Local Gateway, DeepSeek, vLLM") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(HermesTokens.RadiusS)
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    placeholder = { Text("e.g. http://127.0.0.1:8000/v1") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(HermesTokens.RadiusS)
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(HermesTokens.RadiusS)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank()) {
                        onAdd(name, baseUrl, apiKey, selectedType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.Emerald)
            ) {
                Text("Add Provider", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

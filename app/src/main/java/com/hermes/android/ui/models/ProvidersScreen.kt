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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.hermes.android.data.models.ProviderModel
import com.hermes.android.data.models.ProviderRepository
import com.hermes.android.data.models.ProviderType
import com.hermes.android.ui.theme.HermesTokens
import kotlinx.coroutines.launch

/**
 * Minis-Style Provider & Model Management Screen:
 * Exact visual template from Minis with obsidian dark theme & emerald/cyan accents.
 */
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
    var searchQuery by remember { mutableStateOf("") }
    var showAddProviderDialog by remember { mutableStateOf(false) }
    var targetProviderForCustomModel by remember { mutableStateOf<ProviderConfig?>(null) }
    var customModelIdInput by remember { mutableStateOf("") }
    var customModelNameInput by remember { mutableStateOf("") }
    var testingProviderId by remember { mutableStateOf<String?>(null) }
    var fetchingProviderId by remember { mutableStateOf<String?>(null) }
    var bannerMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        detector.probe()
    }

    Scaffold(
        containerColor = HermesTokens.BgDark,
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
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Minis-Style Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Providers",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = HermesTokens.TextPrimary
                        )
                    )
                    Text(
                        "LLM endpoints, authentication & model routing",
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(HermesTokens.RadiusPill),
                    color = HermesTokens.Emerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, HermesTokens.Emerald.copy(alpha = 0.3f))
                ) {
                    Text(
                        "${providers.size} INSTANCES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = HermesTokens.Emerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Search Filter Bar (Minis Style)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search providers and models…", color = HermesTokens.TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HermesTokens.TextMuted, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(HermesTokens.RadiusM),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // Termux Local Environment Banner
            TermuxEnvironmentCard(
                status = termuxStatus,
                onRefresh = { scope.launch { detector.probe() } },
                onOpenTermux = { detector.openTermux() }
            )

            if (bannerMessage.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(HermesTokens.RadiusS),
                    color = HermesTokens.CardElevated,
                    border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        bannerMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.Amber),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Filtered Provider List
            val filteredProviders = if (searchQuery.isBlank()) providers else {
                providers.filter { p ->
                    p.label.contains(searchQuery, ignoreCase = true) ||
                        p.customBaseURL.contains(searchQuery, ignoreCase = true) ||
                        p.models.any { m -> m.displayName.contains(searchQuery, ignoreCase = true) || m.modelId.contains(searchQuery, ignoreCase = true) }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredProviders, key = { it.id }) { provider ->
                    MinisProviderCard(
                        provider = provider,
                        activeModelId = activeModelId,
                        isCurrentActiveProvider = activeProviderId == provider.id,
                        isTesting = testingProviderId == provider.id,
                        isFetching = fetchingProviderId == provider.id,
                        onToggle = {
                            repository.toggleProvider(provider.id)
                        },
                        onSelectModel = { modelId ->
                            repository.setActiveModel(provider.id, modelId)
                            bannerMessage = "Active model set to: $modelId"
                        },
                        onTestLatency = {
                            scope.launch {
                                testingProviderId = provider.id
                                val ping = repository.pingProvider(provider.id)
                                testingProviderId = null
                                bannerMessage = if (ping != null && ping > 0) {
                                    "${provider.label}: ⚡ Ping successful (${ping}ms)"
                                } else {
                                    "${provider.label}: ⚠ Probe failed"
                                }
                            }
                        },
                        onFetchModels = {
                            scope.launch {
                                fetchingProviderId = provider.id
                                val res = repository.fetchModelsFromEndpoint(provider.id)
                                fetchingProviderId = null
                                res.onSuccess {
                                    bannerMessage = "Discovered ${it.size} models from ${provider.label} ✓"
                                }.onFailure {
                                    bannerMessage = "Discovery error: ${it.message}"
                                }
                            }
                        },
                        onAddCustomModel = {
                            targetProviderForCustomModel = provider
                            customModelIdInput = ""
                            customModelNameInput = ""
                        },
                        onDelete = {
                            repository.deleteProvider(provider.id)
                            bannerMessage = "Removed provider: ${provider.label}"
                        }
                    )
                }
            }
        }
    }

    // Minis Add Custom Model Dialog
    if (targetProviderForCustomModel != null) {
        val target = targetProviderForCustomModel!!
        AlertDialog(
            onDismissRequest = { targetProviderForCustomModel = null },
            containerColor = HermesTokens.SurfaceDark,
            title = {
                Text(
                    "Register Model · ${target.label}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HermesTokens.TextPrimary)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Add any model ID without restrictions to route calls through this provider:",
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted)
                    )
                    OutlinedTextField(
                        value = customModelIdInput,
                        onValueChange = { customModelIdInput = it },
                        label = { Text("Model ID (required)") },
                        placeholder = { Text("e.g. gpt-4o, claude-3-7-sonnet, qwen2.5-coder") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(HermesTokens.RadiusS)
                    )
                    OutlinedTextField(
                        value = customModelNameInput,
                        onValueChange = { customModelNameInput = it },
                        label = { Text("Display Name (optional)") },
                        placeholder = { Text("e.g. Qwen 2.5 Coder 32B") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(HermesTokens.RadiusS)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customModelIdInput.isNotBlank()) {
                            repository.addCustomModel(
                                providerId = target.id,
                                modelId = customModelIdInput.trim(),
                                displayName = customModelNameInput.trim().ifBlank { customModelIdInput.trim() }
                            )
                            repository.setActiveModel(target.id, customModelIdInput.trim())
                            bannerMessage = "Added & activated model: $customModelIdInput"
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

    // Minis Add Provider Dialog
    if (showAddProviderDialog) {
        MinisAddProviderDialog(
            onDismiss = { showAddProviderDialog = false },
            onAdd = { label, url, key, type, appendV1 ->
                val p = repository.addProvider(label, url, key, type, appendV1)
                bannerMessage = "Created provider instance: ${p.label}"
                showAddProviderDialog = false
            }
        )
    }
}

/**
 * Minis-styled Provider Card Component
 */
@Composable
private fun MinisProviderCard(
    provider: ProviderConfig,
    activeModelId: String,
    isCurrentActiveProvider: Boolean,
    isTesting: Boolean,
    isFetching: Boolean,
    onToggle: () -> Unit,
    onSelectModel: (String) -> Unit,
    onTestLatency: () -> Unit,
    onFetchModels: () -> Unit,
    onAddCustomModel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HermesTokens.RadiusM),
        colors = CardDefaults.cardColors(containerColor = HermesTokens.CardDark),
        border = BorderStroke(
            1.dp,
            if (isCurrentActiveProvider) HermesTokens.Emerald.copy(alpha = 0.8f) else HermesTokens.BorderSubtle
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name, Type, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minis Monogram Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    provider.isTermuxLocal -> HermesTokens.Amber.copy(alpha = 0.2f)
                                    isCurrentActiveProvider -> HermesTokens.Emerald.copy(alpha = 0.25f)
                                    else -> HermesTokens.CardElevated
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            provider.label.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    provider.isTermuxLocal -> HermesTokens.Amber
                                    isCurrentActiveProvider -> HermesTokens.Emerald
                                    else -> HermesTokens.TextPrimary
                                }
                            )
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                provider.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HermesTokens.TextPrimary
                                )
                            )
                            if (isCurrentActiveProvider) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(HermesTokens.RadiusPill),
                                    color = HermesTokens.Emerald.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = HermesTokens.Emerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Provider Type chip
                        Text(
                            provider.providerType.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HermesTokens.TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = provider.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HermesTokens.Emerald,
                        checkedTrackColor = HermesTokens.Emerald.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // Base URL & Endpoint row
            Surface(
                shape = RoundedCornerShape(HermesTokens.RadiusS),
                color = HermesTokens.CodeBlockBg,
                border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        provider.customBaseURL,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = HermesTokens.TextMuted,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Latency Badge or Ping
                    if (provider.latencyMs != null && provider.latencyMs > 0) {
                        Surface(
                            shape = RoundedCornerShape(HermesTokens.RadiusPill),
                            color = HermesTokens.Emerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "⚡ ${provider.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HermesTokens.Emerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Models section header (Minis style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "MODELS (${provider.models.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = HermesTokens.TextMuted,
                        fontSize = 10.sp
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Test Latency button
                    IconButton(onClick = onTestLatency, modifier = Modifier.size(28.dp)) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = HermesTokens.Emerald, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.FlashOn, contentDescription = "Test Ping", tint = HermesTokens.TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Fetch models from API
                    IconButton(onClick = onFetchModels, modifier = Modifier.size(28.dp)) {
                        if (isFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = HermesTokens.Cyan, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Discover Models", tint = HermesTokens.TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Delete if not Termux local
                    if (!provider.isTermuxLocal) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Provider", tint = HermesTokens.Error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Models Horizontal Carousel (Minis Chips)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(provider.models, key = { it.entryId.ifBlank { it.modelId } }) { model ->
                    val isSelected = isCurrentActiveProvider && activeModelId == model.modelId
                    Surface(
                        shape = RoundedCornerShape(HermesTokens.RadiusPill),
                        color = if (isSelected) HermesTokens.Emerald else HermesTokens.CardElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) HermesTokens.Emerald else HermesTokens.BorderSubtle
                        ),
                        modifier = Modifier.clickable { onSelectModel(model.modelId) }
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
                                model.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.Black else HermesTokens.TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Add Custom Model Chip
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

/**
 * Termux Live Card
 */
@Composable
private fun TermuxEnvironmentCard(
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
                        .size(34.dp)
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
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Termux Daemon",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = HermesTokens.TextPrimary
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        val (badgeText, badgeColor) = when (status) {
                            is TermuxServiceStatus.Active -> "LINKED" to HermesTokens.Emerald
                            is TermuxServiceStatus.InstalledIdle -> "IDLE" to HermesTokens.Amber
                            TermuxServiceStatus.Checking -> "CHECKING" to HermesTokens.Cyan
                            TermuxServiceStatus.NotInstalled -> "MISSING" to HermesTokens.TextMuted
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
                            is TermuxServiceStatus.Active -> "${status.serviceName} active on port ${status.port}"
                            is TermuxServiceStatus.InstalledIdle -> "Termux is installed. Tap to start Hermes."
                            TermuxServiceStatus.Checking -> "Probing 127.0.0.1:8765, 20128…"
                            TermuxServiceStatus.NotInstalled -> "Install Termux for local on-device execution."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = HermesTokens.TextMuted, fontSize = 11.sp)
                    )
                }
            }

            Row {
                IconButton(onClick = onRefresh, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = HermesTokens.TextMuted, modifier = Modifier.size(16.dp))
                }
                if (status is TermuxServiceStatus.InstalledIdle) {
                    Button(
                        onClick = onOpenTermux,
                        colors = ButtonDefaults.buttonColors(containerColor = HermesTokens.CardElevated),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Launch", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = HermesTokens.Emerald)
                    }
                }
            }
        }
    }
}

/**
 * Minis Add Provider Dialog with Presets
 */
@Composable
private fun MinisAddProviderDialog(
    onDismiss: () -> Unit,
    onAdd: (label: String, url: String, key: String, type: ProviderType, appendV1: Boolean) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var customBaseURL by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var providerType by remember { mutableStateOf(ProviderType.OPENAI) }
    var appendV1Suffix by remember { mutableStateOf(false) }

    val presets = listOf(
        Triple("OpenAI", "https://api.openai.com/v1", ProviderType.OPENAI),
        Triple("OpenRouter", "https://openrouter.ai/api/v1", ProviderType.OPENROUTER),
        Triple("Anthropic", "https://api.anthropic.com/v1", ProviderType.ANTHROPIC),
        Triple("Local 9Router", "http://127.0.0.1:20128/v1", ProviderType.OPENAI),
        Triple("Ollama Local", "http://127.0.0.1:11434/v1", ProviderType.OLLAMA),
        Triple("Custom Endpoint", "http://127.0.0.1:8000/v1", ProviderType.CUSTOM)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HermesTokens.SurfaceDark,
        title = {
            Text(
                "Add Provider Instance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HermesTokens.TextPrimary)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Preset Chips
                Text("QUICK PRESETS", style = MaterialTheme.typography.labelSmall.copy(color = HermesTokens.TextMuted, fontSize = 10.sp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presets) { (presetName, presetUrl, presetType) ->
                        Surface(
                            shape = RoundedCornerShape(HermesTokens.RadiusPill),
                            color = HermesTokens.CardElevated,
                            border = BorderStroke(1.dp, HermesTokens.BorderSubtle),
                            modifier = Modifier.clickable {
                                label = presetName
                                customBaseURL = presetUrl
                                providerType = presetType
                            }
                        ) {
                            Text(
                                presetName,
                                style = MaterialTheme.typography.labelSmall.copy(color = HermesTokens.TextPrimary, fontSize = 11.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Provider Label") },
                    placeholder = { Text("e.g. My Remote Router, DeepSeek") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(HermesTokens.RadiusS)
                )

                OutlinedTextField(
                    value = customBaseURL,
                    onValueChange = { customBaseURL = it },
                    label = { Text("Base URL") },
                    placeholder = { Text("e.g. http://127.0.0.1:20128/v1") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(HermesTokens.RadiusS)
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (optional)") },
                    placeholder = { Text("sk-…") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(HermesTokens.RadiusS)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (label.isNotBlank() && customBaseURL.isNotBlank()) {
                        onAdd(label, customBaseURL, apiKey, providerType, appendV1Suffix)
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

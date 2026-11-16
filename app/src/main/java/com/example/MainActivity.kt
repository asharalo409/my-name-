package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.ai.ManobsebaAiRepository
import com.example.data.repository.FoundationRepository
import com.example.data.security.AppLockManager
import com.example.firebase.attemptAutoSignIn
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.firebase.signOutGoogle
import com.example.ui.components.DonateQuickBottomSheet
import com.example.ui.screens.AiHubScreen
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BloodScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IslamicWeatherScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ManobsebaTheme
import com.example.ui.util.tr
import com.example.ui.viewmodel.AiViewModel
import com.example.ui.viewmodel.FoundationViewModel
import com.example.ui.viewmodel.IslamicWeatherViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object FinanceRoute
@Serializable object BloodRoute
@Serializable object IslamicRoute
@Serializable object AiHubRoute
@Serializable object CommunityRoute
@Serializable object SettingsRoute

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure App Check with launch intent BEFORE Compose UI, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            ManobsebaRoot(activity = this)
        }
    }
}

@Composable
private fun ManobsebaRoot(activity: FragmentActivity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    val user = currentUser
    if (user == null) {
        ManobsebaTheme {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        key(user.uid) {
            AuthenticatedManobsebaShell(
                activity = activity,
                currentUserId = user.uid,
                onSignOut = {
                    signOutGoogle(credentialManager, scope)
                }
            )
        }
    }
}

private data class NavTabItem(
    val route: Any,
    val routeKey: String,
    val bnLabel: String,
    val enLabel: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedManobsebaShell(
    activity: FragmentActivity,
    currentUserId: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val appLockManager = remember(context) { AppLockManager(context) }
    var appLockEnabled by remember { mutableStateOf(appLockManager.isEnabled()) }
    var isUnlocked by remember { mutableStateOf(!appLockManager.isEnabled()) }
    val biometricAvailable = remember(context) { appLockManager.canUseBiometric(context) }

    val foundationVm: FoundationViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val dbId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                FoundationViewModel(FoundationRepository(db), db, currentUserId)
            }
        }
    )

    val aiVm: AiViewModel = viewModel(
        key = "ai_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val dbId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                AiViewModel(FoundationRepository(db), ManobsebaAiRepository(), currentUserId)
            }
        }
    )

    val islamicVm: IslamicWeatherViewModel = viewModel(key = "islamic_$currentUserId")

    val settings by foundationVm.settings.collectAsStateWithLifecycle()
    val funds by foundationVm.funds.collectAsStateWithLifecycle()
    val members by foundationVm.members.collectAsStateWithLifecycle()
    val donations by foundationVm.donations.collectAsStateWithLifecycle()
    val expenses by foundationVm.expenses.collectAsStateWithLifecycle()
    val fees by foundationVm.monthlyFees.collectAsStateWithLifecycle()
    val projects by foundationVm.projects.collectAsStateWithLifecycle()
    val reports by foundationVm.fieldReports.collectAsStateWithLifecycle()
    val bloodDonors by foundationVm.bloodDonors.collectAsStateWithLifecycle()
    val bloodRequests by foundationVm.bloodRequests.collectAsStateWithLifecycle()
    val events by foundationVm.events.collectAsStateWithLifecycle()
    val polls by foundationVm.polls.collectAsStateWithLifecycle()
    val notices by foundationVm.notices.collectAsStateWithLifecycle()
    val messages by foundationVm.messages.collectAsStateWithLifecycle()
    val journal by foundationVm.islamicJournal.collectAsStateWithLifecycle()
    val statusBanner by foundationVm.statusBanner.collectAsStateWithLifecycle()

    val selectedDistrict by islamicVm.selectedDistrict.collectAsStateWithLifecycle()
    val prayerSchedule by islamicVm.prayerSchedule.collectAsStateWithLifecycle()
    val hanafiAsr by islamicVm.hanafiAsr.collectAsStateWithLifecycle()
    val weatherState by islamicVm.weatherState.collectAsStateWithLifecycle()
    val quranState by islamicVm.quranState.collectAsStateWithLifecycle()
    val tasbihCount by islamicVm.tasbihCount.collectAsStateWithLifecycle()
    val tasbihTarget by islamicVm.tasbihTarget.collectAsStateWithLifecycle()

    LaunchedEffect(settings.district) {
        islamicVm.syncDistrictByName(settings.district)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        statusBanner?.let {
            snackbarHostState.showSnackbar(it)
            foundationVm.clearStatusBanner()
        }
    }

    ManobsebaTheme(
        paletteKey = settings.themePalette,
        darkTheme = settings.darkMode
    ) {
        if (!isUnlocked && appLockEnabled) {
            AppLockScreen(
                orgName = settings.orgName,
                biometricAvailable = biometricAvailable && appLockManager.isBiometricEnabled(),
                onVerifyPin = { appLockManager.verifyPin(it) },
                onRequestBiometric = {
                    appLockManager.authenticateBiometric(
                        activity = activity,
                        onSuccess = { isUnlocked = true },
                        onError = {}
                    )
                },
                onUnlocked = { isUnlocked = true }
            )
            return@ManobsebaTheme
        }

        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val curRoute = backStackEntry?.destination?.route.orEmpty()
        var showDonateSheet by remember { mutableStateOf(false) }

        val tabs = remember {
            listOf(
                NavTabItem(HomeRoute, HomeRoute::class.qualifiedName!!, "হোম", "Home", Icons.Rounded.Home, "nav_home"),
                NavTabItem(FinanceRoute, FinanceRoute::class.qualifiedName!!, "হিসাব", "Finance", Icons.Rounded.AccountBalance, "nav_finance"),
                NavTabItem(BloodRoute, BloodRoute::class.qualifiedName!!, "রক্তদান", "Blood", Icons.Rounded.Bloodtype, "nav_blood"),
                NavTabItem(IslamicRoute, IslamicRoute::class.qualifiedName!!, "ইসলামিক", "Islamic", Icons.Rounded.Mosque, "nav_islamic"),
                NavTabItem(AiHubRoute, AiHubRoute::class.qualifiedName!!, "AI হাব", "AI Hub", Icons.Rounded.AutoAwesome, "nav_ai"),
                NavTabItem(CommunityRoute, CommunityRoute::class.qualifiedName!!, "কার্যক্রম", "More", Icons.Rounded.Groups, "nav_community")
            )
        }

        fun navigateTab(route: Any) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        val lang = settings.language

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 700.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = settings.orgName,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { showDonateSheet = true },
                                modifier = Modifier.testTag("topbar_donate_button")
                            ) {
                                Icon(Icons.Rounded.VolunteerActivism, contentDescription = "অনুদান দিন", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { navigateTab(SettingsRoute) },
                                modifier = Modifier.testTag("topbar_settings_button")
                            ) {
                                Icon(Icons.Rounded.Settings, contentDescription = "সেটিংস ও প্রোফাইল")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (!curRoute.contains("AiHubRoute")) {
                        FloatingActionButton(
                            onClick = { navigateTab(AiHubRoute) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("floating_ai_fab")
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = "মানবসেবা AI খুলুন")
                        }
                    }
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar {
                            tabs.forEach { item ->
                                val selected = curRoute == item.routeKey
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTab(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.bnLabel) },
                                    label = {
                                        Text(
                                            tr(item.bnLabel, item.enLabel, lang),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail {
                            tabs.forEach { item ->
                                val selected = curRoute == item.routeKey
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTab(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.bnLabel) },
                                    label = { Text(tr(item.bnLabel, item.enLabel, lang)) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                settings = settings,
                                donations = donations,
                                expenses = expenses,
                                fees = fees,
                                projects = projects,
                                bloodRequests = bloodRequests,
                                notices = notices,
                                memberCount = members.size,
                                prayerSchedule = prayerSchedule,
                                weatherState = weatherState,
                                onOpenDonate = { showDonateSheet = true },
                                onNavigateFinance = { navigateTab(FinanceRoute) },
                                onNavigateBlood = { navigateTab(BloodRoute) },
                                onNavigateIslamic = { navigateTab(IslamicRoute) },
                                onNavigateAi = { navigateTab(AiHubRoute) },
                                onNavigateCommunity = { navigateTab(CommunityRoute) }
                            )
                        }

                        composable<FinanceRoute> {
                            FinanceScreen(
                                settings = settings,
                                funds = funds,
                                members = members,
                                donations = donations,
                                expenses = expenses,
                                fees = fees,
                                onSaveDonation = foundationVm::saveDonation,
                                onDeleteDonation = foundationVm::deleteDonation,
                                onSaveExpense = foundationVm::saveExpense,
                                onDeleteExpense = foundationVm::deleteExpense,
                                onSaveMonthlyFee = foundationVm::saveMonthlyFee,
                                onOpenDonateSheet = { showDonateSheet = true }
                            )
                        }

                        composable<BloodRoute> {
                            BloodScreen(
                                settings = settings,
                                bloodDonors = bloodDonors,
                                bloodRequests = bloodRequests,
                                onSaveRequest = foundationVm::saveBloodRequest,
                                onToggleManaged = foundationVm::toggleBloodManaged,
                                onDeleteRequest = foundationVm::deleteBloodRequest,
                                onSaveDonor = foundationVm::saveBloodDonor
                            )
                        }

                        composable<IslamicRoute> {
                            IslamicWeatherScreen(
                                selectedDistrict = selectedDistrict,
                                prayerSchedule = prayerSchedule,
                                hanafiAsr = hanafiAsr,
                                weatherState = weatherState,
                                quranState = quranState,
                                tasbihCount = tasbihCount,
                                tasbihTarget = tasbihTarget,
                                journalEntries = journal,
                                onSelectDistrict = { district ->
                                    islamicVm.selectDistrict(district)
                                    foundationVm.updateSettings(settings.copy(district = district.bnName))
                                },
                                onToggleAsrMethod = islamicVm::toggleAsrMethod,
                                onRefreshWeather = islamicVm::refreshWeather,
                                onLoadSurah = islamicVm::loadSurah,
                                onCloseSurah = islamicVm::closeSurah,
                                onIncrementTasbih = islamicVm::incrementTasbih,
                                onResetTasbih = islamicVm::resetTasbih,
                                onSetTasbihTarget = islamicVm::setTasbihTarget,
                                onSaveJournal = foundationVm::saveIslamicJournal
                            )
                        }

                        composable<AiHubRoute> {
                            val convs by aiVm.conversations.collectAsStateWithLifecycle()
                            val activeConvId by aiVm.activeConversationId.collectAsStateWithLifecycle()
                            val aiMsgs by aiVm.activeMessages.collectAsStateWithLifecycle()
                            val streamState by aiVm.chatStreamState.collectAsStateWithLifecycle()
                            val pendingAction by aiVm.pendingAction.collectAsStateWithLifecycle()
                            val auditState by aiVm.auditState.collectAsStateWithLifecycle()
                            val ocrBusy by aiVm.ocrBusy.collectAsStateWithLifecycle()
                            val ocrSummary by aiVm.ocrSummary.collectAsStateWithLifecycle()
                            val ocrEntries by aiVm.ocrEntries.collectAsStateWithLifecycle()
                            val ocrError by aiVm.ocrError.collectAsStateWithLifecycle()
                            val legacyBusy by aiVm.legacyBusy.collectAsStateWithLifecycle()
                            val legacyEntries by aiVm.legacyEntries.collectAsStateWithLifecycle()
                            val legacyError by aiVm.legacyError.collectAsStateWithLifecycle()

                            AiHubScreen(
                                settings = settings,
                                funds = funds,
                                members = members,
                                donations = donations,
                                expenses = expenses,
                                conversations = convs,
                                activeConversationId = activeConvId,
                                messages = aiMsgs,
                                chatStreamState = streamState,
                                pendingAction = pendingAction,
                                auditState = auditState,
                                ocrBusy = ocrBusy,
                                ocrSummary = ocrSummary,
                                ocrEntries = ocrEntries,
                                ocrError = ocrError,
                                legacyBusy = legacyBusy,
                                legacyEntries = legacyEntries,
                                legacyError = legacyError,
                                onSelectConversation = aiVm::selectConversation,
                                onNewConversation = aiVm::startNewConversation,
                                onSendPrompt = { msg, ctxSummary ->
                                    aiVm.sendChatPrompt(msg, ctxSummary, funds, settings.language)
                                },
                                onExecuteAction = { draft ->
                                    aiVm.executeActionDraft(draft, funds, members)
                                },
                                onDismissAction = aiVm::dismissActionDraft,
                                onRunAudit = aiVm::runFinancialAudit,
                                onAnalyzePhoto = { bmp ->
                                    aiVm.analyzeLedgerPhoto(bmp, funds, members)
                                },
                                onImportOcr = { entries ->
                                    aiVm.importOcrEntries(entries, funds)
                                },
                                onParseLegacy = aiVm::parseLegacyText,
                                onImportLegacy = aiVm::importLegacyMembers
                            )
                        }

                        composable<CommunityRoute> {
                            CommunityScreen(
                                settings = settings,
                                projects = projects,
                                reports = reports,
                                members = members,
                                events = events,
                                polls = polls,
                                notices = notices,
                                messages = messages,
                                onSaveProject = foundationVm::saveProject,
                                onSaveReport = foundationVm::saveFieldReport,
                                onSaveMember = foundationVm::saveMember,
                                onDeleteMember = foundationVm::deleteMember,
                                onSaveEvent = foundationVm::saveEvent,
                                onRsvpEvent = foundationVm::rsvpEvent,
                                onSavePoll = foundationVm::savePoll,
                                onCastVote = foundationVm::castVote,
                                onSaveNotice = foundationVm::saveNotice,
                                onReactNotice = foundationVm::reactNotice,
                                onSendChat = { body -> foundationVm.sendChatMessage("room", body) },
                                onDeleteChat = foundationVm::deleteChatMessage
                            )
                        }

                        composable<SettingsRoute> {
                            SettingsScreen(
                                settings = settings,
                                appLockEnabled = appLockEnabled,
                                biometricAvailable = biometricAvailable,
                                onSaveSettings = foundationVm::updateSettings,
                                onEnableAppLock = { pin, bio ->
                                    appLockManager.savePin(pin, bio)
                                    appLockEnabled = true
                                },
                                onDisableAppLock = {
                                    appLockManager.disableLock()
                                    appLockEnabled = false
                                    isUnlocked = true
                                },
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }

            if (showDonateSheet) {
                DonateQuickBottomSheet(
                    settings = settings,
                    funds = funds,
                    onDismiss = { showDonateSheet = false },
                    onSubmitDonation = foundationVm::saveDonation
                )
            }
        }
    }
}

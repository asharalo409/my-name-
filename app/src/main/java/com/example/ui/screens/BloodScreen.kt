package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bloodtype
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BloodDonor
import com.example.data.model.BloodRequest
import com.example.data.model.FinanceCategories
import com.example.data.model.OrgSettings
import com.example.ui.components.BloodBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.util.dialPhone
import com.example.ui.util.shareText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BloodScreen(
    settings: OrgSettings,
    bloodDonors: List<BloodDonor>,
    bloodRequests: List<BloodRequest>,
    onSaveRequest: (BloodRequest) -> Unit,
    onToggleManaged: (BloodRequest) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onSaveDonor: (BloodDonor) -> Unit
) {
    val context = LocalContext.current
    var selectedGroup by remember { mutableStateOf("সব") }
    var showNewRequestDialog by remember { mutableStateOf(false) }
    var showNewDonorDialog by remember { mutableStateOf(false) }

    val filteredRequests = remember(bloodRequests, selectedGroup) {
        if (selectedGroup == "সব") bloodRequests else bloodRequests.filter { it.bloodGroup == selectedGroup }
    }
    val filteredDonors = remember(bloodDonors, selectedGroup) {
        if (selectedGroup == "সব") bloodDonors else bloodDonors.filter { it.bloodGroup == selectedGroup }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = Color(0xFFB91C1C),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.Bloodtype, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Column {
                                Text("জরুরি রক্তদান ও ২৪/৭ হেল্পলাইন", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                Text("তথ্য জমা দিলেই সাথে সাথে সবার কাছে পৌঁছে যাবে", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { dialPhone(context, settings.hotline ?: "01700000000") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFB91C1C)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ফাউন্ডেশন হটলাইন", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { dialPhone(context, "999") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D), contentColor = Color.White)
                            ) {
                                Text("৯৯৯ জরুরি", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showNewRequestDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_blood_request_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("রক্তের আবেদন")
                    }
                    OutlinedButton(
                        onClick = { showNewDonorDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("register_blood_donor_button")
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("রক্তদাতা নিবন্ধন")
                    }
                }
            }

            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (listOf("সব") + FinanceCategories.BLOOD_GROUPS).forEach { grp ->
                        FilterChip(
                            selected = selectedGroup == grp,
                            onClick = { selectedGroup = grp },
                            label = { Text(grp) }
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "সক্রিয় রক্তের আবেদন (${filteredRequests.size})")
            }

            if (filteredRequests.isEmpty()) {
                item {
                    EmptyStateCard(Icons.Rounded.LocalHospital, "এই গ্রুপের জরুরি আবেদন নেই", "নতুন রক্তের আবেদন করতে উপরের বাটনে ক্লিক করুন।")
                }
            } else {
                items(filteredRequests, key = { it.id }) { req ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BloodBadge(req.bloodGroup, urgent = req.urgent && !req.isManaged)
                                    Text(if (req.isManaged) "ম্যানেজ হয়েছে" else if (req.urgent) "অতি জরুরি" else "সক্রিয়", style = MaterialTheme.typography.labelLarge, color = if (req.isManaged) MaterialTheme.colorScheme.primary else Color(0xFFDC2626))
                                }
                                Row {
                                    IconButton(onClick = {
                                        shareText(
                                            context,
                                            "জরুরি ${req.bloodGroup} রক্ত প্রয়োজন",
                                            "রক্তের গ্রুপ: ${req.bloodGroup}\nহাসপাতাল: ${req.hospital}\nসমস্যা: ${req.patientProblem}\nযোগাযোগ: ${req.contact}\n(${settings.orgName})"
                                        )
                                    }) {
                                        Icon(Icons.Rounded.Share, contentDescription = "শেয়ার")
                                    }
                                    IconButton(onClick = { onDeleteRequest(req.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            Text(req.hospital, style = MaterialTheme.typography.titleMedium)
                            Text("সমস্যা: ${req.patientProblem} • আবেদনকারী: ${req.applicantName}", style = MaterialTheme.typography.bodyMedium)
                            if (!req.neededBy.isNullOrBlank()) {
                                Text("প্রয়োজন: ${req.neededBy} • ${req.district ?: ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { dialPhone(context, req.contact) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(req.contact)
                                }
                                OutlinedButton(onClick = { onToggleManaged(req) }) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (req.isManaged) "পুনরায় চালু" else "ম্যানেজ হয়েছে")
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "নিবন্ধিত রক্তদাতা তালিকা (${filteredDonors.size})")
            }

            items(filteredDonors, key = { it.id }) { donor ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                            BloodBadge(donor.bloodGroup)
                            Column {
                                Text(donor.name, style = MaterialTheme.typography.labelLarge)
                                Text("${donor.district} ${donor.upazila ?: ""} • ${if (donor.available) "দানে প্রস্তুত" else "বিশ্রামে"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row {
                            IconButton(onClick = { onSaveDonor(donor.copy(available = !donor.available)) }) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = "প্রস্তুত টগল", tint = if (donor.available) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                            IconButton(onClick = { dialPhone(context, donor.phone) }) {
                                Icon(Icons.Rounded.Call, contentDescription = "কল", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewRequestDialog) {
        NewBloodRequestDialog(
            onDismiss = { showNewRequestDialog = false },
            onSave = {
                onSaveRequest(it)
                showNewRequestDialog = false
            }
        )
    }

    if (showNewDonorDialog) {
        NewBloodDonorDialog(
            onDismiss = { showNewDonorDialog = false },
            onSave = {
                onSaveDonor(it)
                showNewDonorDialog = false
            }
        )
    }
}

@Composable
private fun NewBloodRequestDialog(
    onDismiss: () -> Unit,
    onSave: (BloodRequest) -> Unit
) {
    var group by remember { mutableStateOf("O+") }
    var hospital by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var applicant by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("ঢাকা") }
    var urgent by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("রক্তের জরুরি আবেদন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FinanceCategories.BLOOD_GROUPS.take(4).forEach { g ->
                        FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FinanceCategories.BLOOD_GROUPS.drop(4).forEach { g ->
                        FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) })
                    }
                }
                OutlinedTextField(value = hospital, onValueChange = { hospital = it }, label = { Text("হাসপাতালের নাম *") }, singleLine = true)
                OutlinedTextField(value = problem, onValueChange = { problem = it }, label = { Text("রোগীর সমস্যা *") }, singleLine = true)
                OutlinedTextField(value = applicant, onValueChange = { applicant = it }, label = { Text("আবেদনকারীর নাম *") }, singleLine = true)
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("ফোন নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("জেলা") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = urgent, onCheckedChange = { urgent = it })
                    Text("অতি জরুরি প্রয়োজন")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (hospital.isNotBlank() && contact.isNotBlank() && problem.isNotBlank() && applicant.isNotBlank()) {
                    onSave(
                        BloodRequest(
                            bloodGroup = group,
                            hospital = hospital.trim(),
                            district = district.trim().ifBlank { "ঢাকা" },
                            contact = contact.trim(),
                            patientProblem = problem.trim(),
                            applicantName = applicant.trim(),
                            urgent = urgent,
                            isManaged = false
                        )
                    )
                }
            }) { Text("প্রকাশ করুন") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
private fun NewBloodDonorDialog(
    onDismiss: () -> Unit,
    onSave: (BloodDonor) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("O+") }
    var district by remember { mutableStateOf("ঢাকা") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("রক্তদাতা হিসেবে যুক্ত হোন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("পূর্ণ নাম *") }, singleLine = true)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FinanceCategories.BLOOD_GROUPS.take(4).forEach { g ->
                        FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FinanceCategories.BLOOD_GROUPS.drop(4).forEach { g ->
                        FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) })
                    }
                }
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("জেলা") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    onSave(
                        BloodDonor(
                            name = name.trim(),
                            phone = phone.trim(),
                            bloodGroup = group,
                            district = district.trim().ifBlank { "ঢাকা" },
                            available = true
                        )
                    )
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

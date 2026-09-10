package com.kmj.ansik.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kmj.ansik.R

private val AI_CITY_CODES = listOf(
    "SEOUL", "BUSAN", "JEJU", "INCHEON", "DAEGU", "DAEJEON", "GWANGJU",
    "ULSAN", "SEJONG", "GYEONGJU", "GANGNEUNG", "JEONJU", "YEOSU", "SOKCHO"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AiRecommendationScreen(
    viewModel: MainViewModel,
    onCourseApplied: () -> Unit
) {
    var showWizard by remember { mutableStateOf(false) }
    val cityNames = stringArrayResource(id = R.array.ai_course_cities).toList()

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.ai_course_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.ai_course_desc),
                fontSize = 15.sp,
                color = AppColors.TextSecondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                border = BorderStroke(3.dp, AppColors.Divider)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = R.string.ai_course_card_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.PrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.ai_course_card_desc),
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    PlayfulButton(
                        onClick = {
                            viewModel.clearAiCourseError()
                            showWizard = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.ai_course_button),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        viewModel.aiCourseError.value?.let { error ->
            item {
                Surface(color = Color(0xFFFFE5E5), shape = RoundedCornerShape(14.dp)) {
                    Text(
                        text = aiCourseErrorText(error),
                        modifier = Modifier.padding(14.dp),
                        color = AppColors.DangerDark,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (viewModel.selectedConditions.value.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.current_selected_conditions),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    viewModel.selectedConditions.value.forEach { condition ->
                        Surface(color = AppColors.PrimarySoft, shape = RoundedCornerShape(50)) {
                            Text(
                                text = condition,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                color = AppColors.PrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.saved_my_courses),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(id = R.string.saved_my_courses_desc),
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
                IconButton(onClick = {
                    viewModel.startNewMyCourse()
                    onCourseApplied()
                }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_my_course),
                        tint = AppColors.Success
                    )
                }
            }
        }

        items(
            count = viewModel.savedMyCourses.size,
            key = { viewModel.savedMyCourses[it].id }
        ) { index ->
            val course = viewModel.savedMyCourses[index]
            MyCourseCard(course = course) {
                viewModel.applyMyCourse(course)
                onCourseApplied()
            }
        }

        if (viewModel.savedMyCourses.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.no_saved_my_courses),
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
            }
        }

        if (viewModel.savedAiCourses.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.saved_ai_courses),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(
                count = viewModel.savedAiCourses.size,
                key = { viewModel.savedAiCourses[it].id }
            ) { index ->
                val course = viewModel.savedAiCourses[index]
                AiCourseCard(course = course) {
                    viewModel.applyAiCourse(course)
                    onCourseApplied()
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showWizard) {
        AiCourseWizard(
            cityNames = cityNames,
            isCreating = viewModel.isCreatingAiCourse.value,
            error = viewModel.aiCourseError.value,
            onDismiss = {
                if (!viewModel.isCreatingAiCourse.value) showWizard = false
            },
            onCreate = { cityIndex, days, stopsPerDay, existingSchedule, preferences ->
                viewModel.createAiCourse(
                    cityCode = AI_CITY_CODES[cityIndex],
                    cityName = cityNames[cityIndex],
                    nights = days - 1,
                    days = days,
                    stopsPerDay = stopsPerDay,
                    existingSchedule = existingSchedule,
                    preferences = preferences
                ) {
                    showWizard = false
                    onCourseApplied()
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiCourseWizard(
    cityNames: List<String>,
    isCreating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (Int, Int, Int, String, List<String>) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedCity by remember { mutableIntStateOf(0) }
    var days by remember { mutableIntStateOf(3) }
    var stopsPerDay by remember { mutableIntStateOf(4) }
    var hasExistingSchedule by remember { mutableStateOf(false) }
    var existingSchedule by remember { mutableStateOf("") }
    var scheduleValidationError by remember { mutableStateOf<String?>(null) }
    var preferences by remember { mutableStateOf(setOf<String>()) }
    val preferenceItems = stringArrayResource(id = R.array.ai_course_preferences).toList()
    val scheduleRequiredText = stringResource(id = R.string.schedule_required)
    val scheduleCityRequiredText = stringResource(id = R.string.schedule_city_required)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !isCreating, dismissOnClickOutside = !isCreating)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            border = BorderStroke(3.dp, AppColors.Divider)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.ai_course_step, step + 1, 5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Info
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = when (step) {
                        0 -> stringResource(id = R.string.existing_schedule_question)
                        1 -> stringResource(id = R.string.choose_destination)
                        2 -> stringResource(id = R.string.choose_trip_length)
                        3 -> stringResource(id = R.string.choose_stops_per_day)
                        else -> stringResource(id = R.string.choose_travel_preferences)
                    },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    0 -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChoiceChip(
                                stringResource(id = R.string.no_schedule),
                                !hasExistingSchedule
                            ) {
                                hasExistingSchedule = false
                                existingSchedule = ""
                                scheduleValidationError = null
                            }
                            ChoiceChip(
                                stringResource(id = R.string.have_schedule),
                                hasExistingSchedule
                            ) {
                                hasExistingSchedule = true
                                scheduleValidationError = null
                            }
                        }
                        if (hasExistingSchedule) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = existingSchedule,
                                onValueChange = {
                                    existingSchedule = it.take(3000)
                                    scheduleValidationError = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp),
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.schedule_paste_hint),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = AppColors.TextSecondary.copy(alpha = 0.55f)
                                    )
                                },
                                minLines = 6,
                                maxLines = 10
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.schedule_paste_notice),
                                fontSize = 11.sp,
                                color = AppColors.TextSecondary
                            )
                            scheduleValidationError?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = it, color = AppColors.DangerDark, fontSize = 12.sp)
                            }
                        }
                    }
                    1 -> FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cityNames.forEachIndexed { index, city ->
                            ChoiceChip(city, selectedCity == index) { selectedCity = index }
                        }
                    }
                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (days > 1) days-- },
                                modifier = Modifier.background(AppColors.SurfaceMuted, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            Text(
                                text = stringResource(id = R.string.nights_days, days - 1, days),
                                modifier = Modifier.padding(horizontal = 24.dp),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.PrimaryDark
                            )
                            IconButton(
                                onClick = { if (days < 7) days++ },
                                modifier = Modifier.background(AppColors.SurfaceMuted, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.ai_course_length_notice),
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                    3 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (stopsPerDay > 3) stopsPerDay-- },
                                modifier = Modifier.background(AppColors.SurfaceMuted, CircleShape)
                            ) { Icon(Icons.Default.Remove, contentDescription = null) }
                            Text(
                                text = stringResource(id = R.string.stops_per_day_value, stopsPerDay),
                                modifier = Modifier.padding(horizontal = 24.dp),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.PrimaryDark
                            )
                            IconButton(
                                onClick = { if (stopsPerDay < 7) stopsPerDay++ },
                                modifier = Modifier.background(AppColors.SurfaceMuted, CircleShape)
                            ) { Icon(Icons.Default.Add, contentDescription = null) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.stops_per_day_notice),
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(id = R.string.preference_optional),
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            preferenceItems.forEach { preference ->
                                ChoiceChip(preference, preferences.contains(preference)) {
                                    preferences = if (preferences.contains(preference)) {
                                        preferences - preference
                                    } else {
                                        preferences + preference
                                    }
                                }
                            }
                        }
                    }
                }

                error?.let {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = aiCourseErrorText(it), color = AppColors.DangerDark, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(22.dp))
                if (isCreating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppColors.Primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.creating_ai_course),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { if (step == 0) onDismiss() else step-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (step == 0) stringResource(id = R.string.close)
                                else stringResource(id = R.string.previous)
                            )
                        }
                        PlayfulButton(
                            onClick = {
                                if (step == 0 && hasExistingSchedule) {
                                    val schedule = existingSchedule.trim()
                                    val inferredCity = inferCityIndex(schedule, cityNames)
                                    when {
                                        schedule.isBlank() -> scheduleValidationError =
                                            scheduleRequiredText
                                        inferredCity == null -> scheduleValidationError =
                                            scheduleCityRequiredText
                                        else -> onCreate(
                                            inferredCity,
                                            inferTripDays(schedule, 1),
                                            inferStopsPerDay(schedule, stopsPerDay),
                                            schedule,
                                            emptyList()
                                        )
                                    }
                                } else if (step < 4) {
                                    step++
                                } else {
                                    onCreate(
                                        selectedCity,
                                        days,
                                        stopsPerDay,
                                        "",
                                        preferences.toList()
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text(
                                text = if ((step == 0 && hasExistingSchedule) || step == 4) {
                                    stringResource(id = R.string.create_course)
                                } else {
                                    stringResource(id = R.string.next)
                                },
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun inferCityIndex(schedule: String, cityNames: List<String>): Int? {
    val normalized = schedule.lowercase()
    val aliases = listOf(
        listOf("서울", "seoul", "ソウル", "首尔", "首爾", "경복궁", "광장시장", "남산", "n서울타워", "홍대", "강남", "성수", "여의도", "잠실", "롯데월드", "코엑스", "인사동", "북촌"),
        listOf("부산", "busan", "釜山", "해운대", "광안리", "감천문화마을", "자갈치", "태종대", "서면"),
        listOf("제주", "jeju", "済州", "济州", "濟州", "성산일출봉", "한라산", "우도", "애월", "서귀포", "협재"),
        listOf("인천", "incheon", "仁川", "송도", "차이나타운", "월미도"),
        listOf("대구", "daegu", "大邱", "동성로", "팔공산", "서문시장"),
        listOf("대전", "daejeon", "大田", "성심당", "엑스포과학공원"),
        listOf("광주", "gwangju", "光州", "무등산", "양림동", "충장로"),
        listOf("울산", "ulsan", "蔚山", "대왕암", "간절곶", "태화강"),
        listOf("세종", "sejong", "世宗", "세종호수공원", "국립세종수목원"),
        listOf("경주", "gyeongju", "慶州", "庆州", "불국사", "첨성대", "황리단길", "석굴암"),
        listOf("강릉", "gangneung", "江陵", "경포대", "안목커피", "주문진"),
        listOf("전주", "jeonju", "全州", "전주한옥마을", "경기전"),
        listOf("여수", "yeosu", "麗水", "丽水", "오동도", "돌산", "여수케이블카"),
        listOf("속초", "sokcho", "束草", "설악산", "속초중앙시장", "영금정")
    )
    return aliases.indices.firstOrNull { index ->
        cityNames.getOrNull(index)?.lowercase()?.let(normalized::contains) == true ||
            aliases[index].any { normalized.contains(it.lowercase()) }
    }
}

private fun inferTripDays(schedule: String, fallback: Int): Int {
    val explicitDuration = Regex("(\\d+)\\s*(?:박|泊|nights?)\\s*(\\d+)\\s*(?:일|日|days?)", RegexOption.IGNORE_CASE)
        .find(schedule)?.groupValues?.getOrNull(2)?.toIntOrNull()
    if (explicitDuration != null) return explicitDuration.coerceIn(1, 7)

    val singleDuration = Regex("(\\d+)\\s*(?:일(?!차)|日(?!目)|days?|天)", RegexOption.IGNORE_CASE)
        .find(schedule)?.groupValues?.getOrNull(1)?.toIntOrNull()
    if (singleDuration != null) return singleDuration.coerceIn(1, 7)

    val mentionedDays = sequenceOf(
        Regex("day\\s*(\\d+)", RegexOption.IGNORE_CASE),
        Regex("(\\d+)\\s*(?:일차|日目|天)")
    ).flatMap { pattern -> pattern.findAll(schedule) }
        .mapNotNull { match -> match.groupValues.getOrNull(1)?.toIntOrNull() }
        .filter { it in 1..7 }
        .maxOrNull()
    return mentionedDays ?: fallback.coerceIn(1, 7)
}

private fun inferStopsPerDay(schedule: String, fallback: Int): Int {
    var currentDayStops = 0
    var maximumStops = 0
    schedule.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        val isDayHeader = Regex("(?:day\\s*\\d+|\\d+\\s*(?:일차|日目|天))", RegexOption.IGNORE_CASE)
            .containsMatchIn(line)
        if (isDayHeader && !line.startsWith("-") && !line.startsWith("•")) {
            maximumStops = maxOf(maximumStops, currentDayStops)
            currentDayStops = 0
        } else if (Regex("^(?:[-•]|\\d+[.)])\\s+").containsMatchIn(line)) {
            currentDayStops++
        }
    }
    maximumStops = maxOf(maximumStops, currentDayStops)
    return if (maximumStops > 0) maximumStops.coerceIn(3, 7) else fallback.coerceIn(3, 7)
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) AppColors.Primary else AppColors.Surface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(2.dp, if (selected) AppColors.PrimaryDark else AppColors.Divider)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else AppColors.TextPrimary
        )
    }
}

@Composable
private fun AiCourseCard(course: AiCourse, onApply: () -> Unit) {
    var expanded by remember(course.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(3.dp, AppColors.Divider)
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.PrimaryDark
                    )
                    Text(
                        text = "${course.cityName} · " + stringResource(
                            id = R.string.nights_days, course.nights, course.days
                        ),
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.PrimaryDark
                )
            }
            if (expanded) {
                if (course.summary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = course.summary, fontSize = 13.sp, lineHeight = 18.sp)
                }
                if (course.selectionReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(11.dp))
                    Text(
                        text = stringResource(id = R.string.why_this_course),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Info
                    )
                    Text(text = course.selectionReason, fontSize = 12.sp, lineHeight = 17.sp)
                }
                course.itinerary.sortedBy { it.day }.forEach { day ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = AppColors.PrimarySoft, shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(11.dp)) {
                            Text(
                                text = stringResource(id = R.string.day_format, day.day) +
                                    if (day.theme.isBlank()) "" else " · ${day.theme}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.PrimaryDark
                            )
                            day.stops.forEach { stop ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = listOf(stop.recommendedTime, stop.name)
                                        .filter { it.isNotBlank() }.joinToString("  "),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (stop.reason.isNotBlank()) {
                                    Text(text = stop.reason, fontSize = 11.sp, color = AppColors.TextSecondary)
                                }
                                if (stop.visitTip.isNotBlank()) {
                                    Text(text = "↳ ${stop.visitTip}", fontSize = 10.sp, color = AppColors.InfoDark)
                                }
                                if (stop.healthNote.isNotBlank()) {
                                    Text(text = "♥ ${stop.healthNote}", fontSize = 10.sp, color = AppColors.DangerDark)
                                }
                            }
                        }
                    }
                }
                if (course.travelTips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.touring_tips),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Info
                    )
                    course.travelTips.take(4).forEach { tip ->
                        Text(text = "• $tip", fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PlayfulButton(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.apply_course_to_map),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun MyCourseCard(course: SavedMyCourse, onApply: () -> Unit) {
    var expanded by remember(course.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(3.dp, AppColors.Divider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text(
                        stringResource(id = R.string.saved_my_course_summary, course.days, course.places.size),
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            if (expanded) {
                course.places.groupBy { it.day }.toSortedMap().forEach { (day, places) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        stringResource(id = R.string.day_format, day),
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Success
                    )
                    places.forEachIndexed { index, place ->
                        Text("${index + 1}. ${place.name}", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PlayfulButton(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.apply_course_to_map),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun aiCourseErrorText(status: String): String = when (status) {
    "OPENAI_NOT_CONFIGURED" -> stringResource(id = R.string.ai_course_error_not_configured)
    "INSUFFICIENT_PLACES" -> stringResource(id = R.string.ai_course_error_no_places)
    "NETWORK_ERROR" -> stringResource(id = R.string.ai_course_error_network)
    else -> stringResource(id = R.string.ai_course_error_general)
}

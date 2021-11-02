package bot.features.scheduling.model.results

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.time.DayOfWeek

sealed class ParsingResult {
    object ParsingError : ParsingResult()
    data class ParsedDayOfWeek(val dayOfWeek: DayOfWeek) : ParsingResult()
    data class ParsedTime(val hoursToMinutes: Pair<Int, Int>) : ParsingResult()
    data class ParsedLocalDateTime(val localDateTime: LocalDateTime) : ParsingResult()
    data class ParsedLocalDate(val localDate: LocalDate) : ParsingResult()
}

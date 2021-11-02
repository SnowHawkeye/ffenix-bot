package bot.features.scheduling.data

import dev.kord.common.Color

const val schedulingFeatureName = "scheduling"

const val schedulerNecessaryRoleName = "Scheduler"
val schedulerNecessaryRoleColor = Color(rgb = 0x91ADFF)

const val dateFormattingPattern = "dd/MM/yyyy"
const val defaultTimezone = "CET"

// COMMANDS

// Timezones
const val getTimezonesCommandName = "timezones"
const val getTimezonesCommandDescription = "Return a list of timezone IDs"

// Display schedule
const val displayScheduleCommandName = "nextraids"
const val displayScheduleCommandDescription = "Display the upcoming raids"

const val displayDefaultScheduleCommandName = "default"
const val displayDefaultScheduleCommandDescription = "Display the default raid schedule, regardless of punctual events"

const val displayScheduleByNumberCommandName = "bynumber"
const val displayScheduleByNumberCommandDescription =
    "Display a number of upcoming raids, cancelled and exceptional raids, and absences"

const val displayScheduleByDateCommandName = "until"
const val displayScheduleByDateCommandDescription =
    "Display upcoming raids until a certain date, cancelled and exceptional raids, and absences"

// Edit the schedule
const val editScheduleCommandName = "schedule"
const val editScheduleCommandDescription =
    "Edit the current raid schedule, exceptionally add or cancel raids, or manage absences"

// Edit the default schedule
const val editDefaultScheduleCommandName = "default"
const val editDefaultScheduleCommandDescription = "Edit the default raid schedule"

const val editDefaultScheduleTimezoneCommandName = "timezone"
const val editDefaultScheduleTimezoneCommandDescription =
    "Sets the default timezone for the default raid schedule. Defaults to CET"

const val addDefaultRaidCommandName = "add"
const val addDefaultRaidCommandDescription = "Add a new raid to the default schedule"

const val removeDefaultRaidCommandName = "remove"
const val removeDefaultRaidCommandDescription = "Remove a raid from the default schedule"

// Cancel a default raid
const val cancellationsCommandName = "cancel"
const val cancellationsCommandDescription = "Manage cancelled raids"

const val cancelDefaultRaidCommandName = "on"
const val cancelDefaultRaidCommandDescription = "Cancel one of the default schedule's raids for a specific date"

const val revertDefaultRaidCancellationCommandName = "revert"
const val revertDefaultRaidCancellationCommandDescription = "Revert a default raid cancellation"

// Exceptional raids
const val exceptionsCommandName = "exception"
const val exceptionsCommandDescription = "Manage exceptional raids"

const val addExceptionalRaidCommandName = "add"
const val addExceptionalRaidCommandDescription = "Add an exceptional raid on the given date at the given time"

const val cancelExceptionalRaidCommandName = "cancel"
const val cancelExceptionalRaidCommandDescription = "Cancel the specified exceptional raid"

// Absences
const val absencesCommandName = "absence"
const val absencesCommandDescription = "Manage absences"

const val addAbsenceCommandName = "add"
const val addAbsenceCommandDescription = "Add an absence on the given date for the mentioned user"

const val removeAbsenceCommandName = "remove"
const val removeAbsenceCommandDescription = "Remove an absence on the given date for the mentioned user"

// ARGUMENTS

// Display raids arguments
const val numberOfRaidsToDisplayArgumentName = "howmany"
const val numberOfRaidsToDisplayArgumentDescription = "The number of raids to display. Must be 1 or more"

const val limitDisplayDateArgumentName = "date"
const val limitDisplayDateArgumentDescription = "The limit date. Must be in the DD/MM/YYYY format"

const val timezoneDisplayArgumentName = "timezone"
const val timezoneDisplayArgumentDescription =
    "The ID of the timezone to output raids in. Use /timezones for a list of valid IDs"

// Edit raids arguments
const val dayArgumentName = "day"
const val dayArgumentDescription = "The day of the week when the raid happens"

const val dateArgumentName = "date"
const val dateArgumentDescription = "The day of the raid. Must be in the DD/MM/YYYY format"

const val timeArgumentName = "time"
const val timeArgumentDescription = "The time of the day when the raid starts. Must be in the HH:MM 24h format"

const val timezoneArgumentName = "timezone"
const val timezoneArgumentDescription =
    "The ID of the timezone the times are entered in. Use /timezones for a list of valid IDs"

const val absentUserArgumentName = "who"
const val absentUserArgumentDescription = "The person who will be absent"

const val raidCommentArgumentName = "comment"
const val raidCommentArgumentDescription = "A comment to append to the raid"

const val cancellationCommentArgumentName = "comment"
const val cancellationCommentArgumentDescription = "A comment to append to the cancellation"

const val absenceDateArgumentName = "date"
const val absenceDateArgumentDescription = "The day of the absence. Must be in the DD/MM/YYYY format"

const val absenceCommentArgumentName = "comment"
const val absenceCommentArgumentDescription = "A comment to append to the absence"

const val mondayChoice = "Monday"
const val tuesdayChoice = "Tuesday"
const val wednesdayChoice = "Wednesday"
const val thursdayChoice = "Thursday"
const val fridayChoice = "Friday"
const val saturdayChoice = "Saturday"
const val sundayChoice = "Sunday"

// MESSAGES

// Error messages
const val genericErrorMessage = "Sorry, something went wrong..."

const val dateIsInThePastErrorMessage = "This date is in the past, please use a future date."

const val raidDoesNotExistErrorMessage = "It seems that this raid does not exist!"
const val absenceAlreadyExistsErrorMessage = "It seems that this absence already exists!"
const val raidAlreadyExistsErrorMessage = "It seems that this raid already exists!"
const val noSuchAbsenceErrorMessage = "It seems that this absence does not exist!"
const val noRaidPlannedErrorMessage = "It seems that no raid is planned on that day!"
const val raidAlreadyCancelledErrorMessage = "It seems that this raid is already cancelled!"
const val noCancellationToRevertErrorMessage = "It seems that this raid was not cancelled!"

const val incorrectDateErrorMessage = "Sorry, the date you used seems to be invalid... " +
        "Make sure you are writing it as `DD/MM/YYYY`."
const val incorrectTimeErrorMessage = "Sorry, the time you used seems to be invalid... " +
        "Make sure you are writing it as `HH:MM`."
const val incorrectTimezoneIdErrorMessage = "Sorry, this timezone ID seems to be invalid... " +
        "Use `/timezones` for a list of valid timezone IDs."

const val nothingToCancelErrorMessage = "Sorry, there is no exceptional raid to cancel at that date... " +
        "You can check upcoming raids by using `/nextraids`."

const val nothingToDisplayErrorMessage = "Sorry, it seems that there is nothing to display... "
const val incorrectNumberOfRaidsMessage = "This number of raids is invalid. " +
        "Please use a number that is equal to or higher than 1."

const val userNotFoundErrorMessage = "Sorry, an error occurred while fetching that user... "


// Success messages
const val getTimezonesResponse =
    "`CET`, `UTC+[a number]`, and `Europe/[Capital city]` are all valid timezone IDs.\n" +
            "A complete list can be found here: https://gist.github.com/arpit/1035596"

const val setDefaultTimezoneSuccessMessage = "Default timezone set successfully!"
const val addDefaultRaidSuccessMessage = "A new default raid was successfully added! " +
        "Use `/nextraids default` to display the current default schedule."
const val removeDefaultRaidSuccessMessage = "This default raid was successfully removed. " +
        "Use `/nextraids default` to display the current default schedule."

const val addExceptionalRaidSuccessMessage = "A new exceptional raid was added successfully! " +
        "It should now appear when using `/nextraids`."
const val cancelExceptionalRaidSuccessMessage = "A new exceptional raid was added successfully! " +
        "You can check that it is gone by using `/nextraids`."

const val addAbsenceSuccessMessage = "This absence was added successfully."
const val removeAbsenceSuccessMessage = "This absence was removed successfully."
const val cancelDefaultRaidSuccess = "This raid was cancelled successfully."
const val revertCancellationSuccess = "This raid cancellation was successfully reverted."
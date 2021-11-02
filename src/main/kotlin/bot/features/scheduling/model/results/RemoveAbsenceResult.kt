package bot.features.scheduling.model.results

sealed class RemoveAbsenceResult {
    object Success : RemoveAbsenceResult()
    object Failure : RemoveAbsenceResult()
    object NoSuchAbsence : RemoveAbsenceResult()
    object IncorrectDate : RemoveAbsenceResult()
    object DateIsInThePast : RemoveAbsenceResult()
}

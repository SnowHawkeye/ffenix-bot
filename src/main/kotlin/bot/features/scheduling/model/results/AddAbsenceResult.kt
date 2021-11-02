package bot.features.scheduling.model.results

sealed class AddAbsenceResult {
    object Success : AddAbsenceResult()
    object Failure : AddAbsenceResult()
    object AbsenceAlreadyExists : AddAbsenceResult()
    object DateIsInThePast : AddAbsenceResult()
    object IncorrectDate : AddAbsenceResult()
}

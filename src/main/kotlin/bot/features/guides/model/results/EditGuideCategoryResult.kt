package bot.features.guides.model.results

import bot.features.guides.model.GuideCategory

sealed class EditGuideCategoryResult {
    object NoSuchCategory : EditGuideCategoryResult()
    object CategoryAlreadyExists : EditGuideCategoryResult()
    object Failure : EditGuideCategoryResult()
    data class Success(val guideCategory: GuideCategory) : EditGuideCategoryResult()
}

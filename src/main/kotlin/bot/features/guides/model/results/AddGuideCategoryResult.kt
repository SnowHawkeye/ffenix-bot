package bot.features.guides.model.results

import bot.features.guides.model.GuideCategory

sealed class AddGuideCategoryResult {
    object CategoryAlreadyExists : AddGuideCategoryResult()
    object Failure : AddGuideCategoryResult()
    data class Success(val category: GuideCategory) : AddGuideCategoryResult()
}

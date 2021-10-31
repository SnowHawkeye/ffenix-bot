package bot.features.guides.model.results

import bot.features.guides.model.GuideCategory

sealed class RemoveGuideCategoryResult {
    object NoSuchCategory : RemoveGuideCategoryResult()
    object CategoryNotEmpty : RemoveGuideCategoryResult()
    object Failure : RemoveGuideCategoryResult()
    data class Success(val category: GuideCategory) : RemoveGuideCategoryResult()
}

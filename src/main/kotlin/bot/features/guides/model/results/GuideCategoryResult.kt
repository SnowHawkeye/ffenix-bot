package bot.features.guides.model.results

import bot.features.guides.model.GuideCategory

sealed class GuideCategoryResult {
    object NoSuchCategory : GuideCategoryResult()
    data class Success(val category: GuideCategory) : GuideCategoryResult()
}

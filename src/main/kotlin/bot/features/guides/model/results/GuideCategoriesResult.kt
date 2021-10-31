package bot.features.guides.model.results

import bot.features.guides.model.GuideCategory

sealed class GuideCategoriesResult {
    object Empty : GuideCategoriesResult()
    data class Success(val guideCategories: List<GuideCategory>) : GuideCategoriesResult()
}

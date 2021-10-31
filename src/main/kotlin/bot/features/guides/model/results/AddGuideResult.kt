package bot.features.guides.model.results

import bot.features.guides.model.Guide
import bot.features.guides.model.GuideCategory

sealed class AddGuideResult {
    object NoSuchCategory : AddGuideResult()
    object GuideAlreadyExists : AddGuideResult()
    object Failure : AddGuideResult()
    data class Success(val guide: Guide, val category: GuideCategory) : AddGuideResult()
}

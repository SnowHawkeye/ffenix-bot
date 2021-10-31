package bot.features.guides.model.results

import bot.features.guides.model.Guide
import bot.features.guides.model.GuideCategory

sealed class GuideResult {
    object NoSuchCategory : GuideResult()
    object NoSuchGuide : GuideResult()
    data class Success(val guide: Guide, val category: GuideCategory) : GuideResult()

}

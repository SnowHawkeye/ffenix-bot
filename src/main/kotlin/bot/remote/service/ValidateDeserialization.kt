package bot.remote.service

import com.google.gson.JsonParseException

/**
 * Annotates a class where all fields must not be null.
 * Used to circumvent GSON's [missing field management](https://medium.com/@clay07g/gson-will-assign-missing-values-as-null-3f02f19f2145).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class JsonAllNotNull

/**
 * Annotates a field that must not be null.
 * Used to circumvent GSON's [missing field management](https://medium.com/@clay07g/gson-will-assign-missing-values-as-null-3f02f19f2145).
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class JsonNotNull

/**
 * Validates the given object's fields in respect to [JsonNotNull] and [JsonAllNotNull] annotations.
 * Used to circumvent GSON's [missing field management](https://medium.com/@clay07g/gson-will-assign-missing-values-as-null-3f02f19f2145).
 */
internal inline fun <reified T> validateDeserialization(pojo: T): T {
    val fields = pojo!!::class.java.declaredFields
    fields.forEach {
        if (pojo.javaClass.getAnnotation(JsonAllNotNull::class.java) != null
            || it.getAnnotation(JsonNotNull::class.java) != null
        ) {
            it.isAccessible = true
            if (it.get(pojo) == null) {
                throw JsonParseException("Missing field in JSON: " + it.name)
            }
        }
    }
    return pojo
}
package utils

inline fun <T> Iterable<T>.withEach(action: T.() -> Unit): Unit {
    for (element in this) element.action()
}
package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    if (this.isEmpty()) {
        return this
    }
    for (i in this.lastIndex downTo 0) {
        if (predicate(this[i])) {
            return this.subList(0, i)
        }
    }
    return this
}

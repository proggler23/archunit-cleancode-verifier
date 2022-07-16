package io.cloudflight.cleancode.archunit.kotlin

fun Class<*>.isKotlinClass(): Boolean {
    return this.declaredAnnotations.any {
        it.annotationClass == Metadata::class
    }
}


package io.cloudflight.cleancode.archunit

import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchTests
import io.cloudflight.cleancode.archunit.rules.jdk.JdkRuleSet
import io.cloudflight.cleancode.archunit.rules.jpa.JpaRuleSet
import io.cloudflight.cleancode.archunit.rules.logging.LoggingRuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.javaField

class DocumentationTest {

    @ParameterizedTest
    @MethodSource("classes")
    fun `all rules are explained in the documentation`(clazz: KClass<*>) {
        val file = clazz.simpleName!!.lowercase().removeSuffix("ruleset")
        val ids = getArchTests(clazz)
            .map {
                (it.call(clazz.createInstance()) as ArchRuleWithId).id.removeSuffix("$file-")
            }
        val headers = DocParser("rules/${file}.md").getHeaders().map { "${file}.${it}" }
        assertThat(ids).containsExactlyInAnyOrderElementsOf(headers)
    }

    companion object {
        @JvmStatic
        fun classes() = getArchTests(CleanCodeRuleSets::class).map {
            (it.call(CleanCodeRuleSets::class.createInstance()) as ArchTests).definitionLocation.kotlin
        }

        /**
         * this is q quite simplified version of the [com.tngtech.archunit.junit.internal.ArchUnitTestEngine]
         * in order to discover [ArchTest]s inside a class. As long as we're just
         * using members, that should be fine
         */
        private fun getArchTests(clazz: KClass<*>): List<KCallable<*>> {
            return clazz.declaredMembers.filter {
                (it as KProperty).javaField!!.isAnnotationPresent(ArchTest::class.java)
            }
        }
    }
}

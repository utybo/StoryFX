/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
package guru.zoroark.libstorytree.dsl

import guru.zoroark.libstorytree.*
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

private val Throwable.stackTraceString: String
    get() {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        this.printStackTrace(pw)
        pw.close()
        return sw.toString()
    }

interface ResourcesGetter {
    operator fun get(resourceName: String): Resource
}

class StoryBuilder(val env: StoryEnvironment) {
    val built: MutableList<Story> = mutableListOf()

    val resources = object : ResourcesGetter {
        override operator fun get(resourceName: String): Resource {
            if (env.engine is ResourceEngine)
                return env.engine.getResource(resourceName)
            else
                throw StoryBuilderException("Engine does not support resources")
        }
    }

    fun story(init: Story.() -> Unit): Story {
        val story = Story()
        init(story)
        built.add(story)
        return story
    }

    /**
     * Get the engine this script will is ran, making sure it supports the specified standard engine.
     *
     * Standard engines include, from least to most featured:
     *  * BaseEngine, the most basic one which is always supported. It enables using
     *    the warn and error functions
     *  * CommonEngine, which
     *
     */
    inline fun <reified T : BaseEngine> requireEngine(): T {
        if (env.engine is T) {
            return env.engine
        } else {
            throw IncompatibleEngineException(T::class.simpleName ?: "Unknown")
        }
    }

    fun forceFail() {
        throw StoryBuilderException("The story was forced to crash by calling the forceFail() function")
    }

    fun loadResources() {
        if (env.engine is ResourceEngine)
            env.engine.loadResources()
        else
            throw StoryBuilderException("Engine does not support resources")
    }

}

fun buildStoryDsl(script: String, env: StoryEnvironment) = buildStoryDsl(script.toScriptSource(), env)

fun buildStoryDsl(file: File, env: StoryEnvironment) = buildStoryDsl(file.toScriptSource(), env)

fun buildStoryDsl(source: SourceCode, env: StoryEnvironment): MutableList<Story> {
    val builder = StoryBuilder(env)
    val cfg = createJvmCompilationConfigurationFromTemplate<StoryBuildScript>()
    val read = BasicJvmScriptingHost().eval(source, cfg, ScriptEvaluationConfiguration {
        implicitReceivers(builder)
    })
    if (read is ResultWithDiagnostics.Success<*>) {
        return builder.built
    } else {
        val reports = read.reports
        val exceptionReports = reports.filter { it.exception != null && it.exception!!.cause != null }
        if (exceptionReports.count() == 1) {
            val sb = StringBuilder("Story building failed due to an unexpected exception.\n")
            val ex = exceptionReports[0]
            sb.appendln(ex.exception!!.cause!!.stackTraceString)
            throw StoryBuilderException(sb.toString(), cause = ex.exception!!.cause)

        } else {
            val sb = StringBuilder("Story building failed. Check the diagnostics for why.\n")
            read.reports.forEach {
                sb.appendln(it.message)
                if (it.exception != null) {
                    sb.appendln("  Stack trace:")
                    sb.appendln(it.exception!!.stackTraceString.prependIndent("  "))
                }
            }
            throw StoryBuilderException(sb.toString())
        }
    }
}

open class StoryBuilderException(val diagnosticsMessage: String, message: String? = diagnosticsMessage.split("\n")[0], cause: Throwable? = null) : Exception(message, cause)

class IncompatibleEngineException(requiredEngine: String) : StoryBuilderException("Engine does not match story requirements. Required: $requiredEngine")
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
import java.io.*
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

/**
 * A simple interface for something that allows you to get a resource.
 *
 *     resourceGetterObject["Resource name"]
 */
interface ResourcesGetter {
    /**
     * Get a resource by its name from this getter object.
     * @return a Resource object that matches the resource name.
     * @throws StoryBuilderException if the engine does not support resources.
     */
    operator fun get(resourceName: String): Resource
}

/**
 * The StoryBuilder is an object receiver for all story.kts scripts, which provides basic functionality for building and
 * managing stories. All of its members can be used directly in story.kts files.
 *
 * @property env The environment the story will live in.
 */
class StoryBuilder(val env: StoryEnvironment) {
    /**
     * Stories that have been built with this builder.
     */
    val built: MutableList<Story> = mutableListOf()

    /**
     * An object that can be used to retrieve a resource with a nice syntax. It uses the engine under the hood.
     *
     * Usage:
     * ```
     * resources["resource name"]
     * ```
     *
     * @return a [ResourcesGetter] object that can be used for retrieving resources.
     */
    val resources = object : ResourcesGetter {
        override operator fun get(resourceName: String): Resource {
            if (env.engine is ResourceEngine)
                return env.engine.getResource(resourceName)
            else
                throw StoryBuilderException("Engine does not support resources")
        }
    }

    /**
     * Story block definition. This creates a story and uses a lambda-with-receiver to allow customization of the
     * story.
     *
     * Usage:
     * ```
     *     story {
     *         // Story block content goes here (nodes, title, author...)
     *     }
     * ```
     */
    fun story(init: Story.() -> Unit): Story {
        val story = Story()
        init(story)
        registerStory(story)
        return story
    }

    private fun registerStory(story: Story) {
        // Stories should be registered after being initialized
        if (built.any { it.id == story.id }) {
            throw StoryBuilderException("Stories built at the same time must not have identical IDs.")
        }
        built.add(story)
    }

    /**
     * Get the engine this script is ran with, making sure it supports the specified engine type. If the specified
     * engine type is not supported, throws an exception.
     *
     * A list of standard engines can be found
     * [in the official documentation](https://storyfx.zoroark.guru/docs/storykts/engines.html)
     *
     * @throws IncompatibleEngineException if the engine the story is ran in is not compatible with the one that is
     * specified
     * @param T The engine type required by the story
     * @return The required engine
     */
    inline fun <reified T : BaseEngine> requireEngine(): T {
        if (env.engine is T) {
            return env.engine
        } else {
            throw IncompatibleEngineException(T::class.simpleName ?: "Unknown")
        }
    }

    /**
     * This function throws an exception, instantly making the story crash.
     *
     * @throws StoryBuilderException always
     */
    fun forceFail() {
        throw StoryBuilderException("The story was forced to crash by calling the forceFail() function")
    }

    /**
     * Trigger the loading of all of the resources in the engine. If the engine does not support resources, throws
     * an exception. The exact behavior of the resources loading depends on the engine.
     *
     * Resources are loaded from a folder named "resources" located in the same folder as the story.kts file.
     */
    fun loadResources() {
        if (env.engine is ResourceEngine)
            env.engine.loadResources()
        else
            throw StoryBuilderException("Engine does not support resources")
    }

    /**
     * Import the string as a story.txt file, parse it and use it in the lambda, which is a story block, for additional
     * customization.
     *
     * Usage:
     * ```
     *     """
     *     A story.txt story
     *     """ import {
     *         // This is a story block
     *     }
     * ```
     */
    infix fun String.import(init: Story.() -> Unit): Story {
        val story = parseStoryText(BufferedReader(StringReader(this.trimIndent())))
        init(story)
        registerStory(story)
        return story
    }

    fun importTxt(resource: Resource, init: Story.() -> Unit): Story {
        val story = parseStoryText(resource.openStream().bufferedReader())
        init(story)
        registerStory(story)
        return story
    }

    fun warnNsfw(vararg problematicContent: String) {
        choices {
            icon { "gmi-do-not-disturb-on" }
            title { "NSFW Warning" }
            text {
                """
                This story contains explicit content that is not appropriate for people under legal age.
                By clicking "Continue", you agree that you legally have the age and are willing to watch this content.
                """.trimIndent() + if (problematicContent.isNotEmpty())
                    "\n\nPotentially problematic content includes:" +
                            problematicContent.joinToString(separator = "\n- ", prefix = "\n- ")
                else
                    ""
            }
            choice("Exit") withColor("red") withWhiteText(true) does {
                env.engine.closeStory()
            }
            choice("Continue")
        }
    }
}

/**
 * Build a story from the given string, assumed to be the contents of a story.kts file.
 *
 * @param script The story.kts string, directly represented as a string
 * @param env The environment in which the story will be ran.
 * @return The stories that were built from the string
 */
fun buildStoryDsl(script: String, env: StoryEnvironment) = buildStoryDsl(script.toScriptSource(), env)

/**
 * Build and run the given file as a story.kts file.
 *
 * @param file The story.kts file to execute
 * @param env The environment in which the story will be ran.
 * @return The stories that were built from the file
 */
fun buildStoryDsl(file: File, env: StoryEnvironment) = buildStoryDsl(file.toScriptSource(), env)

/**
 * Build and run the given source code as a story.kts script.
 *
 * @param source The SourceCode object from which to load and execute the script
 * @param env The environment in which the story will be ran
 * @return the stories built from the script
 */
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
        // Taking the cause of it.exception because Kotlin wraps the underlying exception
        val exceptionReport = reports.firstOrNull { it.exception != null && it.exception!!.cause != null }
        if (exceptionReport != null) {
            throw StoryBuilderException("Story building failed due to an unexpected exception.", cause = exceptionReport.exception!!.cause)

        } else {
            val sb = StringBuilder("Story building failed. Check the diagnostics for why.\n")
            read.reports.forEach {
                with(sb) {
                    appendln("${it.severity.name}: ${it.message}")
                    val loc = it.location
                    if (loc != null) {
                        appendln("  Location: line ${loc.start.line} @ character ${loc.start.col}")
                        val end = loc.end
                        if (end != null) {
                            appendln("              to line ${end.line} @ character ${end.col}")
                        }
                    }
                    if (it.exception != null) {
                        appendln("  Stack trace:")
                        appendln(it.exception!!.stackTraceString.prependIndent("    "))
                    }
                    appendln()
                }
            }
            throw StoryBuilderException(sb.toString())
        }
    }
}

/**
 * Run a StoryBuilder block directly.
 *
 * @param env The environment the story will be ran in
 * @param init The code to execute
 * @return The stories that were built using the [init] block.
 */
fun buildStoryDsl(env: StoryEnvironment, init: StoryBuilder.() -> Unit): MutableList<Story> {
    val storyBuilder = StoryBuilder(env)
    try {
        init(storyBuilder)
    } catch (e: Exception) {
        val sb = StringBuilder("Story building failed due to an unexpected exception.\n")
        sb.appendln(e.stackTraceString)
        throw StoryBuilderException(sb.toString(), cause = e)
    }
    return storyBuilder.built
}

/**
 * General exception for stories.
 *
 * @property diagnosticsMessage A possibly multi-line message with full information on what happened
 * @param message The regular [Exception] message. Optional. Default value is the first line of the [diagnosticsMessage].
 * @param cause The cause of this builder exception. Optiona. Default value is null.
 */
open class StoryBuilderException(val diagnosticsMessage: String, message: String? = diagnosticsMessage.split("\n")[0], cause: Throwable? = null) : Exception(message, cause)

/**
 * The exception thrown when [StoryBuilder.requireEngine] detects that the engine the story is ran in is incompatible with the one the story requires.
 *
 * @param requiredEngine The engine that is required by the story.
 */
class IncompatibleEngineException(requiredEngine: String) : StoryBuilderException("Engine does not match story requirements. Required: $requiredEngine")
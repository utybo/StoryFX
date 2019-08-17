package guru.zoroark.libstorytree

/**
 * General exception for stories.
 *
 * @property diagnosticsMessage A possibly multi-line message with full
 * information on what happened
 * @param message The regular [Exception] message. Optional. Default value is
 * the first line of the [diagnosticsMessage].
 * @param cause The cause of this builder exception. Optiona. Default value is
 * null.
 */
open class StoryException(
        val diagnosticsMessage: String,
        message: String? = diagnosticsMessage.split("\n")[0],
        cause: Throwable? = null
) : Exception(message, cause)

/**
 * The exception thrown when [StoryBuilder.requireEngine] detects that the
 * engine the story is ran in is incompatible with the one the story requires.
 *
 * @param requiredEngine The engine that is required by the story.
 */
class IncompatibleEngineException(requiredEngine: String) :
        StoryException("Engine does not match story requirements. Required: $requiredEngine")

class InvalidResourceType(resourceName: String) :
        StoryException(
            """
            The provided resource ($resourceName) does not correspond to what was expected.
            
            This can happen for a few reasons.
            - You provided something that's not what was wanted (e.g. trying to put a sound as a background image)
            - The resource provided does not correspond to the story player's internal representation. Make sure you do not manually create your own resources and use loadResources() and resources["name"]
            """.trimIndent())
package guru.zoroark.storyfx.story

import guru.zoroark.libstorytree.StoryException

/**
 * Exception thrown when the loading of the story is terminated. Should not be
 * displayed to the user. If it is displayed to the user: that's a bug.
 */
class StoryLoadingAbortedException: StoryException("Story loading aborted by user. You should not see this issue!")

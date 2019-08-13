package guru.zoroark.storyfx.story

import guru.zoroark.libstorytree.dsl.StoryBuilderException

class StoryLoadingAbortedException: StoryBuilderException("Story loading aborted by user") {
}

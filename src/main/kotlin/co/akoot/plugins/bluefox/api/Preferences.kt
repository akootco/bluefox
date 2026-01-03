package co.akoot.plugins.bluefox.api

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.delegating.Delegate
import co.akoot.plugins.bluefox.api.delegating.Key
import java.util.UUID

class Preferences(uuid: UUID) {
    val config = BlueFox.getPrefs(uuid)

//    @Key(path="path.to.testString")
//    var testString: String by config.delegate("erm")
//
//    var testStringList: List<String> by config.delegate(listOf("one", "two", "three"))

}
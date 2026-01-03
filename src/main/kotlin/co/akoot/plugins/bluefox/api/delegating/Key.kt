package co.akoot.plugins.bluefox.api.delegating

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Key(val namespace: String = "BlueFox", val path: String)
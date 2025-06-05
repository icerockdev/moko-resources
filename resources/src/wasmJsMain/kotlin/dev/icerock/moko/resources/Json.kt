external object JSON {
    fun stringify(value: JsAny?): String
}

@JsName("Object")
external class JsObject : JsAny {
    operator fun get(key: JsString): JsAny?
    operator fun set(key: JsString, value: JsAny?)
}

fun json(vararg entries: Pair<String, Any>) =
    entries.fold(JsObject()) { result, (key, value) ->
        result.apply {
            set(key.toJsString(), value.toString().toJsString())
        }
    }
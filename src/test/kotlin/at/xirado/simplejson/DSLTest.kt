package at.xirado.simplejson

import kotlin.test.Test

internal class DSLTest {
    @Test
    fun testDSL() {
        val json = json {
            "someKey" by "someValue"
            "someObject" by {
                "someKey" by 14
                "someOtherKey" by "someOtherValue"
            }
        }
        println(json.toPrettyString())
        assert(json["someKey"] == "someValue")
        assert(json.getObject("someObject")["someKey"] == 14)
        assert(json.getObject("someObject")["someOtherKey"] == "someOtherValue")
    }
}
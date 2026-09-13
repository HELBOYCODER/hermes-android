package com.hermes.android.ipc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamParserTest {
    @Test fun tokenLine() {
        val e = StreamParser.parseLine("""{"type":"token","text":"hello"}""")
        assertEquals(StreamEvent.Token("hello"), e)
    }
    @Test fun toolLifecycle() {
        val s = StreamParser.parseLine("""{"type":"tool_start","id":"t1","name":"terminal","input":"ls"}""")
        assertTrue(s is StreamEvent.ToolStart && (s as StreamEvent.ToolStart).name == "terminal")
        val o = StreamParser.parseLine("""{"type":"tool_output","id":"t1","chunk":"bin"}""")
        assertTrue(o is StreamEvent.ToolOutput)
        val e = StreamParser.parseLine("""{"type":"tool_end","id":"t1","ok":"true","summary":"done"}""")
        assertTrue(e is StreamEvent.ToolEnd && (e as StreamEvent.ToolEnd).ok)
    }
    @Test fun sseAndDone() {
        assertEquals(StreamEvent.Token("hi"), StreamParser.parseLine("data: {\"type\":\"token\",\"text\":\"hi\"}"))
        assertEquals(StreamEvent.Done, StreamParser.parseLine("[DONE]"))
    }
    @Test fun plainTextFallback() {
        assertEquals(StreamEvent.Token("raw"), StreamParser.parseLine("raw"))
    }
    @Test fun sessionAndError() {
        assertTrue(StreamParser.parseLine("""{"type":"session","id":"s1"}""") is StreamEvent.Session)
        assertTrue(StreamParser.parseLine("""{"type":"error","message":"x"}""") is StreamEvent.Error)
    }
}

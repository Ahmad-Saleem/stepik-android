package org.stepic.droid.code.ui

import org.stepic.droid.util.countWhile
import org.stepic.droid.util.substringOrNull

/**
 * Class for smart code analyzing
 */
object CodeAnalyzer {
    private const val LINE_BREAK = '\n'

    private val brackets = hashMapOf(
        "{" to "}",
        "(" to ")",
        "[" to "]"
    )

    private val indentation = hashMapOf( // code specific indentation symbols
            "py" to hashSetOf(
                    ":"
            )
    )

    private val quotes = hashMapOf(
        "\"" to "\"",
        "'"  to "'"
    )

    private val pairedSymbols = brackets + quotes


    private fun getIndentForCurrentLine(cursorPosition: Int, text: String) : Int {
        val prevLineStart = text.substring(0, cursorPosition).lastIndexOf(LINE_BREAK)
        return text.countWhile (prevLineStart + 1) { Character.isWhitespace(it) && it != LINE_BREAK }
    }

    fun getIndentForLines(lines: List<String>) =
            Math.min(lines.map { getIndentForCurrentLine(0, it) }.filter { it > 0 }.min() ?: CodeEditor.DEFAULT_INDENT_SIZE, CodeEditor.MAX_INDENT_SIZE)


    fun onTextInserted(start: Int, count: Int, codeEditor: CodeEditor) {
        val inserted = codeEditor.editableText.toString().substring(start, start + count)
        val text = codeEditor.editableText.toString()
        when (inserted) {
            LINE_BREAK.toString() -> {
                val indent = getIndentForCurrentLine(start, text)

                val prev = getPrevSymbol(start, text)
                val next = getNextSymbol(start + 1, text)

                codeEditor.editableText.insert(start + count, " ".repeat(indent))

                if (prev in brackets || indentation[codeEditor.lang]?.contains(prev) == true) {
                    if (next != null && brackets[prev] == next) {
                        codeEditor.editableText.insert(start + count + indent, LINE_BREAK.toString())
                        codeEditor.setSelection(start + count + indent)
                    }
                    codeEditor.editableText.insert(start + count, " ".repeat(codeEditor.indentSize))
                }
            }

            in brackets -> {
                val next = getNextSymbol(start + 1, text)
                if (next == null || Character.isWhitespace(next[0]) || next in brackets.values) { // don't want auto bracket if there is a statement next
                    insertTextAfterCursor(start, count, codeEditor, brackets[inserted])
                }
            }

            in brackets.values -> {
                onClosingSymbolInserted(start, count, codeEditor, inserted, text)
            }

            in quotes -> {
                val next = getNextSymbol(start + 1, text)
                val prev = getPrevSymbol(start, text)
                if ((next == null || Character.isWhitespace(next[0])) && prev != inserted) { // don't want auto quote if there is a statement next
                    insertTextAfterCursor(start, count, codeEditor, inserted)
                } else {
                    onClosingSymbolInserted(start, count, codeEditor, inserted, text)
                }
            }
        }
    }

    private fun insertTextAfterCursor(start: Int, count: Int, codeEditor: CodeEditor, textToInsert: String?) {
        codeEditor.withoutAnalyze {
            it.editableText.insert(start + count, textToInsert)
            it.setSelection(start + count)
        }
    }

    private fun onClosingSymbolInserted(start: Int, count: Int, codeEditor: CodeEditor, inserted: String, text: String) {
        if (inserted == text.substringOrNull(start + count, start + 2 * count)) {
            codeEditor.withoutAnalyze {
                it.editableText.replace(start, start + count, "")
                it.setSelection(start + count)
            }
        }
    }

    fun onTextReplaced(start: Int, count: Int, codeEditor: CodeEditor, replaced: String) {
        val text = codeEditor.editableText.toString()
        when (replaced) {
            in pairedSymbols -> {
                val next = getNextSymbol(start, text)
                if (next != null && next == pairedSymbols[replaced]) {
                    codeEditor.withoutAnalyze {
                        it.editableText.replace(start, start + next.length, "")
                    }
                }
            }
        }
    }

    private fun getPrevSymbol(start: Int, text: String) : String? =
            text.substringOrNull(start - 1, start)

    private fun getNextSymbol(start: Int, text: String) : String? =
            text.substringOrNull(start, start + 1)


    fun findSecondBracket(bracket: String, start: Int, text: String) : Int {
        getBracketsPair(bracket)?.let {
            val delta = if (it.key == bracket) 1 else -1 // if bracket is opening delta will be positive otherwise negative
            var deep = 1
            var pos = start + delta

            while (0 <= pos && pos < text.length) {
                when (text.substring(pos, pos + 1)) {
                    it.key   -> deep += delta  // opening bracket
                    it.value -> deep -= delta  // closing bracket
                }

                if (deep == 0)
                    return pos

                pos += delta
            }
        }
        return -1
    }

    fun getBracketsPair(bracket: String) = brackets.entries.find { it.key == bracket || it.value == bracket }
}
package org.example

import org.example.PSIProgram.CodeSnippet
import org.example.PSIProgram.PSIElement
import org.example.PSIProgram.convert
import org.example.PSIProgram.snippets
import org.example.PSIProgram.validate

object ImperativeProgram {
    fun findFirstValidElement(snippets: List<CodeSnippet>): PSIElement? {
        for (snippet in snippets) {
            val psiElement = convert(snippet)
            if (validate(psiElement)) {
                return psiElement
            }
        }
        return null
    }
}

fun main() {
    val result = ImperativeProgram.findFirstValidElement(snippets)
    println(result)
}
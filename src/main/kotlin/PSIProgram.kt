package org.example

import kotlin.random.Random

object PSIProgram {
    data class CodeSnippet(val code: String)

    val snippets = listOf(
        CodeSnippet("val x = 1"),
        CodeSnippet("val y = 2 // trust me bro"),
        CodeSnippet("val z = x + y"),
        CodeSnippet("if (x > 0) println(\"x is positive\") else println(\"x is negative\")"),
        CodeSnippet("for (i in 1..5) println(i)"),
        CodeSnippet("while (x < 5) { println(\"i = \$x\"); x++ }"),
        CodeSnippet("val array = arrayOf(1, 2, 3)"),
        CodeSnippet("for (i in array) println(i)"),
        CodeSnippet("val list = listOf(1, 2, 3)"),
        CodeSnippet("for (i in list) println(i)"),
        CodeSnippet("val map = mapOf(1 to \"one\", 2 to \"two\", 3 to \"three\")"),
        CodeSnippet("for ((k, v) in map) println(\"key = \$k, value = \$v\")")
    )

    abstract class PSIElement(val subElements: List<PSIElement>, val originalCodeSnippet: CodeSnippet? = null) {
        open fun isValid(): Boolean = subElements.all { it.isValid() }
    }

    class PSIVariable(val name: String, val value: Int) : PSIElement(emptyList()) {
        override fun isValid(): Boolean = true
    }

    class PSILiteral(val value: Int) : PSIElement(emptyList()) {
        override fun isValid(): Boolean = true
    }

    class PSIAssignment(val variable: PSIVariable, val value: PSILiteral)
        : PSIElement(listOf(variable, value))
    class PSIExpression(subElements: List<PSIElement>)
        : PSIElement(subElements)
    class PSIIfExpression(val condition: PSIExpression, val ifBody: PSIElement, val elseBody: PSIElement)
        : PSIElement(listOf(condition, ifBody, elseBody))
    class PSILoopStatement(val generator: PSIExpression, val body: PSIElement)
        : PSIElement(listOf(generator, body))

    class ValidPSIElement(val snippet: CodeSnippet) : PSIElement(emptyList(), snippet) {
        override fun isValid(): Boolean = true
        override fun toString(): String = snippet.code
    }

    class InvalidPSIElement(val snippet: CodeSnippet) : PSIElement(emptyList(), snippet) {
        override fun isValid(): Boolean = false
        override fun toString(): String = snippet.code
    }

    private val random = Random(System.currentTimeMillis())

    fun convert(codeSnippet: CodeSnippet, verbose: Boolean = false): PSIElement {
        if (verbose) println("converting $codeSnippet")
        val snippet =
            if (random.nextBoolean()) ValidPSIElement(codeSnippet)
            else InvalidPSIElement(codeSnippet)
        if (verbose) println("converted $codeSnippet to $snippet")
        return snippet
    }

    fun validate(psiElement: PSIElement, verbose: Boolean = false): Boolean {
        val result = psiElement.isValid()
        if (verbose) println("$psiElement is valid: $result")
        return result
    }

    fun nullableConvert(codeSnippet: CodeSnippet, verbose: Boolean = false): PSIElement? {
        if (verbose) println("unsafely converting $codeSnippet")
        val snippet = when (random.nextInt(0, 3)) {  // 0 - null, 1 - valid, 2 - invalid
            1 -> ValidPSIElement(codeSnippet)
            2 -> InvalidPSIElement(codeSnippet)
            else -> null
        }
        if (verbose) println("unsafely converted $codeSnippet to $snippet")
        return snippet
    }

    fun unsafeValidate(psiElement: PSIElement, verbose: Boolean = false): Boolean {
        if (random.nextBoolean()) {
            if (verbose) println("Validation failed for $psiElement")
            error("Validation failed")
        }
        val result = psiElement.isValid()
        if (verbose) println("$psiElement is valid: $result")
        return result
    }

    fun otherValidate(psiElement: PSIElement, verbose: Boolean = false): Boolean {
        if (verbose) println("This is the other validation for $psiElement")
        return psiElement.originalCodeSnippet?.code?.endsWith("// trust me bro") == true
    }

    fun variableValidate(variableElement: PSIVariable, verbose: Boolean = false): Boolean {
        if (verbose) println("This is the variable validation for $variableElement")
        return variableElement.name == "x" && variableElement.value > 0
    }

    fun literalValidate(literalElement: PSILiteral, verbose: Boolean = false): Boolean {
        if (verbose) println("This is the literal validation for $literalElement")
        return literalElement.value > 0
    }
}

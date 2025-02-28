package org.example.finished

import arrow.core.Either
import arrow.core.flatMap
import org.example.PSIProgram.CodeSnippet
import org.example.PSIProgram.PSIElement
import org.example.PSIProgram.PSILiteral
import org.example.PSIProgram.PSIVariable
import org.example.PSIProgram.convert
import org.example.PSIProgram.nullableConvert
import org.example.PSIProgram.validate
import org.example.PSIProgram.literalValidate
import org.example.PSIProgram.otherValidate
import org.example.PSIProgram.unsafeValidate
import org.example.PSIProgram.snippets
import org.example.PSIProgram.variableValidate

object Imperative {
    // 1
    fun findFirstValidElement(snippets: List<CodeSnippet>): PSIElement? {
        for (snippet in snippets) {
            val psiElement = convert(snippet)
            if (validate(psiElement)) return psiElement
        }
        return null
    }

    // 2
    fun findFirstValidElementComplicated(snippets: List<CodeSnippet>): PSIElement? {
        for (snippet in snippets) {
            val psiElement = nullableConvert(snippet)
            if (psiElement != null) {
 /*               try {
                    if (unsafeValidation(psiElement)) return psiElement // write the `safe`method out of it
                } catch (e: Exception) {
                    // ...
                }*/
                if (safe { unsafeValidate(psiElement) }) return psiElement
                if (otherValidate(psiElement)) return psiElement
                if (psiElement is PSIVariable && variableValidate(psiElement)) return psiElement
            }
        }
        return null
    }
}

object Naive {
    // 3
    fun findFirstValidElement(snippets: List<CodeSnippet>): PSIElement? =
        snippets
            .find { validate(convert(it)) }
            ?.let { convert(it) } // complex conversion done twice!
}

inline fun <T, R: Any> Iterable<T>.collectFirst(transform: (T) -> R?): R? = firstNotNullOfOrNull(transform)

fun safe(validate: () -> Boolean): Boolean =
    try {
        validate()
    } catch (e: Exception) {
        false
    }

fun <T> safe(convert: () -> T?): T? =
    try {
        convert()
    } catch (e: Exception) {
        null
    }

object CollectFirst {
    // 4
    fun findFirstValidElement(snippets: List<CodeSnippet>): PSIElement? =
        snippets.collectFirst {
            convert(it).takeIf { validate(it) }
        }
}

object MoreThanOneValidation {
    // 5
    fun findFirstValidTwoElements(snippets: List<CodeSnippet>): PSIElement? =
        snippets.collectFirst {
            nullableConvert(it)?.takeIf {
                safe { unsafeValidate(it) } || otherValidate(it) || (it is PSIVariable && variableValidate(it))
            }
        }

    // 6
    fun findFirstValidManyElements(snippets: List<CodeSnippet>): PSIElement? =
        snippets.collectFirst {
            when (val el = nullableConvert(it)) {
                null                                     -> null
                is PSIVariable if variableValidate(el) -> el
                is PSILiteral  if literalValidate(el)  -> el
                else           if validate(el)         -> el
                else                                     -> null
            }
        }
}

object AsSequence {
    // 7
    fun findFirstValidElement(snippets: List<CodeSnippet>): PSIElement? =
        snippets
            .asSequence()
            .map { convert(it, verbose = true) }
            .firstOrNull { validate(it, verbose = true) }

    fun findFirstValidElementUnsafe(snippets: List<CodeSnippet>): PSIElement? =
        snippets
            .asSequence()
            .mapNotNull { nullableConvert(it, verbose = true) }
            .firstOrNull {
                safe { unsafeValidate(it, verbose = true) } || otherValidate(it, verbose = true)
            }
}

object WithArrow {
    enum class PSIError {
        FailedConversion, FailedValidation, InvalidElement
    }
    // 8
    fun eitherConversion(codeSnippet: CodeSnippet): Either<PSIError, PSIElement> =
        nullableConvert(codeSnippet)
            ?.let { Either.Right(it) }
            ?: Either.Left(PSIError.FailedConversion)

    fun eitherValidation(psiElement: PSIElement): Either<PSIError, Boolean> =
        Either
            .catch { unsafeValidate(psiElement) }
            .mapLeft { PSIError.FailedValidation }

    fun convertAndValidate(snippet: CodeSnippet): Either<PSIError, PSIElement> {
        val el = eitherConversion(snippet)
        val validated = el.flatMap { eitherValidation(it) }
        return when (validated) {
            is Either.Left                     -> validated
            is Either.Right if !validated.value -> Either.Left(PSIError.InvalidElement)
            else -> el
        }
    }

    fun findFirstValidElement(snippets: List<CodeSnippet>): PSIElement? =
        snippets.collectFirst { convertAndValidate(it).getOrNull() }
}

fun main() {
    val result = WithArrow.findFirstValidElement(snippets)
    println(result)
}
package com.tuto.kotlin.arrow

import arrow.core.Option
import arrow.core.extensions.option.applicative.applicative
import arrow.data.extensions.list.traverse.sequence

fun main() {
    println("Hello")

    val listofOptionalNumbers: List<Option<Int>> =
        listOf(Option(1), Option(2), Option(3))

    val sequenceOptions = listofOptionalNumbers.sequence(Option.applicative())

}


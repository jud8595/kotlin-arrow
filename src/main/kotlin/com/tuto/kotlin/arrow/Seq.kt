package com.tuto.kotlin.arrow

//import arrow.core.extensions.either.applicative.map2
//import arrow.core.extensions.either.applicative.product
import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.either.apply.product
import arrow.core.extensions.either.monad.monad
import arrow.core.extensions.listk.semigroup.semigroup
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.apply.product
import arrow.core.extensions.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.data.extensions.list.traverse.traverse

fun main() {
    println("Hello")

    // traverse
    val listofOptionalNumbers: List<Option<Int>> = listOf(Option(1), Option(2), Option(3))
    val traverseOptions = listofOptionalNumbers.traverse(Option.applicative(), ::identity).fix()

    // Option
    val opt1: Option<String> = None.map { a -> "str $a" }
    val opt2: Option<String> = None.flatMap { a -> Some("str $a") }
    println("opt1: $opt1")
    println("opt2: $opt2")

    val product1: Option<Tuple2<Int, Int>> = Some(1).product(Some(2)).fix()
    val product2: Option<Tuple2<Int, Int>> = Some(1).product(None).fix()
    println("product1: $product1")
    println("product2: $product2")

    // Either
    val toEither: (a:String) -> Either<Int, String> = { a -> if (a.contains("y")) Left(0) else Right(a) }
    val eitherA = toEither("Hello")
    val eitherB = toEither("my")
    val map1 = Either.applicative<Int>().map(eitherA, eitherB, { (a,b) -> a + b })
    val map2 = eitherA.product(eitherB).map { (a,b) -> a + b }
    val map3 = Either.monad<Int>().map(eitherA, eitherB, { (a,b) -> a + b })
    //val map4 = eitherA.map2(eitherB, { t -> t.a + t.b })
    println("map1: $map1")
    println("map2: $map2")
    println("map3: $map3")
    //println("map4: $map4")

    val monad: Either<Int, String> = Either.monad<Int>().fx.monad {
        val (a) = toEither("Hello")
        val (b) = toEither("my")
        toEither(a + b).bind()
    }.fix()

    println("monad: $monad")

    // Validated
    val toValidated: (a:String) -> Validated<ListK<String>, String> = { a -> if (a.contains("y")) Invalid(listOf("x"+a).k()) else Valid(a) }
    val vmap1 = Validated.applicative(ListK.semigroup<String>()).map(toValidated("Helloy"), toValidated("my"), { (a, b) -> a + b })
    println("vmap1: $vmap1")

}


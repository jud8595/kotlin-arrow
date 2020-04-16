package com.tuto.kotlin.arrow

import arrow.core.Eval
import arrow.core.Id

fun main() {
    val eager = Eval.now(5).map { it + 1 }
    val veager = eager.value()
    println(veager)

    val lazy = Eval.later { 5 }.map { it + 1 }
    val vlazy = lazy.value()  // value is cached
    println(vlazy)

    val always = Eval.always { 5 }.map { it + 1 }
    val valways = lazy.value()  // value is not cached
    println(valways)


    printEval("even", Eval.always { even(99999999) })
    //printEval(evenEval(999999))
    printEval("evenEval2", evenEval2(99999999))
    printEval("evenEval3", evenEval3(99999999))
    printEval("evenEval4", evenEval4(99999999))
    printEval("evenEval5", Eval.always { evenEval5(99999999).extract() })
}

fun printEval(str: String, eval: Eval<Boolean>) {
    try {
        println(str + ": " + eval.value())
    } catch (s: StackOverflowError) { println("stackOverFlow!") }
}

// even(10) -> odd(9) -> even(8) => ne fonctionne pas.

fun even(n: Int): Boolean =
    if (n == 0)
        true
    else if (n == 1)
        false
    else odd(n-1)

fun odd(n: Int): Boolean =
    if (n == 1)
        true
    else if (n == 0)
        false
    else even(n-1)

// evenEval(10) -> oddEval(9) -> evenEval(8) => ne fonctionne pas non plus. On a juste modifié le datatype de retour. Il faut que chaque appel retourne directement un Eval

fun evenEval(n: Int): Eval<Boolean> =
        if (n == 0)
            Eval.now(true)
        else if (n == 1)
            Eval.now(false)
        else oddEval(n-1)

fun oddEval(n: Int): Eval<Boolean> =
        if (n == 1)
            Eval.now(true)
        else if (n == 0)
            Eval.now(false)
        else evenEval(n-1)

// evenEval2(10): Eval.always { oddEval2(9).value() } => ca va évaluer oddEval2(9). Le prob est value() dans les always qui annule le prochain Eval.always
// C'est ce que dit la doc: "It is also not good style to create Eval instances whose computation involves calling .value on another Eval instance – this can defeat the trampolining and lead to stack overflows."

fun evenEval2(n: Int): Eval<Boolean> =
        if (n == 0)
            Eval.now(true)
        else if (n == 1)
            Eval.now(false)
        else Eval.always { oddEval2(n-1).value() }

fun oddEval2(n: Int): Eval<Boolean> =
        if (n == 1)
            Eval.now(true)
        else if (n == 0)
            Eval.now(false)
        else Eval.always { evenEval2(n-1).value() }

// evenEval3(10): Eval.always { true }.flatMap { addEval3(9) }
// Eval.always { true }.flatMap { addEval3(9) }
// Eval.always { true }.flatMap { Eval.always { true }.flatMap { evenEval3(8) } }
// ...
// Rmq: D'après la doc, https://arrow-kt.io/docs/0.10/apidocs/arrow-core-data/arrow.core/-eval/,
// "methods, which use an internal trampoline to avoid stack overflows. Computation done within .map and .flatMap is always done lazily, even when applied to a Now instance."
// Ce qui évite donc les stackoverflow n'est pas l'utilisation de Eval, mais l'implémentation de flatMap qui évite les stackoverflows.

// A retenir, le pattern Eval.always { true }.flatMap{ f() } permet d'éviter les StackOverFlowError dans les appels récursifs

fun evenEval3(n: Int): Eval<Boolean> =
        if (n == 0)
            Eval.now(true)
        else if (n == 1)
            Eval.now(false)
        else Eval.always { true }.flatMap { oddEval3(n-1) }

fun oddEval3(n: Int): Eval<Boolean> =
        if (n == 1)
            Eval.now(true)
        else if (n == 0)
            Eval.now(false)
        else Eval.always { true }.flatMap { evenEval3(n-1) }

// On vérifie que ça marche avec Eval.now

fun evenEval4(n: Int): Eval<Boolean> =
        if (n == 0)
            Eval.now(true)
        else if (n == 1)
            Eval.now(false)
        else Eval.now { true }.flatMap { oddEval4(n-1) }

fun oddEval4(n: Int): Eval<Boolean> =
        if (n == 1)
            Eval.now(true)
        else if (n == 0)
            Eval.now(false)
        else Eval.now { true }.flatMap { evenEval4(n-1) }

// On vérifie que ça ne marche pas avec un autre datatype que Eval, par exemple Id

fun evenEval5(n: Int): Id<Boolean> =
        if (n == 0)
            Id(true)
        else if (n == 1)
            Id(false)
        else Id { true }.flatMap { oddEval5(n-1) }

fun oddEval5(n: Int): Id<Boolean> =
        if (n == 1)
            Id(true)
        else if (n == 0)
            Id(false)
        else Id { true }.flatMap { evenEval5(n-1) }

// Solution de la doc Eval (https://arrow-kt.io/docs/0.10/apidocs/arrow-core-data/arrow.core/-eval/)

fun evenRight(n: Int): Eval<Boolean> =
        Eval.always { n == 0 }.flatMap { if (it==true) Eval.now(true) else oddRight(n-1) }

fun oddRight(n: Int): Eval<Boolean> =
        Eval.always { n == 0 }.flatMap { if (it==false) Eval.now(true) else evenRight(n-1) }

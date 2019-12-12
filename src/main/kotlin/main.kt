/*
    nm(c|d)<k>n<n|m>

  INIT: n -> s1, 0
    s1: m -> s2, 0
    s2: ( -> s3, 0
    s3: c -> s4, 0
        d -> s4, 0
    s4: ) -> s5, 0
    s5: k -> s6, 0
    s6: k -> s6, 0
        n -> s7, 0
    s7: n -> s8, 0
        m -> s8, 0
    s8: n -> s8, 0
        m -> s8, 0
      EOF -> s9, 0
    s9:   -> s9, 1

 */

fun main() {
    val lexer = Lexer("nm(c|d)<k>n<n|m>")

    println(lexer.exactMatch("nm(c)kkknmmm"))
    println(lexer.exactMatch("nm(c)kkkmmmm"))
    println(lexer.exactMatch("nm(d)kkkkknmnm"))
}
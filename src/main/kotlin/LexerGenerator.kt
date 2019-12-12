import java.lang.StringBuilder

class Lexer(private val regex: String) {

    private fun lex(): List<Lexeme> {
        return regex.map {
            when (it) {
                '|' -> LOr
                '<' -> LLeftIter
                '>' -> LRightIter
                else -> LCharacter(listOf(it))
            }
        }
    }

    private fun lexAndJoinOrs(): List<Lexeme> {
        val ml = lex().toMutableList()
        var i = 0
        while (i <= ml.size) {
            val pr2 = ml.getOrNull(i - 2)
            val pr = ml.getOrNull(i - 1)
            val cr = ml.getOrNull(i)
            if (pr2 is LCharacter && pr is LOr && cr is LCharacter) {
                ml[i - 2] = LCharacter(pr2.chars + cr.chars)
                ml.removeAt(i - 1)
                ml.removeAt(i - 1)
                i -= 2
            }
            i++
        }
        return ml
    }

    private fun parse(): Sentence {
        val join = lexAndJoinOrs()
        fun parse(caret: Int): Pair<Sentence, Int> {
            val ml = mutableListOf<Phrase>()
            var i = caret
            while (i < join.size) {
                when (val l = join[i]) {
                    is LCharacter -> ml.add(LCharacter(l.chars))
                    is LLeftIter -> {
                        val (p, j) = parse(i + 1)
                        ml += p
                        i = j
                    }
                    is LRightIter -> {
                        return Sentence(ml.toList()) to i
                    }
                }
                i++
            }
            return Sentence(ml.toList()) to (i + 1)
        }
        return Sentence(parse(0).first.phrases + FinalPhrase)
    }

    private val states = mutableListOf<State>()
    private val finalState: State
    private val initialState: State

    init {
        val sentence = parse().phrases
        var currentState = State()
        initialState = currentState
        sentence.forEach { currentState = it.handle(currentState) }
        finalState = State()
        currentState.on(null, finalState)
        states.forEach { println(it) }
    }

    private interface Lexeme

    private inner class LCharacter(private val _chars: List<Char>) : Lexeme, Phrase {
        override val chars: List<Char>
            get() = _chars

        override fun toString(): String =
            chars.joinToString("|") { "$it" }

        override fun handle(initialState: State): State {
            val nextState = State()
            chars.forEach {
                initialState.on(it, nextState)
            }
            return nextState
        }
    }

    private object LLeftIter : Lexeme {
        override fun toString() = "<"
    }

    private object LRightIter : Lexeme {
        override fun toString() = ">"
    }

    private object LOr : Lexeme {
        override fun toString() = "|"
    }

    private interface Phrase {
        val chars: List<Char>

        fun handle(initialState: State): State
    }

    private object FinalPhrase : Phrase {
        override fun toString(): String = ""

        override val chars: List<Char>
            get() = listOf()

        override fun handle(initialState: State): State {
            return initialState
        }
    }

    private inner class State {
        val index = states.size

        init {
            states.add(this)
        }

        val transitions = mutableMapOf<Char?, State>()

        fun on(char: Char?, state: State) {
            transitions[char] = state
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.appendln("s$index:")
            for ((key, value) in transitions)
                sb.appendln("$key -> s${value.index}")
            return sb.toString()
        }
    }

    private class Sentence(val phrases: List<Phrase>) : Phrase {
        override fun toString(): String {
            return "<${phrases.joinToString("")}>"
        }

        override val chars: List<Char>
            get() = phrases.first().chars

        override fun handle(initialState: State): State {
            var currentState = initialState
            var firstState = null as State?
            phrases.forEach {
                currentState = it.handle(currentState)
                if (firstState == null)
                    firstState = currentState
            }
            val firstStateFinal = firstState
            if (firstStateFinal != null)
                chars.forEach { currentState.on(it, firstStateFinal) }
            return currentState
        }
    }

    fun exactMatch(string: String): Boolean {
        var cs = initialState
        for (c in string) {
            val nextState = cs.transitions[c]
            if (nextState == null)
                return false
            else
                cs = nextState
        }
        return cs.transitions[null] == finalState
    }
}
package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import java.util.HashMap
import kotlin.collections.HashSet
import kotlin.collections.set
import kotlin.reflect.KFunction

class Context(var parent: Context?, val name: String, var obj: Any?,
              val defaults: Array<Any?>,
              internal val command: KFunction<*>,
              internal val longOptParsers: Map<String, LongOptParser>,
              internal val shortOptParsers: Map<String, ShortOptParser>,
              internal val subcommands: HashSet<Context>) {
    companion object {
        fun fromFunction(command: KFunction<*>): Context {
            val longOptParsers = HashMap<String, LongOptParser>()
            val shortOptParsers = HashMap<String, ShortOptParser>()
            val defaults = arrayOfNulls<Any?>(command.parameters.size)

            fun registerNames(shortParser: ShortOptParser, longParser: LongOptParser, vararg names: String) {
                for (name in names) {
                    when {
                        name.isEmpty() -> Unit
                        name.startsWith("--") -> longOptParsers[name] = longParser
                        name.startsWith("-") -> shortOptParsers[name] = shortParser
                        else -> throw IllegalArgumentException("Invalid option name: $name")
                    }
                }
            }

            // Set up long options
            for (param in command.parameters) {
                for (anno in param.annotations) {
                    when (anno) {
                        is IntOption -> {
                            // TODO typechecks, check name format
                            defaults[param.index] = anno.default
                            val parser = OptionParser(param.index, IntParamType)
                            registerNames(parser, parser, anno.name, anno.alternateName)
                        }
                        is FlagOption -> {
                            defaults[param.index] = false
                            val parser = FlagOptionParser(param.index)
                            registerNames(parser, parser, anno.name, anno.alternateName)
                        }
                        else -> TODO()
                    }
                }
            }
            val name = command.name // TODO allow customization
            return Context(null, name, null, defaults, command, longOptParsers,
                    shortOptParsers, HashSet())
        }
    }
}
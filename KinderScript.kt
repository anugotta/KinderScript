import java.io.File

// Define the scope for variables and functions
class Scope(val parent: Scope? = null) {
    private val variables = mutableMapOf<String, Any>()
    private val functions = mutableMapOf<String, Block.Function>()

    fun setVariable(name: String, value: Any) {
        variables[name] = value
    }

    fun getVariable(name: String): Any? {
        return variables[name] ?: parent?.getVariable(name)
    }

    fun setFunction(name: String, function: Block.Function) {
        functions[name] = function
    }

    fun getFunction(name: String): Block.Function? {
        return functions[name] ?: parent?.getFunction(name)
    }
}

// Sealed class to represent different types of blocks/commands
sealed class Block {
    data class Print(val message: String) : Block()
    data class MathOperation(val operation: String, val numbers: List<Int>) : Block()
    data class CodeBlock(val blocks: List<Block>) : Block()
    data class Repeat(val times: Int, val block: Block) : Block()
    data class Function(val name: String, val parameters: List<String>, val block: Block) : Block()
    data class FunctionCall(val name: String, val arguments: List<String>) : Block()
    data class Variable(val name: String, val value: Any) : Block()
    data class If(val condition: String, val thenBlock: Block, val elseBlock: Block?) : Block()
    data class SetVariable(val name: String, val value: Any) : Block()
}

// Version information
const val KINDERSCRIPT_VERSION = "1.2.0"

// Main interpreter class
class KinderScript {
    private val globalScope = Scope()
    private val mathOperations = setOf("add", "subtract", "multiply", "divide", "modulo", "power")
    
    fun parseFile(fileName: String) {
        try {
            println("Reading KinderScript file: $fileName")
            val content = File(fileName).readText()
            
            // Debug: Print file content with line numbers
            // println("\nDebug: File contents:")
            // content.lines().forEachIndexed { index, line ->
            //     println("${index + 1}: $line")
            // }
            
            val cleanContent = content.trim() // Remove leading/trailing whitespace
            if (cleanContent.isEmpty()) {
                throw Exception("File is empty or contains only whitespace")
            }
            
            val blocks = parseBlocks(cleanContent)
            executeBlocks(blocks, globalScope)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            println("\nTroubleshooting tips:")
            println("1. Ensure there are no empty lines at the start of the file")
            println("2. Each command should start at the beginning of a line")
            println("3. Valid commands are: say, function, call, repeat, add(), subtract(), multiply(), divide()")
            println("4. Check for any hidden characters or extra spaces")
            e.printStackTrace()
        }
    }

    private fun parseBlocks(content: String): List<Block> {
        val blocks = mutableListOf<Block>()
        var currentIndex = 0

        while (currentIndex < content.length) {
            // Skip whitespace and newlines
            while (currentIndex < content.length && 
                   (content[currentIndex].isWhitespace() || content[currentIndex] == '\n' || 
                    content[currentIndex] == '\r')) {
                currentIndex++
            }
            
            if (currentIndex >= content.length) break

            try {
                val (block, newIndex) = parseNextBlock(content, currentIndex)
                blocks.add(block)
                currentIndex = newIndex
            } catch (e: Exception) {
                // Print debug information about the parsing failure
                val contextStart = maxOf(0, currentIndex - 20)
                val contextEnd = minOf(content.length, currentIndex + 20)
                println("\nDebug: Error occurred near:")
                println(content.substring(contextStart, contextEnd))
                println(" ".repeat(currentIndex - contextStart) + "^")
                throw e
            }
        }

        return blocks
    }

    private fun parseNextBlock(content: String, startIndex: Int): Pair<Block, Int> {
        var currentIndex = startIndex
        
        // Skip whitespace
        while (currentIndex < content.length && 
               (content[currentIndex].isWhitespace() || content[currentIndex] == '\n' || 
                content[currentIndex] == '\r')) {
            currentIndex++
        }

        // Get the command
        val command = content.substring(currentIndex).takeWhile { 
            !it.isWhitespace() && it != '(' && it != '{' && it != '\n' && it != '\r'
        }
        
        if (command.isEmpty()) {
            throw Exception("Empty command found at position $currentIndex")
        }
        
        currentIndex += command.length

        return when (command.lowercase()) {
            "say" -> {
                // Skip whitespace after 'say'
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Find end of message (end of line or end of content)
                val endOfLine = content.indexOf('\n', currentIndex)
                val message = if (endOfLine == -1) {
                    content.substring(currentIndex).trim()
                } else {
                    content.substring(currentIndex, endOfLine).trim()
                }
                
                Pair(Block.Print(message), if (endOfLine == -1) content.length else endOfLine)
            }

            "function" -> {
                // Skip whitespace after 'function'
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Parse function name
                val nameEnd = content.indexOf('(', currentIndex)
                val name = if (nameEnd != -1) {
                    content.substring(currentIndex, nameEnd).trim()
                } else {
                    // Fallback to space-separated name
                    val spaceEnd = content.indexOfFirst { it.isWhitespace() || it == '{' }
                    content.substring(currentIndex, spaceEnd).trim()
                }
                
                currentIndex = if (nameEnd != -1) nameEnd + 1 else content.indexOf('{', currentIndex)
                
                // Modify parameter parsing to handle $ prefix
                val parameters = if (nameEnd != -1) {
                    val paramsEnd = content.indexOf(')', currentIndex)
                    if (paramsEnd == -1) {
                        throw Exception("Unclosed parentheses in function definition")
                    }
                    
                    val paramString = content.substring(currentIndex, paramsEnd).trim()
                    if (paramString.isNotEmpty()) 
                        paramString.split(',').map { it.trim().removePrefix("$") } 
                        else emptyList()
                } else {
                    // Fallback to space-separated parameters
                    val funcBodyStart = content.indexOf('{', currentIndex)
                    val paramString = content.substring(currentIndex, funcBodyStart).trim()
                    if (paramString.isNotEmpty()) 
                        paramString.split(Regex("\\s+")).map { it.removePrefix("$") }
                        else emptyList()
                }
                
                // Find opening brace for function body
                val openBraceIndex = content.indexOf('{', currentIndex)
                if (openBraceIndex == -1) {
                    throw Exception("Missing function body")
                }
                
                val (bodyBlock, endIndex) = parseCodeBlock(content, openBraceIndex)
                
                val function = Block.Function(name, parameters, bodyBlock)
                globalScope.setFunction(name, function)
                
                Pair(function, endIndex)
            }
            
            "call" -> {
                // Skip whitespace after 'call'
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Parse function name
                val nameEnd = content.indexOf('(', currentIndex)
                val name = if (nameEnd != -1) {
                    content.substring(currentIndex, nameEnd).trim()
                } else {
                    // Fallback to space-separated parsing
                    val endOfLine = content.indexOf('\n', currentIndex).let { 
                        if (it == -1) content.length else it 
                    }
                    content.substring(currentIndex, endOfLine).trim().split(Regex("\\s+")).first()
                }
                
                // Parse arguments
                val arguments = if (nameEnd != -1) {
                    val argsEnd = content.indexOf(')', nameEnd)
                    if (argsEnd == -1) {
                        throw Exception("Unclosed parentheses in function call")
                    }
                    
                    val argString = content.substring(nameEnd + 1, argsEnd).trim()
                    if (argString.isNotEmpty()) 
                        argString.split(',').map { it.trim() } 
                        else emptyList()
                } else {
                    // Fallback to space-separated arguments
                    val parts = content.substring(currentIndex).trim().split(Regex("\\s+"))
                    if (parts.size > 1) parts.drop(1) else emptyList()
                }
                
                Pair(Block.FunctionCall(name, arguments), 
                     content.indexOf('\n', currentIndex).let { 
                         if (it == -1) content.length else it 
                     })
            }

            "repeat" -> {
                // Skip whitespace after 'repeat'
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Parse number of repetitions
                val timesStr = content.substring(currentIndex).takeWhile { 
                    !it.isWhitespace() && it != '{' 
                }
                val times = timesStr.toInt()
                
                currentIndex += timesStr.length
                
                // Find opening brace for repeat body
                val openBraceIndex = content.indexOf('{', currentIndex)
                if (openBraceIndex == -1) {
                    throw Exception("Missing opening brace for repeat")
                }
                
                val (bodyBlock, endIndex) = parseCodeBlock(content, openBraceIndex)
                
                Pair(Block.Repeat(times, bodyBlock), endIndex)
            }

            "if" -> {
                // Skip whitespace after 'if'
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Find opening brace for if body
                val openBraceIndex = content.indexOf('{', currentIndex)
                if (openBraceIndex == -1) {
                    throw Exception("Missing opening brace for if statement")
                }
                
                // Extract condition (everything before the opening brace)
                val condition = content.substring(currentIndex, openBraceIndex).trim()
                if (condition.isEmpty()) {
                    throw Exception("If statement requires a condition")
                }
                
                val (thenBlock, thenEndIndex) = parseCodeBlock(content, openBraceIndex)
                
                // Check for else
                var elseBlock: Block? = null
                var finalIndex = thenEndIndex
                
                // Skip whitespace after then block
                var checkIndex = thenEndIndex
                while (checkIndex < content.length && 
                       (content[checkIndex].isWhitespace() || content[checkIndex] == '\n' || content[checkIndex] == '\r')) {
                    checkIndex++
                }
                
                // Check if next command is "else"
                if (checkIndex < content.length) {
                    val nextCommand = content.substring(checkIndex).takeWhile { 
                        !it.isWhitespace() && it != '{' && it != '\n' && it != '\r'
                    }
                    
                    if (nextCommand.lowercase() == "else") {
                        val elseStartIndex = checkIndex + nextCommand.length
                        val elseOpenBraceIndex = content.indexOf('{', elseStartIndex)
                        if (elseOpenBraceIndex == -1) {
                            throw Exception("Missing opening brace for else statement")
                        }
                        
                        val (elseBlockParsed, elseEndIndex) = parseCodeBlock(content, elseOpenBraceIndex)
                        elseBlock = elseBlockParsed
                        finalIndex = elseEndIndex
                    }
                }
                
                Pair(Block.If(condition, thenBlock, elseBlock), finalIndex)
            }

            "set" -> {
                // Skip whitespace after 'set'
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Parse variable name (with or without $)
                val varNameEnd = content.indexOf('=', currentIndex)
                if (varNameEnd == -1) {
                    throw Exception("Set command requires '=' to assign value")
                }
                
                val varName = content.substring(currentIndex, varNameEnd).trim().removePrefix("$")
                if (varName.isEmpty()) {
                    throw Exception("Set command requires a variable name")
                }
                
                currentIndex = varNameEnd + 1
                
                // Skip whitespace after '='
                while (currentIndex < content.length && content[currentIndex].isWhitespace()) {
                    currentIndex++
                }
                
                // Find end of value (end of line or end of content)
                val endOfLine = content.indexOf('\n', currentIndex)
                val valueStr = if (endOfLine == -1) {
                    content.substring(currentIndex).trim()
                } else {
                    content.substring(currentIndex, endOfLine).trim()
                }
                
                // Try to parse as integer, otherwise use as string
                val value = try {
                    valueStr.toInt()
                } catch (e: NumberFormatException) {
                    // Remove quotes if present
                    if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                        valueStr.substring(1, valueStr.length - 1)
                    } else {
                        valueStr
                    }
                }
                
                Pair(Block.SetVariable(varName, value), 
                     if (endOfLine == -1) content.length else endOfLine)
            }

            else -> {
                // Check for math operations with parentheses
                if (mathOperations.contains(command.lowercase()) && content[currentIndex] == '(') {
                    // Find closing parenthesis
                    val closingParenIndex = content.indexOf(')', currentIndex)
                    if (closingParenIndex == -1) {
                        throw Exception("Unclosed parentheses in math operation")
                    }
                    
                    // Extract numbers inside parentheses
                    val numberStr = content.substring(currentIndex + 1, closingParenIndex).trim()
                    
                    // Validate that numbers are provided
                    if (numberStr.isEmpty()) {
                        throw Exception("Math operation '${command}' requires at least one number")
                    }
                    
                    // Support comma-separated numbers
                    val numbers = numberStr
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .map { it.toInt() }
                    
                    if (numbers.isEmpty()) {
                        throw Exception("Math operation '${command}' requires at least one valid number")
                    }
                    
                    Pair(Block.MathOperation(command, numbers), 
                         content.indexOf('\n', closingParenIndex).let { 
                             if (it == -1) content.length else it 
                         })
                } else {
                    throw Exception("Unknown command: '$command'. Valid commands are: say, function, call, repeat, if, set, add(), subtract(), multiply(), divide(), modulo(), power()")
                }
            }
        }
    }

    private fun parseCodeBlock(content: String, openBraceIndex: Int): Pair<Block, Int> {
        var braceCount = 1
        var currentIndex = openBraceIndex + 1
        val blocks = mutableListOf<Block>()

        while (braceCount > 0 && currentIndex < content.length) {
            when (content[currentIndex]) {
                '{' -> braceCount++
                '}' -> braceCount--
                else -> {
                    if (!content[currentIndex].isWhitespace()) {
                        val (block, newIndex) = parseNextBlock(content, currentIndex)
                        blocks.add(block)
                        currentIndex = newIndex
                    }
                }
            }
            currentIndex++
        }

        return Pair(Block.CodeBlock(blocks), currentIndex)
    }

    private fun executeBlocks(blocks: List<Block>, scope: Scope) {
        blocks.forEach { block -> executeBlock(block, scope) }
    }

    private fun executeBlock(block: Block, scope: Scope) {
        when (block) {
            is Block.Print -> {
                // Replace variable references in the message
                val processedMessage = replaceVariables(block.message, scope)
                println(processedMessage)
            }
            
            is Block.MathOperation -> {
                val result = when (block.operation.lowercase()) {
                    "add" -> block.numbers.sum()
                    "subtract" -> {
                        if (block.numbers.isEmpty()) throw Exception("Subtract requires at least one number")
                        block.numbers.reduce { acc, num -> acc - num }
                    }
                    "multiply" -> block.numbers.reduce { acc, num -> acc * num }
                    "divide" -> {
                        if (block.numbers.isEmpty()) throw Exception("Divide requires at least one number")
                        // Check for division by zero
                        val hasZero = block.numbers.drop(1).any { it == 0 }
                        if (hasZero) {
                            throw Exception("Division by zero is not allowed")
                        }
                        block.numbers.reduce { acc, num -> acc / num }
                    }
                    "modulo" -> {
                        if (block.numbers.size < 2) throw Exception("Modulo requires at least two numbers")
                        val hasZero = block.numbers.drop(1).any { it == 0 }
                        if (hasZero) {
                            throw Exception("Modulo by zero is not allowed")
                        }
                        block.numbers.reduce { acc, num -> acc % num }
                    }
                    "power" -> {
                        if (block.numbers.size < 2) throw Exception("Power requires at least two numbers")
                        var result = block.numbers[0].toDouble()
                        for (i in 1 until block.numbers.size) {
                            result = Math.pow(result, block.numbers[i].toDouble())
                        }
                        result.toInt()
                    }
                    else -> throw Exception("Unknown operation: ${block.operation}")
                }
                println("Result of ${block.operation}: $result")
            }
            
            is Block.CodeBlock -> {
                val newScope = Scope(scope)
                executeBlocks(block.blocks, newScope)
            }
            
            is Block.Repeat -> {
                repeat(block.times) {
                    executeBlock(block.block, scope)
                }
            }
            
            is Block.Function -> {
                // Function definition is stored in scope during parsing
            }
            
            is Block.FunctionCall -> {
                val function = scope.getFunction(block.name)
                if (function != null) {
                    // Validate parameter/argument count match
                    if (function.parameters.size != block.arguments.size) {
                        throw Exception("Function '${block.name}' expects ${function.parameters.size} parameter(s), but ${block.arguments.size} argument(s) were provided")
                    }
                    
                    val functionScope = Scope(scope)
                    
                    // Match arguments to parameters
                    function.parameters.zip(block.arguments).forEach { (param, arg) ->
                        // Try to resolve variable references
                        val resolvedArg = resolveArgument(arg, scope)
                        functionScope.setVariable(param, resolvedArg)
                    }
                    
                    executeBlock(function.block, functionScope)
                } else {
                    throw Exception("Function '${block.name}' not found!")
                }
            }
            
            is Block.Variable -> {
                scope.setVariable(block.name, block.value)
            }
            
            is Block.If -> {
                val conditionResult = evaluateCondition(block.condition, scope)
                if (conditionResult) {
                    executeBlock(block.thenBlock, scope)
                } else {
                    block.elseBlock?.let { executeBlock(it, scope) }
                }
            }
            
            is Block.SetVariable -> {
                // Resolve value if it's a variable reference
                val resolvedValue = if (block.value is String && block.value.toString().startsWith("$")) {
                    val varName = block.value.toString().removePrefix("$")
                    scope.getVariable(varName) ?: block.value
                } else {
                    block.value
                }
                scope.setVariable(block.name, resolvedValue)
            }
        }
    }
    
    // Helper method to evaluate conditions
    private fun evaluateCondition(condition: String, scope: Scope): Boolean {
        val trimmed = condition.trim()
        
        // Handle comparison operators: ==, !=, <, >, <=, >=
        val operators = listOf("==", "!=", "<=", ">=", "<", ">")
        for (op in operators) {
            if (trimmed.contains(op)) {
                val parts = trimmed.split(op, limit = 2)
                if (parts.size == 2) {
                    val left = resolveValue(parts[0].trim(), scope)
                    val right = resolveValue(parts[1].trim(), scope)
                    
                    return when (op) {
                        "==" -> left == right
                        "!=" -> left != right
                        "<" -> (left as? Number)?.toDouble() ?: 0.0 < (right as? Number)?.toDouble() ?: 0.0
                        ">" -> (left as? Number)?.toDouble() ?: 0.0 > (right as? Number)?.toDouble() ?: 0.0
                        "<=" -> (left as? Number)?.toDouble() ?: 0.0 <= (right as? Number)?.toDouble() ?: 0.0
                        ">=" -> (left as? Number)?.toDouble() ?: 0.0 >= (right as? Number)?.toDouble() ?: 0.0
                        else -> false
                    }
                }
            }
        }
        
        // If no operator, check if it's a truthy value
        val value = resolveValue(trimmed, scope)
        return when (value) {
            is Number -> value.toInt() != 0
            is String -> value.isNotEmpty() && value.lowercase() != "false"
            is Boolean -> value
            else -> true  // Non-null values are truthy
        }
    }
    
    // Helper method to resolve a value (variable or literal)
    private fun resolveValue(valueStr: String, scope: Scope): Any {
        // Remove $ prefix if present
        val cleanValue = valueStr.removePrefix("$")
        
        // Try to get as variable first
        val variable = scope.getVariable(cleanValue)
        if (variable != null) {
            return variable
        }
        
        // Try to parse as integer
        return try {
            cleanValue.toInt()
        } catch (e: NumberFormatException) {
            // Return as string (remove quotes if present)
            if (cleanValue.startsWith("\"") && cleanValue.endsWith("\"")) {
                cleanValue.substring(1, cleanValue.length - 1)
            } else {
                cleanValue
            }
        }
    }

    // Helper method to replace variable references in a string
    private fun replaceVariables(message: String, scope: Scope): String {
        return message.replace(Regex("\\$\\w+")) { matchResult ->
            // Remove the $ prefix when looking up the variable
            val variableName = matchResult.value.substring(1)
            scope.getVariable(variableName)?.toString() ?: matchResult.value
        }
    }

    // Helper method to resolve function call arguments
    private fun resolveArgument(arg: String, scope: Scope): Any {
        // Remove $ prefix if present
        val cleanArg = arg.removePrefix("$")

        // Try to parse as an integer first
        return try {
            cleanArg.toInt()
        } catch (e: NumberFormatException) {
            // If not an integer, try to resolve as a variable
            scope.getVariable(cleanArg) ?: cleanArg
        }
    }
}

fun main(args: Array<String>) {
    // Check for version flag
    if (args.isNotEmpty() && (args[0] == "--version" || args[0] == "-v")) {
        println("KinderScript version $KINDERSCRIPT_VERSION")
        return
    }
    
    // Check if a file name is provided
    if (args.isEmpty()) {
        println("KinderScript $KINDERSCRIPT_VERSION")
        println("Usage: java -jar KinderScript.jar <filename.kinder>")
        println("       java -jar KinderScript.jar --version")
        println("Please provide a KinderScript file to execute.")
        return
    }

    println("Welcome to KinderScript v$KINDERSCRIPT_VERSION!")
    println("Running your KinderScript program...")
    try {
        val kinderScript = KinderScript()
        kinderScript.parseFile(args[0])
    } catch (e: Exception) {
        println("\nError: ${e.message}")
        e.printStackTrace()
    }
}
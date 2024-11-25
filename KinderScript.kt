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
}

// Main interpreter class
class KinderScript {
    private val globalScope = Scope()
    private val mathOperations = setOf("add", "subtract", "multiply", "divide")
    
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
                    
                    // Support comma-separated numbers
                    val numbers = numberStr
                        .split(",")
                        .map { it.trim().toInt() }
                    
                    Pair(Block.MathOperation(command, numbers), 
                         content.indexOf('\n', closingParenIndex).let { 
                             if (it == -1) content.length else it 
                         })
                } else {
                    throw Exception("Unknown command: '$command'. Valid commands are: say, function, call, repeat, add(), subtract(), multiply(), divide()")
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
                    "subtract" -> block.numbers.reduce { acc, num -> acc - num }
                    "multiply" -> block.numbers.reduce { acc, num -> acc * num }
                    "divide" -> block.numbers.reduce { acc, num -> acc / num }
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
                    val functionScope = Scope(scope)
                    
                    // Match arguments to parameters
                    function.parameters.zip(block.arguments).forEach { (param, arg) ->
                        // Try to resolve variable references
                        val resolvedArg = resolveArgument(arg, scope)
                        functionScope.setVariable(param, resolvedArg)
                    }
                    
                    executeBlock(function.block, functionScope)
                } else {
                    println("Function ${block.name} not found!")
                }
            }
            
            is Block.Variable -> {
                scope.setVariable(block.name, block.value)
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
    // Check if a file name is provided
    if (args.isEmpty()) {
        println("Usage: java -jar KinderScript.jar <filename.kinder>")
        println("Please provide a KinderScript file to execute.")
        return
    }

    println("Welcome to KinderScript!")
    println("Running your KinderScript program...")
    try {
        val kinderScript = KinderScript()
        kinderScript.parseFile(args[0])
    } catch (e: Exception) {
        println("\nError: ${e.message}")
        e.printStackTrace()
    }
}
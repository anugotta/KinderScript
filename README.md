# KinderScript

A simple, kid-friendly programming language interpreter written in Kotlin!

**Version 1.2.0** - See [CHANGELOG.md](CHANGELOG.md) for version history.

## Overview

KinderScript is designed to introduce children to programming concepts in a fun and accessible way. It features a simple syntax with commands for printing messages, performing math operations, defining functions, creating loops, making decisions with conditionals, and storing data in variables.

## Features

- **Simple Commands**: Easy-to-understand syntax
- **Math Operations**: Add, subtract, multiply, divide, modulo, and power
- **Functions**: Define and call reusable functions with parameters
- **Loops**: Repeat blocks of code (including nested loops)
- **Variables**: Store and use variables with `set` command
- **Conditionals**: Make decisions with `if/else` statements
- **Error Handling**: Clear error messages for common mistakes
- **Versioning**: Built-in version tracking and display

## Installation

### Prerequisites
- Java 8 or higher
- Kotlin compiler (optional, for building from source)

### Quick Start

1. Compile the interpreter (if needed):
   ```bash
   kotlinc KinderScript.kt -include-runtime -d KinderScript.jar
   ```

2. Check the version:
   ```bash
   java -jar KinderScript.jar --version
   ```

3. Run a KinderScript program:
   ```bash
   java -jar KinderScript.jar program.kinder
   ```

## Complete Examples

Copy any of these examples into a `.kinder` file and run them instantly!

### Example 1: Your First Program
**Learn:** Basic output with `say`

```kinder
say Hello, World!
say Welcome to KinderScript!
say Programming is fun!
```

**Output:**
```
Hello, World!
Welcome to KinderScript!
Programming is fun!
```

---

### Example 2: Math Operations
**Learn:** Performing calculations with `add`, `subtract`, `multiply`, and `divide`

```kinder
say Let's do some math!

say Adding numbers:
add(5, 3, 2)

say Subtracting numbers:
subtract(20, 5, 3)

say Multiplying numbers:
multiply(4, 3, 2)

say Dividing numbers:
divide(100, 5, 2)
```

**Output:**
```
Let's do some math!
Adding numbers:
Result of add: 10
Subtracting numbers:
Result of subtract: 12
Multiplying numbers:
Result of multiply: 24
Dividing numbers:
Result of divide: 10
```

---

### Example 3: Functions with Parameters
**Learn:** Creating reusable functions that accept parameters

```kinder
function introduce($name, $hobby) {
say Hi! My name is $name
say I love $hobby
say Nice to meet you!
}

call introduce(Alice, reading)
call introduce(Bob, coding)
call introduce(Charlie, drawing)
```

**Output:**
```
Hi! My name is Alice
I love reading
Nice to meet you!
Hi! My name is Bob
I love coding
Nice to meet you!
Hi! My name is Charlie
I love drawing
Nice to meet you!
```

---

### Example 4: Simple Loops
**Learn:** Repeating code with `repeat`

```kinder
say Counting to 5:
repeat 5 {
say I can count!
}

say Let's sing Happy Birthday:
repeat 4 {
say Happy Birthday!
}
```

**Output:**
```
Counting to 5:
I can count!
I can count!
I can count!
I can count!
I can count!

Let's sing Happy Birthday:
Happy Birthday!
Happy Birthday!
Happy Birthday!
Happy Birthday!
```

---

### Example 5: Nested Loops
**Learn:** Loops inside loops for complex patterns

```kinder
say Creating a pattern:
repeat 3 {
say Row number
repeat 2 {
say  Item
}
}
```

**Output:**
```
Creating a pattern:
Row number
 Item
 Item
Row number
 Item
 Item
Row number
 Item
 Item
```

---

### Example 6: Functions with Loops
**Learn:** Combining functions and loops

```kinder
function celebrate($name) {
say Happy celebration, $name!
repeat 3 {
say Cheers!
}
say Party time!
}

function sing($name) {
say Let's sing for $name!
repeat 2 {
say Happy birthday to $name!
}
}

call celebrate(Emma)
call sing(Lucas)
```

**Output:**
```
Happy celebration, Emma!
Cheers!
Cheers!
Cheers!
Party time!

Let's sing for Lucas!
Happy birthday to Lucas!
Happy birthday to Lucas!
```

---

### Example 7: Complete Program - Birthday Card Generator
**Learn:** Putting it all together

```kinder
function birthdayCard($name, $age) {
say ============================
say Happy Birthday, $name!
say ============================
say You are turning $age years old!
say Let's celebrate:
repeat 3 {
say Hip hip hooray!
}
say Have an amazing day!
say ============================
}

call birthdayCard(Sarah, 8)
call birthdayCard(Max, 10)
```

**Output:**
```
============================
Happy Birthday, Sarah!
============================
You are turning 8 years old!
Let's celebrate:
Hip hip hooray!
Hip hip hooray!
Hip hip hooray!
Have an amazing day!
============================
============================
Happy Birthday, Max!
============================
You are turning 10 years old!
Let's celebrate:
Hip hip hooray!
Hip hip hooray!
Hip hip hooray!
Have an amazing day!
============================
```

---

### Example 8: Math Calculator
**Learn:** Using math operations in a practical way

```kinder
say Math Calculator Demo

say Adding my test scores:
add(85, 90, 92, 88)

say Finding the total cost:
multiply(5, 3)

say Calculating change:
subtract(100, 35, 12)

say Sharing equally:
divide(24, 3)
```

**Output:**
```
Math Calculator Demo

Adding my test scores:
Result of add: 355
Finding the total cost:
Result of multiply: 15
Calculating change:
Result of subtract: 53
Sharing equally:
Result of divide: 8
```

---

## Quick Reference

### All Available Commands

| Command | Description | Example |
|---------|-------------|---------|
| `say` | Print a message | `say Hello!` |
| `set` | Assign a variable | `set $age = 10` |
| `if` | Conditional statement | `if $age >= 18 { say Adult }` |
| `else` | Alternative block | `if $x > 0 { say Positive } else { say Negative }` |
| `add()` | Add numbers | `add(5, 3, 2)` |
| `subtract()` | Subtract numbers | `subtract(10, 3)` |
| `multiply()` | Multiply numbers | `multiply(4, 2)` |
| `divide()` | Divide numbers | `divide(100, 5)` |
| `modulo()` | Remainder after division | `modulo(17, 5)` |
| `power()` | Exponentiation | `power(2, 8)` |
| `function` | Define a function | `function greet($name) { say Hello $name }` |
| `call` | Call a function | `call greet(Alice)` |
| `repeat` | Repeat code | `repeat 5 { say Hi }` |

### Important Notes

- **Variables in strings**: Use `$variableName` to insert variables into text
- **Variable assignment**: Use `set $name = value` to store values (numbers or strings)
- **Conditions**: Support `==`, `!=`, `<`, `>`, `<=`, `>=` operators
- **Function parameters**: Use `$paramName` in function definitions
- **Math operations**: Must use literal numbers (e.g., `add(5, 3)`, not `add($x, $y)`)
- **Division by zero**: Not allowed - will show an error
- **Function calls**: Must match parameter count exactly
- **Version**: Check version with `java -jar KinderScript.jar --version`

## More Examples

See `program.kinder` for additional advanced examples demonstrating all language features.

## Error Handling

KinderScript provides clear error messages for:
- Division by zero
- Empty math operations
- Function parameter/argument mismatches
- Missing functions
- Invalid syntax

## Project Structure

```
KinderScript/
├── KinderScript.kt    # Main interpreter source code
├── KinderScript.jar   # Compiled executable
├── program.kinder     # Example program
├── README.md          # This file
└── LICENSE            # MIT License
```

## Development

### Building from Source

```bash
kotlinc KinderScript.kt -include-runtime -d KinderScript.jar
```

### Running Tests

Test the interpreter with the example program:
```bash
java -jar KinderScript.jar program.kinder
```

## Language Limitations

- Variables can only be used in string interpolation and conditions (not in math operations)
- Math operations require literal numbers
- Functions must be defined before they are called

## Future Improvements

- [ ] Add Gradle support
- [ ] Add to Homebrew
- [ ] Add support for variables in math operations
- [ ] Add arrays/lists support
- [ ] Add string manipulation functions
- [ ] Add file I/O operations

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed list of changes and version history.

## License

MIT License - See LICENSE file for details

## Author

Anu S Pillai

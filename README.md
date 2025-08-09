# Mabel Programming Language Documentation

**Version 1.0**

## Table of Contents
1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Language Basics](#language-basics)
4. [Data Types](#data-types)
5. [Variables](#variables)
6. [Operators](#operators)
7. [Control Flow](#control-flow)
8. [Functions](#functions)
9. [Object-Oriented Programming](#object-oriented-programming)
10. [Arrays](#arrays)
11. [Built-in Functions Reference](#built-in-functions-reference)
12. [Examples](#examples)
13. [Language Specifications](#language-specifications)
14. [Error Messages](#error-messages)

---

## Introduction

Mabel is a dynamically-typed, object-oriented programming language named after my Chow Chow that compiles to bytecode and runs on the Java Virtual Machine. It features:

- **Simple, clean syntax** inspired by JavaScript and Python
- **Object-oriented programming** with classes and methods
- **First-class functions** with recursion support
- **Dynamic arrays** with comprehensive built-in operations
- **Bytecode compilation** for efficient execution
- **JAR packaging** for easy distribution

## Getting Started

### Installation Requirements
- Java 8 or higher
- All Mabel compiler files (`*.java` compiled to `*.class`)

### Your First Program

Create a file called `hello.mabel`:
```javascript
print "Hello, World!"
```

Compile and run:
```bash
java MabelCompiler hello.mabel
java -jar hello.jar
```

## Language Basics

### Program Structure
- Statements are terminated by newlines or semicolons
- Code blocks use curly braces `{}`
- Indentation is not significant but recommended for readability
- Entry point is the top-level code (no main function required)

### Comments
```javascript
// This is a single-line comment
// Use multiple lines for longer comments
// like this
```

### Print Statement
```javascript
print "Hello"                    // Simple string
print "Age: " + str(25)          // Concatenation
print "Array: " + str([1, 2, 3]) // Print arrays
```
**Note:** `print` is a statement keyword, not a function - no parentheses needed.

## Data Types

### Numbers
All numbers are double-precision floating point:
```javascript
let integer = 42
let decimal = 3.14159
let negative = -17.5
```

### Strings
Text enclosed in double quotes:
```javascript
let name = "Alice"
let message = "Hello, World!"
let empty = ""
```

### Booleans
```javascript
let isTrue = true
let isFalse = false
```

### Null
```javascript
let nothing = null  // Prints as "nil"
```

### Arrays
Ordered, mutable collections:
```javascript
let numbers = [1, 2, 3, 4, 5]
let mixed = [1, "two", 3.5, true, null]
let nested = [[1, 2], [3, 4], [5, 6]]
let empty = []
```

## Variables

### Declaration
Variables are declared with `let`:
```javascript
let x = 10
let name = "Bob"
let items = []
```

### Assignment
Variables can be reassigned:
```javascript
let x = 10
x = 20        // Reassignment
x = "hello"   // Dynamic typing - type can change
```

### Scope
- Variables are function-scoped or global-scoped
- Block scope is supported within functions

## Operators

### Arithmetic Operators
| Operator | Description | Example | Result |
|----------|-------------|---------|--------|
| `+` | Addition | `5 + 3` | `8` |
| `-` | Subtraction | `10 - 4` | `6` |
| `*` | Multiplication | `3 * 4` | `12` |
| `/` | Division | `15 / 3` | `5` |
| `%` | Modulo | `10 % 3` | `1` |

### Comparison Operators
| Operator | Description | Example | Result |
|----------|-------------|---------|--------|
| `==` | Equal to | `5 == 5` | `true` |
| `!=` | Not equal to | `5 != 3` | `true` |
| `<` | Less than | `3 < 5` | `true` |
| `>` | Greater than | `5 > 3` | `true` |
| `<=` | Less than or equal | `5 <= 5` | `true` |
| `>=` | Greater than or equal | `5 >= 3` | `true` |

### Logical Operators
| Operator | Description | Example | Result |
|----------|-------------|---------|--------|
| `and` | Logical AND | `true and false` | `false` |
| `or` | Logical OR | `true or false` | `true` |
| `not` | Logical NOT | `not true` | `false` |

### Special Operations
- **String concatenation:** `"Hello " + "World"` → `"Hello World"`
- **Array concatenation:** `[1, 2] + [3, 4]` → `[1, 2, 3, 4]`
- **Array append:** `[1, 2, 3] + 4` → `[1, 2, 3, 4]`

## Control Flow

### If-Else Statements
```javascript
if (condition) {
    // code if true
}

if (x > 0) {
    print "Positive"
} else if (x < 0) {
    print "Negative"
} else {
    print "Zero"
}
```

### While Loops
```javascript
let i = 0
while (i < 10) {
    print str(i)
    i = i + 1
}
```

### For Loops
```javascript
for (let i = 0; i < 10; i = i + 1) {
    print str(i)
}
```

## Functions

### Function Declaration
```javascript
function functionName(param1, param2) {
    // function body
    return result
}
```

### Function Calls
```javascript
let result = functionName(arg1, arg2)
```

### Recursion
```javascript
function factorial(n) {
    if (n <= 1) {
        return 1
    }
    return n * factorial(n - 1)
}

print str(factorial(5))  // Output: 120
```

### Functions as Values
```javascript
function greet(name) {
    return "Hello, " + name
}

let myFunc = greet  // Functions are first-class
print myFunc("Alice")
```

## Object-Oriented Programming

### Class Definition
```javascript
class ClassName {
    // Constructor (optional)
    function init(param1, param2) {
        this.field1 = param1
        this.field2 = param2
    }
    
    // Methods
    function methodName() {
        return this.field1
    }
    
    function setField(value) {
        this.field1 = value
    }
}
```

### Object Creation
```javascript
let obj = ClassName(arg1, arg2)  // Create instance
```

### Accessing Members
```javascript
// Method calls
obj.methodName()

// Field access
let value = obj.field1

// Field modification
obj.field2 = newValue
```

### The `this` Keyword
- Refers to the current instance
- Required when accessing instance variables
- Automatically bound in methods

### Complete Class Example
```javascript
class BankAccount {
    function init(owner, balance) {
        this.owner = owner
        this.balance = balance
    }
    
    function deposit(amount) {
        if (amount > 0) {
            this.balance = this.balance + amount
            return true
        }
        return false
    }
    
    function withdraw(amount) {
        if (amount > 0 and amount <= this.balance) {
            this.balance = this.balance - amount
            return true
        }
        return false
    }
    
    function getBalance() {
        return this.balance
    }
}

let account = BankAccount("Alice", 1000)
account.deposit(500)
print "Balance: " + str(account.getBalance())
```

## Arrays

### Array Creation
```javascript
let empty = []
let numbers = [1, 2, 3, 4, 5]
let mixed = [1, "two", 3.5, true]
let nested = [[1, 2], [3, 4]]
```

### Array Access
```javascript
let first = arr[0]        // Get first element
let last = arr[len(arr) - 1]  // Get last element
let element = matrix[0][1]     // Nested array access
```

### Array Modification
```javascript
arr[0] = newValue         // Modify element
arr[len(arr) - 1] = 100   // Modify last element
matrix[0][1] = 5          // Modify nested array element
```

### Array Operations
```javascript
// Concatenation
let combined = [1, 2] + [3, 4]    // [1, 2, 3, 4]

// Append single element
let withExtra = [1, 2, 3] + 4     // [1, 2, 3, 4]

// Prepend single element
let withFirst = 0 + [1, 2, 3]     // [0, 1, 2, 3]
```

## Built-in Functions Reference

### Type Conversion Functions

#### `str(value)`
Converts any value to its string representation.
- **Parameters:** `value` - Any value to convert
- **Returns:** String representation
- **Example:** `str(42)` → `"42"`

#### `num(string)`
Converts a string to a number.
- **Parameters:** `string` - String to convert
- **Returns:** Numeric value
- **Throws:** RuntimeException if conversion fails
- **Example:** `num("3.14")` → `3.14`

### Array/String Functions

#### `len(array/string)`
Returns the length of an array or string.
- **Parameters:** `array/string` - Array or string
- **Returns:** Length as number
- **Example:** `len([1, 2, 3])` → `3`

### Stack Operations

#### `push(array, item)`
Adds an element to the end of the array.
- **Parameters:** `array` - Target array, `item` - Item to add
- **Returns:** The pushed item
- **Modifies:** Original array
- **Example:** `push([1, 2], 3)` → Array becomes `[1, 2, 3]`

#### `pop(array)`
Removes and returns the last element.
- **Parameters:** `array` - Target array
- **Returns:** The removed element
- **Throws:** RuntimeException if array is empty
- **Example:** `pop([1, 2, 3])` → Returns `3`, array becomes `[1, 2]`

### Queue Operations

#### `shift(array)`
Removes and returns the first element.
- **Parameters:** `array` - Target array
- **Returns:** The removed element
- **Throws:** RuntimeException if array is empty
- **Example:** `shift([1, 2, 3])` → Returns `1`, array becomes `[2, 3]`

#### `unshift(array, item)`
Adds an element to the beginning of the array.
- **Parameters:** `array` - Target array, `item` - Item to add
- **Returns:** The added item
- **Modifies:** Original array
- **Example:** `unshift([2, 3], 1)` → Array becomes `[1, 2, 3]`

### Array Manipulation

#### `slice(array, start, end)`
Extracts a portion of the array.
- **Parameters:** `array` - Source array, `start` - Start index, `end` - End index (exclusive)
- **Returns:** New array with extracted elements
- **Note:** Supports negative indices (count from end)
- **Example:** `slice([0, 1, 2, 3, 4], 1, 3)` → `[1, 2]`
- **Example:** `slice([0, 1, 2, 3, 4], -2, len(array))` → `[3, 4]`

#### `copy(array)`
Creates a shallow copy of the array.
- **Parameters:** `array` - Array to copy
- **Returns:** New array with same elements
- **Example:** `copy([1, 2, 3])` → `[1, 2, 3]` (new array)

#### `clear(array)`
Removes all elements from the array.
- **Parameters:** `array` - Array to clear
- **Returns:** `null`
- **Modifies:** Original array becomes empty
- **Example:** `clear([1, 2, 3])` → Array becomes `[]`

#### `reverse(array)`
Reverses the array in place.
- **Parameters:** `array` - Array to reverse
- **Returns:** The reversed array
- **Modifies:** Original array
- **Example:** `reverse([1, 2, 3])` → Array becomes `[3, 2, 1]`

#### `sort(array)`
Sorts the array in place.
- **Parameters:** `array` - Array to sort
- **Returns:** The sorted array
- **Modifies:** Original array
- **Note:** Numbers sorted numerically, strings alphabetically
- **Example:** `sort([3, 1, 2])` → Array becomes `[1, 2, 3]`

### Search Operations

#### `indexOf(array, item)`
Finds the index of the first occurrence of an item.
- **Parameters:** `array` - Array to search, `item` - Item to find
- **Returns:** Index of item, or -1 if not found
- **Example:** `indexOf([1, 2, 3, 2], 2)` → `1`

#### `contains(array, item)`
Checks if an array contains an item.
- **Parameters:** `array` - Array to search, `item` - Item to find
- **Returns:** `true` if found, `false` otherwise
- **Example:** `contains([1, 2, 3], 2)` → `true`

## Examples

### Example 1: Fibonacci Sequence
```javascript
function fibonacci(n) {
    if (n <= 1) {
        return n
    }
    return fibonacci(n - 1) + fibonacci(n - 2)
}

for (let i = 0; i < 10; i = i + 1) {
    print "fib(" + str(i) + ") = " + str(fibonacci(i))
}
```

### Example 2: Bubble Sort Implementation
```javascript
function bubbleSort(arr) {
    let n = len(arr)
    let swapped = true
    
    while (swapped) {
        swapped = false
        let i = 0
        while (i < n - 1) {
            if (arr[i] > arr[i + 1]) {
                // Swap elements
                let temp = arr[i]
                arr[i] = arr[i + 1]
                arr[i + 1] = temp
                swapped = true
            }
            i = i + 1
        }
        n = n - 1
    }
    return arr
}

let numbers = [64, 34, 25, 12, 22, 11, 90]
print "Original: " + str(numbers)
bubbleSort(numbers)
print "Sorted: " + str(numbers)
```

### Example 3: Stack-based Calculator
```javascript
class Calculator {
    function init() {
        this.stack = []
    }
    
    function push(value) {
        push(this.stack, value)
    }
    
    function add() {
        if (len(this.stack) >= 2) {
            let b = pop(this.stack)
            let a = pop(this.stack)
            push(this.stack, a + b)
        }
    }
    
    function multiply() {
        if (len(this.stack) >= 2) {
            let b = pop(this.stack)
            let a = pop(this.stack)
            push(this.stack, a * b)
        }
    }
    
    function result() {
        if (len(this.stack) > 0) {
            return this.stack[len(this.stack) - 1]
        }
        return 0
    }
}

let calc = Calculator()
calc.push(10)
calc.push(5)
calc.add()        // 10 + 5 = 15
calc.push(2)
calc.multiply()   // 15 * 2 = 30
print "Result: " + str(calc.result())
```

### Example 4: Todo List Manager
```javascript
class TodoList {
    function init(owner) {
        this.owner = owner
        this.todos = []
        this.completed = []
    }
    
    function addTask(task) {
        push(this.todos, task)
        print "Added: " + task
    }
    
    function completeNext() {
        if (len(this.todos) > 0) {
            let task = shift(this.todos)
            push(this.completed, task)
            print "Completed: " + task
            return true
        }
        return false
    }
    
    function addUrgent(task) {
        unshift(this.todos, task)
        print "Added urgent: " + task
    }
    
    function getPending() {
        return copy(this.todos)
    }
    
    function getCompleted() {
        return copy(this.completed)
    }
    
    function summary() {
        print this.owner + "'s Tasks:"
        print "  Pending: " + str(len(this.todos))
        print "  Completed: " + str(len(this.completed))
    }
}

let todo = TodoList("Alice")
todo.addTask("Write documentation")
todo.addTask("Test code")
todo.addUrgent("Fix bug")
todo.completeNext()
todo.summary()
```

## Language Specifications

### Lexical Structure
- **Keywords:** `let`, `if`, `else`, `while`, `for`, `function`, `return`, `true`, `false`, `and`, `or`, `not`, `print`, `class`, `this`, `null`
- **Identifiers:** Start with letter or underscore, followed by letters, digits, or underscores
- **Numbers:** Integer and floating-point literals
- **Strings:** Double-quoted only
- **Comments:** Single-line only (`//`)

### Type System
- **Dynamic typing:** Variables can hold any type
- **Type coercion:** Automatic in some contexts (e.g., string concatenation)
- **Pass by value:** Primitives (numbers, strings, booleans, null)
- **Pass by reference:** Arrays and objects

### Memory Model
- **Garbage collection:** Automatic (handled by JVM)
- **Stack depth:** Limited to 100 recursive calls
- **Maximum parameters:** 255 per function
- **Maximum local variables:** 255 per scope

### Compilation Process
1. **Lexical analysis:** Source → Tokens
2. **Parsing:** Tokens → AST
3. **Compilation:** AST → Bytecode
4. **Packaging:** Bytecode → JAR

### File Structure
```
program.mabel → [Compiler] → program.mbc + program.jar
                                ↓           ↓
                              [VM]      [JVM]
```

## Error Messages

### Compile-Time Errors
- `Expect expression` - Invalid syntax
- `Expect ')' after expression` - Missing parenthesis
- `Undefined variable 'x'` - Variable not declared
- `Can't have more than 255 parameters` - Function limit exceeded

### Runtime Errors
- `Stack overflow: recursion depth exceeded 100` - Too much recursion
- `Array index out of bounds` - Invalid array access
- `Division by zero` - Mathematical error
- `Can only call functions and classes` - Invalid function call
- `Only instances have properties` - Invalid property access
- `Cannot pop from empty array` - Empty array operation
- `'x' can only be applied to arrays` - Type mismatch

### Best Practices for Error Handling
1. Check array bounds before access
2. Verify array is not empty before pop/shift
3. Check divisor is not zero
4. Limit recursion depth
5. Initialize variables before use

## Limitations and Future Enhancements

### Current Limitations
- No multi-line comments
- No string escape sequences
- No inheritance (classes are standalone)
- No interfaces or abstract classes
- No static class members
- No exception handling (try/catch)
- No switch/case statements
- No break/continue in loops
- No string interpolation
- No operator overloading
- No modules/imports

### Planned Enhancements
- Class inheritance with `extends`
- String escape sequences (`\n`, `\t`, etc.)
- Multi-line comments (`/* */`)
- Exception handling
- Module system
- Standard library expansion
- Optimization improvements

---

## Quick Reference Card

### Basic Syntax
```javascript
let x = 10                    // Variable
print "Hello"                 // Print
if (x > 5) { }               // Conditional
while (x < 10) { }           // Loop
function f(x) { return x }   // Function
class C { }                  // Class
```

### Array Operations
```javascript
let arr = [1, 2, 3]          // Create
arr[0]                       // Access
arr[0] = 5                   // Modify
arr + [4, 5]                 // Concatenate
push(arr, 4)                 // Add to end
pop(arr)                     // Remove from end
len(arr)                     // Get length
```

### Class Usage
```javascript
class Point {
    function init(x, y) {
        this.x = x
        this.y = y
    }
}
let p = Point(10, 20)
print str(p.x)
```

---

*Mabel Programming Language - Version 1.0*  
*Created by Aidan Fahey*  
*Documentation Last Updated: 08/2025*
# Mabel
A programming language I made for fun, named after my Chow Chow.

Written entirely in Java, Mabel is pretty much not anything you would use over any other programming language.
But, it was fun to make, and it's pretty cool that it works at all.

Mabel has its own lexer, parser, compiler, and virtual machine to allow one to write code, compile, and then run an executable.

I am currently working on adding functionality for inheritance and interfaces.

# Interactive REPL
- java MabelCompiler
# Compile to bytecode then run
- java MabelCompiler program.mabel - Creates program.mbc and program.jar
- java MabelCompiler program.mbc - Runs bytecode
- java -jar program.jar - Also runs bytecode

# Sample Program and Output:
## Program
```
function factorial(n) {
    if (n <= 1) {
        print "Base case: " + str(n)
        return 1
    }
    print "Computing factorial of " + str(n)
    return n * factorial(n - 1)
}

print "Running factorial(5):"
let result = factorial(5)
print "Result: " + str(result)
```

## Output
```
Running factorial(5):
Computing factorial of 5
Computing factorial of 4
Computing factorial of 3
Computing factorial of 2
Base case: 1
Result: 120
```

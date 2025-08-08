# Mabel
A programming language I made for fun, named after my Chow Chow.

Written entirely in Java, Mabel is pretty much not anything you would use over any other programming language.
But, it was fun to make, and it's pretty cool that it works at all.

Mabel has its own lexer, parser, compiler, and virtual machine to allow one to write code, compile, and then run an executable.

I am currently working on adding functionality for user defined classes, as I would like Mabel to be Object Oriented.

# Interactive REPL
java MabelCompiler
# Compile to bytecode then run
java MabelCompiler program.mabel - Creates program.mbc and program.jar
java MabelCompiler program.mbc - Runs bytecode
java -jar program.jar - Also runs bytecode

## Sample Program and Output:
# Program
'''
function greet(name) {
    print "Hello, " + name + "!"
}

function add(a, b) {
    let result = a + b
    print str(a) + " + " + str(b) + " = " + str(result)
    return result
}

greet("Mabel")
greet("Aidan")

let sum = add(5, 3)
print "Sum returned: " + str(sum)
'''

# Output
'''
Hello, Mabel!
Hello, Aidan!
5 + 3 = 8
Sum returned: 8
'''

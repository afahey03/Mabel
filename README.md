# Mabel
A programming language I made for fun, named after my Chow Chow.

Written entirely in Java, Mabel is pretty much not anything you would use over any other programming language.
But, it was fun to make, and it's pretty cool that it works at all.

# Interactive REPL
java MabelCompiler

# Compile to bytecode then run
java MabelCompiler program.mabel  # Creates program.mbc and program.jar

java MabelCompiler program.mbc    # Runs bytecode

java -jar program.jar             # Also runs bytecode

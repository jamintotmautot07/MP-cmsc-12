# MP-cmsc-12
Final game project for cmsc 12

To compile...

type this in the terminal 

"javac -d bin @sources.txt" 

//this compiles every file in the sources list and then compiles the found .java files then puts the .class files in "bin"

to compile new file...

1. Either you compile it individually if only one file was changed. or
2. Type this in the terminal: 
"(for /r src %i in (*.java) do @set "line=%i" && call echo "%line:\=/%") > sources.txt"

this finds all .java files and writes their directory in "sources.txt" with certain specific format changes

then compile all files in sources.txt using "javac -d bin @sources.txt"


Finally, to run, type this in terminal...

"java -cp bin main.GameLauncher"

# Architicture Lab MIPS Assembler

A 16-bit MIPS Assembler for Architicture Lab in Kuwait University

## Getting Started

To use this project please follow the instructions below.

### Prerequisites

This project require JDK 1.8.

To test to see if there is java in your System open the terminal (Linux, OSX) or the CMD (Windows)
and type the following:

```
javac -version
```

### Building

To build the project either download it as a zip file or clone it by this command

```
git clone https://github.com/Y-AH/ArchitictureLab.git
```

After that navigate to the folder in your terminal.
Then create a bin directory.

```
mkdir bin
```

Compile the java files and put the outputed class files into bin

```
javac -sourcepath src src/MIPSAssemblerGUI.java -d bin
```

After that you can run the program from this command

```
java -cp bin/ MIPSAssemblerGUI
```

# CPD Project 1
## Performance	evaluation	of	a	single	core

The project was developed in 2 languages: C++ and JAVA.

Compile both versions with:

    $ make all

Or separatly:

    $ make buildJava

    $ make buildCpp

Run the C++ version:

    $ ./matrixproduct [-t|tests]

Run the JAVA version:

    $ java MatrixProduct.java [-t|-tests]

Options:
    
    -t | -tests -  Omit some output to console.

### Tests
Test values are present in the folder ./tests/input.

Test results are present in the folder ./tests/results.

To execute all the tests (both C++ and JAVA versions have to be compiled first):

    $ make tests

Or separatly:

    $ make test1

    $ make test2

    $ make test3




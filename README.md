# Foie gras

This project aims at finding the limits imposed by the 
Java class format when dealing with code generation.

The main class is `Benchmark.kt` and contains a few hardcoded 
options you can opt in/out right below the logger declaration.

## Procedure

The benchmark works by using a `BenchmarkCase`, 
which describes everything to generate a piece of code.
The benchmarking engine will then try to find the limit for each case and report it.

First, a slow start to guess the upper bound,
then refines it with a binary search.

## Models

### Flat class

All the data is in the same class. 
This tests various keywords influence on the number 
of field it's possible to have.

### Object as field

Evaluates what happens when a complex object is used to 
initialize each field.


### Class as field

Evaluates what happens when the data is extracted to a different class

### Class hierarchy

A model to overcome the class file limitation by 
using the class hierarchy to spread the declarations 
in several files.

## Run the tests

* Review the benchmark options above the main function.
* Free several GB of disk space.
* Execute the main function in `Benchmark.kt`.
* Clean up the `generated` folder.

## TODOs

Add option to clear the generated data of each test.

---

###### Foie gras

This is the result of 
[force feeding a duck](https://en.wikipedia.org/wiki/Foie_gras#Force-feeding_procedure) 
and that's how I felt when I was giving the compiler my generated source code.

Please be nice and don't do that to your duck :3

Also don't do that to your classes, as flirting with the limits may break tools relying on bytecode modification.
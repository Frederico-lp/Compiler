# Java subset Compiler

## Compiling and executing

To compile our code,  run:

`gradle installDist` 

To execute you should run the following command: 

`.\comp2022-7c <filename.jmm> `

To run the tests, you can either use:

`gradle test` or `gradle build`.


## 1. Syntatic Analysis
Everything is being correctly parsed, excluding comments. The operations have different precedences, as expected

## 2. Semantic Analysis
This stage reports an error whenever one of the following cases happens:

* Type mismatching between operands (e.g. 1 + true);
* Type mismatching between assignment variable and the assignment value;
* Type mismatching between a return statement and the return type of the method;
* Trying to index a variable that is not an array;
* Accessing an array with anything other than an integer expression (e.g. a[3.2]);
* Using undeclared variables/fields;
* Using a class that is not imported;
* Using a non-boolean expression in an if condition (e.g. if(1 + 2));
* Using an array in a while condition (e.g. int[] a; while(a));
* Calling a non-static variable in main;
* Calling a method not declared in scope;
* Calling a method with the wrong number of parameters;
* Calling a method with at least one of the parameters of the wrong type;


## 3. OLLIR Generation 
Ollir receives a jmm file as input and outputs an Ollir file. The types of code parsed by Ollir are:
* Class and Method declarations
* Return statements
* Variables and Fields declarations
* Assignments 
* Operations
* Function calls
* If else conditions
* While cycles

## 4. Jasmin Generation
* Class and Method declarations
* Return statements
* Variables declarations
* Assignments
* Operations except and and less than
* Function calls
----------------
**TASK DISTRIBUTION:**

| Task             | Contributors    | 
| ---------------- | ---------  |
| Syntatic Analysis  | All                         |
| Semantic Analysis  | Carolina Cintra, Nuno Jesus  |
| OLLIR Generation   | Frederico Lopes              |
| Jasmin Generation  | Frederico Lopes, Marta Mariz |

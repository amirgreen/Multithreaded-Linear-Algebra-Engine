# SPL Assignment 2 — Linear Algebra Engine (LAE)

A multithreaded Java linear algebra engine that evaluates matrix expressions described in JSON.

The project parses an input computation tree, evaluates supported matrix operations using a custom thread scheduler, and writes the final matrix result or an error message to a JSON output file.

## Features

* Parse nested matrix expressions from JSON.
* Support matrix addition, multiplication, negation, and transpose.
* Evaluate complex expression trees step by step.
* Execute row/vector-level work in parallel using a custom executor.
* Store matrices in row-major or column-major orientation depending on the operation.
* Protect shared matrix/vector data with read-write locks.
* Write successful results and errors in a consistent JSON format.
* Include unit and integration tests for parsing, matrix operations, concurrency, and error handling.

## Tech Stack

* **Language:** Java 21
* **Build Tool:** Maven
* **JSON Library:** Jackson Databind
* **Testing:** JUnit 5
* **Development Environment:** VS Code Dev Container support

## Project Structure

```text
.
├── Examples/                    # Example input JSON files
├── Output/                      # Expected/sample output JSON files
├── Tests/                       # Additional manual test inputs
├── src/
│   ├── main/java/
│   │   ├── memory/              # Thread-safe matrix/vector data structures
│   │   ├── parser/              # JSON parser, computation tree, output writer
│   │   ├── scheduling/          # Custom thread executor and worker threads
│   │   └── spl/lae/             # Main application and linear algebra engine
│   └── test/java/               # JUnit tests
├── pom.xml                      # Maven configuration
└── README.md
```

## How It Works

The engine receives a JSON file that represents a computation tree. Each internal node is an operation, and each leaf node is a matrix.

For example, the expression:

```text
(A * (-B)) + C + T(D)
```

can be represented as nested JSON. The parser builds a `ComputationNode` tree, and the engine repeatedly finds a node whose operands are already concrete matrices. That node is computed, replaced by its result matrix, and the process continues until the root becomes the final result.

The main flow is:

1. `Main` receives command-line arguments.
2. `InputParser` reads the JSON file and builds a computation tree.
3. `ComputationNode.associativeNesting()` converts operations with more than two operands into left-associative binary trees.
4. `LinearAlgebraEngine` resolves the tree bottom-up.
5. Matrix work is split into tasks and submitted to `TiredExecutor`.
6. The final matrix is written to the output file using `OutputWriter`.

## Supported Operations

| Operator | Meaning               | Operand Count | Example     |
| -------- | --------------------- | ------------: | ----------- |
| `+`      | Matrix addition       |     2 or more | `A + B + C` |
| `*`      | Matrix multiplication |     2 or more | `A * B * C` |
| `-`      | Matrix negation       |             1 | `-A`        |
| `T`      | Matrix transpose      |             1 | `T(A)`      |

## JSON Input Format

A matrix is represented as a rectangular 2D array:

```json
[
  [1, 2, 3],
  [4, 5, 6]
]
```

An operation is represented as an object with:

* `operator`: one of `+`, `*`, `-`, or `T`
* `operands`: an array of matrices or nested operations

Example:

```json
{
  "operator": "+",
  "operands": [
    {
      "operator": "*",
      "operands": [
        [
          [1, 2, 3],
          [4, 5, 6]
        ],
        {
          "operator": "-",
          "operands": [
            [
              [7, 8],
              [9, 10],
              [11, 12]
            ]
          ]
        }
      ]
    },
    [
      [13, 14],
      [15, 16]
    ],
    {
      "operator": "T",
      "operands": [
        [
          [17, 18],
          [19, 20]
        ]
      ]
    }
  ]
}
```

## Output Format

On success, the program writes:

```json
{
  "result": [
    [1.0, 2.0],
    [3.0, 4.0]
  ]
}
```

On failure, the program writes:

```json
{
  "error": "error message"
}
```

The program also prints a worker performance report to standard output after successful execution.

## Requirements

* Java 21
* Maven 3.x

## Build

From the project root, run:

```bash
mvn clean package
```

The Maven Shade plugin creates an executable JAR with all required dependencies included.

## Run

```bash
java -jar target/lga-1.0.jar <number-of-threads> <input-json-path> <output-json-path>
```

Example:

```bash
java -jar target/lga-1.0.jar 4 Examples/example1.json result.json
```

Arguments:

1. `number-of-threads` — positive integer specifying how many worker threads to use.
2. `input-json-path` — path to the JSON computation file.
3. `output-json-path` — path where the result or error JSON should be written.

## Run Tests

```bash
mvn test
```

The test suite includes checks for:

* Shared vector operations
* Shared matrix loading and reading
* Matrix addition, multiplication, negation, and transpose
* Complex computation trees
* Main program integration
* Worker thread behavior
* Error propagation

## Architecture Overview

### `spl.lae`

Contains the application entry point and the main computation engine.

* `Main` validates CLI arguments, parses input, runs the engine, and writes output.
* `LinearAlgebraEngine` manages matrix loading, operation execution, task creation, and final result resolution.
* `TestGenerator` can generate a large random multiplication test file.

### `parser`

Responsible for translating JSON input into an executable computation tree.

* `InputParser` reads and validates JSON input.
* `ComputationNode` represents either an operation node or a matrix leaf.
* `ComputationNodeType` defines supported node types.
* `OutputWriter` writes result and error JSON files.

### `memory`

Provides shared matrix and vector abstractions.

* `SharedMatrix` stores a matrix as an array of `SharedVector` objects.
* `SharedVector` supports vector-level operations such as addition, negation, dot product, transposition, and vector-matrix multiplication.
* `VectorOrientation` marks vectors and matrices as row-major or column-major.

### `scheduling`

Implements a custom multithreaded scheduler.

* `TiredExecutor` manages worker threads and submits computation tasks.
* `TiredThread` executes one task at a time and tracks execution statistics.

Workers are selected using a priority queue ordered by fatigue. Fatigue is calculated from each worker's accumulated execution time and a random fatigue factor. This allows the scheduler to prefer less-fatigued idle workers when distributing tasks.

## Matrix Execution Strategy

The engine uses different task strategies depending on the operation:

* **Addition:** creates one task per row vector.
* **Negation:** creates one task per row vector.
* **Transpose:** creates one task per vector and switches its orientation.
* **Multiplication:** loads the right matrix in column-major form and creates one task per left-side row vector. Each row is multiplied against the columns of the right matrix using dot products.

This design keeps matrix operations modular and allows independent row/vector work to run concurrently.

## Error Handling

The program detects and reports common errors such as:

* Invalid number of command-line arguments
* Non-positive thread count
* Invalid JSON input
* Unsupported operators
* Empty or malformed matrices
* Inconsistent row sizes
* Dimension mismatch in matrix addition or multiplication
* Invalid vector orientation for vector operations

When an error occurs during execution, the output file is written with an `error` field instead of a `result` field.

## Example Files

The repository includes several ready-to-run examples:

```bash
java -jar target/lga-1.0.jar 4 Examples/example1.json Output/my_out1.json
java -jar target/lga-1.0.jar 4 Examples/example2.json Output/my_out2.json
java -jar target/lga-1.0.jar 4 Examples/example3.json Output/my_out3.json
```

The `Output/` directory contains sample output files for the provided examples.

## Notes

* Matrices must be rectangular 2D arrays.
* Standalone 1D vectors are not supported as input nodes.
* Matrix multiplication is order-sensitive.
* Multi-operand operations are transformed into left-associative nested operations.
* The final output is always written as formatted JSON.

# Column Generation Algorithm

This repository contains two Java implementations of column generation using CPLEX:

- `CSP/`: Crew Scheduling Problem
- `VRP/`: Vehicle Routing Problem

## Project Structure

```text
cplex/
├── CSP/                     # Crew Scheduling Problem module
│   ├── data/                # Input CSV files
│   ├── src/                 # Java source code
│   └── CSP.iml              # IntelliJ module file
├── VRP/                     # Vehicle Routing Problem module
│   ├── data/                # Input CSV files
│   ├── src/                 # Java source code
│   └── VRP.iml              # IntelliJ module file
├── bin/                     # Compiled class output
├── docs/                    # Project documentation
├── run.sh                   # Compile and run entry script
└── .gitignore               # Local/generated file exclusions
```

## Run

Update the CPLEX paths in `run.sh`, then run:

```bash
./run.sh
./run.sh csp
./run.sh vrp
```

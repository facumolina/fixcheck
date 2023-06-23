# fixcheck
FixCheck is a tool for patch correctness analysis. It uses random testing to generate new inputs to test the patch and large language models to generate meaningful assertions for the new inputs. 

## Setup

### Requirements

- Java >= 1.8
- Python3

### Steps

1. Clone the repository and build the project:
```bash
git clone https://github.com/facumolina/fixcheck
```
```bash
./gradlew shadowJar
```
2. Install the python requirements:
```bash
pip3 install -r requirements.txt
```

## Example

Preliminary example analyzing a candidate patch from the [DefectRepairing](https://github.com/Ultimanecat/DefectRepairing) benchmark:

1. Download and install [defects4j](https://github.com/rjust/defects4j) (make sure the command defects4j is available)
2. Download the [DefectRepairing](https://github.com/Ultimanecat/DefectRepairing) benchmark
3. Define the following environment variables:
```bash
export DEFECT_REPAIRING_DATASET=path_to_defectrepairing
export FIXCHECK=path_to_thisrepo
```
4. Run FixCheck on the Patch1 (or any other):
```bash
python3 experiments/setup-defect-repairing.py Patch1
python3 experiments/run-fixcheck-defect-repairing.py Patch1
```

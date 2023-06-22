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
./gradlew shadowJar
```
2. Install the python requirements:
```bash
pip3 install -r requirements.txt
```

## Example

Preliminary example analyzing a candidate patch from the [DefectRepairing](https://github.com/Ultimanecat/DefectRepairing) benchmark:

TBD

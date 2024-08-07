# fixcheck
FixCheck is a tool for improving patch correctness analysis. 
Given a target Java patch, it uses static analysis and random testing to generate 
new inputs to test the patch, and LLMs to generate meaningful assertions for the new inputs. 
The new tests are executed and those that fail are selected and prioritised 
according to their likelihood of revealing a defect in the patch.

## Setting up FixCheck

### Requirements

- Java >= 17 (tested with 17)
- Python3 (tested with 3.11.5)

### Local Installation

To install FixCheck, clone the repository, build the project with gradle and install the python requirements:
```bash  
git clone https://github.com/facumolina/fixcheck
cd fixcheck
./gradlew shadowJar 
pip3 install -r experiments/requirements.txt
pip3 install -r llms/requirements.txt
```

Set up the FixCheck environment variable:
```bash  
export FIXCHECK=<path_to_thisrepo>
```

Although FixCheck support multiple LLMs (and can be extended), 
it is necessary to download the LLMs that will be used, or interact
with them through an API. Here we provide instructions on the currently 
supported LLMs:

#### codellama-7b-instruct 
This model can be downloaded and executed with the following commands:
```bash  
wget https://huggingface.co/TheBloke/CodeLlama-7B-Instruct-GGUF/resolve/main/codellama-7b-instruct.Q5_K_M.gguf -P llms/models/
python3 llms/codellama-7b-instruct.py
```
> [!Note]
> We are using a version of the codellama-7b-instruct model in the GGUF format 
> obtained through quantization. The GGUF format is developed 
> by the [llama.cpp](https://github.com/ggerganov/llama.cpp)
> team, with the goal to enable LLM inference with minimal 
> setup and state-of-the-art performance.


#### replit-code
To use this model, other python requirements are needed, which, 
for the sake of simplicity, are not included in the requirements.txt files. 
To install them, run the following command:
```bash
pip3 install sentencepiece==0.1.99
pip3 install torch==2.0.1
python3 llms/replit-code.py
```

After setting any of the LLMs, FixCheck is ready to be used.

### Docker

We provide a `Dockerfile` that can be used to build a docker image with 
FixCheck and all its dependencies. To build and run the docker image, 
execute the following commands:
```bash  
docker build -t fixcheck .
docker run -it fixcheck
```
> [!Note]
> Building the image may take a while, as it will download and install all the dependencies for running
> FixCheck, and also to analyze the example patches discussed below.

> [!Note]
> Also, no LLM will be configured in the Docker image.
> Thus, the user will need to set up the LLMs as described in the previous section.

## Using FixCheck

In general, to analyse a patch with FixCheck the following steps are needed:

1. Build the **buggy** version of the project corresponding to the patch under analysis
2. Run an initial bug revealing test to collect its failure trace
3. Apply the patch to the project and built the **patched** version
4. Run FixCheck with the right parameters

### Example

This section contains a simple example analysing a patch 
from [DefectRepairing](https://github.com/Ultimanecat/DefectRepairing), 
a repository containing benchmark of correct and incorrect patches 
for [defects4j](https://github.com/rjust/defects4j) bugs and generated by APR tools, 
used in the evaluation of PATCH-SIM 
(a patch correctness assessment tool).

> [!Note]
> If you are using the Docker image, defects4j and DefectRepairing are already installed, 
> so you can move to the setup step.

To run FixCheck on any of these patches, perform the following steps:

1. Download and install [defects4j](https://github.com/rjust/defects4j) (make sure the command defects4j is available)
2. Download the [DefectRepairing](https://github.com/Ultimanecat/DefectRepairing) benchmark, and set the environgment variable `DEFECT_REPAIRING_DATASET` to the path where the benchmark is located.

Before executing FixCheck, a setup step is needed, that essentially will 
clone the project corresponding to the patch under analysis, 
apply the patch and build the project. 
For instance, to analyse the Patch1, the setup can be performed by running:
```bash  
python3 experiments/setup-defect-repairing.py Patch169
```
> [!IMPORTANT]
> When running the setup script, Java 8 should be configured, as it is required to build defects4j projects.

From this point, we can now execute FixCheck as follows:
```bash
nohup python3 llms/codellama-7b-instruct.py &
python3 experiments/run-fixcheck-defect-repairing.py Patch169 org.imdea.fixcheck.assertion.CodeLlamaInstruct
```
The first script will setup the `codellama-7b-instruct` language model, and leave it ready to be called by FixCheck. 
The second script will automatically extract the arguments 
from the file `experiments/defect-repairing-subjects.csv`, and call FixCheck 
with the right parameters. For instance, the command used for the Patch1 
subject is the following:
```bash  
java -cp build/libs/fixcheck-all-1.0.0.jar:$DEFECT_REPAIRING_DATASET/tmp/Patch169/Math69b/target/classes:$DEFECT_REPAIRING_DATASET/tmp/Patch169/Math69b/target/test-classes org.imdea.fixcheck.FixCheck 
  -tp $DEFECT_REPAIRING_DATASET/tmp/Patch169/Math69b/target/test-classes
  -tc org.apache.commons.math.stat.correlation.PearsonsCorrelationTest
  -tm testPValueNearZero 
  -ts $DEFECT_REPAIRING_DATASET/tmp/Patch169/Math69b/src/test/java 
  -i int
  -tf $DEFECT_REPAIRING_DATASET/tmp/Patch169/Math69b/failing_tests 
  -np 100 
  -ag org.imdea.fixcheck.assertion.CodeLlamaInstruct
```

Once it finishes, the results will be stored in the folder `fixcheck-output/defects-repairing`.
Other patches from the [DefectRepairing](https://github.com/Ultimanecat/DefectRepairing) benchmark can also be analysed following the same procedure, as they are all configured in the csv file `experiments/defect-repairing-subjects.csv`.

### FixCheck Parameters

<table class="tg">
<thead>
  <tr>
    <th class="tg-73oq">Parameter</th>
    <th class="tg-73oq">Long Option</th>
    <th class="tg-73oq">Description</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-73oq">tp</td>
    <td class="tg-73oq">test-classes-path</td>
    <td class="tg-73oq">Path to the test classes directory</td>
  </tr>
 <tr>
    <td class="tg-73oq">ts</td>
    <td class="tg-73oq">test-classes-src</td>
    <td class="tg-73oq">Path to the test classes sources directory</td>
  </tr>
 <tr>
    <td class="tg-73oq">tc</td>
    <td class="tg-73oq">test-class</td>
    <td class="tg-73oq">Fully qualified name of the target test class</td>
  </tr>
 <tr>
    <td class="tg-73oq">tm</td>
    <td class="tg-73oq">test-methods</td>
    <td class="tg-73oq">List of names of the initial fault revealing test methods, seperated by ':'</td>
  </tr>
 <tr>
    <td class="tg-73oq">tf</td>
    <td class="tg-73oq">test-failure-trace-log</td>
    <td class="tg-73oq">File containing the failure trace of the target test method</td>
  </tr>
  <tr>
    <td class="tg-73oq">i</td>
    <td class="tg-73oq">inputs-class</td>
    <td class="tg-73oq">Name of the inputs class (int,float,double,java.lang.String, etc)</td>
  </tr>
  <tr>
    <td class="tg-73oq">np</td>
    <td class="tg-73oq">number-of-prefixes</td>
    <td class="tg-73oq">Number of prefixes variations to generate</td>
  </tr>
  <tr>
    <td class="tg-73oq">ag</td>
    <td class="tg-73oq">assertion-generator</td>
    <td class="tg-73oq">Assertion generator class fully qualified name (e.g., org.imdea.fixcheck.assertion.ReplitCodeLLM)</td>
  </tr>
</tbody>
</table>

### Extending FixCheck

FixCheck can be extended by adding new assertion generators,
e.g., based on other LLMs or other techniques. To extend 
FixCheck with a new assertion generator, the following steps are needed:

1. Create a new class that extends `org.imdea.fixcheck.assertion.AssertionGenerator`
2. Implement the `generateAssertions` method, which, given a test prefix, 
   returns a list of assertions for it. 
3. Ivoking FixCheck with the `-ag` parameter pointing to the fully qualified name of the new assertion generator.

Examples of different assertion generators can be found in the 
package `org.imdea.fixcheck.assertion`.
## Contact
If you experience any issues, please submit an issue or contact us at facundo.molina@imdea.org!




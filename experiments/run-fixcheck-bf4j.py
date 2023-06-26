import os
import sys
import subprocess
import pandas as pd

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
BF4J_DATASET = os.getenv('BF4J_DATASET')

dataset_csv = 'experiments/badfixes.csv'
outputs_dir = 'fixcheck-output'

# Get the arguments
subject_id = sys.argv[1]
print(f'Running FixCheck for subject: {subject_id}')
df = pd.read_csv(dataset_csv)
subject_data = df[df['id'] == subject_id]

# Get and setup the subject data
dataset_base_dir = os.path.join(BF4J_DATASET, 'tmp')
project = subject_data['project'].values[0]
subject_base_dir = os.path.join(dataset_base_dir, subject_id+'/badfix/'+project)
# Dependencies
main_dep = subject_data['main_dep'].values[0]
test_classes = subject_data['tests_classes'].values[0]
test_classes_path = os.path.join(subject_base_dir, test_classes)
# Split main deps, and append base dir to each dep
main_deps = main_dep.split(':')
main_deps = [os.path.join(subject_base_dir, dep) for dep in main_deps]
subject_cp = ':'.join(main_deps)+':'+test_classes_path
# Classes
target_test = subject_data['target_test'].values[0]
target_test_methods = subject_data['target_test_methods'].values[0]
tests_src_dir = subject_data['test_src_dir'].values[0]
target_test_dir = os.path.join(subject_base_dir, tests_src_dir)
target_class = subject_data['target_class'].values[0]
input_class = subject_data['input_class'].values[0]
# Failure log

failure_dir = os.path.join(BF4J_DATASET, 'data/'+subject_id+'/failures')
failure_log = ""
if os.path.exists(failure_dir):
    for filename in os.listdir(failure_dir):
        if filename.endswith(".log"):
            f = os.path.join(failure_dir, filename)
            failure_log += f+":"
    failure_log = failure_log[:-1]
else:
    print(f'No failure log found for subject: {subject_id}')
    sys.exit(0)

# Run FixCheck
subprocess.run(f'./fixcheck.sh {subject_cp} {test_classes_path} {target_test} {target_test_methods} {target_test_dir} {target_class} {input_class} {failure_log}', shell=True)

# Rename output file for this subject
output_file = os.path.join(outputs_dir, subject_id+'-report.csv')
print(f'Renaming output file to: {output_file}')
subprocess.run(f'mv {outputs_dir}/report.csv {output_file}', shell=True)

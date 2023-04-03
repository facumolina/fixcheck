import os
import sys
import subprocess
import pandas as pd

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
BADFIXES_DATASET = os.getenv('BADFIXES_DATASET')

dataset_csv = 'experiments/badfixes.csv'
outputs_dir = 'fixcheck-output'

# Get the arguments
subject_id = sys.argv[1]
print(f'Running FixCheck for subject: {subject_id}')
df = pd.read_csv(dataset_csv)
subject_data = df[df['id'] == subject_id]

# Get and setup the subject data
dataset_base_dir = os.path.join(BADFIXES_DATASET, 'tmp')
project = subject_data['project'].values[0]
subject_base_dir = os.path.join(dataset_base_dir, subject_id+'/badfix/'+project)
# Dependencies
main_dep = subject_data['main_dep'].values[0]
test_classes = subject_data['tests_classes'].values[0]
test_classes_path = os.path.join(subject_base_dir, test_classes)
subject_cp = os.path.join(subject_base_dir, main_dep)+':'+test_classes_path
# Classes
target_test = subject_data['target_test'].values[0]
target_test_methods = subject_data['target_test_methods'].values[0]
tests_src_dir = subject_data['test_src_dir'].values[0]
target_test_dir = os.path.join(subject_base_dir, tests_src_dir)
target_class = subject_data['target_class'].values[0]
input_class = subject_data['input_class'].values[0]

# Run FixCheck
subprocess.run(f'./fixcheck.sh {subject_cp} {test_classes_path} {target_test} {target_test_methods} {target_test_dir} {target_class} {input_class}', shell=True)

# Rename output file for this subject
output_file = os.path.join(outputs_dir, subject_id+'-report.csv')
print(f'Renaming output file to: {output_file}')
subprocess.run(f'mv {outputs_dir}/report.csv {output_file}', shell=True)

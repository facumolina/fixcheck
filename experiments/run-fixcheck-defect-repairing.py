import os
import sys
import subprocess
import pandas as pd

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')

dataset_csv = 'experiments/defect-repairing-subjects.csv'
outputs_dir = 'fixcheck-output'

# Get the arguments
subject_id = sys.argv[1]
print(f'Running FixCheck for subject: {subject_id}')
df = pd.read_csv(dataset_csv)
subject_data = df[df['id'] == subject_id]

# Get and setup the subject data
project = subject_data['project'].values[0]
bug = subject_data['bug'].values[0]
subject_base_dir = os.path.join('/tmp', subject_data['base_dir'].values[0])

patch_base_dir = project+str(bug)+"b"
subject_base_dir = os.path.join(DEFECT_REPAIRING_DATASET, f'tmp/{subject_id}/{patch_base_dir}')


# Dependencies
main_dep = subject_data['main_dep'].values[0]
test_classes = subject_data['tests_build'].values[0]
test_classes_path = os.path.join(subject_base_dir, test_classes)
subject_cp = os.path.join(subject_base_dir, main_dep)+':'+test_classes_path
# Classes and methods
target_test = subject_data['target_test'].values[0]
target_test_methods = subject_data['target_test_methods'].values[0]
tests_src_dir = subject_data['tests_src_dir'].values[0]
target_test_dir = os.path.join(subject_base_dir, tests_src_dir)
target_class = subject_data['target_class'].values[0]
input_class = subject_data['input_class'].values[0]
failure_log = os.path.join(subject_base_dir, 'failing_tests')

# Run FixCheck
subprocess.run(f'./fixcheck.sh {subject_cp} {test_classes_path} {target_test} {target_test_methods} {target_test_dir} {target_class} {input_class} {failure_log}', shell=True)

output_file = os.path.join(outputs_dir, subject_id+'-report.csv')
print(f'Renaming output file to: {output_file}')
subprocess.run(f'mv {outputs_dir}/report.csv {output_file}', shell=True)

import os
import sys
import pandas as pd

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
dataset_csv = 'experiments/badfixes.csv'
outputs_dir = 'fixcheck-output'

# Get the arguments
subject_id = sys.argv[1]
print(f'Running FixCheck for subject: {subject_id}')
df = pd.read_csv(dataset_csv)
subject_data = df[df['id'] == subject_id]

# Get the subject data and run FixCheck
subject_cp = ''
target_test = subject_data['target_test'].values[0]
target_class = subject_data['target_class'].values[0]
input_class = subject_data['input_class'].values[0]
print(f'Target test: {target_test}')
print(f'Target class: {target_class}')
print(f'Input class: {input_class}')

print('Done!')
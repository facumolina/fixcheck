import sys
import pandas as pd
import subprocess
import os

assertion_generation = sys.argv[1]

PCA_HOW_FAR_DATASET = os.getenv('PCA_HOW_FAR_DATASET')

how_far_csv = 'experiments/ase2020-pca-how-far.csv'
df_how_far = pd.read_csv(how_far_csv)

patching_failed_subjects_file = os.path.join(PCA_HOW_FAR_DATASET, 'patching-failed-subjects.txt')
# Get all lines of file
with open(patching_failed_subjects_file) as f:
    lines_failing_patches = f.readlines()
# Remove \n from lines
lines_failing_patches = [line.strip() for line in lines_failing_patches]

# Loop through the dataset and run the setup for each subject
for index, row in df_how_far.iterrows():
    # Do not run if report exits
    subject_id = row['id']
    report_file = f'fixcheck-output/ase2020/{subject_id}/{assertion_generation}/report.csv'
    if os.path.exists(report_file):
        print(f'Report already exists for patch: {subject_id}')
        continue
    # Do not run if subject is in the list of failing patches
    if subject_id in lines_failing_patches:
        print(f'Subject is in the list of failing patches: {subject_id}')
        continue
    print(f'Running fixcheck for patch: {subject_id}')
    subprocess.run(f'python3 experiments/run-fixcheck-ase2020-pca-how-far.py {subject_id} {assertion_generation}', shell=True)

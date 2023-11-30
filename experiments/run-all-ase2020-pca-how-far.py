import sys
import pandas as pd
import subprocess
import os

assertion_generation = sys.argv[1]

PCA_HOW_FAR_DATASET = os.getenv('PCA_HOW_FAR_DATASET')

how_far_csv = 'experiments/ase2020-pca-how-far.csv'
df_how_far = pd.read_csv(how_far_csv)

# Loop through the dataset and run the setup for each subject
for index, row in df_how_far.iterrows():
    # Do not run if report exits
    subject_id = row['id']
    report_file = f'fixcheck-output/ase2020/{subject_id}/{assertion_generation}/report.csv'
    if os.path.exists(report_file):
        print(f'Report already exists for patch: {patch}')
        continue
    print(f'Running fixcheck for patch: {subject_id}')
    subprocess.run(f'python3 experiments/run-fixcheck-ase2020-pca-how-far.py {subject_id} {assertion_generation}', shell=True)

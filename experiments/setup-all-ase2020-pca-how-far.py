import sys
import pandas as pd
import subprocess
import os

PCA_HOW_FAR_DATASET = os.getenv('PCA_HOW_FAR_DATASET')

how_far_csv = 'experiments/ase2020-pca-how-far.csv'
df_how_far = pd.read_csv(how_far_csv)

log_dir = os.path.join(PCA_HOW_FAR_DATASET, 'setup-logs')
if not os.path.exists(log_dir):
    os.makedirs(log_dir)

# Loop through the dataset and run the setup for each subject
for index, row in df_how_far.iterrows():
    subject_id = row['id']
    print(f'Running setup for subject: {subject_id}')
    # Run setup and save output to a log file
    log_file = os.path.join(log_dir, subject_id+'.log')
    cmd = f'python3 experiments/setup-ase2020-pca-how-far.py {subject_id} > {log_file}'
    subprocess.call(cmd, shell=True)

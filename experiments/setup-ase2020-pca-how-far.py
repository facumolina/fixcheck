import os
import sys
import json
import subprocess
import pandas as pd

# Script to setup a subject from the paper Automated Patch Correctness Assessment: How Far are We? to run FixCheck
# The script performs the following steps:
#   1. Extract the corresponding defects4j project from the subject config file
#   2. Checkout and compile the project
#   3. Apply the target patch and compile the patched project
#
# Usage: python3 experiments/setup-ase2020-pca-how-far.py <subject_id>
# Example: python3 experiments/setup-ase2020-pca-how-far.py Patch1

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
PCA_HOW_FAR_DATASET = os.getenv('PCA_HOW_FAR_DATASET')

subject_id = sys.argv[1]
how_far_csv = 'experiments/ase2020-pca-how-far.csv'
df_how_far = pd.read_csv(how_far_csv)

print(f'> Setup for ASE2020PCA-HOW-FAR subject: {subject_id}')

# Extract the data from the config file and show it
subject_data = df_how_far[df_how_far['id'] == subject_id]
if subject_data.empty:
    print(f'subject {subject_id} does not exist in the dataset')
    sys.exit(1)
subject_id = subject_data['id'].values[0]
patch_correctness = subject_data['correctness'].values[0]
defects4j_project = subject_data['project'].values[0]
defects4j_bug_id = str(subject_data['bug'].values[0])
tool = subject_data['tool'].values[0]

print(f'project: {defects4j_project}')
print(f'bug_id: {defects4j_bug_id}')
print(f'tool: {tool}')
print(f'correctness: {patch_correctness}')

# Checkout the project
# Only checkout the corresponding version if the working dir do not exist.
patch_base_dir = defects4j_project+defects4j_bug_id+'b'
working_dir = os.path.join(PCA_HOW_FAR_DATASET, f'tmp/{subject_id}/{patch_base_dir}')
if not os.path.isdir(working_dir):
    cmd = f'defects4j checkout -p {defects4j_project} -v {defects4j_bug_id}b -w {working_dir}'
    print(f'---> checking out with command: {cmd}')
    subprocess.call(cmd, shell=True)
    print()
    # Compile and run the defects4j test command to preserve the current test failure
    os.chdir(working_dir)
    cmd = f'defects4j compile'
    print(f'---> compiling with command: {cmd}')
    subprocess.call(cmd, shell=True)
    cmd = f'defects4j test'
    print(f'---> generating test report with command: {cmd}')
    subprocess.call(cmd, shell=True)
    print()
    # Search for the patch in the dir PCA_HOW_FAR_DATASET/Patches
    patches_dir = os.path.join(PCA_HOW_FAR_DATASET, 'Patches')
    for root, dirs, files in os.walk(patches_dir):
        for file in files:
            if file == subject_id+'.patch':
                 patch_file = os.path.join(root, file)
    if not os.path.exists(patch_file):
        print(f'unable to find patch file for subject {subject_id}')
        sys.exit(1)
    #subject_dir = os.path.join(PCA_HOW_FAR_DATASET, f'tmp/{subject_id}')
    print(f'---> patch file: {patch_file}')
    os.chdir(working_dir)
    print(f'---> current dir: {os.getcwd()}')
    cmd = f'patch -u -p1 < {patch_file}'
    print(f'---> applying patch with command: {cmd}')
    subprocess.call(cmd, shell=True)
    print()
else:
    print(f'---> working dir already exists: {working_dir}')
    print()

# Compile the project including the patch
os.chdir(working_dir)
cmd = f'defects4j compile'
print(f'--> compiling with cmd: {cmd}')
subprocess.call(cmd, shell=True)
print()

print('Setup finished!')
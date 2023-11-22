import os
import sys
import json
import subprocess

# Script to setup a DefectsRepairing subject to run FixCheck
# The script performs the following steps:
#   1. Extract the corresponding defects4j project from the subject config file
#   2. Checkout and compile the project
#   3. Apply the target patch and compile the patched project
#
# Usage: python3 experiments/setup-defect-repairing.py <subject_id>
# Example: python3 experiments/setup-defect-repairing.py Patch1

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')

subject_id = sys.argv[1]
print(f'> Setup for DefectsRepairing subject: {subject_id}')
subject_config_file = os.path.join(DEFECT_REPAIRING_DATASET, 'tool/patches/INFO/', subject_id+'.json')

if not os.path.exists(subject_config_file):
    print(f'subject {subject_id} does not exist in the dataset')
    sys.exit(1)

# Extract the data from the config file and show it
f = open(subject_config_file)
subject_data = json.load(f)
subject_id = subject_data['ID']
patch_correctness = subject_data['correctness']
defects4j_project = subject_data['project']
defects4j_bug_id = subject_data['bug_id']
print(f'id: {subject_id}')
print(f'correctness: {patch_correctness}')
print(f'project: {defects4j_project}')
print(f'bug_id: {defects4j_bug_id}')
print()

# Checkout the project
# Only checkout the corresponding version if the working dir do not exist.
patch_base_dir = defects4j_project+defects4j_bug_id+'b'
working_dir = os.path.join(DEFECT_REPAIRING_DATASET, f'tmp/{subject_id}/{patch_base_dir}')
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
	# Apply the patch
	patch_file = os.path.join(DEFECT_REPAIRING_DATASET, f'tool/patches/{subject_id}')
	subject_dir = os.path.join(DEFECT_REPAIRING_DATASET, f'tmp/{subject_id}')
	os.chdir(subject_dir)
	cmd = f'patch -u -p0 < {patch_file}'
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
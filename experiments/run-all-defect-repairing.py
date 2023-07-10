import sys
import pandas as pd
import subprocess
import os

assertion_generation = sys.argv[1] # One of no-assertion, previous-assertion or llm-assertion

target_ids = [1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 58, 59, 62, 63, 64, 65, 66, 67, 68, 69, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 88, 89, 90, 91, 92, 93, 150, 151, 152, 153, 154, 155, 157, 158, 159, 160, 161, 162, 163, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 191, 192, 193, 194, 195, 196, 197, 198, 199, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 'HDRepair1', 'HDRepair3', 'HDRepair4', 'HDRepair5', 'HDRepair6', 'HDRepair7', 'HDRepair8', 'HDRepair9', 'HDRepair10']
# Append each id to the word Patch
target_patches = ['Patch' + str(id) for id in target_ids]

# Run the script run-fixcheck-defect-repairing.py for each patch
for patch in target_patches:
    # Do not run if report exits
    report_file = f'fixcheck-output/defects-repairing/{patch}/{assertion_generation}/report.csv'
    if os.path.exists(report_file):
        print(f'Report already exists for patch: {patch}')
        continue
    print(f'Running fixcheck for patch: {patch}')
    subprocess.run(f'python3 experiments/run-fixcheck-defect-repairing.py {patch} {assertion_generation}', shell=True)

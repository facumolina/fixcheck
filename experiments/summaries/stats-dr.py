import sys
import pandas as pd
import subprocess
import os

target_ids = [1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 58, 59, 62, 63, 64, 65, 66, 67, 68, 69, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 88, 89, 90, 91, 92, 93, 150, 151, 152, 153, 154, 155, 157, 158, 159, 160, 161, 162, 163, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 191, 192, 193, 194, 195, 196, 197, 198, 199, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 'HDRepair1', 'HDRepair3', 'HDRepair4', 'HDRepair5', 'HDRepair6', 'HDRepair7', 'HDRepair8', 'HDRepair9', 'HDRepair10']
# Append each id to the word Patch
target_patches = ['Patch' + str(id) for id in target_ids]
dataset_csv = 'experiments/defect-repairing-subjects.csv'
df = pd.read_csv(dataset_csv)

# Set of bugs ids
bug_ids = set()
# Run the script run-fixcheck-defect-repairing.py for each patch
for patch in target_patches:
    # Do not run if report exits
    subject_data = df[df['id'] == patch]
    project = subject_data['project'].values[0]
    bug = subject_data['bug'].values[0]
    bug_id = project + '-' + str(bug)
    bug_ids.add(bug_id)

print(f'Number of bugs: {len(bug_ids)}')
print(f'Bugs ids: {bug_ids}')

# Get unique bugs ids for project Chart from the df
chart_df = df[df['project'] == 'Chart']
chart_bug_ids = set()
for index, row in chart_df.iterrows():
    if row['id'] not in target_patches:
        continue
    bug_id = row['project'] + '-' + str(row['bug'])
    chart_bug_ids.add(bug_id)
print(f'Number of bugs for Chart: {len(chart_bug_ids)}')
print(f'Bugs ids for Chart: {chart_bug_ids}')

# Get unique bugs ids for project Lang from the df
lang_df = df[df['project'] == 'Lang']
lang_bug_ids = set()
for index, row in lang_df.iterrows():
    if row['id'] not in target_patches:
        continue
    bug_id = row['project'] + '-' + str(row['bug'])
    lang_bug_ids.add(bug_id)
print(f'Number of bugs for Lang: {len(lang_bug_ids)}')
print(f'Bugs ids for Lang: {lang_bug_ids}')

# Get unique bugs ids for project Math from the df
math_df = df[df['project'] == 'Math']
math_bug_ids = set()
for index, row in math_df.iterrows():
    if row['id'] not in target_patches:
        continue
    bug_id = row['project'] + '-' + str(row['bug'])
    math_bug_ids.add(bug_id)
print(f'Number of bugs for Math: {len(math_bug_ids)}')
print(f'Bugs ids for Math: {math_bug_ids}')

# Get unique bugs ids for project Time from the df
time_df = df[df['project'] == 'Time']
time_bug_ids = set()
for index, row in time_df.iterrows():
    if row['id'] not in target_patches:
        continue
    bug_id = row['project'] + '-' + str(row['bug'])
    time_bug_ids.add(bug_id)
print(f'Number of bugs for Time: {len(time_bug_ids)}')
print(f'Bugs ids for Time: {time_bug_ids}')
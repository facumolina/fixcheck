import sys
import pandas as pd
import os

# Config variables
outputs_dir = 'fixcheck-output'

# Generate a new file by combining all the files in the output directory
output_file = os.path.join(outputs_dir, 'bad-fixes-summary.csv')
print(f'Generating summary file: {output_file}')
df = pd.DataFrame()

files = os.listdir(outputs_dir)
# Sort files by names
files.sort()
# List the files in the directory
for file in files:
    if file.endswith(".csv") and not file.endswith('summary.csv'):
        file_path = os.path.join(outputs_dir, file)
        print(f'Processing file: {file_path}')
        subject_df = pd.read_csv(file_path)
        subject_df.insert(0, 'subject', file.split('-report')[0])
        df = pd.concat([df, subject_df])

# Remove column inputs_class
df = df.drop(columns=['inputs_class', 'target_class'])
df.to_csv(output_file, index=False)


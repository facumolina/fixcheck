import sys
import pandas as pd
import os

results_dir = "fixcheck-output/ase2020g"
dataset_csv = 'experiments/ase2020-pca-how-far.csv'
df_how_far = pd.read_csv(dataset_csv)

# Empty dataframe with the columns we need
time_and_detection = pd.DataFrame(columns=['subject','project','incorrect','fixcheck_time_tg','fixcheck_time_ag','fixcheck_pred'])

assertion_generation = sys.argv[1]

FIXCHECK_SCORE_THRESHOLD = 0.4
fixcheck_detected = []
incorrect_patches = []

# Loop through the dataset and analyze the results for each subject
for index, row in df_how_far.iterrows():
    subject_id = row['id']
    if row['correctness'] == 'Incorrect':
        incorrect_patches.append(subject_id)

    report_file = f'fixcheck-output/ase2020/{subject_id}/{assertion_generation}/report.csv'
    if not os.path.exists(report_file):
        print(f'No results for: {subject_id}')
        continue
    report = pd.read_csv(report_file)
    prefixes_gen_time_ms = report['prefixes_gen_time'].sum()
    assertions_gen_time_ms = report['assertions_gen_time'].sum()
    prefix_running_time_ms = report['prefixes_running_time'].sum()

    prefixes_gen_time = prefixes_gen_time_ms // 1000
    assertions_gen_time = assertions_gen_time_ms // 1000
    prefix_running_time = prefix_running_time_ms // 1000

    fixcheck_time_tg = prefixes_gen_time + prefix_running_time
    fixcheck_time_ag = assertions_gen_time

    score_file = f'fixcheck-output/ase2020/{subject_id}/{assertion_generation}/scores-failing-tests.csv'
    score_df = pd.read_csv(score_file)
    max_score = score_df['score'].max()
    fixcheck_pred = 1 if max_score >= FIXCHECK_SCORE_THRESHOLD else 0
    if fixcheck_pred == 1:
        fixcheck_detected.append(subject_id)

    inc = 1 if row['correctness'] == 'Incorrect' else 0
    new_row = {'subject': subject_id, 'project': row['project'], 'incorrect': inc, 'fixcheck_time_tg': fixcheck_time_tg, 'fixcheck_time_ag': fixcheck_time_ag, 'fixcheck_pred': fixcheck_pred}
    time_and_detection = pd.concat([time_and_detection, pd.DataFrame([new_row])])

# Save the results to a csv file
time_and_detection.to_csv(f'fixcheck-output/ase2020/fixcheck-time-and-detection-{assertion_generation}.csv', index=False)

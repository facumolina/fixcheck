import os
import sys
import json
import pandas as pd

results_dir = "fixcheck-output/defects-repairing"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')
dataset_csv = 'experiments/defect-repairing-subjects.csv'
dataset_df = pd.read_csv(dataset_csv)
failing_same_reason = []
failing_different_reason = []
assertion_generation = sys.argv[1] # One of no-assertion, previous-assertion or llm-assertion

# First save the results for DEFECT_REPAIRING_DATASET
no_failure = []
no_report = []
score_threshold = 0.80

# Initialize map of patch -> exception_type
patch_with_same_exceptions = {}
patch_tests = {}
for subject_id in os.listdir(results_dir):
    if subject_id.endswith('.csv'):
        continue
    print('---------------------------------')
    print(f'Processing subject: {subject_id}')

    # Get the FixCheck results
    base_folder = os.path.join(subject_id,assertion_generation)
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')
    if not os.path.exists(report_csv):
        print(f'No report for {subject_id}')
        no_report.append(subject_id)
        continue

    score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
    score_df = pd.read_csv(score_file)
    failing_tests = len(score_df)
    if failing_tests == 0:
        print(f'No failing tests for {subject_id}')
        continue

    max_score = score_df['score'].max()
    prediction = 0 if max_score >= score_threshold else 1
    if prediction == 0:
        subject_data = dataset_df[dataset_df['id'] == subject_id]
        project = subject_data['project'].values[0]
        bug = subject_data['bug'].values[0]
        patch_base_dir = project+str(bug)+"b"
        subject_base_dir = os.path.join(DEFECT_REPAIRING_DATASET, f'tmp/{subject_id}/{patch_base_dir}')
        failure_log = os.path.join(subject_base_dir, 'failing_tests')
        # get the first line of the failure log
        if not os.path.exists(failure_log):
            print(f'No failure log for {subject_id}')
            no_failure.append(subject_id)
            continue
        with open(failure_log, 'r') as f:
            first_line = f.readline()
            failing_reason = f.readline()
            exception_type = failing_reason.split(':')[0]
            exception_type = exception_type.replace('\n', '')
            print(f'original exception: {exception_type}')

        # The patch is predicted as incorrect, thus we need to check if the exception is the same
        # Get the test with score max_score
        failing_tests_folder = os.path.join(results_dir, base_folder, 'failing-tests')

        failing_test = score_df[score_df['score'] == max_score]['prefix'].values[0]
        patch_tests[subject_id] = failing_test
        failing_test_log_file = os.path.join(failing_tests_folder, f'{failing_test}-failure0.txt')
        print(f'Processing failing log: {failing_test_log_file}')
        # Read first line of failing_test_log_file
        with open(failing_test_log_file) as f:
            first_line = f.readline()
            found_exception = first_line.split(':')[0]
            found_exception = found_exception.replace('\n', '')
            print(f'Found exception: {found_exception}')
            if exception_type == found_exception:
                failing_same_reason.append(subject_id)
                patch_with_same_exceptions[subject_id] = exception_type
            else:
                failing_different_reason.append(subject_id)
    else:
        print(f'Patch is predicted as correct for {subject_id}')

print()
print('---------------------------------')
print(f'No failure: {len(no_failure)}')
print(f'No report: {len(no_report)}')
print(no_report)
print()
print(f'Failing same reason: {len(failing_same_reason)}')
print(failing_same_reason)
print('With failing reasons:')
print(patch_with_same_exceptions)
print('With failing tests:')
print(patch_tests)
print()
print(f'Failing different reason: {len(failing_different_reason)}')
print(failing_different_reason)

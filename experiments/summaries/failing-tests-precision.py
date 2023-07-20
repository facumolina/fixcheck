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

per_patches = pd.DataFrame(columns=['project','patch_id','correctness','original_failure','found_failure','same_reason'])

patch_with_same_exceptions = {}
patch_tests = {}
# Save results for DEFECT_REPAIRING_DATASET
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
                same_reason = 1
            else:
                failing_different_reason.append(subject_id)
                same_reason = 0
        new_row = {'project': project, 'patch_id': subject_id, 'correctness': 0, 'original_failure': exception_type, 'found_failure': found_exception, 'same_reason': same_reason}
        per_patches = pd.concat([per_patches, pd.DataFrame([new_row])])
    else:
        print(f'Patch is predicted as correct for {subject_id}')

# Now save results for BF4J_DATASET
results_dir = "fixcheck-output/bf4j"
BF4J_DATASET = os.getenv('BF4J_DATASET')
for subject_id in os.listdir(results_dir):
    if subject_id.endswith('.csv'):
        continue
    base_folder = os.path.join(subject_id,assertion_generation)
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')
    subject_config_json = os.path.join(BF4J_DATASET, f'data/{subject_id}/bad-fix.json')
    # Load json
    with open(subject_config_json) as f:
        patch_json = json.load(f)
    print(f'Processing report: {report_csv}')
    if not os.path.exists(report_csv):
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
        failures_files = patch_json['bug']['failures']
        # Get failures which name ends in .failure
        failures_files = [f['failure'] for f in failures_files]
        if len(failures_files) == 0:
            print(f'No failure log for {subject_id}')
            no_failure.append(subject_id)
            continue
        # Get the first failure
        failure_file = failures_files[0]
        full_failure_filename = os.path.join(BF4J_DATASET, f'data/{subject_id}/{failure_file}')
        with open(full_failure_filename, 'r') as f:
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
                same_reason = 1
            else:
                failing_different_reason.append(subject_id)
                same_reason = 0

        project = 'bf4j'
        correctness = 0
        new_row = {'project': project, 'patch_id': subject_id, 'correctness': 0, 'original_failure': exception_type, 'found_failure': found_exception, 'same_reason': same_reason}
        per_patches = pd.concat([per_patches, pd.DataFrame([new_row])])
    else:
        print(f'Patch is predicted as correct for {subject_id}')


print()
print('---------------------------------')
print(f'No failure: {len(no_failure)}')
print(f'No report: {len(no_report)}')
print(no_report)

chart_row_latex = ['chart']
lang_row_latex = ['lang']
math_row_latex = ['math']
time_row_latex = ['time']
bf4j_row_latex = ['bf4j']
total_row_latex = ['total']

def save_results_for_project(patches,project,row_latex):
    if project == 'TOTAL':
        proj_rows = patches
    else:
        proj_rows = patches[patches['project'] == project]

    total_patches = len(proj_rows)
    total_same_reason_patches = len(proj_rows[proj_rows['same_reason'] == 1])
    total_different_reason_patches = len(proj_rows[proj_rows['same_reason'] == 0])



    row_latex.append(total_patches)
    row_latex.append(total_same_reason_patches)
    row_latex.append(total_different_reason_patches)
    # Precision with 2 decimals
    precision = round(total_same_reason_patches/total_patches,2)
    row_latex.append(precision)


save_results_for_project(per_patches,'Chart',chart_row_latex)
save_results_for_project(per_patches,'Lang',lang_row_latex)
save_results_for_project(per_patches,'Math',math_row_latex)
save_results_for_project(per_patches,'Time',time_row_latex)
save_results_for_project(per_patches,'bf4j',bf4j_row_latex)
save_results_for_project(per_patches,'TOTAL',total_row_latex)
print()

print('----------------------------------')
print('Latex table')
# Print latex rows for chart with each element in the list interleaved with &
print(' & '.join([str(elem) for elem in chart_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in lang_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in math_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in time_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in bf4j_row_latex]) + ' \\\\')
print('\midrule')
print(' & '.join([str(elem) for elem in total_row_latex]) + ' \\\\')


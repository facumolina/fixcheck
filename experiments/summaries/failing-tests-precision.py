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
score_threshold = 0.40


ground_truth_target_exceptions = {
    'avro-2944': ['org.apache.avro.InvalidAvroMagicException'],
    'caffeine-220': [],
    'choco-600': ['java.lang.AssertionError'],
    'cli-162': ['java.lang.RuntimeException'],
    'cli-164': ['junit.framework.AssertionFailedError', 'java.lang.AssertionError'],
    'cli-265': ['java.lang.AssertionError'],
    'csv-93': ['java.lang.AssertionError'],
    'graphhopper-145': ['java.lang.AssertionError'],
    'graphhopper-172': ['java.lang.AssertionError'],
    'graphhopper-539': ['java.lang.AssertionError'],
    'io-101': ['junit.framework.AssertionFailedError', 'java.lang.AssertionError'],
    'jackson-databind-118': ['junit.framework.ComparisonFailure','java.lang.NullPointerException'],
    'jackson-databind-1195': ['junit.framework.AssertionFailedError', 'java.lang.AssertionError'],
    'jackson-databind-735': ['junit.framework.AssertionFailedError', 'java.lang.AssertionError'],
    'jxpath-108': ['junit.framework.AssertionFailedError', 'java.lang.AssertionError'],
    'jxpath-93': ['junit.framework.AssertionFailedError', 'java.lang.AssertionError'],
    'lang-710': ['java.lang.StringIndexOutOfBoundsException'], # May be wrong
    'math-1208': ['java.lang.AssertionError'],
    'math-790': ['java.lang.AssertionError'], # May be wrong
    'time4j-140': ['java.lang.AssertionError'],
    'time4j-577': ['java.text.ParseException'],
    'time4j-728': ['java.lang.AssertionError']
    }

map_names = {
    'commons-math': 'math',
    'commons-cli': 'cli',
    'Time4J' : 'time4j',
    'commons-jxpath': 'jxpath',
    'commons-lang': 'lang',
    'choco-solver': 'choco',
    'commons-csv': 'csv',
    'commons-io': 'io'
}

per_patches = pd.DataFrame(columns=['project','patch_id','correctness','original_failure','found_failure','same_reason'])

patch_with_same_exceptions = {}
patch_tests = {}

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
    print('------------------')
    print(f'Processing patch: {subject_id}')
    if not os.path.exists(report_csv):
        no_report.append(subject_id)
        continue

    score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
    score_df = pd.read_csv(score_file)
    failing_tests = len(score_df)

    # Discarded tests are the ones with score < score_threshold
    discarded_tests = 0
    ranked_tests = 0
    if failing_tests > 0:
        discarded_tests = len(score_df[score_df['score'] < score_threshold])
        ranked_tests = len(score_df[score_df['score'] >= score_threshold])

    max_score = score_df['score'].max()
    prediction = 0 if max_score >= score_threshold else 1
    if prediction == 0 or prediction == 1:
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
        #print(f'Processing failure log: {full_failure_filename}')
        with open(full_failure_filename, 'r') as f:
            failing_reason = f.readline()
            exception_type = failing_reason.split(':')[0]
            exception_type = exception_type.replace('\n', '')
            #print(f'original exception: {exception_type}')

        if ground_truth_target_exceptions[subject_id] == []:
            print(f'No ground truth exception for {subject_id}')
            continue
        target_exceptions = ground_truth_target_exceptions[subject_id]
        print(f'ground truth exception: {target_exceptions}')

        if failing_tests == 0:
            project = patch_json['project']
            if project in map_names:
                project = map_names[project]
            new_row = {'project': project, 'patch_id': subject_id, 'correctness': 0, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'ranked_tests': ranked_tests, 'original_failure': exception_type, 'found_failure': '-', 'same_reason': '-'}
            per_patches = pd.concat([per_patches, pd.DataFrame([new_row])])
            continue

        # The patch is predicted as incorrect, thus we need to check if the exception is the same
        # Get the test with score max_score
        failing_tests_folder = os.path.join(results_dir, base_folder, 'failing-tests')
        failing_test = score_df[score_df['score'] == max_score]['prefix'].values[0]
        patch_tests[subject_id] = failing_test
        failing_test_log_file = os.path.join(failing_tests_folder, f'{failing_test}-failure0.txt')
        print(f'Failing test is: {failing_test}')
        print(f'failing test trace: {failing_test_log_file}')
        # Read first line of failing_test_log_file
        with open(failing_test_log_file) as f:
            first_line = f.readline()
            found_exception = first_line.split(':')[0]
            found_exception = found_exception.replace('\n', '')
            print(f'test exception: {found_exception}')
            found = exception_type == found_exception
            found = found_exception in target_exceptions
            if found:
                failing_same_reason.append(subject_id)
                patch_with_same_exceptions[subject_id] = exception_type
                same_reason = 1
            else:
                failing_different_reason.append(subject_id)
                same_reason = 0

        project = patch_json['project']
        if project in map_names:
            project = map_names[project]

        correctness = 0
        new_row = {'project': project, 'patch_id': subject_id, 'correctness': 0, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'ranked_tests': ranked_tests, 'original_failure': exception_type, 'found_failure': found_exception, 'same_reason': same_reason}
        per_patches = pd.concat([per_patches, pd.DataFrame([new_row])])
    else:
        print(f'Patch is predicted as correct for {subject_id}')


print()
print('---------------------------------')
print(f'No failure: {len(no_failure)}')
print(f'No report: {len(no_report)}')
print(no_report)

total_row_latex = ['total']

def save_results_for_project(patches,project,row_latex):
    if project == 'TOTAL':
        proj_rows = patches
    else:
        proj_rows = patches[patches['project'] == project]

    total_patches = len(proj_rows)
    # total_same_reason_patches are patches with same_reason == 1 and ranked_tests > 0
    total_same_reason_patches = len(proj_rows[(proj_rows['same_reason'] == 1) & (proj_rows['ranked_tests'] > 0)])
    total_different_reason_patches = len(proj_rows[(proj_rows['same_reason'] == 0) & (proj_rows['ranked_tests'] > 0)])
    failing_tests = int(proj_rows['failing_tests'].sum())
    discarded_tests = int(proj_rows['discarded_tests'].sum())
    ranked_tests = int(proj_rows['ranked_tests'].sum())

    # Count rows with ranked_tests > 0
    reported_fault_revealing_tests = len(proj_rows[proj_rows['ranked_tests'] > 0])
    recall = round(total_same_reason_patches/total_patches,2)

    row_latex.append(total_patches)
    row_latex.append(failing_tests)
    row_latex.append(discarded_tests)
    row_latex.append(ranked_tests)
    row_latex.append(reported_fault_revealing_tests)

    row_latex.append(total_same_reason_patches)
    #row_latex.append(total_different_reason_patches)

    # Precision with 2 decimals
    precision = 1.0
    if reported_fault_revealing_tests > 0:
        precision = round(total_same_reason_patches/reported_fault_revealing_tests,2)

    row_latex.append(precision)
    row_latex.append(recall)



print('----------------------------------')
print('INFO')
print(f'score_threshold: {score_threshold}')
print(f'Patches with same reason: {len(patch_with_same_exceptions)}')
print('Failing same reason')
print(failing_same_reason)
print('Failing different reason')
print(failing_different_reason)
print()


# Sort per_patches by project name
per_patches = per_patches.sort_values(by=['project'])

# Loop for each unique project
print('----------------------------------')
print('Latex table')
for project in per_patches['project'].unique():
    row_latex = [project]
    save_results_for_project(per_patches,project,row_latex)
    print(' & '.join([str(elem) for elem in row_latex]) + ' \\\\')
save_results_for_project(per_patches,'TOTAL',total_row_latex)
print('\midrule')
print(' & '.join([str(elem) for elem in total_row_latex]) + ' \\\\')
print('----------------------------------')

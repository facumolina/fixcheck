import sys
import os

assertion_generation = sys.argv[1] # Assertion generation
failure_reason = sys.argv[2] # Failure reason

results_dir = "fixcheck-output/defects-repairing"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')

# Set of subjects failing by reason
subjects_failing_by_reason = set()

for subject_id in os.listdir(results_dir):
    if subject_id.endswith('.csv'):
        continue
    base_folder = os.path.join(subject_id, assertion_generation)
    failing_tests_folder = os.path.join(results_dir, base_folder, 'failing-tests')
    # if the failing tests folder exists
    if os.path.exists(failing_tests_folder):
        # Loop over all failing tests
        for failing_test in os.listdir(failing_tests_folder):
            if failing_test.endswith('.java'):
                continue
            failing_test_log_file = os.path.join(failing_tests_folder, failing_test)
            print(f'Processing failing test: {failing_test_log_file}')
            # Read first line of failing_test_log_file
            with open(failing_test_log_file) as f:
                first_line = f.readline()
                exception_type = first_line.split(':')[0]
                print(f'Exception type: {exception_type}')
                if exception_type == failure_reason:
                    subjects_failing_by_reason.add(subject_id)
                    break
print()
print(f'Subjects failing by reason: {len(subjects_failing_by_reason)}')
print(subjects_failing_by_reason)
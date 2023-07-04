import sys
import os

assertion_generation = sys.argv[1] # Assertion generation
results_dir = "fixcheck-output/defects-repairing"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')

# Initialize map of exception_type -> exception_count
exception_type_count = {}

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
                print()
                if exception_type in exception_type_count:
                    exception_type_count[exception_type] += 1
                else:
                    exception_type_count[exception_type] = 1


print(f'Exception type count: {len(exception_type_count)}')
print(f'Total exceptions: {sum(exception_type_count.values())}')
# Print exception counts sorted by value
sorted_exception_type_count = {k: v for k, v in sorted(exception_type_count.items(), key=lambda item: item[1], reverse=True)}
print(sorted_exception_type_count)
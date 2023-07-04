import plotly.graph_objs as go
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

sorted_exception_type_count = {k: v for k, v in sorted(exception_type_count.items(), key=lambda item: item[1], reverse=True)}
print(sorted_exception_type_count)

failure_types = ['Crashes','Assertion Failure']
failures_by_type = [0,0]
assertion_failure_types = ['junit.framework.AssertionFailedError', 'java.lang.AssertionError']
crashes_types = []
assertion_failure_values = []
crashes_values = []

# Loop over each exception type
for exception_type in sorted_exception_type_count:
    if exception_type in assertion_failure_types:
        failures_by_type[1] += exception_type_count[exception_type]
        assertion_failure_values.append(exception_type_count[exception_type])
    else:
        crashes_types.append(exception_type)
        failures_by_type[0] += exception_type_count[exception_type]
        crashes_values.append(exception_type_count[exception_type])

print(f'Failures by type: {failures_by_type}')
outer_values = failures_by_type
failure_reasons = list(crashes_types) + assertion_failure_types
# Inner values is the join of crashes_values and assertion_failure_values
inner_values = crashes_values + assertion_failure_values

trace1 = go.Pie(
    hole=0.5,
    sort=False,
    direction='clockwise',
    domain={'x': [0.15, 0.85], 'y': [0.15, 0.85]},
    values=inner_values,
    labels=failure_reasons,
    textinfo='label',
    textposition='inside',
    marker={'line': {'color': 'white', 'width': 1}}
)

trace2 = go.Pie(
    hole=0.7,
    sort=False,
    direction='clockwise',
    values=outer_values,
    labels=failure_types,
    textinfo='label',
    textposition='outside',
    marker={'colors': ['green', 'red'],
            'line': {'color': 'white', 'width': 1}}
)

fig = go.FigureWidget(data=[trace1, trace2])
fig.show()
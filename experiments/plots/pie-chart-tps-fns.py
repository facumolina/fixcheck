import plotly.graph_objs as go
import sys
import os
import plotly.io as pio
import pandas as pd
import json

pio.kaleido.scope.mathjax = None

results_dir = "fixcheck-output/defects-repairing"
results_dir_2 = "fixcheck-output/bf4j"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')
BF4J_DATASET = os.getenv('BF4J_DATASET')
dataset_csv = 'experiments/defect-repairing-subjects.csv'
dataset_df = pd.read_csv(dataset_csv)

score_threshold = 0.80
assertion_generation = sys.argv[1]
no_report = []
no_failure = []

# True positives/False negatives is a map of subject_id -> original_failure_reason
true_positives_map = {}
false_negatives_map = {}
true_positives_exceptions_count = {}
false_negatives_exceptions_count = {}

# First save the results for DEFECT_REPAIRING_DATASET
for subject_id in os.listdir(results_dir):
    if subject_id.endswith('.csv'):
        continue
    base_folder = os.path.join(subject_id,assertion_generation)
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')
    subject_config_json = os.path.join(DEFECT_REPAIRING_DATASET, f'tool/patches/INFO/{subject_id}.json')
    with open(subject_config_json) as f:
        patch_json = json.load(f)
    print(f'Processing report: {report_csv}')
    print(f'Correctness: {patch_json["correctness"]}')
    if not os.path.exists(report_csv):
        no_report.append(subject_id)
        continue
    project = patch_json["project"]
    # Correctness is 1 if Correct and 0 if Incorrect
    correctness = 1 if patch_json["correctness"] == "Correct" else 0
    score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
    score_df = pd.read_csv(score_file)
    max_score = score_df['score'].max()
    # Prediction is 0 if max_score >= 0.80 and 1 otherwise
    prediction = 0 if max_score >= score_threshold else 1

    if (correctness == 0):
        subject_data = dataset_df[dataset_df['id'] == subject_id]
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

        if (prediction==0):
            true_positives_map[subject_id] = exception_type
            if exception_type in true_positives_exceptions_count:
                true_positives_exceptions_count[exception_type] += 1
            else:
                true_positives_exceptions_count[exception_type] = 1
        else:
            false_negatives_map[subject_id] = exception_type
            if exception_type in false_negatives_exceptions_count:
                false_negatives_exceptions_count[exception_type] += 1
            else:
                false_negatives_exceptions_count[exception_type] = 1

# Now save the results from BF4J_DATASET
for subject_id in os.listdir(results_dir_2):
    if subject_id.endswith('.csv'):
        continue
    base_folder = os.path.join(subject_id,assertion_generation)
    report_csv = os.path.join(results_dir_2, base_folder, 'report.csv')
    subject_config_json = os.path.join(BF4J_DATASET, f'data/{subject_id}/bad-fix.json')
    # Load json
    with open(subject_config_json) as f:
        patch_json = json.load(f)
    print(f'Processing report: {report_csv}')
    if not os.path.exists(report_csv):
        no_report.append(subject_id)
        continue
    project = 'bf4j'
    score_file = os.path.join(results_dir_2, base_folder, 'scores-failing-tests.csv')
    score_df = pd.read_csv(score_file)
    max_score = score_df['score'].max()
    prediction = 0 if max_score >= score_threshold else 1

    # get the first line of the failure log
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

    if (prediction==0):
        true_positives_map[subject_id] = exception_type
        if exception_type in true_positives_exceptions_count:
            true_positives_exceptions_count[exception_type] += 1
        else:
            true_positives_exceptions_count[exception_type] = 1
    else:
        false_negatives_map[subject_id] = exception_type
        if exception_type in false_negatives_exceptions_count:
            false_negatives_exceptions_count[exception_type] += 1
        else:
            false_negatives_exceptions_count[exception_type] = 1


# Show true positives / false negatives
print(f'True positives: {len(true_positives_map)}')
print(true_positives_map)
print()
print(f'False negatives: {len(false_negatives_map)}')
print(false_negatives_map)

sorted_true_positives_exceptions_count = {k: v for k, v in sorted(true_positives_exceptions_count.items(), key=lambda item: item[1], reverse=True)}
sorted_false_negatives_exceptions_count = {k: v for k, v in sorted(false_negatives_exceptions_count.items(), key=lambda item: item[1], reverse=True)}
print(sorted_true_positives_exceptions_count)
print(sorted_false_negatives_exceptions_count)

prediction_types = ['TP','FN']
prediction_by_type = [len(true_positives_map), len(false_negatives_map)]
# Positives percentage 2 decimals
positives_percentage = round((len(true_positives_map) / (len(true_positives_map) + len(false_negatives_map))) * 100, 1)
positives_percentage = str(positives_percentage) + '%'
negatives_percentage = round((len(false_negatives_map) / (len(true_positives_map) + len(false_negatives_map))) * 100, 1)
negatives_percentage = str(negatives_percentage) + '%'
prediction_types = ['TP('+positives_percentage+')','FN('+negatives_percentage+')']
outer_values = prediction_by_type

failure_reasons = []
failure_counts = []
all_colors = []

assertion_failure_types = ['junit.framework.AssertionFailedError', 'java.lang.AssertionError', 'junit.framework.ComparisonFailure']
index_out_of_bound_types = ['java.lang.ArrayIndexOutOfBoundsException', 'java.lang.IndexOutOfBoundsException', 'java.lang.StringIndexOutOfBoundsException']
null_pointer_types = ['java.lang.NullPointerException']
illegal_argument_types = ['java.lang.IllegalArgumentException']

count_in_positives = []
all_assertion_failures = 0
all_index_out_of_bound = 0
all_null_pointer = 0
all_illegal_argument = 0
all_other = 0
for exception_type in sorted_true_positives_exceptions_count:
    if exception_type in assertion_failure_types:
        all_assertion_failures += sorted_true_positives_exceptions_count[exception_type]
    elif exception_type in index_out_of_bound_types:
        all_index_out_of_bound += sorted_true_positives_exceptions_count[exception_type]
    elif exception_type in null_pointer_types:
        all_null_pointer += sorted_true_positives_exceptions_count[exception_type]
    elif exception_type in illegal_argument_types:
        all_illegal_argument += sorted_true_positives_exceptions_count[exception_type]
    else:
        all_other += sorted_true_positives_exceptions_count[exception_type]

count_in_positives = [all_assertion_failures, all_index_out_of_bound, all_null_pointer, all_illegal_argument, all_other]

count_in_negatives = []
all_assertion_failures = 0
all_index_out_of_bound = 0
all_null_pointer = 0
all_illegal_argument = 0
all_other = 0
for exception_type in sorted_false_negatives_exceptions_count:
    if exception_type in assertion_failure_types:
        all_assertion_failures += sorted_false_negatives_exceptions_count[exception_type]
    elif exception_type in index_out_of_bound_types:
        all_index_out_of_bound += sorted_false_negatives_exceptions_count[exception_type]
    elif exception_type in null_pointer_types:
        all_null_pointer += sorted_false_negatives_exceptions_count[exception_type]
    elif exception_type in illegal_argument_types:
        all_illegal_argument += sorted_false_negatives_exceptions_count[exception_type]
    else:
        all_other += sorted_false_negatives_exceptions_count[exception_type]

count_in_negatives = [all_assertion_failures, all_index_out_of_bound, all_null_pointer, all_illegal_argument, all_other]

#for exception_type in sorted_true_positives_exceptions_count:
#    failure_reasons.append(exception_type)
#    failure_counts.append(sorted_true_positives_exceptions_count[exception_type])
#    all_colors.append('mediumseagreen')
#for exception_type in sorted_false_negatives_exceptions_count:
#    failure_reasons.append(exception_type)
#    failure_counts.append(sorted_false_negatives_exceptions_count[exception_type])
#    all_colors.append('darksalmon')

#print(failure_reasons)

#inner_values = failure_counts
failure_reasons = ['Assertion', 'IOOB', 'NullPointer', 'IllegalArg', 'Other']
failure_reasons = failure_reasons + failure_reasons
inner_values = count_in_positives + count_in_negatives

failure_reasons = [failure_reason + '(' + str(round((inner_values[i] / sum(inner_values)) * 100, 1)) + '%)' for i, failure_reason in enumerate(failure_reasons)]

# all colors is 5 times mediumseagreen and 5 times darksalmon
all_colors = ['mediumseagreen', 'mediumseagreen', 'mediumseagreen', 'mediumseagreen', 'mediumseagreen', 'darksalmon', 'darksalmon', 'darksalmon', 'darksalmon', 'darksalmon']

# Get the string after the dot for each failure reason element
#failure_reasons = [failure_reason.split('.')[-1] for failure_reason in failure_reasons]
# Append percentage to each failure reason
#failure_reasons = [failure_reason + ' - ' + str(round((failure_counts[i] / sum(failure_counts)) * 100, 2)) + '%' for i, failure_reason in enumerate(failure_reasons)]

# Remove the word Exception for each failure reason only if it is distinct of the word 'Exception'
#failure_reasons = [failure_reason.replace('Exception', '') if failure_reason != 'Exception' else failure_reason for failure_reason in failure_reasons]
#failure_reasons = [failure_reason.replace('Error', '') for failure_reason in failure_reasons]

print(failure_reasons)

# sum inner values and outer values
total_inner_values = sum(inner_values)
total_outer_values = sum(outer_values)
print(f'Total inner values: {total_inner_values}')
print(f'Total outer values: {total_outer_values}')

trace1 = go.Pie(
    hole=0.4,
    sort=False,
    direction='clockwise',
    values=inner_values,
    labels=failure_reasons,
    textinfo='label',
    textposition='inside',
    textfont={'color': 'white', 'size': 40},
    marker={
    'colors': all_colors,
    'line': {'color': 'white', 'width': 1}}
)

trace2 = go.Pie(
    #hole=0.5,
    sort=False,
    direction='clockwise',
    domain={'x': [0.35, 0.65], 'y': [0.65, 0.35]},
    values=outer_values,
    labels=prediction_types,
    textinfo='label',
    textposition='inside',
    textfont={'color': 'white', 'size': 40},
    marker={'colors': ['forestgreen', 'red'],
            'line': {'color': 'white', 'width': 1}}
)

fig = go.FigureWidget(data=[trace1, trace2])
fig.update_traces(textposition='inside', textinfo='label', insidetextorientation='horizontal', textfont_size=16)
# Do not show legend
fig.update_layout(showlegend=False)
fig.update_layout(
    autosize=False,
    margin = {'l':0,'r':0,'t':0,'b':0}
)

# Save the plot
fig.write_image(f'experiments/plots/pie-chart-tps-fns-{assertion_generation}.pdf')
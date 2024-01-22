import plotly.graph_objs as go
import sys
import os
import plotly.io as pio
import pandas as pd
import json

pio.kaleido.scope.mathjax = None

results_dir = "fixcheck-output/defects-repairing"
results_dir_2 = "fixcheck-output/bf4j"
BF4J_DATASET = os.getenv('BF4J_DATASET')
dataset_csv = 'experiments/defect-repairing-subjects.csv'
dataset_df = pd.read_csv(dataset_csv)

score_threshold = 0.40
assertion_generation = sys.argv[1]
no_report = []
no_failure = []

# True positives/False negatives is a map of subject_id -> original_failure_reason
true_positives_map = {}
false_negatives_map = {}
true_positives_exceptions_count = {}
false_negatives_exceptions_count = {}

ground_truth_target_exceptions = {
    'avro-2944': ['org.apache.avro.InvalidAvroMagicException'],
    'choco-600': ['java.lang.AssertionError'],
    'cli-162': ['java.lang.RuntimeException'],
    'cli-164': ['java.lang.AssertionError'],
    'cli-265': ['java.lang.AssertionError'],
    'csv-93': ['java.lang.AssertionError'],
    'graphhopper-145': ['java.lang.AssertionError'],
    'graphhopper-172': ['java.lang.AssertionError'],
    'graphhopper-539': ['java.lang.AssertionError'],
    'io-101': ['java.lang.AssertionError'],
    'jackson-databind-118': ['java.lang.NullPointerException'],
    'jackson-databind-1195': ['java.lang.AssertionError'],
    'jackson-databind-735': ['java.lang.AssertionError'],
    'jxpath-108': ['java.lang.AssertionError'],
    'jxpath-93': ['java.lang.AssertionError'],
    'lang-710': ['java.lang.StringIndexOutOfBoundsException'], # May be wrong
    'math-1208': ['java.lang.AssertionError'],
    'math-790': ['java.lang.AssertionError'], # May be wrong
    'time4j-140': ['java.lang.AssertionError'],
    'time4j-577': ['java.text.ParseException'],
    'time4j-728': ['java.lang.AssertionError']
    }

true_positives = ['math-790', 'cli-265', 'graphhopper-172', 'time4j-140', 'cli-164', 'time4j-577', 'jxpath-93', 'time4j-728', 'jackson-databind-118', 'math-1208', 'graphhopper-539', 'csv-93', 'jxpath-108']


# Now save the results from BF4J_DATASET
for subject_id in os.listdir(results_dir_2):
    if subject_id not in ground_truth_target_exceptions:
        continue
    exception_type = ground_truth_target_exceptions[subject_id][0]
    if subject_id in true_positives:
        prediction = 0
    else:
        prediction = 1

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
positives_percentage = round((len(true_positives_map) / (len(true_positives_map) + len(false_negatives_map))) * 100,1)
positives_percentage = str(positives_percentage) + '%'
negatives_percentage = round((len(false_negatives_map) / (len(true_positives_map) + len(false_negatives_map))) * 100,1)
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
failure_reasons = ['Assert', 'IOOB', 'NPE', 'IAE', 'Other']
failure_reasons = failure_reasons + failure_reasons
inner_values = count_in_positives + count_in_negatives

failure_reasons = [failure_reason + '(' + str(round((inner_values[i] / sum(inner_values)) * 100,1)) + '%)' for i, failure_reason in enumerate(failure_reasons)]

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
fig.update_traces(textposition='inside', textinfo='label', insidetextorientation='radial', textfont_size=16)
# Do not show legend
fig.update_layout(showlegend=False)
fig.update_layout(
    autosize=False,
    margin = {'l':0,'r':0,'t':0,'b':0}
)

# Save the plot
fig.write_image(f'experiments/plots/pie-chart-tps-fns-on-bf4j-{assertion_generation}.pdf')
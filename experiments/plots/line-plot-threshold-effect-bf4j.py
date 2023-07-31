import sys
import os
import json
import pandas as pd
import plotly.io as pio
import plotly.graph_objects as go

pio.kaleido.scope.mathjax = None

assertion_generation = sys.argv[1] # Assertion generation

results_dir = "fixcheck-output/bf4j"
BF4J_DATASET = os.getenv('BF4J_DATASET')
no_report = []

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

def collect_patches_reports_for_threshold(threshold_value):
    patches_reports = pd.DataFrame(columns=['project','patch_id','correctness','original_failure','found_failure','same_reason'])

    # Now save the results for BF4J_DATASET
    for subject_id in os.listdir(results_dir):
        if subject_id.endswith('.csv'):
            continue
        base_folder = os.path.join(subject_id,assertion_generation)
        report_csv = os.path.join(results_dir, base_folder, 'report.csv')
        subject_config_json = os.path.join(BF4J_DATASET, f'data/{subject_id}/bad-fix.json')
        with open(subject_config_json) as f:
            patch_json = json.load(f)
        print('------------------')
        print(f'Processing patch: {subject_id}')
        if not os.path.exists(report_csv):
            print(f'Report does not exist for patch: {subject_id}')
            continue

        score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
        score_df = pd.read_csv(score_file)
        failing_tests = len(score_df)
        # Discarded tests are the ones with score < threshold_value
        discarded_tests = 0
        ranked_tests = 0
        if failing_tests > 0:
            discarded_tests = len(score_df[score_df['score'] < threshold_value])
            ranked_tests = len(score_df[score_df['score'] >= threshold_value])

        max_score = score_df['score'].max()
        prediction = 0 if max_score >= threshold_value else 1

        target_exceptions = ground_truth_target_exceptions[subject_id]
        print(f'ground truth exception: {target_exceptions}')

        if failing_tests == 0:
            project = patch_json['project']
            if project in map_names:
                project = map_names[project]
            new_row = {'project': project, 'patch_id': subject_id, 'correctness': 0, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'ranked_tests': ranked_tests, 'found_failure': '-', 'same_reason': '-'}
            patches_reports = pd.concat([patches_reports, pd.DataFrame([new_row])])
            continue

        failing_tests_folder = os.path.join(results_dir, base_folder, 'failing-tests')
        failing_test = score_df[score_df['score'] == max_score]['prefix'].values[0]
        failing_test_log_file = os.path.join(failing_tests_folder, f'{failing_test}-failure0.txt')
        print(f'Failing test is: {failing_test}')
        print(f'failing test trace: {failing_test_log_file}')
        with open(failing_test_log_file) as f:
            first_line = f.readline()
            found_exception = first_line.split(':')[0]
            found_exception = found_exception.replace('\n', '')
            print(f'test exception: {found_exception}')
            found = found_exception in target_exceptions
            if found:
                same_reason = 1
            else:
                same_reason = 0

            project = patch_json['project']
            if project in map_names:
                project = map_names[project]

            correctness = 0
            new_row = {'project': project, 'patch_id': subject_id, 'correctness': 0, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'ranked_tests': ranked_tests, 'found_failure': found_exception, 'same_reason': same_reason}
            patches_reports = pd.concat([patches_reports, pd.DataFrame([new_row])])

    return patches_reports

def compute_precision_recall(patches_reports, threshold_value):
    total_patches = len(patches_reports)
    total_same_reason_patches = len(patches_reports[(patches_reports['same_reason'] == 1) & (patches_reports['ranked_tests'] > 0)])
    reported_fault_revealing_tests = len(patches_reports[patches_reports['ranked_tests'] > 0])

    precision = 1.0
    if reported_fault_revealing_tests > 0:
        precision = round(total_same_reason_patches/reported_fault_revealing_tests,2)
    recall = round(total_same_reason_patches/total_patches,2)

    return precision, recall

# Loop over all threshold values and compute precision and recall
threshold_values = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
precision = []
recall = []

for threshold in threshold_values:
    threshold_patches_reports = collect_patches_reports_for_threshold(threshold)
    prec, rec = compute_precision_recall(threshold_patches_reports, threshold)
    print(f'Precision: {prec}, Recall: {rec} for threshold: {threshold}')
    precision.append(prec)
    recall.append(rec)

print('thresholds: ', threshold_values)
print('precision: ', precision)
print('recall: ', recall)

print()
print('Latex Table:')
print('& ' + ' & '.join([str(elem) for elem in threshold_values]) + ' \\\\')
print('precision & ' + ' & '.join([str(elem) for elem in precision]) + ' \\\\')
print('recall & ' + ' & '.join([str(elem) for elem in recall]) + ' \\\\')

# Create traces
fig = go.Figure()
fig.add_trace(go.Scatter(x=threshold_values, y=precision,
                    mode='lines+markers',
                    line = dict(dash='dot', width=0.5),
                    marker=dict(symbol='circle'),
                    name='Precision'))
fig.add_trace(go.Scatter(x=threshold_values, y=recall,
                    mode='lines+markers',
                    marker=dict(symbol='diamond'),
                    line = dict(dash='dot', width=0.5),
                    name='Recall'))

fig.update_layout(
    xaxis_title="Threshold",
    xaxis_tickmode = 'linear',
    xaxis_dtick = 0.1,
    yaxis_title="Performance Metric",
    yaxis_tickmode = 'linear',
    yaxis_dtick = 0.1,
    template='plotly_white',
    autosize=False,
    margin = {'l':0,'r':0,'t':0,'b':0},
    width=600,
    height=350
    #margin=dict(l=20, r=20, t=20, b=20),
)

# Save the plot
fig.write_image(f'experiments/plots/prec-recall-threshold-on-bf4j-{assertion_generation}.pdf')



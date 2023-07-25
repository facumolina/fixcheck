import sys
import os
import json
import pandas as pd
import plotly.io as pio
import plotly.graph_objects as go

pio.kaleido.scope.mathjax = None

assertion_generation = sys.argv[1] # Assertion generation

results_dir = "fixcheck-output/defects-repairing"
results_dir_2 = "fixcheck-output/bf4j"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')
BF4J_DATASET = os.getenv('BF4J_DATASET')
no_report = []

def collect_patches_reports_for_threshold(threshold_value):
    patches_reports = pd.DataFrame(columns=['project','patch_id','correctness','failing_tests','discarded_tests','reported_tests','max_score','prediction'])
    # First save the results for DEFECT_REPAIRING_DATASET
    for subject_id in os.listdir(results_dir):
        if subject_id.endswith('.csv'):
            continue
        base_folder = os.path.join(subject_id,assertion_generation)
        report_csv = os.path.join(results_dir, base_folder, 'report.csv')
        subject_config_json = os.path.join(DEFECT_REPAIRING_DATASET, f'tool/patches/INFO/{subject_id}.json')
        with open(subject_config_json) as f:
            patch_json = json.load(f)
        if not os.path.exists(report_csv):
            no_report.append(subject_id)
            continue
        project = patch_json["project"]
        # Correctness is 1 if Correct and 0 if Incorrect
        correctness = 1 if patch_json["correctness"] == "Correct" else 0
        score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
        score_df = pd.read_csv(score_file)
        failing_tests = len(score_df)
        # Discarded tests are the ones with score < 0.80
        discarded_tests = len(score_df[score_df['score'] < threshold_value])
        reported_tests = len(score_df[score_df['score'] >= threshold_value])
        max_score = score_df['score'].max()
        # Prediction is 0 if max_score >= 0.80 and 1 otherwise
        prediction = 0 if max_score >= threshold_value else 1
        new_row = {'project': project, 'patch_id': subject_id, 'correctness': correctness, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'reported_tests': reported_tests, 'max_score': max_score, 'prediction': prediction}
        patches_reports = pd.concat([patches_reports, pd.DataFrame([new_row])])

    # Now save the results for BF4J_DATASET
    for subject_id in os.listdir(results_dir_2):
        if subject_id.endswith('.csv'):
            continue
        base_folder = os.path.join(subject_id,assertion_generation)
        report_csv = os.path.join(results_dir_2, base_folder, 'report.csv')
        subject_config_json = os.path.join(BF4J_DATASET, f'data/{subject_id}/bad-fix.json')
        # Load json
        with open(subject_config_json) as f:
            patch_json = json.load(f)
        if not os.path.exists(report_csv):
            no_report.append(subject_id)
            continue
        project = 'bf4j'
        correctness = 0
        score_file = os.path.join(results_dir_2, base_folder, 'scores-failing-tests.csv')
        score_df = pd.read_csv(score_file)
        failing_tests = len(score_df)
        # Discarded tests are the ones with score < 0.80
        discarded_tests = len(score_df[score_df['score'] < threshold_value])
        reported_tests = len(score_df[score_df['score'] >= threshold_value])
        max_score = score_df['score'].max()
        # Prediction is 0 if max_score >= 0.80 and 1 otherwise
        prediction = 0 if max_score >= threshold_value else 1
        new_row = {'project': project, 'patch_id': subject_id, 'correctness': correctness, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'reported_tests': reported_tests, 'max_score': max_score, 'prediction': prediction}
        patches_reports = pd.concat([patches_reports, pd.DataFrame([new_row])])

    return patches_reports

def compute_precision_recall(patches_reports, threshold_value):
    # Patches correctness
    proj_correct_patches = patches_reports[patches_reports['correctness'] == 1]
    proj_incorrect_patches = patches_reports[patches_reports['correctness'] == 0]
    # Sum column failing_tests
    proj_failing_tests = patches_reports['failing_tests'].sum()
    proj_discarded = patches_reports['discarded_tests'].sum()
    proj_reported = patches_reports['reported_tests'].sum()
    # Sum correct predictions
    proj_correct_predictions = patches_reports[patches_reports['prediction'] == 1]
    proj_incorrect_predictions = patches_reports[patches_reports['prediction'] == 0]

    # Count true positives, false positives, true negatives, false negatives
    # True positives are the patches that are incorrect and are predicted as incorrect
    # False positives are the patches that are correct and are predicted as incorrect
    # True negatives are the patches that are correct and are predicted as correct
    # False negatives are the patches that are incorrect and are predicted as correct
    true_positives = len(proj_incorrect_patches[proj_incorrect_patches['prediction'] == 0])
    true_positives_patches = proj_incorrect_patches[proj_incorrect_patches['prediction'] == 0]
    false_positives = len(proj_correct_patches[proj_correct_patches['prediction'] == 0])
    true_negatives = len(proj_correct_patches[proj_correct_patches['prediction'] == 1])
    false_negatives = len(proj_incorrect_patches[proj_incorrect_patches['prediction'] == 1])
    # Precision is the ratio tp / (tp + fp)
    if (true_positives + false_positives) == 0:
        precision = 1
    else:
        precision = true_positives / (true_positives + false_positives)
    # Recall is the ratio tp / (tp + fn)
    if (true_positives + false_negatives) == 0:
        recall = 0
    else:
        recall = true_positives / (true_positives + false_negatives)

    # Round precision and recall to 2 decimal places
    precision = round(precision, 2)
    recall = round(recall, 2)
    return precision, recall

# Loop over all threshold values and compute precision and recall
threshold_values = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]
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
fig.write_image(f'experiments/plots/prec-recall-threshold-{assertion_generation}.pdf')



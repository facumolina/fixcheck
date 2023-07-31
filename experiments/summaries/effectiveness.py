import os
import sys
import json
import pandas as pd

results_dir = "fixcheck-output/defects-repairing"
results_dir_2 = "fixcheck-output/bf4j"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')
BF4J_DATASET = os.getenv('BF4J_DATASET')

# Empty dataframe with columns project,patch_id,correctness,total_tests,discarded_tests,reported_tests,max_score,prediction
patches_reports = pd.DataFrame(columns=['project','patch_id','correctness','failing_tests','discarded_tests','reported_tests','max_score','prediction'])
score_threshold = 0.40
assertion_generation = sys.argv[1]
no_report = []
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
    failing_tests = len(score_df)
    # Discarded tests are the ones with score < 0.80
    discarded_tests = len(score_df[score_df['score'] < score_threshold])
    reported_tests = len(score_df[score_df['score'] >= score_threshold])
    max_score = score_df['score'].max()
    # Prediction is 0 if max_score >= 0.80 and 1 otherwise
    prediction = 0 if max_score >= score_threshold else 1
    print(f'Max score: {max_score}')

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
    print(f'Processing report: {report_csv}')
    if not os.path.exists(report_csv):
        no_report.append(subject_id)
        continue
    project = 'bf4j'
    correctness = 0
    score_file = os.path.join(results_dir_2, base_folder, 'scores-failing-tests.csv')
    score_df = pd.read_csv(score_file)
    failing_tests = len(score_df)
    # Discarded tests are the ones with score < 0.80
    discarded_tests = len(score_df[score_df['score'] < score_threshold])
    reported_tests = len(score_df[score_df['score'] >= score_threshold])
    max_score = score_df['score'].max()
    # Prediction is 0 if max_score >= 0.80 and 1 otherwise
    prediction = 0 if max_score >= score_threshold else 1
    print(f'Max score: {max_score}')
    new_row = {'project': project, 'patch_id': subject_id, 'correctness': correctness, 'failing_tests': failing_tests, 'discarded_tests': discarded_tests, 'reported_tests': reported_tests, 'max_score': max_score, 'prediction': prediction}
    patches_reports = pd.concat([patches_reports, pd.DataFrame([new_row])])


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
    # Patches correctness
    proj_correct_patches = proj_rows[proj_rows['correctness'] == 1]
    proj_incorrect_patches = proj_rows[proj_rows['correctness'] == 0]
    # Sum column failing_tests
    proj_failing_tests = proj_rows['failing_tests'].sum()
    proj_discarded = proj_rows['discarded_tests'].sum()
    proj_reported = proj_rows['reported_tests'].sum()
    # Sum correct predictions
    proj_correct_predictions = proj_rows[proj_rows['prediction'] == 1]
    proj_incorrect_predictions = proj_rows[proj_rows['prediction'] == 0]

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
        precision = 0
    else:
        precision = true_positives / (true_positives + false_positives)
    # Recall is the ratio tp / (tp + fn)
    if (true_positives + false_negatives) == 0:
        recall = 0
    else:
        recall = true_positives / (true_positives + false_negatives)

    row_latex.append(len(proj_correct_patches))
    row_latex.append(len(proj_incorrect_patches))
    row_latex.append(proj_failing_tests)
    row_latex.append(proj_discarded)
    row_latex.append(proj_reported)
    row_latex.append(true_positives)
    row_latex.append(false_positives)
    row_latex.append(true_negatives)
    row_latex.append(false_negatives)
    # Format precision and recall to 2 decimal places
    precision = "{:.2f}".format(precision)
    recall = "{:.2f}".format(recall)
    row_latex.append(precision)
    row_latex.append(recall)
    print(f'Summary project {project}')
    print(f'tps: {true_positives}')
    print('True positives patches ids:')
    true_positive_patches_list = true_positives_patches['patch_id'].tolist()
    print(true_positive_patches_list)
    print(f'fps: {false_positives}')
    print(f'tns: {true_negatives}')
    print(f'fns: {false_negatives}')
    print(f'Precision: {precision}')
    print(f'Recall: {recall}')
    print()

save_results_for_project(patches_reports,'Chart',chart_row_latex)
save_results_for_project(patches_reports,'Lang',lang_row_latex)
save_results_for_project(patches_reports,'Math',math_row_latex)
save_results_for_project(patches_reports,'Time',time_row_latex)
save_results_for_project(patches_reports,'bf4j',bf4j_row_latex)
save_results_for_project(patches_reports,'TOTAL',total_row_latex)
print()
print('----------------------------------')
print(f'No report for: {no_report}')
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


import sys
import pandas as pd
import os

results_dir = "fixcheck-output/defects-repairing"
dataset_csv = 'experiments/defect-repairing-subjects.csv'

# Empty dataframe with columns project,patch_id,correctness,total_tests,discarded_tests,reported_tests,max_score,prediction
time_and_detection = pd.DataFrame(columns=['project','patch_id','patch_sim_time','patch_sim_pred','fixcheck_time_tg','fixcheck_time_ag','fixcheck_pred','both_pred'])

assertion_generation = sys.argv[1]

# Target patches
target_ids = [1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 58, 59, 62, 63, 64, 65, 66, 67, 68, 69, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 88, 89, 90, 91, 92, 93, 150, 151, 152, 153, 154, 155, 157, 158, 159, 160, 161, 162, 163, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 191, 192, 193, 194, 195, 196, 197, 198, 199, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 'HDRepair1', 'HDRepair3', 'HDRepair4', 'HDRepair5', 'HDRepair6', 'HDRepair7', 'HDRepair8', 'HDRepair9', 'HDRepair10']
target_patches = ['Patch' + str(id) for id in target_ids]
df = pd.read_csv(dataset_csv)

# patch_sim detected patches
patch_sim_detected_ids = [1, 2, 4, 8, 9, 13, 15, 16, 17, 19, 22, 23, 32, 33, 34, 36, 38, 48, 53, 55, 58, 65, 66, 67, 68, 69, 72, 74, 77, 79, 80, 81, 84, 88, 92, 93, 150, 151, 154, 157, 158, 159, 160, 163, 167, 169, 170, 174, 176, 181, 183, 184, 185, 186, 187, 193, 198, 208, 'HDRepair5', 'HDRepair6', 'HDRepair8', 'HDRepair9']
patch_sim_detected = ['Patch' + str(id) for id in patch_sim_detected_ids]
fixcheck_detected = []

# shibboleth detected patches
shibboleth_detected_ids = [1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 58, 59, 62, 63, 64, 65, 66, 67, 68, 69, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 88, 89, 90, 91, 92, 93, 150, 151, 152, 153, 154, 155, 157, 158, 159, 160, 161, 162, 163, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 191, 192, 193, 194, 195, 196, 197, 198, 199, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 'HDRepair1', 'HDRepair3', 'HDRepair4', 'HDRepair5', 'HDRepair6', 'HDRepair7', 'HDRepair8', 'HDRepair9', 'HDRepair10']
shibboleth_detected = ['Patch' + str(id) for id in shibboleth_detected_ids]

FIXCHECK_SCORE_THRESHOLD = 0.40

for target_patch in target_patches:
    subject_data = df[df['id'] == target_patch]
    correctness = subject_data['correctness'].values[0]
    if correctness == 'Correct':
        continue
    project = subject_data['project'].values[0]
    patch_sim_time = 180 # 3 minutes of Randoop generation, as specified in the paper
    patch_sim_pred = 1 if target_patch in patch_sim_detected else 0

    shibboleth_pred = 1 if target_patch in shibboleth_detected else 0

    base_folder = os.path.join(target_patch,assertion_generation)
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')

    fixcheck_time_tg = 0
    fixcheck_time_ag = 0
    fixcheck_pred = 0

    if os.path.exists(report_csv):
        report = pd.read_csv(report_csv)
        prefixes_gen_time_ms = report['prefixes_gen_time'].sum()
        assertions_gen_time_ms = report['assertions_gen_time'].sum()
        prefix_running_time_ms = report['prefixes_running_time'].sum()

        prefixes_gen_time = prefixes_gen_time_ms // 1000
        assertions_gen_time = assertions_gen_time_ms // 1000
        prefix_running_time = prefix_running_time_ms // 1000

        fixcheck_time_tg = prefixes_gen_time + prefix_running_time
        fixcheck_time_ag = assertions_gen_time

        score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
        score_df = pd.read_csv(score_file)
        max_score = score_df['score'].max()
        fixcheck_pred = 1 if max_score >= FIXCHECK_SCORE_THRESHOLD else 0
        if fixcheck_pred == 1:
            fixcheck_detected.append(target_patch)

    both_pred = 1 if (patch_sim_pred == 1 or fixcheck_pred == 1) else 0
    patch_sim_and_fixcheck = 1 if (patch_sim_pred == 1 and fixcheck_pred == 1) else 0

    both_pred_with_shibboleth = 1 if (shibboleth_pred == 1 or fixcheck_pred == 1) else 0
    shiboletth_and_fixcheck = 1 if (shibboleth_pred == 1 and fixcheck_pred == 1) else 0

    new_row = {'project': project, 'patch_id': target_patch, 'patch_sim_time': patch_sim_time, 'patch_sim_pred': patch_sim_pred, 'fixcheck_time_tg': fixcheck_time_tg, 'fixcheck_time_ag': fixcheck_time_ag, 'fixcheck_pred': fixcheck_pred, 'both_pred': both_pred, 'patch_sim_and_fixcheck': patch_sim_and_fixcheck, 'shibboleth_pred': shibboleth_pred ,'both_pred_with_shibboleth': both_pred_with_shibboleth, 'shiboletth_and_fixcheck': shiboletth_and_fixcheck}
    time_and_detection = pd.concat([time_and_detection, pd.DataFrame([new_row])])


chart_row_latex = ['chart']
lang_row_latex = ['lang']
math_row_latex = ['math']
time_row_latex = ['time']
total_row_latex = ['total']

def save_results_for_project(results,project,row_latex):
    if project == 'TOTAL':
        proj_rows = results
    else:
        proj_rows = results[results['project'] == project]

    incorrect_patches = len(proj_rows)
    patch_sim_time = proj_rows['patch_sim_time'].sum()
    path_sim_pred = proj_rows['patch_sim_pred'].sum()

    fixcheck_time_tg = proj_rows['fixcheck_time_tg'].sum()
    fixcheck_time_ag = proj_rows['fixcheck_time_ag'].sum()
    fixcheck_pred = proj_rows['fixcheck_pred'].sum()

    both_pred = proj_rows['both_pred'].sum()
    patch_sim_and_fixcheck = int(proj_rows['patch_sim_and_fixcheck'].sum())

    row_latex.append(incorrect_patches)
    row_latex.append(path_sim_pred)
    percent = round((patch_sim_and_fixcheck / path_sim_pred) * 100, 1)
    row_latex.append(str(patch_sim_and_fixcheck)+ ' (' + str(percent) + '\%)')

    shibboleth_pred = int(proj_rows['shibboleth_pred'].sum())
    shibboleth_and_fixcheck = int(proj_rows['shiboletth_and_fixcheck'].sum())
    row_latex.append(shibboleth_pred)
    percent = round((shibboleth_and_fixcheck / shibboleth_pred) * 100, 1)
    row_latex.append(str(shibboleth_and_fixcheck)+ ' (' + str(percent) + '\%)')

save_results_for_project(time_and_detection,'Chart',chart_row_latex)
save_results_for_project(time_and_detection,'Lang',lang_row_latex)
save_results_for_project(time_and_detection,'Math',math_row_latex)
save_results_for_project(time_and_detection,'Time',time_row_latex)
save_results_for_project(time_and_detection,'TOTAL',total_row_latex)


print('Summary info')
print(f'detected by PATCH-SIM: {time_and_detection["patch_sim_pred"].sum()}')
print(f'detected by FixCheck: {time_and_detection["fixcheck_pred"].sum()}')
# Count the patches that are in patch_sim_detected and in fixcheck_detected
patch_sim_and_fixcheck = [patch for patch in patch_sim_detected if patch in fixcheck_detected]
print(f'detected by PATCH-SIM and also FixCheck: {len(patch_sim_and_fixcheck)}')
only_by_fixcheck = [patch for patch in fixcheck_detected if patch not in patch_sim_detected]
print(f'detected only by FixCheck: {len(only_by_fixcheck)}')
print(f'detected by both: {time_and_detection["both_pred"].sum()}')
print()
print(f'detected by Shibboleth: {time_and_detection["shibboleth_pred"].sum()}')
print(f'detected by Shibboleth and also FixCheck: {time_and_detection["shiboletth_and_fixcheck"].sum()}')
print(f'detected by both Shibboleth and FixCheck: {time_and_detection["both_pred_with_shibboleth"].sum()}')

print('----------------------------------')
print('Latex table')
# Print latex rows for chart with each element in the list interleaved with &
print(' & '.join([str(elem) for elem in chart_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in lang_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in math_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in time_row_latex]) + ' \\\\')
print('\midrule')
print(' & '.join([str(elem) for elem in total_row_latex]) + ' \\\\')
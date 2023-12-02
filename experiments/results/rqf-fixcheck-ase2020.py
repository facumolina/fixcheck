import os
import sys
import pandas as pd

results_dir = "fixcheck-output/ase2020"

assertion_generation = sys.argv[1]

results_csv_file = os.path.join(results_dir, f'fixcheck-time-and-detection-{assertion_generation}.csv')
results = pd.read_csv(results_csv_file)

# Count the number of incorrect patches
incorrect_patches = len(results[results['incorrect'] == 1])
correct_patches = len(results[results['incorrect'] == 0])

# Count the number of patches detected by FixCheck
fixcheck_incorrect = len(results[results['fixcheck_pred'] == 1])
fixcheck_correct = len(results[results['fixcheck_pred'] == 0])

# Count true positives, false positives, true negatives, false negatives
true_positives = len(results[(results['incorrect'] == 1) & (results['fixcheck_pred'] == 1)])
false_positives = len(results[(results['incorrect'] == 0) & (results['fixcheck_pred'] == 1)])
true_negatives = len(results[(results['incorrect'] == 0) & (results['fixcheck_pred'] == 0)])
false_negatives = len(results[(results['incorrect'] == 1) & (results['fixcheck_pred'] == 0)])

# Calculate precision, recall, f1-score
precision = true_positives / (true_positives + false_positives)
recall = true_positives / (true_positives + false_negatives)
f1_score = 2 * (precision * recall) / (precision + recall)
accuracy = (true_positives + true_negatives) / (true_positives + true_negatives + false_positives + false_negatives)

print(f'Incorrect patches: {incorrect_patches}')
print(f'Correct patches: {correct_patches}')
print(f'FixCheck incorrect: {fixcheck_incorrect}')
print(f'FixCheck correct: {fixcheck_correct}')

print()
print(f'True positives: {true_positives}')
print(f'False positives: {false_positives}')
print(f'True negatives: {true_negatives}')
print(f'False negatives: {false_negatives}')
print()
print(f'Accuracy: {accuracy}')
print(f'Precision: {precision}')
print(f'Recall: {recall}')
print(f'F1-score: {f1_score}')


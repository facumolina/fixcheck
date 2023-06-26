import sys
import pandas as pd

target_ids = [1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 44, 45, 46, 47, 48, 49, 51, 53, 54, 55, 58, 59, 62, 63, 64, 65, 66, 67, 68, 69, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 88, 89, 90, 91, 92, 93, 150, 151, 152, 153, 154, 155, 157, 158, 159, 160, 161, 162, 163, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 191, 192, 193, 194, 195, 196, 197, 198, 199, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 'HDRepair1', 'HDRepair3', 'HDRepair4', 'HDRepair5', 'HDRepair6', 'HDRepair7', 'HDRepair8', 'HDRepair9', 'HDRepair10']
# Append each id to the word Patch
target_patches = ['Patch' + str(id) for id in target_ids]

# Read arg
patches_csv = sys.argv[1]
df = pd.read_csv(patches_csv)

# Remove rows in which project=Closure or project=Mockito
df = df[df['project'] != 'Closure']
df = df[df['project'] != 'Mockito']

for idx in df.index:
    # Get the target patch
    patch_name = df['id'][idx]
    # Remove from the dt the patches that are not in the target_patches list
    if patch_name not in target_patches:
        df = df.drop(idx)

# Count number of rows in which the project is Chart
chart_rows = df[df['project'] == 'Chart'].count()
math_rows = df[df['project'] == 'Math'].count()
lang_rows = df[df['project'] == 'Lang'].count()
time_rows = df[df['project'] == 'Time'].count()

# Count number of rows in which the project is Chart and the correctness is Correct
chart_correct_rows = df[(df['project'] == 'Chart') & (df['correctness'] == 'Correct')].count()
math_correct_rows = df[(df['project'] == 'Math') & (df['correctness'] == 'Correct')].count()
lang_correct_rows = df[(df['project'] == 'Lang') & (df['correctness'] == 'Correct')].count()
time_correct_rows = df[(df['project'] == 'Time') & (df['correctness'] == 'Correct')].count()

# Print results for Chart
print('Chart')
print('Total rows: ' + str(chart_rows['id']))
print('Correct rows: ' + str(chart_correct_rows['id']))
incorrect = chart_rows['id'] - chart_correct_rows['id']
print('Incorrect rows: ' + str(incorrect))
print()

# Print results for Math
print('Math')
print('Total rows: ' + str(math_rows['id']))
print('Correct rows: ' + str(math_correct_rows['id']))
incorrect = math_rows['id'] - math_correct_rows['id']
print('Incorrect rows: ' + str(incorrect))
print()

# Print results for Lang
print('Lang')
print('Total rows: ' + str(lang_rows['id']))
print('Correct rows: ' + str(lang_correct_rows['id']))
incorrect = lang_rows['id'] - lang_correct_rows['id']
print('Incorrect rows: ' + str(incorrect))
print()

# Print results for Time
print('Time')
print('Total rows: ' + str(time_rows['id']))
print('Correct rows: ' + str(time_correct_rows['id']))
incorrect = time_rows['id'] - time_correct_rows['id']
print('Incorrect rows: ' + str(incorrect))




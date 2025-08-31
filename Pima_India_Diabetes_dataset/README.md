**Contents:**
- `pima_india_diabetes_analysis.ipynb` - Notebook with data loading, cleaning, imputation, feature analysis, model training, hyperparameter tuning, and evaluation.

**Preprocessing:**
- Handling of erroneous zero values in features like Glucose, BloodPressure, SkinThickness, Insulin, and BMI by replacing them with NaN.
- Imputation of missing values using KNN imputer for Glucose, BMI, and BloodPressure.
- Imputation of missing Insulin values using a formula based on Glucose, BMI, and Age.
- Imputation of missing SkinThickness values using KNeighborsRegressor.
- Skewness identification and analysis of feature distributions.
- Feature scaling using StandardScaler and log transformation for some features.

**Models tested:**
- K-Nearest Neighbors (KNN) Classifier
- Decision Tree Classifier
- Naive Bayes (GaussianNB)

**Evaluation metrics:**
- Accuracy Score
- Classification Report (Precision, Recall, F1-score)
- Cross-validation score (using GridSearchCV)

**Notes:**
1. Erroneous zero values in several features were treated as missing values and imputed.
2. Outliers were identified but kept in the dataset based on the assumption that they are important in a medical context.
3. Several KNN models were tested with different feature sets and scaling.
4. Hyperparameter tuning was applied to the KNN and Decision Tree models using GridSearchCV.
5. Based on the test set accuracies, the KNN model trained on the dataset without the 'Pregnancies' and 'DiabetesPedigreeFunction' features performed the best.
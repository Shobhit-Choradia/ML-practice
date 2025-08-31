# Abalone Age Prediction

This folder contains work on predicting the age (number of rings) of abalones using machine learning.

**Contents:**
- `abalone_age_prediction.ipynb` - Notebook with feature engineering, model training, hyperparameter tuning, and evaluation.
- Preprocessing: scaling, encoding categorical features, log transform for skewed data.
- Models tested: Linear Regression, KNN, XGBoost.
- Evaluation metrics: Mean Squared Error (MSE), R² score.

**Notes:**
  1. Derived features like `length_diameter_height_sqrt` improved model correlation.
  2. In this case, the best results were achieved with minimal preprocessing, feature engineering, and hyperparameter tuning (as seen in last part).
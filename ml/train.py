import os
import pandas as pd
import numpy as np
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import joblib

DATA_PATH = "real_data.csv"

if not os.path.exists(DATA_PATH):
    raise FileNotFoundError(f"Dataset not found at {DATA_PATH}. Please ensure data.csv was copied to {DATA_PATH}.")

data = pd.read_csv(DATA_PATH)

# Target
y = data["Creditworthiness"].astype(int)

# Features
X = data.drop("Creditworthiness", axis=1)

# Identify numerical and categorical columns
numeric_features = ["Age", "Income", "Debt", "Credit_Score", "Loan_Amount", "Loan_Term", "Num_Credit_Cards"]
categorical_features = ["Gender", "Education", "Payment_History", "Employment_Status", "Residence_Type", "Marital_Status"]

# Preprocessing for numerical data
numeric_transformer = Pipeline(steps=[
    ('imputer', SimpleImputer(strategy='median')),
    ('scaler', StandardScaler())
])

# Preprocessing for categorical data
categorical_transformer = Pipeline(steps=[
    ('imputer', SimpleImputer(strategy='most_frequent')),
    ('onehot', OneHotEncoder(handle_unknown='ignore'))
])

# Combine preprocessing steps
preprocessor = ColumnTransformer(
    transformers=[
        ('num', numeric_transformer, numeric_features),
        ('cat', categorical_transformer, categorical_features)
    ])

# Split data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Candidate models
candidates = {
    "logistic_regression": LogisticRegression(max_iter=1000),
    "decision_tree": DecisionTreeClassifier(random_state=42),
    "random_forest": RandomForestClassifier(n_estimators=100, random_state=42)
}

best_score = 0.0
best_name = None
best_pipeline = None

# Train and evaluate models
for name, model in candidates.items():
    pipe = Pipeline(steps=[
        ('preprocessor', preprocessor),
        ('model', model)
    ])
    pipe.fit(X_train, y_train)
    preds = pipe.predict(X_test)
    score = accuracy_score(y_test, preds)
    print(f"{name} accuracy: {score:.4f}")
    
    if score > best_score:
        best_score = score
        best_name = name
        best_pipeline = pipe

if best_pipeline is None:
    raise RuntimeError("No model trained")

print(f"Best model: {best_name} with accuracy {best_score:.4f}")

# Save the best pipeline (which includes the preprocessor)
joblib.dump(best_pipeline, "model.pkl")
print("Saved advanced pipeline to model.pkl")

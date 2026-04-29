import pandas as pd
import numpy as np

np.random.seed(42)

n_samples = 5000

age = np.random.randint(18, 70, n_samples)
income = np.random.randint(15000, 200000, n_samples)
credit_score = np.random.randint(300, 850, n_samples)
loan_amount = np.random.randint(1000, 100000, n_samples)
employment_status = np.random.choice(['EMPLOYED', 'SELF_EMPLOYED', 'UNEMPLOYED'], n_samples, p=[0.65, 0.25, 0.10])
credit_utilization = np.random.uniform(0, 100, n_samples)
existing_loans = np.random.randint(0, 8, n_samples)
payment_history = np.random.choice(['GOOD', 'FAIR', 'POOR'], n_samples, p=[0.5, 0.35, 0.15])

# Creditworthiness = 1 means CREDITWORTHY (LOW RISK / APPROVED)
# Key factors:
# - Credit score (most important)
# - Payment history
# - Credit utilization
# - Income vs loan amount ratio
# - Employment stability
# - Existing loans

score = np.zeros(n_samples, dtype=float)

# Credit score: range 300-850, normalized 0-40 points
score += ((credit_score - 300) / 550) * 40

# Payment history: 0-25 points
score += np.where(payment_history == 'GOOD', 25, np.where(payment_history == 'FAIR', 10, 0))

# Credit utilization (lower is better): 0-15 points
score += np.where(credit_utilization < 30, 15,
         np.where(credit_utilization < 60, 7, 0))

# Income vs loan amount: 0-10 points
debt_ratio = loan_amount / np.maximum(income, 1)
score += np.where(debt_ratio < 0.3, 10,
         np.where(debt_ratio < 0.6, 5,
         np.where(debt_ratio < 1.0, 2, 0)))

# Employment: 0-8 points
score += np.where(employment_status == 'EMPLOYED', 8,
         np.where(employment_status == 'SELF_EMPLOYED', 4, 0))

# Existing loans (fewer is better): 0-7 points
score += np.where(existing_loans == 0, 7,
         np.where(existing_loans <= 2, 4,
         np.where(existing_loans <= 4, 1, 0)))

# Age bonus (slight): 0-5 points
score += np.where(age > 30, 5, np.where(age > 25, 2, 0))

# Noise
score += np.random.normal(0, 5, n_samples)

# Threshold at 55 out of 110 possible points (~50% mark for balance)
creditworthiness = np.where(score >= 55, 1, 0)

df = pd.DataFrame({
    'age': age,
    'income': income,
    'credit_score': credit_score,
    'loan_amount': loan_amount,
    'employment_status': employment_status,
    'credit_utilization': credit_utilization,
    'existing_loans': existing_loans,
    'payment_history': payment_history,
    'Creditworthiness': creditworthiness
})

df.to_csv('synthetic_data.csv', index=False)
balance = creditworthiness.mean()
print(f"Saved synthetic_data.csv with {n_samples} records.")
print(f"Creditworthy (1): {creditworthiness.sum()} ({balance*100:.1f}%) | Not Creditworthy (0): {(creditworthiness==0).sum()} ({(1-balance)*100:.1f}%)")

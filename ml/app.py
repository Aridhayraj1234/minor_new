from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import pandas as pd

app = Flask(__name__)
CORS(app)

model = joblib.load("model.pkl")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()
    print(f"Prediction requested: {data}")
    try:
        # Extract features with defaults if missing
        features = {
            "Age": float(data.get("age", 0)),
            "Gender": str(data.get("gender", "Male")),
            "Education": str(data.get("education", "Bachelor")),
            "Income": float(data.get("income", 0)),
            "Debt": float(data.get("debt", 0)),
            "Credit_Score": float(data.get("credit_score", 0)),
            "Loan_Amount": float(data.get("loan_amount", 0)),
            "Loan_Term": int(data.get("loan_term", 36)),
            "Num_Credit_Cards": int(data.get("num_credit_cards", 1)),
            "Payment_History": str(data.get("payment_history", "Good")),
            "Employment_Status": str(data.get("employment_status", "Employed")),
            "Residence_Type": str(data.get("residence_type", "Owned")),
            "Marital_Status": str(data.get("marital_status", "Single"))
        }
    except Exception as e:
        print(f"Input parsing failed: {e}")
        return jsonify({"error": f"Invalid input values: {str(e)}"}), 400

    df = pd.DataFrame([features])

    try:
        pred = model.predict(df)[0]
        prob = float(model.predict_proba(df)[0][1])
        print(f"Prediction result: {pred}, Probability: {prob}")
    except Exception as e:
        print(f"Prediction failed: {e}")
        return jsonify({"error": f"Model prediction failed: {str(e)}"}), 500

    return jsonify({"prediction": int(pred), "probability": prob})

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "UP",
        "service": "ML Service",
        "model_loaded": model is not None
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

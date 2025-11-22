import sys
import json
import numpy as np
import joblib

# ABSOLUTE PATH TO YOUR FILES
BASE = r"D:\AI\src\main\resources\ml\\"

# Load model + encoders
label_encoders = joblib.load(BASE + "label_encoders.pkl")
model = joblib.load(BASE + "crop_model.pkl")

# Load correct crop mapping
with open(BASE + "crop_mapping.json") as f:
    crop_map = json.load(f)

# Read JSON input from Spring Boot
raw = sys.stdin.read().strip()
if not raw:
    sys.stderr.write("No JSON received\n")
    sys.exit(1)

data = json.loads(raw)

# Encode ONLY categorical inputs
for col, le in label_encoders.items():
    if col == "crop":  # IMPORTANT: skip crop encoder
        continue

    if col in data:
        try:
            data[col] = le.transform([data[col]])[0]
        except:
            sys.stderr.write(f"Unknown label '{data[col]}' for '{col}'\n")
            sys.exit(1)
    else:
        sys.stderr.write(f"Missing required field: {col}\n")
        sys.exit(1)

# The model was trained using ONLY these features (NOT land_size_ha)
X = np.array([[
    data["district"],
    data["soil_type"],
    data["rainfall_mm"],
    data["irrigation_type"]
]])

# Make prediction
probs = model.predict_proba(X)[0]

# Get top 3 crop indices
top3_idx = np.argsort(probs)[-3:][::-1]

# Map indices → crop names
top3 = []
for i in top3_idx:
    key = str(i)
    if key in crop_map:
        top3.append(crop_map[key])
    else:
        top3.append(f"UnknownCrop_{i}")

# Return result to Java
print(json.dumps(top3))
sys.stdout.flush()

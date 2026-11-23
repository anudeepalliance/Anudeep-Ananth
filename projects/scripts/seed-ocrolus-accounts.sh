#!/bin/bash
# ==============================================================================
# Loads the 50 Ocrolus assignment accounts into MongoDB (collection ocrolus-accounts).
# The dataset is Ocrolus's material, so it is read from a local file and never committed.
# Needs: the SSH tunnel to the VPS MongoDB open on localhost:27017, python3 + openpyxl, mongosh.
# Usage: scripts/seed-ocrolus-accounts.sh "/path/to/Project Dataset (1).xlsx"
# ==============================================================================
set -euo pipefail

DATASET_XLSX_PATH="${1:?Pass the path to the dataset .xlsx file}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SECRETS_FILE="${SCRIPT_DIR}/../server/src/main/kotlin/com/anudeepananth/projects/util/ConfidentialObjects.kt"
MONGO_URI=$(grep -A1 "MONGO_COMMUNITY_TUNNELED_CONNECTION_STRING" "${SECRETS_FILE}" | grep -oE '"mongodb[^"]*"' | tr -d '"')
SEED_SCRIPT_FILE="$(mktemp -t seed-ocrolus-accounts).js"
trap 'rm -f "${SEED_SCRIPT_FILE}"' EXIT

python3 - "${DATASET_XLSX_PATH}" > "${SEED_SCRIPT_FILE}" <<'PYTHON'
import json, sys, openpyxl
worksheet = openpyxl.load_workbook(sys.argv[1], data_only=True).worksheets[0]
accounts = []
for row in worksheet.iter_rows(min_row=2, values_only=True):
    if not row[0]:
        continue
    accounts.append({
        "_id": row[0], "accountName": row[1], "vertical": row[2],
        "annualRecurringRevenueUsd": float(row[3]), "monthlyProcessingVolume": float(row[4]),
        "hasAdoptedFraudShield": row[5] == "Y", "hasAdoptedCashFlowInsights": row[6] == "Y", "hasAdoptedIncomeVerify": row[7] == "Y",
        "monthsSinceLastQuarterlyBusinessReview": int(row[8]), "netPromoterScore": int(row[9]), "renewalDaysOut": int(row[10]),
        "customerSuccessManagerNote": row[11],
    })
print("const accounts = " + json.dumps(accounts) + ";")
print('const collection = db.getCollection("ocrolus-accounts");')
print("accounts.forEach(account => { account.monthsSinceLastQuarterlyBusinessReview = NumberInt(account.monthsSinceLastQuarterlyBusinessReview); account.netPromoterScore = NumberInt(account.netPromoterScore); account.renewalDaysOut = NumberInt(account.renewalDaysOut); collection.replaceOne({ _id: account._id }, account, { upsert: true }); });")
print('print("ocrolus-accounts now holds " + collection.countDocuments() + " documents");')
PYTHON

mongosh "${MONGO_URI}" --quiet --file "${SEED_SCRIPT_FILE}"

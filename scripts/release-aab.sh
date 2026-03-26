#!/usr/bin/env bash
# Bump versionCode + patch versionName in app/build.gradle.kts, then build a signed release AAB
# with production AdMob IDs and Play-style injected signing.
#
# AdMob: set ADMOB_* in the environment, or add to local.properties (same keys as Gradle):
#   admob.application.id, admob.banner.id, admob.interstitial.id, admob.rewarded.id
# Env vars override local.properties when both are set.
#
# Signing: set RELEASE_SIGNING_* (see below) — optional keys in local.properties:
#   release.signing.store.file, release.signing.store.password,
#   release.signing.key.alias, release.signing.key.password
#
# One-shot example (fill in real values; use an absolute path for the keystore):
#
#   ADMOB_APPLICATION_ID='ca-app-pub-XXXX~YYYY' \
#   ADMOB_BANNER_ID='ca-app-pub-XXXX/AAAA' \
#   ADMOB_INTERSTITIAL_ID='ca-app-pub-XXXX/BBBB' \
#   ADMOB_REWARDED_ID='ca-app-pub-XXXX/CCCC' \
#   RELEASE_SIGNING_STORE_FILE="$HOME/keys/upload.jks" \
#   RELEASE_SIGNING_STORE_PASSWORD='…' \
#   RELEASE_SIGNING_KEY_ALIAS='…' \
#   RELEASE_SIGNING_KEY_PASSWORD='…' \
#     "/path/to/Zombie Lane Shooter/scripts/release-aab.sh"

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

GRADLE_KTS="$ROOT/app/build.gradle.kts"

# If env var is empty, set it from local.properties (last matching line, ignores # comments on own lines).
export_from_local() {
  local env_var="$1" prop_key="$2"
  [[ -z "${!env_var:-}" ]] || return 0
  local f="$ROOT/local.properties"
  [[ -f "$f" ]] || return 0
  local line val
  line="$(grep -F "${prop_key}=" "$f" | grep -v '^[[:space:]]*#' | tail -n1)" || return 0
  [[ -n "$line" ]] || return 0
  val="${line#*${prop_key}=}"
  val="${val%%#*}"
  val="$(printf '%s' "$val" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
  [[ -n "$val" ]] || return 0
  eval "export $(printf '%s=%q' "$env_var" "$val")"
}

export_from_local ADMOB_APPLICATION_ID admob.application.id
export_from_local ADMOB_BANNER_ID admob.banner.id
export_from_local ADMOB_INTERSTITIAL_ID admob.interstitial.id
export_from_local ADMOB_REWARDED_ID admob.rewarded.id

export_from_local RELEASE_SIGNING_STORE_FILE release.signing.store.file
export_from_local RELEASE_SIGNING_STORE_PASSWORD release.signing.store.password
export_from_local RELEASE_SIGNING_KEY_ALIAS release.signing.key.alias
export_from_local RELEASE_SIGNING_KEY_PASSWORD release.signing.key.password

for v in ADMOB_APPLICATION_ID ADMOB_BANNER_ID ADMOB_INTERSTITIAL_ID ADMOB_REWARDED_ID; do
  if [[ -z "${!v:-}" ]]; then
    echo "error: $v is not set (export it or set admob.*.id in local.properties)" >&2
    exit 1
  fi
done

for v in RELEASE_SIGNING_STORE_FILE RELEASE_SIGNING_STORE_PASSWORD RELEASE_SIGNING_KEY_ALIAS RELEASE_SIGNING_KEY_PASSWORD; do
  if [[ -z "${!v:-}" ]]; then
    echo "error: $v is not set" >&2
    exit 1
  fi
done

if [[ ! -f "$RELEASE_SIGNING_STORE_FILE" ]]; then
  echo "error: keystore not found: $RELEASE_SIGNING_STORE_FILE" >&2
  exit 1
fi

perl -i -pe '
  s/(versionCode = )(\d+)/$1 . ($2 + 1)/e;
  s/(versionName = ")(\d+)\.(\d+)\.(\d+)(")/$1 . $2 . "." . $3 . "." . ($4 + 1) . $5/e;
' "$GRADLE_KTS"

STORE_ABS="$(cd "$(dirname "$RELEASE_SIGNING_STORE_FILE")" && pwd)/$(basename "$RELEASE_SIGNING_STORE_FILE")"

exec ./gradlew bundleRelease \
  -Pandroid.injected.signing.store.file="$STORE_ABS" \
  -Pandroid.injected.signing.store.password="$RELEASE_SIGNING_STORE_PASSWORD" \
  -Pandroid.injected.signing.key.alias="$RELEASE_SIGNING_KEY_ALIAS" \
  -Pandroid.injected.signing.key.password="$RELEASE_SIGNING_KEY_PASSWORD"

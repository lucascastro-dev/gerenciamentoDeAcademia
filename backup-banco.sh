#!/usr/bin/env bash
exec "$(dirname "$0")/scripts/backup-postgres.sh" manual "$@"

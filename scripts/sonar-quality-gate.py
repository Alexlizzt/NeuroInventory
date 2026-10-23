#!/usr/bin/env python3
"""Espera al análisis de SonarQube y muestra el estado del quality gate.

Variables de entorno:
    SONAR_HOST_URL    URL del servidor (la inyecta `withSonarQubeEnv`).
    SONAR_AUTH_TOKEN  Token de análisis (lo inyecta `withSonarQubeEnv`).
    SONAR_PROJECT_KEY Clave del proyecto (por defecto: neuroinventory).

Pensado para el Jenkinsfile, pero también sirve en local:

    SONAR_HOST_URL=http://localhost:9000 SONAR_AUTH_TOKEN=<token> \\
        python3 scripts/sonar-quality-gate.py

Solo imprime el estado (OK, WARN, ERROR, NONE). La decisión de fallar el
build es del pipeline, para que el análisis sea informativo por defecto.
"""

import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request

TIMEOUT_SECONDS = int(os.environ.get("SONAR_GATE_TIMEOUT", "300"))
POLL_SECONDS = int(os.environ.get("SONAR_GATE_POLL", "5"))


def build_client(base_url: str, token: str):
    auth = base64.b64encode((token + ":").encode()).decode()

    def get(path: str) -> dict:
        request = urllib.request.Request(base_url + path)
        request.add_header("Authorization", "Basic " + auth)
        with urllib.request.urlopen(request, timeout=30) as response:
            return json.load(response)

    return get


def wait_for_analysis(get, project_key: str) -> None:
    deadline = time.monotonic() + TIMEOUT_SECONDS
    while time.monotonic() < deadline:
        try:
            status = get("/api/ce/component?component=" + project_key)
        except urllib.error.HTTPError as error:
            # 404: el proyecto todavía no tiene ningún análisis.
            if error.code == 404:
                return
            raise
        current = status.get("current") or {}
        if not status.get("queue") and current.get("status") not in (
            "PENDING",
            "IN_PROGRESS",
        ):
            return
        time.sleep(POLL_SECONDS)

    raise SystemExit(
        f"SonarQube no terminó el análisis de {project_key} "
        f"en {TIMEOUT_SECONDS} segundos."
    )


def main() -> int:
    base_url = os.environ["SONAR_HOST_URL"].rstrip("/")
    token = os.environ["SONAR_AUTH_TOKEN"]
    project_key = os.environ.get("SONAR_PROJECT_KEY", "neuroinventory")

    get = build_client(base_url, token)
    try:
        wait_for_analysis(get, project_key)
        gate = get("/api/qualitygates/project_status?projectKey=" + project_key)
    except urllib.error.HTTPError as error:
        if error.code == 404:
            print("NONE")
            return 0
        print(
            f"Error al consultar SonarQube ({error.code}): {error.reason}",
            file=sys.stderr,
        )
        return 2

    print(gate["projectStatus"]["status"])
    return 0


if __name__ == "__main__":
    sys.exit(main())

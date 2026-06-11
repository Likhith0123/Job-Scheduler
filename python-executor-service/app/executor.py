import logging
import os
import subprocess
import sys
import tempfile
from pathlib import Path

from .config import EXECUTION_TIMEOUT_SEC, SCRIPTS_DIR

logger = logging.getLogger(__name__)


class PythonJobExecutor:
    def execute(self, payload: dict) -> str:
        if not payload or payload.get("type") != "python":
            raise ValueError("Payload type must be 'python'")

        if "code" in payload and payload["code"]:
            return self._run_inline_code(str(payload["code"]))

        if "module" in payload and payload["module"]:
            return self._run_module(str(payload["module"]))

        raise ValueError("Python payload must include 'code' or 'module'")

    def _run_inline_code(self, code: str) -> str:
        with tempfile.NamedTemporaryFile(mode="w", suffix=".py", delete=False) as handle:
            handle.write(code)
            script_path = handle.name

        try:
            return self._run_script_file(script_path)
        finally:
            os.unlink(script_path)

    def _run_module(self, module: str) -> str:
        scripts_root = Path(SCRIPTS_DIR)
        if not scripts_root.exists():
            raise FileNotFoundError(f"Scripts directory not found: {SCRIPTS_DIR}")

        app_root = scripts_root.parent
        return self._run_process(
            [sys.executable, "-m", module],
            cwd=str(app_root),
        )

    def _run_script_file(self, script_path: str) -> str:
        return self._run_process([sys.executable, script_path])

    def _run_process(self, command: list[str], cwd: str | None = None) -> str:
        logger.info("Running command: %s", " ".join(command))
        completed = subprocess.run(
            command,
            capture_output=True,
            text=True,
            timeout=EXECUTION_TIMEOUT_SEC,
            cwd=cwd,
            env={**os.environ, "PYTHONUNBUFFERED": "1"},
        )

        output = "\n".join(
            part for part in [completed.stdout.strip(), completed.stderr.strip()] if part
        )

        if completed.returncode != 0:
            raise RuntimeError(output or f"Python process exited with code {completed.returncode}")

        return output or "Python job completed successfully"

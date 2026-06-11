import requests

from .config import JOB_SERVICE_URL


class JobServiceClient:
    def __init__(self, base_url: str = JOB_SERVICE_URL) -> None:
        self.base_url = base_url.rstrip("/")

    def start_run(self, job_id: str, tenant_id: str) -> dict:
        response = requests.post(
            f"{self.base_url}/internal/jobs/{job_id}/runs",
            json={"tenantId": tenant_id},
            timeout=10,
        )
        response.raise_for_status()
        return response.json()

    def complete_run(self, run_id: str, tenant_id: str, status: str, result: str) -> dict:
        response = requests.post(
            f"{self.base_url}/internal/jobs/runs/{run_id}/complete",
            json={"tenantId": tenant_id, "status": status, "result": result},
            timeout=10,
        )
        response.raise_for_status()
        return response.json()

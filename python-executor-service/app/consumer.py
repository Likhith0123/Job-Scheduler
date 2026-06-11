import json
import logging
from datetime import datetime, timezone

from kafka import KafkaConsumer, KafkaProducer

from .config import KAFKA_BOOTSTRAP_SERVERS, KAFKA_GROUP_ID, TOPIC_JOB_COMPLETED, TOPIC_JOB_DUE_PYTHON
from .executor import PythonJobExecutor
from .job_client import JobServiceClient

logger = logging.getLogger(__name__)


class PythonJobConsumer:
    def __init__(self) -> None:
        self.job_client = JobServiceClient()
        self.executor = PythonJobExecutor()
        self.consumer = KafkaConsumer(
            TOPIC_JOB_DUE_PYTHON,
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            group_id=KAFKA_GROUP_ID,
            value_deserializer=lambda data: json.loads(data.decode("utf-8")),
            key_deserializer=lambda data: data.decode("utf-8") if data else None,
            auto_offset_reset="earliest",
            enable_auto_commit=True,
        )
        self.producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda data: json.dumps(data).encode("utf-8"),
            key_serializer=lambda data: data.encode("utf-8") if data else None,
        )

    def run(self) -> None:
        logger.info("Python executor listening on topic %s", TOPIC_JOB_DUE_PYTHON)
        for message in self.consumer:
            event = message.value
            self._handle_event(event)

    def _handle_event(self, event: dict) -> None:
        job_id = event["jobId"]
        tenant_id = event["tenantId"]
        job_name = event.get("jobName", job_id)
        payload = event.get("payload", {})

        logger.info("Received python job %s (%s)", job_id, job_name)
        run = self.job_client.start_run(job_id, tenant_id)
        run_id = run["id"]

        try:
            result = self.executor.execute(payload)
            completed = self.job_client.complete_run(run_id, tenant_id, "SUCCEEDED", result)
            self._publish_completed(completed, event)
            logger.info("Completed python run %s for job %s", run_id, job_id)
        except Exception as exc:
            logger.exception("Failed python run %s for job %s", run_id, job_id)
            failed = self.job_client.complete_run(run_id, tenant_id, "FAILED", str(exc))
            self._publish_completed(failed, event)

    def _publish_completed(self, run: dict, source_event: dict) -> None:
        event = {
            "jobRunId": run["id"],
            "jobId": source_event["jobId"],
            "tenantId": source_event["tenantId"],
            "status": run["status"],
            "result": run.get("result"),
            "completedAt": datetime.now(timezone.utc).isoformat(),
        }
        self.producer.send(TOPIC_JOB_COMPLETED, key=run["id"], value=event)
        self.producer.flush()

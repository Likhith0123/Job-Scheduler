import os

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
JOB_SERVICE_URL = os.getenv("JOB_SERVICE_URL", "http://localhost:8082")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "python-executor-service")
TOPIC_JOB_DUE_PYTHON = "chronoflow.job.due.python"
TOPIC_JOB_COMPLETED = "chronoflow.job.completed"
EXECUTION_TIMEOUT_SEC = int(os.getenv("PYTHON_EXECUTION_TIMEOUT_SEC", "30"))
SCRIPTS_DIR = os.getenv("PYTHON_SCRIPTS_DIR", "/app/scripts")

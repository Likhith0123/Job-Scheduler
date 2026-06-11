"""Example module job. Use payload: {"type": "python", "module": "scripts.hello"}"""

from datetime import datetime


def main() -> None:
    print(f"Hello from built-in Python module at {datetime.utcnow().isoformat()}Z")


if __name__ == "__main__":
    main()

import logging

from .consumer import PythonJobConsumer


def main() -> None:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [python-executor] %(message)s",
    )
    PythonJobConsumer().run()


if __name__ == "__main__":
    main()

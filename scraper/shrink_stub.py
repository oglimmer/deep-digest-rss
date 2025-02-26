import subprocess
import sys

import shrink
from loguru import logger

def shrink_stub(input_data):
    result = subprocess.run(
        ['node', 'shrink.js'],
        input=input_data,
        text=True,
        capture_output=True
    )
    if result.returncode != 0:
        logger.error("Failed to run shrink.js with input data")
        logger.error(input_data)
        logger.error("Error message:")
        logger.error(result.stderr)
        logger.error("Falling back to shrink.py")
        shrink_output = shrink.process(input_data)
        return shrink_output
    
    return result.stdout

def main() -> int:
    html = sys.stdin.read()
    if not html:
        return 1

    shrunk_html = shrink_stub(html)
    if shrunk_html:
        print(shrunk_html, end='', flush=True)
        return 0
    else:
        return 1


if __name__ == '__main__':
    sys.exit(main())

import subprocess
import sys

import shrink

def shrink_stub(input_data):
    result = subprocess.run(
        ['node', 'shrink.js'],
        input=input_data,
        text=True,
        capture_output=True
    )
    if result.returncode != 0:
        print(result.stderr, file=sys.stderr)
        print("Falling back to shrink.py", file=sys.stderr)
        shrink_output = shrink.process(input_data)
        return shrink_output
    
    return result.stdout

def main() -> int:
    html = sys.stdin.read()
    if not html:
        return 1

    shrunk_html = shrink_stub(html)
    if shrunk_html:
        print(shrunk_html, end='')
        return 0
    else:
        return 1


if __name__ == '__main__':
    sys.exit(main())

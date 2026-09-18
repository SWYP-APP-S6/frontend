import shutil
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from prepare_specs import INPUT_FILES, ROOT, check_inputs, main


class InputFilesTest(unittest.TestCase):
    def test_missing_bundle_lists_all_required_files(self):
        with tempfile.TemporaryDirectory() as directory:
            with self.assertRaises(ValueError) as result:
                check_inputs(Path(directory))
            for name in INPUT_FILES:
                self.assertIn(name, str(result.exception))
            self.assertIn('remote/README.md', str(result.exception))

    def test_older_specs_do_not_replace_full_0918_merged_spec(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in INPUT_FILES[1:]:
                (root / name).touch()
            (root / 'mangro-app-openapi-2026-09-17-v3.json').touch()
            (root / 'mangro-app-openapi-2026-09-17-v2.json').touch()
            with self.assertRaisesRegex(ValueError, 'v3-merged.json'):
                check_inputs(root)

    def test_complete_bundle_passes_presence_check(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in INPUT_FILES:
                (root / name).touch()
            check_inputs(root)

    def test_documented_bundle_generates_all_modules(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            inputs = root / 'openapi'
            inputs.mkdir()
            for name in INPUT_FILES:
                shutil.copyfile(ROOT / 'openapi' / name, inputs / name)
            output = root / 'generated'
            with patch('prepare_specs.ROOT', root), patch('sys.argv', ['prepare_specs.py', '--output', str(output)]):
                main()
            self.assertEqual({'auth.json', 'consumer.json', 'owner.json', 'user.json'}, {p.name for p in output.iterdir()})

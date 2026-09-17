import tempfile
import unittest
from pathlib import Path

from prepare_specs import INPUT_FILES, check_inputs


class InputFilesTest(unittest.TestCase):
    def test_missing_bundle_lists_all_required_files(self):
        with tempfile.TemporaryDirectory() as directory:
            with self.assertRaises(ValueError) as result:
                check_inputs(Path(directory))
            for name in INPUT_FILES:
                self.assertIn(name, str(result.exception))
            self.assertIn('remote/README.md', str(result.exception))

    def test_owner_spec_does_not_replace_merged_spec(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in INPUT_FILES[1:]:
                (root / name).touch()
            (root / 'mangro-app-openapi-2026-09-17.json').touch()
            with self.assertRaisesRegex(ValueError, 'merged.json'):
                check_inputs(root)

    def test_complete_bundle_passes_presence_check(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            for name in INPUT_FILES:
                (root / name).touch()
            check_inputs(root)

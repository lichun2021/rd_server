import re
import sys
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path


HERO_IDS = tuple(range(1115, 1124))
EXPECTED_COUNTS = {
    "hero.xml": {hero_id: 1 for hero_id in HERO_IDS},
    "item.xml": {hero_id: 2 for hero_id in HERO_IDS},
    "hero_collect.xml": {hero_id: 1 for hero_id in HERO_IDS},
    "hero_star_level.xml": {hero_id: 21 for hero_id in HERO_IDS},
    "hero_skill.xml": {
        1115: 9, 1116: 5, 1117: 9, 1118: 5, 1119: 9,
        1120: 5, 1121: 9, 1122: 5, 1123: 9,
    },
    "hero_soul_level.xml": {
        hero_id: 300 if hero_id in (1116, 1118, 1120, 1122) else 0
        for hero_id in HERO_IDS
    },
    "hero_soul_skill.xml": {
        hero_id: 6 if hero_id in (1116, 1118, 1120, 1122) else 0
        for hero_id in HERO_IDS
    },
    "hero_soul_stage.xml": {
        hero_id: 6 if hero_id in (1116, 1118, 1120, 1122) else 0
        for hero_id in HERO_IDS
    },
}
MIRRORED_FILES = tuple(EXPECTED_COUNTS) + ("effectid.xml",)


def rows(xml_root: Path, name: str):
    return ET.parse(xml_root / name).getroot().findall("data")


def hero_id_for(name: str, row: ET.Element):
    if name == "hero.xml" or name == "hero_collect.xml":
        return int(row.attrib["heroId"])
    if name == "item.xml":
        item_id = int(row.attrib["id"])
        if 1001115 <= item_id <= 1001123:
            return item_id - 1000000
        if 1101115 <= item_id <= 1101123:
            return item_id - 1100000
        return None
    if name == "hero_star_level.xml":
        return int(row.attrib["heroId"])
    if name == "hero_skill.xml":
        return int(row.attrib["skillId"]) // 100
    if name == "hero_soul_level.xml":
        return int(row.attrib["hero"])
    if name in ("hero_soul_skill.xml", "hero_soul_stage.xml"):
        return int(row.attrib["hero"])
    raise AssertionError(f"unsupported table: {name}")


def effect_ids(value: str):
    for item in value.split("|"):
        if item:
            yield int(item.split("_", 1)[0])


class NewHeroConfigClosureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.repo = Path(__file__).resolve().parents[4]
        cls.game_xml = cls.repo / "GameServer" / "xml"
        cls.publish_xml = cls.repo / "Publish" / "xml"

    def test_expected_rows_exist_once(self):
        for name, expected in EXPECTED_COUNTS.items():
            actual = {hero_id: 0 for hero_id in HERO_IDS}
            for row in rows(self.game_xml, name):
                hero_id = hero_id_for(name, row)
                if hero_id in actual:
                    actual[hero_id] += 1
            self.assertEqual(expected, actual, name)

    def test_new_skill_effect_references_are_defined(self):
        referenced = set()
        for row in rows(self.game_xml, "hero_skill.xml"):
            if int(row.attrib["skillId"]) // 100 not in HERO_IDS:
                continue
            referenced.update(effect_ids(row.attrib.get("effectList", "")))
            referenced.update(effect_ids(row.attrib.get("proficiencyEffect", "")))
        for row in rows(self.game_xml, "hero_soul_skill.xml"):
            if int(row.attrib["hero"]) in HERO_IDS:
                referenced.update(effect_ids(row.attrib.get("attr", "")))
        defined = {int(row.attrib["id"]) for row in rows(self.game_xml, "effectid.xml")}
        self.assertEqual(set(), referenced - defined)

    def test_new_collect_references_resolve_to_heroes(self):
        defined = {int(row.attrib["heroId"]) for row in rows(self.game_xml, "hero.xml")}
        missing = {}
        for row in rows(self.game_xml, "hero_collect.xml"):
            hero_id = int(row.attrib["heroId"])
            if hero_id not in HERO_IDS:
                continue
            referenced = {
                int(item.split("_", 1)[0])
                for item in re.split(r"[|,]", row.attrib.get("refHeroIds", ""))
                if item
            }
            unresolved = sorted(referenced - defined)
            if unresolved:
                missing[hero_id] = unresolved
        self.assertEqual({}, missing)

    def test_gameserver_and_publish_xml_are_identical(self):
        for name in MIRRORED_FILES:
            self.assertEqual(
                (self.game_xml / name).read_bytes(),
                (self.publish_xml / name).read_bytes(),
                name,
            )


if __name__ == "__main__":
    unittest.main(verbosity=2)

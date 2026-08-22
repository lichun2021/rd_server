import re
import unittest
from pathlib import Path


HERO_RUNTIME = {
    1116: {
        "skill_ids": (111601, 111602, 111603, 111604, 111605),
        "support": (
            "Checker12721.java", "Checker12722.java", "Checker12723.java",
            "Checker12724.java", "Checker12725.java", "Checker12726.java",
            "Checker12727.java", "Checker12728.java", "Checker12729.java",
            "Checker12730.java", "Checker12731.java", "Checker12741.java",
            "Checker12742.java", "Checker12743.java", "Checker12744.java",
            "Checker12745.java", "Debuff12724.java", "Hero1116Param.java",
        ),
        "hooks": {
            "BattleSoldier.java": ("Hero1116Param",),
            "BattleSoldier_6.java": ("hero1116", "HERO_12721"),
        },
        "soul_duration_effect": "HERO_12746",
    },
    1118: {
        "skill_ids": (111801, 111802, 111803, 111804, 111805),
        "support": (
            "Checker12781.java", "Checker12782.java", "Checker12783.java",
            "Checker12784.java", "Checker12785.java", "Checker12786.java",
            "Checker12787.java", "Checker12791.java", "Checker12801.java",
            "Checker12802.java", "Checker12803.java", "Debuff12785.java",
            "Hero1118.java",
        ),
        "hooks": {
            "BattleSoldier.java": ("Hero1118",),
            "BattleSoldier_1.java": ("hero1118",),
            "BattleTroop.java": ("HERO_12781",),
            "sssSolomon/SolomonPet_1.java": ("HERO_12781",),
        },
        "soul_duration_effect": "HERO_12804",
    },
    1120: {
        "skill_ids": (112001, 112002, 112003, 112004, 112005),
        "support": (
            "Buff12835.java", "Checker12831.java", "Checker12832.java",
            "Checker12833.java", "Checker12834.java", "Checker12835.java",
            "Checker12836.java", "Checker12837.java", "Checker12838.java",
            "Checker12839.java", "Checker12841.java", "Checker12851.java",
            "Checker12852.java", "Checker12853.java",
        ),
        "hooks": {
            "BattleSoldier_3.java": ("HERO_12831", "hero12831"),
        },
        "soul_duration_effect": "HERO_12854",
    },
}


class NewHeroRuntimeClosureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.repo = Path(__file__).resolve().parents[4]
        cls.java = cls.repo / "GameServer" / "src" / "main" / "java" / "com" / "hawk" / "game"
        cls.skills = cls.java / "player" / "hero" / "skill"
        cls.effects = cls.java / "battle" / "effect" / "impl"
        cls.battle = cls.java / "battle"

    def assert_runtime_source_closure(self, hero_id):
        expected = HERO_RUNTIME[hero_id]
        skill_file = self.skills / f"Skill{hero_id}.java"
        self.assertTrue(skill_file.is_file(), f"missing dedicated runtime skill: {skill_file}")

        skill_source = skill_file.read_text(encoding="utf-8")
        annotation = re.search(r"@HeroSkill\s*\(\s*skillID\s*=\s*\{([^}]*)\}", skill_source)
        self.assertIsNotNone(annotation, f"missing @HeroSkill registration for {hero_id}")
        registered = tuple(int(value) for value in re.findall(r"\d+", annotation.group(1)))
        self.assertEqual(expected["skill_ids"], registered)
        self.assertIn(expected["soul_duration_effect"], skill_source)

        effect_dir = self.effects / f"hero{hero_id}"
        missing_support = [name for name in expected["support"] if not (effect_dir / name).is_file()]
        self.assertEqual([], missing_support, f"hero {hero_id} runtime support files")

        for relative_path, tokens in expected["hooks"].items():
            source = (self.battle / relative_path).read_text(encoding="utf-8")
            for token in tokens:
                self.assertIn(token, source, f"hero {hero_id} missing battle hook {token} in {relative_path}")

    def test_hero_1116_runtime_source_closure(self):
        self.assert_runtime_source_closure(1116)

    def test_hero_1118_runtime_source_closure(self):
        self.assert_runtime_source_closure(1118)

    def test_hero_1120_runtime_source_closure(self):
        self.assert_runtime_source_closure(1120)

    @unittest.skip("trusted server runtime source for hero 1122 is not available")
    def test_hero_1122_runtime_source_closure(self):
        self.fail("enable after a source-backed hero 1122 implementation is available")


if __name__ == "__main__":
    unittest.main(verbosity=2)

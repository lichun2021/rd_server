import pathlib
import subprocess
import tempfile
import textwrap
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[3]
RULES = ROOT / "src/main/java/com/hawk/game/battle/effect/impl/hero1122/Hero1122Rules.java"
RUNTIME_DIR = RULES.parent


class Hero1122RulesTest(unittest.TestCase):
    def test_rules_and_boundaries(self):
        self.assertTrue(RULES.exists(), "Hero1122Rules.java has not been implemented")

        harness = textwrap.dedent(
            """
            import com.hawk.game.battle.effect.impl.hero1122.Hero1122Rules;

            public class Hero1122RulesHarness {
                private static void check(boolean condition, String message) {
                    if (!condition) throw new AssertionError(message);
                }

                public static void main(String[] args) {
                    check(!Hero1122Rules.qualifies(50, 100, 1000), "self ratio must be strictly above 50%");
                    check(Hero1122Rules.qualifies(51, 100, 1020), "rally ratio may equal 5%");
                    check(!Hero1122Rules.qualifies(51, 100, 1021), "rally ratio below 5% must fail");
                    check(!Hero1122Rules.qualifies(1, 0, 1), "zero player total must fail");

                    check(Hero1122Rules.airContribution(50000, 111) == 555, "air contribution scaling");
                    check(Hero1122Rules.scaledEffect(1000, 133, 1200) == 13, "difference scaling");
                    check(Hero1122Rules.scaledEffect(100000, 133, 1200) == 1200, "damage reduction cap");

                    check(Hero1122Rules.synergyLayers(4, true) == 0, "no layer before round 5");
                    check(Hero1122Rules.synergyLayers(5, true) == 1, "first layer at round 5");
                    check(Hero1122Rules.synergyLayers(10, true) == 2, "second layer at round 10");
                    check(Hero1122Rules.synergyLayers(15, true) == 2, "layers are capped at two");
                    check(Hero1122Rules.synergyLayers(10, false) == 0, "Vera is required");
                    check(Hero1122Rules.coefficientWithSynergy(1000, 1) == 1080,
                            "one synergy layer raises the coefficient by 8%");
                    check(Hero1122Rules.coefficientWithSynergy(1000, 2) == 1160,
                            "two synergy layers stack additively");
                    check(Hero1122Rules.isInterferenceRound(10, true), "interference fires every fifth round");
                    check(!Hero1122Rules.isInterferenceRound(11, true), "interference lasts one round");

                    check(Hero1122Rules.windFieldTriggerRound(999, 1000, 1200) == 5, "air threshold trigger");
                    check(Hero1122Rules.windFieldTriggerRound(999, 999, 999) == 40, "round 40 fallback");
                    check(Hero1122Rules.isWindFieldDouble(5, 5), "trigger round is doubled");
                    check(Hero1122Rules.isWindFieldDouble(9, 5), "five-round window includes round 9");
                    check(!Hero1122Rules.isWindFieldDouble(10, 5), "five-round window ends before round 10");
                }
            }
            """
        )

        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            harness_file = tmp_path / "Hero1122RulesHarness.java"
            harness_file.write_text(harness, encoding="utf-8")
            subprocess.run(
                ["javac", "-encoding", "UTF-8", "-d", str(tmp_path), str(RULES), str(harness_file)],
                check=True,
            )
            subprocess.run(["java", "-cp", str(tmp_path), "Hero1122RulesHarness"], check=True)

    def test_runtime_registration_is_closed(self):
        expected_checkers = {
            "Checker12961.java", "Checker12962.java", "Checker12963.java",
            "Checker12964.java", "Checker12965.java", "Checker12966.java",
            "Checker12967.java", "Checker12968.java", "Checker12969.java",
            "Checker12970.java", "Checker12981.java", "Checker12991.java",
            "Checker12992.java", "Checker12993.java",
        }
        self.assertTrue(expected_checkers.issubset({path.name for path in RUNTIME_DIR.glob("Checker*.java")}))

        skill = (ROOT / "src/main/java/com/hawk/game/player/hero/skill/Skill1122.java").read_text(encoding="utf-8")
        self.assertIn("112201, 112202, 112203, 112204, 112205", skill)
        self.assertIn("HERO_12994", skill)

        battle = (ROOT / "src/main/java/com/hawk/game/battle/BattleSoldier.java").read_text(encoding="utf-8")
        for hook in (
            "attackAdjustment", "superAttackBonus", "outgoingDamageBonus",
            "windFieldExtraDamage", "incomingDamageReduction", "bomberInterferenceDodge",
        ):
            self.assertIn("Hero1122Runtime." + hook, battle)

    def test_electromagnetic_interception_attribute_effects_use_selected_source(self):
        checker_path = RUNTIME_DIR / "Checker12968.java"
        self.assertTrue(checker_path.is_file(), "Checker12968.java has not been implemented")
        checker = checker_path.read_text(encoding="utf-8")
        self.assertIn("extends Hero1122SourceChecker", checker)
        self.assertIn("EffType.HERO_12968", checker)
        for tuple_type in ("Type.ATK", "Type.DEF", "Type.HP"):
            self.assertIn(tuple_type, checker)

    def test_electromagnetic_interception_damage_reduction_uses_selected_source(self):
        checker_path = RUNTIME_DIR / "Checker12969.java"
        self.assertTrue(checker_path.is_file(), "Checker12969.java has not been implemented")
        checker = checker_path.read_text(encoding="utf-8")
        self.assertIn("extends Hero1122SourceChecker", checker)
        self.assertIn("EffType.HERO_12969", checker)
        self.assertIn("Type.REDUCE_HURT_PCT", checker)


if __name__ == "__main__":
    unittest.main()

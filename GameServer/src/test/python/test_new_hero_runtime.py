import re
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path


HERO_RUNTIME = {
    1116: {
        "skill_ids": (111601, 111602, 111603, 111604, 111605),
        "effect_ids": (
            12721, 12722, 12723, 12724, 12725, 12726, 12727, 12728,
            12729, 12730, 12731, 12741, 12742, 12743, 12744, 12745,
            12746,
        ),
        "const_fields": (
            ("effect12721TargetWeight", "effect12721TargetWeightMap"),
            ("effect12722AtkRound", "effect12722AtkRound"),
            ("effect12722AtkNums", "effect12722AtkNums"),
            ("effect12722RoundWeight", "effect12722RoundWeightMap"),
            ("effect12722SoldierAdjust", "effect12722SoldierAdjustMap"),
            ("effect12723NumLimit", "effect12723NumLimit"),
            ("effect12724SoldierAdjust", "effect12724SoldierAdjustMap"),
            ("effect12726SoldierAdjust", "effect12726SoldierAdjustMap"),
            ("effect12727SoldierAdjust", "effect12727SoldierAdjustMap"),
            ("effect12728SoldierAdjust", "effect12728SoldierAdjustMap"),
            ("effect12729AtkRound", "effect12729AtkRound"),
            ("effect12729SoldierAdjust", "effect12729SoldierAdjustMap"),
            ("effect12730AtkRound", "effect12730AtkRound"),
            ("effect12730SoldierAdjust", "effect12730SoldierAdjustMap"),
        ),
        "support": (
            "Checker12721.java", "Checker12722.java", "Checker12723.java",
            "Checker12724.java", "Checker12725.java", "Checker12726.java",
            "Checker12727.java", "Checker12728.java", "Checker12729.java",
            "Checker12730.java", "Checker12731.java", "Checker12741.java",
            "Checker12742.java", "Checker12743.java", "Checker12744.java",
            "Checker12745.java", "Debuff12724.java", "Hero1116Param.java",
        ),
        "hooks": {
            "BattleSoldier.java": (
                r"\bHashMultimap\s*<\s*Integer\s*,\s*Debuff12724\s*>\s+debuff12724\b",
                r"\bdebuff12724Num\s*=\s*debuff12724Num\s*\(\s*\)",
            ),
            "BattleSoldier_6.java": (
                r"\bhero1116\s*=\s*new\s+Hero1116Param\s*\(\s*this\s*\)",
                r"\bhero1116\.roundStart\s*\(\s*\)",
                r"\bhero1116\.skill12722Atk\s*\(\s*defSoldier\s*\)",
                r"\bhero1116\.reduceHurtValPct\s*\(",
                r"\bhero1116\.addHurtValPct\s*\(",
            ),
        },
        "soul_duration_effect": "HERO_12746",
    },
    1118: {
        "skill_ids": (111801, 111802, 111803, 111804, 111805),
        "effect_ids": (
            12781, 12782, 12783, 12784, 12785, 12786, 12787, 12791,
            12801, 12802, 12803, 12804,
        ),
        "const_fields": (
            ("effect12781AllNumLimit", "effect12781AllNumLimit"),
            ("effect12781SelfNumLimit", "effect12781SelfNumLimit"),
            ("effect12781Maxinum", "effect12781Maxinum"),
            ("effect12781AtkRound", "effect12781AtkRound"),
            ("effect12782AtkRound", "effect12782AtkRound"),
            ("effect12782SoldierAdjust", "effect12782SoldierAdjustMap"),
            ("effect12783ExtraVaule", "effect12783ExtraVaule"),
            ("effect12783BaseVaule", "effect12783BaseVaule"),
            ("effect12783SoldierAdjust", "effect12783SoldierAdjustMap"),
            ("effect12784AddFirePoint", "effect12784AddFirePoint"),
            ("effect12784Round", "effect12784Round"),
            ("effect12784Nums", "effect12784Nums"),
            ("effect12784BaseVaule", "effect12784BaseVaule"),
            ("effect12785AtkThresholdValue1", "effect12785AtkThresholdValue1"),
            ("effect12785AtkThresholdValue2", "effect12785AtkThresholdValue2"),
            ("effect12785AtkThresholdValue3", "effect12785AtkThresholdValue3"),
            ("effect12785AtkThresholdValue4", "effect12785AtkThresholdValue4"),
            ("effect12785SoldierAdjust", "effect12785SoldierAdjustMap"),
            ("effect12786SoldierAdjust", "effect12786SoldierAdjustMap"),
            ("effect12787BaseVaule", "effect12787BaseVaule"),
            ("effect12787SoldierAdjust", "effect12787SoldierAdjustMap"),
        ),
        "support": (
            "Checker12781.java", "Checker12782.java", "Checker12783.java",
            "Checker12784.java", "Checker12785.java", "Checker12786.java",
            "Checker12787.java", "Checker12791.java", "Checker12801.java",
            "Checker12802.java", "Checker12803.java", "Debuff12785.java",
            "Hero1118.java", "Hero1118Rules.java",
        ),
        "hooks": {
            "BattleSoldier.java": (
                r"\bHashMultimap\s*<\s*Integer\s*,\s*Debuff12785\s*>\s+debuff12785\b",
                r"\bdebuff12785Num\s*=\s*debuff12785Num\s*\(\s*\)",
                r"\btank\.hero1118\.buff12787\s*\(",
                r"\btank\.hero1118\.debuff12786\s*\(",
                r"\btank\.hero1118\.buff12784reduceHurtValPct\s*\(",
                r"\btank\.hero1118SoulLink\s*\(",
                r"\bdebuff12724Num\s*\+\s*debuff12785Num\b",
            ),
            "BattleSoldier_1.java": (
                r"\bhero1118\s*=\s*new\s+Hero1118\s*\(\s*this\s*\)",
                r"\bhero1118\.roundStart\s*\(\s*\)",
                r"\bhero1118\.roundEnd\s*\(\s*\)",
                r"\bhero1118\.attackOver\s*\(\s*defSoldier\s*\)",
                r"\bhero1118\.soulLink\s*\(\s*atkSoldier\s*,\s*hurtVal\s*\)",
                r"\bgetEffVal\s*\(\s*EffType\.HERO_12791\s*\)",
            ),
            "BattleTroop.java": (r"\bgetEffVal\s*\(\s*EffType\.HERO_12781\s*\)",),
            "sssSolomon/SolomonPet_1.java": (r"\beff\s*==\s*EffType\.HERO_12781\b",),
        },
        "soul_duration_effect": "HERO_12804",
    },
    1120: {
        "skill_ids": (112001, 112002, 112003, 112004, 112005),
        "effect_ids": (
            12831, 12832, 12833, 12834, 12835, 12836, 12837, 12838,
            12839, 12841, 12851, 12852, 12853, 12854,
        ),
        "const_fields": (
            ("effect12831SelfNumLimit", "effect12831SelfNumLimit"),
            ("effect12831AtkRound", "effect12831AtkRound"),
            ("effect12831AtkTimesForMass", "effect12831AtkTimesForMass"),
            ("effect12831AtkTimesForPerson", "effect12831AtkTimesForPerson"),
            ("effect12835BaseVaule", "effect12835BaseVaule"),
            ("effect12835AtkThresholdValue1", "effect12835AtkThresholdValue1"),
            ("effect12835SoldierAdjust", "effect12835SoldierAdjustMap"),
            ("effect12836AtkThresholdValue1", "effect12836AtkThresholdValue1"),
            ("effect12836BaseVaule", "effect12836BaseVaule"),
            ("effect12836ContinueRound", "effect12836ContinueRound"),
            ("effect12837BaseVaule", "effect12837BaseVaule"),
            ("effect12837SoldierAdjust", "effect12837SoldierAdjustMap"),
            ("effect12838AtkRound", "effect12838AtkRound"),
            ("effect12838Maxinum", "effect12838Maxinum"),
            ("effect12838SoldierAdjust", "effect12838SoldierAdjustMap"),
            ("effect12839SoldierAdjust", "effect12839SoldierAdjustMap"),
        ),
        "support": (
            "Buff12835.java", "Checker12831.java", "Checker12832.java",
            "Checker12833.java", "Checker12834.java", "Checker12835.java",
            "Checker12836.java", "Checker12837.java", "Checker12838.java",
            "Checker12839.java", "Checker12841.java", "Checker12851.java",
            "Checker12852.java", "Checker12853.java", "Hero1120Rules.java",
        ),
        "hooks": {
            "BattleSoldier_3.java": (
                r"\bgetEffVal\s*\(\s*EffType\.HERO_12831\s*\)\s*>\s*0",
                r"\bhero12831\s*\(\s*defSoldier\s*\)",
            ),
        },
        "soul_duration_effect": "HERO_12854",
    },
    1122: {
        "skill_ids": (112201, 112202, 112203, 112204, 112205),
        "effect_ids": (
            12961, 12962, 12963, 12964, 12965, 12966, 12967, 12968,
            12969, 12970, 12981, 12991, 12992, 12993, 12994,
        ),
        "const_fields": (),
        "support": (
            "Checker12961.java", "Checker12962.java", "Checker12963.java",
            "Checker12964.java", "Checker12965.java", "Checker12966.java",
            "Checker12967.java", "Checker12968.java", "Checker12969.java",
            "Checker12970.java", "Checker12981.java", "Checker12991.java",
            "Checker12992.java", "Checker12993.java",
            "Hero1122Rules.java", "Hero1122Runtime.java",
            "Hero1122SourceChecker.java",
        ),
        "hooks": {
            "BattleSoldier.java": (
                r"\bHero1122Runtime\.attackAdjustment\s*\(\s*this\s*\)",
                r"\bHero1122Runtime\.superAttackBonus\s*\(\s*this\s*\)",
                r"\bHero1122Runtime\.outgoingDamageBonus\s*\(\s*this\s*\)",
                r"\bHero1122Runtime\.windFieldExtraDamage\s*\(\s*this\s*\)",
                r"\bHero1122Runtime\.incomingDamageReduction\s*\(\s*this\s*\)",
                r"\bHero1122Runtime\.bomberInterferenceDodge\s*\(\s*this\s*,\s*target\s*\)",
            ),
        },
        "soul_duration_effect": "HERO_12994",
    },
}


def strip_java_comments(source: str) -> str:
    """Remove Java comments while preserving string and character literals verbatim."""
    result = []
    index = 0
    state = "code"
    while index < len(source):
        char = source[index]
        next_char = source[index + 1] if index + 1 < len(source) else ""
        if state == "code" and char == "/" and next_char == "/":
            result.extend((" ", " "))
            index += 2
            state = "line_comment"
        elif state == "code" and char == "/" and next_char == "*":
            result.extend((" ", " "))
            index += 2
            state = "block_comment"
        elif state == "line_comment":
            result.append("\n" if char == "\n" else " ")
            index += 1
            if char == "\n":
                state = "code"
        elif state == "block_comment":
            if char == "*" and next_char == "/":
                result.extend((" ", " "))
                index += 2
                state = "code"
            else:
                result.append("\n" if char == "\n" else " ")
                index += 1
        else:
            result.append(char)
            index += 1
            if state == "code" and char == '"':
                state = "string"
            elif state == "code" and char == "'":
                state = "char"
            elif state in ("string", "char") and char == "\\" and index < len(source):
                result.append(source[index])
                index += 1
            elif state == "string" and char == '"':
                state = "code"
            elif state == "char" and char == "'":
                state = "code"
    return "".join(result)


def mask_java_literals(source: str) -> str:
    """Mask literal contents so a call-shaped string cannot satisfy a source hook."""
    result = []
    index = 0
    state = "code"
    while index < len(source):
        char = source[index]
        if state == "code" and char in ('"', "'"):
            result.append(" ")
            state = "string" if char == '"' else "char"
        elif state in ("string", "char"):
            result.append("\n" if char == "\n" else " ")
            if char == "\\" and index + 1 < len(source):
                index += 1
                result.append("\n" if source[index] == "\n" else " ")
            elif (state == "string" and char == '"') or (state == "char" and char == "'"):
                state = "code"
        else:
            result.append(char)
        index += 1
    return "".join(result)


def has_java_hook(source: str, pattern: str) -> bool:
    return re.search(pattern, mask_java_literals(strip_java_comments(source))) is not None


def extract_java_method_body(source: str, method_pattern: str) -> str:
    """Return a comment/literal-masked Java method body, balancing nested braces."""
    code = mask_java_literals(strip_java_comments(source))
    declaration = re.search(method_pattern + r"\s*\{", code)
    if declaration is None:
        return ""
    opening_brace = declaration.end() - 1
    depth = 1
    index = opening_brace + 1
    while index < len(code) and depth:
        if code[index] == "{":
            depth += 1
        elif code[index] == "}":
            depth -= 1
        index += 1
    return code[opening_brace + 1:index - 1] if depth == 0 else ""


def strip_constant_false_blocks(code: str) -> str:
    """Mask braced or single-statement if(false) bodies in an extracted method."""
    result = list(code)
    search_from = 0
    pattern = re.compile(r"\bif\s*\(\s*false\s*\)\s*")
    while True:
        match = pattern.search(code, search_from)
        if match is None:
            break
        statement_start = match.end()
        if statement_start < len(code) and code[statement_start] == "{":
            depth = 1
            index = statement_start + 1
            while index < len(code) and depth:
                if code[index] == "{":
                    depth += 1
                elif code[index] == "}":
                    depth -= 1
                index += 1
            if depth != 0:
                return ""
        else:
            semicolon = code.find(";", statement_start)
            if semicolon < 0:
                return ""
            index = semicolon + 1
        for offset in range(match.start(), index):
            result[offset] = "\n" if code[offset] == "\n" else " "
        search_from = index
    return "".join(result)


def has_executable_java_method_hook(source: str, method_pattern: str, hook_pattern: str) -> bool:
    body = extract_java_method_body(source, method_pattern)
    return re.search(hook_pattern, strip_constant_false_blocks(body)) is not None


class NewHeroRuntimeClosureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.repo = Path(__file__).resolve().parents[4]
        cls.java = cls.repo / "GameServer" / "src" / "main" / "java" / "com" / "hawk" / "game"
        cls.skills = cls.java / "player" / "hero" / "skill"
        cls.effects = cls.java / "battle" / "effect" / "impl"
        cls.battle = cls.java / "battle"
        cls.const_property = cls.java / "config" / "ConstProperty.java"
        cls.const_xml = cls.repo / "GameServer" / "xml" / "const.xml"
        cls.const_proto = cls.repo / "Protocol" / "Const.proto"

    def test_rule_harnesses_execute_production_rules(self):
        javac = shutil.which("javac")
        java = shutil.which("java")
        self.assertIsNotNone(javac, "javac is required to execute hero rule harnesses")
        self.assertIsNotNone(java, "java is required to execute hero rule harnesses")

        heroes = (1116, 1118, 1120)
        sources = []
        for hero_id in heroes:
            package = self.effects / f"hero{hero_id}"
            sources.extend((
                package / f"Hero{hero_id}Rules.java",
                self.repo / "GameServer" / "src" / "test" / "java" / "com" / "hawk" / "game"
                / "battle" / "effect" / "impl" / f"hero{hero_id}" / f"Hero{hero_id}RulesHarness.java",
            ))

        with tempfile.TemporaryDirectory(prefix="hero-rules-harness-") as output_dir:
            compile_result = subprocess.run(
                [javac, "-encoding", "UTF-8", "-d", output_dir, *(str(path) for path in sources)],
                cwd=self.repo,
                capture_output=True,
                text=True,
            )
            self.assertEqual(
                0,
                compile_result.returncode,
                f"hero rule harness compilation failed:\n{compile_result.stdout}\n{compile_result.stderr}",
            )
            for hero_id in heroes:
                class_name = (
                    f"com.hawk.game.battle.effect.impl.hero{hero_id}."
                    f"Hero{hero_id}RulesHarness"
                )
                with self.subTest(hero_id=hero_id):
                    run_result = subprocess.run(
                        [java, "-cp", output_dir, class_name],
                        cwd=self.repo,
                        capture_output=True,
                        text=True,
                    )
                    self.assertEqual(
                        0,
                        run_result.returncode,
                        f"{class_name} failed:\n{run_result.stdout}\n{run_result.stderr}",
                    )

    def test_hook_matcher_ignores_comments_and_literals(self):
        source = (
            "// hero1116.roundStart()\n"
            "/* hero1116.roundStart() */\n"
            "String text = \"hero1116.roundStart()\";\n"
        )
        self.assertIn('"hero1116.roundStart()"', strip_java_comments(source))
        self.assertFalse(has_java_hook(source, r"\bhero1116\.roundStart\s*\(\s*\)"))

    def test_hook_matcher_rejects_deleted_real_hook(self):
        source = (self.battle / "BattleSoldier_6.java").read_text(encoding="utf-8")
        pattern = r"\bhero1116\.roundStart\s*\(\s*\)"
        self.assertTrue(has_java_hook(source, pattern))
        self.assertFalse(has_java_hook(source.replace("hero1116.roundStart();", ""), pattern))

    def test_method_hook_matcher_rejects_wrong_method_comments_literals_and_if_false(self):
        hook = r"\btank\.hero1118\.buff12782\s*\(\s*this\s*\)"
        source = (
            "int wrongMethod() { return tank.hero1118.buff12782(this); }\n"
            "int skillHurtExactly(BattleSoldier target) {\n"
            "  // tank.hero1118.buff12782(this)\n"
            "  String token = \"tank.hero1118.buff12782(this)\";\n"
            "  if (false) { return tank.hero1118.buff12782(this); }\n"
            "  if (false) return tank.hero1118.buff12782(this);\n"
            "  if (false) tank.hero1118.buff12782(this);\n"
            "  return 0;\n"
            "}\n"
        )
        method = r"\bskillHurtExactly\s*\(\s*BattleSoldier\s+\w+\s*\)"
        self.assertFalse(has_executable_java_method_hook(source, method, hook))
        live_source = source.replace("  return 0;", "  return tank.hero1118.buff12782(this);")
        self.assertTrue(has_executable_java_method_hook(live_source, method, hook))

    def test_hero_1118_round_kill_chain_and_12782_execution_site(self):
        source = (self.battle / "BattleSoldier.java").read_text(encoding="utf-8")
        self.assertTrue(has_java_hook(source, r"\bprivate\s+Map\s*<\s*Integer\s*,\s*Integer\s*>\s+roundKill\s*;"))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\binit\s*\(\s*Player\s+\w+\s*,\s*BattleSoldierCfg\s+\w+\s*,\s*int\s+\w+\s*,\s*int\s+\w+\s*\)",
            r"\bthis\.roundKill\s*=\s*new\s+HashMap\s*<\s*>\s*\(\s*\)\s*;",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\baddKillCnt\s*\(\s*BattleSoldier\s+\w+\s*,\s*int\s+\w+\s*\)",
            r"\bthis\.roundKill\.merge\s*\(\s*getBattleRound\s*\(\s*\)\s*,\s*kill\s*,\s*\(\s*v1\s*,\s*v2\s*\)\s*->\s*v1\s*\+\s*v2\s*\)\s*;",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bgetRoundKill\s*\(\s*\)",
            r"\breturn\s+roundKill\s*;",
        ))

        buff12782 = r"\btank\.hero1118\.buff12782\s*\(\s*this\s*\)"
        additive12782 = (
            r"\bresult\s*=\s*Hero1118Rules\.combineAdditiveDamageBonus\s*\(\s*result\s*,\s*"
            + buff12782
            + r"\s*\)\s*;"
        )
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bskillHurtExactly\s*\(\s*BattleSoldier\s+\w+\s*\)",
            additive12782,
        ))
        self.assertFalse(has_executable_java_method_hook(
            source,
            r"\baddHurtValPct\s*\(\s*BattleSoldier\s+\w+\s*,\s*double\s+\w+\s*\)",
            buff12782,
        ))

    def test_hero_1116_target_war_eff_plumbing(self):
        checker_params = (self.java / "battle" / "effect" / "CheckerParames.java").read_text(encoding="utf-8")
        battle_unity = (self.battle / "BattleUnity.java").read_text(encoding="utf-8")
        battle_service = (self.battle / "BattleService.java").read_text(encoding="utf-8")
        checker_12729 = (self.effects / "hero1116" / "Checker12729.java").read_text(encoding="utf-8")
        checker_12730 = (self.effects / "hero1116" / "Checker12730.java").read_text(encoding="utf-8")

        self.assertTrue(has_java_hook(checker_params, r"\bpublic\s+final\s+BattleConst\.WarEff\s+tarTroopEffType\b"))
        self.assertTrue(has_java_hook(checker_params, r"\bsetTarTroopEffType\s*\(\s*BattleConst\.WarEff\s+tarTroopEffType\s*\)"))
        self.assertTrue(has_java_hook(checker_params, r"\bnew\s+CheckerParames\s*\([^;]*\btarTroopEffType\b"))

        self.assertTrue(has_java_hook(battle_unity, r"(?m)^\s*import\s+com\.hawk\.game\.battle\.effect\.BattleConst\s*;"))
        self.assertTrue(has_java_hook(battle_unity, r"\bBattleConst\.WarEff\s+troopEffType\b"))
        self.assertTrue(has_java_hook(battle_unity, r"\bBattleConst\.WarEff\s+tarTroopEffType\b"))
        self.assertTrue(has_java_hook(battle_unity, r"\bsetTarTroopEffType\s*\(\s*BattleConst\.WarEff\s+tarTroopEffType\s*\)"))

        code = mask_java_literals(strip_java_comments(battle_service))
        self.assertRegex(
            code,
            r"\bbuildBattleSoldierList\s*\(\s*List<BattleUnity>\s+unitList\s*,\s*List<BattleUnity>\s+tarUnitList\s*,\s*BattleConst\.WarEff\s+troopEffType\s*,\s*BattleConst\.WarEff\s+tarTroopEffType\s*,",
        )
        build_calls = re.findall(r"\bbuildBattleSoldierList\s*\(([^\n;]+)\)", code)
        build_calls = [call for call in build_calls if "List<" not in call]
        self.assertTrue(build_calls, "missing battle-soldier build calls")
        for call in build_calls:
            args = [arg.strip() for arg in call.split(",")]
            self.assertGreaterEqual(len(args), 5, f"missing target WarEff in build call: {call}")
            self.assertIn(
                (args[2], args[3]),
                (("atkTroopEffType", "defTroopEffType"), ("defTroopEffType", "atkTroopEffType")),
                f"attacker/defender WarEff pairing is not propagated: {call}",
            )
        self.assertTrue(has_java_hook(battle_service, r"\bunity\.setTroopEffType\s*\(\s*troopEffType\s*\)"))
        self.assertTrue(has_java_hook(battle_service, r"\bunity\.setTarTroopEffType\s*\(\s*tarTroopEffType\s*\)"))
        self.assertTrue(has_java_hook(battle_service, r"\bsetTarTroopEffType\s*\(\s*unity\.getTarTroopEffType\s*\(\s*\)\s*\)"))

        for checker_source in (checker_12729, checker_12730):
            checker_code = mask_java_literals(strip_java_comments(checker_source))
            self.assertTrue(has_java_hook(checker_source, r"\bHero1116Rules\.isBothSelfFight\s*\("))
            self.assertEqual(1, len(re.findall(r"\bWarEff\.SELF_FIGHT\.check\s*\(\s*parames\.troopEffType\s*\)", checker_code)))
            self.assertEqual(1, len(re.findall(r"\bWarEff\.SELF_FIGHT\.check\s*\(\s*parames\.tarTroopEffType\s*\)", checker_code)))

    def test_hero_1120_execution_sites_use_live_production_rules(self):
        source = (self.battle / "BattleSoldier_3.java").read_text(encoding="utf-8")
        checker_path = self.effects / "hero1120" / "Checker12831.java"
        buff_path = self.effects / "hero1120" / "Buff12835.java"
        skill_path = self.skills / "Skill1120.java"
        self.assertTrue(checker_path.is_file(), "missing live 12831 checker")
        self.assertTrue(buff_path.is_file(), "missing live 12835 buff")
        self.assertTrue(skill_path.is_file(), "missing live Skill1120")
        checker = checker_path.read_text(encoding="utf-8")
        buff = buff_path.read_text(encoding="utf-8")
        skill = skill_path.read_text(encoding="utf-8")

        self.assertTrue(has_java_hook(buff, r"\bclass\s+Buff12835\s+extends\s+ISoldierbuff\b"))
        self.assertTrue(has_java_hook(checker, r"\bHero1120Rules\.is12831Eligible\s*\("))

        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\broundStart\s*\(\s*\)",
            r"\bHero1120Rules\.isFullyCharged\s*\(\s*effect12835BaseVaule\s*,\s*ConstProperty\.getInstance\s*\(\s*\)\.effect12835BaseVaule\s*\)",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\broundStart\s*\(\s*\)",
            r"\beffect12835BaseVaule\s*=\s*Hero1120Rules\.nextCharge\s*\(",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bbeforeAttack\s*\(\s*BattleSoldier\s+\w+\s*\)",
            r"\bgetEffVal\s*\(\s*EffType\.HERO_12831\s*\)\s*>\s*0[^}]*\bhero12831\s*\(\s*\w+\s*\)",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bhero12831\s*\(\s*BattleSoldier\s+\w+\s*\)",
            r"\bHero1120Rules\.isBombingRound\s*\(",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bhero12831\s*\(\s*BattleSoldier\s+\w+\s*\)",
            r"\bHero1120Rules\.attackTimes\s*\(",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bhero12831Atk\s*\(\s*BattleTroop\s+\w+\s*,\s*int\s+\w+\s*,\s*SoldierType\s+\w+\s*\)",
            r"\bHero1120Rules\.targetPriority\s*\(",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bhero12831Atk\s*\(\s*BattleTroop\s+\w+\s*,\s*int\s+\w+\s*,\s*SoldierType\s+\w+\s*\)",
            r"\bHero1120Rules\.combinedEffectValue\s*\(\s*getEffVal\s*\(\s*EffType\.HERO_12831\s*\)\s*,\s*getEffVal\s*\(\s*EffType\.HERO_12852\s*\)\s*\)",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bhero12831Atk\s*\(\s*BattleTroop\s+\w+\s*,\s*int\s+\w+\s*,\s*SoldierType\s+\w+\s*\)",
            r"\bHero1120Rules\.nextCharge\s*\(",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bskillHurtExactly\s*\(\s*BattleSoldier\s+\w+\s*\)",
            r"\bHero1120Rules\.cappedRoundStacks\s*\(",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\breduceHurtValPct\s*\(\s*BattleSoldier\s+\w+\s*,\s*double\s+\w+\s*\)",
            r"\bHero1120Rules\.combinedEffectValue\s*\(\s*getEffVal\s*\(\s*EffType\.HERO_12837\s*\)\s*,\s*getEffVal\s*\(\s*EffType\.HERO_12853\s*\)\s*\)",
        ))
        self.assertTrue(has_executable_java_method_hook(
            source,
            r"\bskillFireAtkExactly\s*\(\s*BattleSoldier\s+\w+\s*\)",
            r"\bHero1120Rules\.combinedEffectValue\s*\(\s*getEffVal\s*\(\s*EffType\.HERO_12836\s*\)\s*,\s*getEffVal\s*\(\s*EffType\.HERO_12851\s*\)\s*\)",
        ))
        self.assertTrue(has_executable_java_method_hook(
            skill,
            r"\beffectTime\s*\(\s*\)",
            r"\bHero1120Rules\.effectDurationMillis\s*\(\s*effectTime\s*,\s*getSoulEffVal\s*\(\s*EffType\.HERO_12854\s*\)\s*\)",
        ))

    def test_hero_1120_method_hook_rejects_comments_literals_if_false_and_wrong_method(self):
        source = (self.battle / "BattleSoldier_3.java").read_text(encoding="utf-8")
        method = r"\bbeforeAttack\s*\(\s*BattleSoldier\s+\w+\s*\)"
        hook = r"\bhero12831\s*\(\s*defSoldier\s*\)"
        self.assertTrue(has_executable_java_method_hook(source, method, hook))

        without_live_hook = source.replace("\t\t\thero12831(defSoldier);", "")
        decoys = (
            "\nvoid wrong1120Method() { hero12831(defSoldier); }\n"
            "// hero12831(defSoldier);\n"
            "String hero1120Token = \"hero12831(defSoldier);\";\n"
        )
        self.assertFalse(has_executable_java_method_hook(without_live_hook + decoys, method, hook))
        dead_hook = without_live_hook.replace(
            "\t\tif (getEffVal(EffType.HERO_12831) > 0) {",
            "\t\tif (getEffVal(EffType.HERO_12831) > 0) {\n\t\t\tif (false) hero12831(defSoldier);",
        )
        self.assertFalse(has_executable_java_method_hook(dead_hook, method, hook))

    def assert_protocol_and_const_configuration_closure(self, hero_id):
        proto_source = self.const_proto.read_text(encoding="utf-8")
        const_property_source = strip_java_comments(self.const_property.read_text(encoding="utf-8"))
        constructor = re.search(r"\bpublic\s+ConstProperty\s*\(", const_property_source)
        self.assertIsNotNone(constructor, "ConstProperty constructor missing")
        const_property_declarations = const_property_source[:constructor.start()]
        const_xml_source = self.const_xml.read_text(encoding="utf-8")
        expected = HERO_RUNTIME[hero_id]
        source_paths = [
            self.skills / f"Skill{hero_id}.java",
            *(self.effects / f"hero{hero_id}").glob("*.java"),
            *(self.battle / path for path in expected["hooks"]),
        ]
        production_sources = "\n".join(
            mask_java_literals(strip_java_comments(path.read_text(encoding="utf-8")))
            for path in source_paths
            if path.is_file()
        )
        for effect_id in expected["effect_ids"]:
            if re.search(rf"(?m)^\s*HERO_{effect_id}\s*=\s*{effect_id}\s*;", proto_source) is None:
                self.fail(f"hero {hero_id} missing Protocol/Const.proto enum HERO_{effect_id}")
        for field, reader in expected["const_fields"]:
            if re.search(rf"\b{re.escape(field)}\b", const_property_declarations) is None:
                self.fail(f"hero {hero_id} missing ConstProperty field {field}")
            if re.search(rf"(?m)^\s*{re.escape(field)}\s*=", const_xml_source) is None:
                self.fail(f"hero {hero_id} missing const.xml key {field}")
            if re.search(
                rf"\bConstProperty\.getInstance\s*\(\s*\)\s*\.\s*{re.escape(reader)}\b",
                production_sources,
            ) is None:
                self.fail(f"hero {hero_id} does not read ConstProperty.{reader}")

    def test_hero_1116_protocol_and_const_configuration_closure(self):
        self.assert_protocol_and_const_configuration_closure(1116)

    def test_hero_1118_protocol_and_const_configuration_closure(self):
        self.assert_protocol_and_const_configuration_closure(1118)

    def test_hero_1120_protocol_and_const_configuration_closure(self):
        self.assert_protocol_and_const_configuration_closure(1120)

    def test_hero_1122_protocol_and_const_configuration_closure(self):
        self.assert_protocol_and_const_configuration_closure(1122)

    def assert_runtime_source_closure(self, hero_id):
        expected = HERO_RUNTIME[hero_id]
        skill_file = self.skills / f"Skill{hero_id}.java"
        self.assertTrue(skill_file.is_file(), f"missing dedicated runtime skill: {skill_file}")

        skill_source = skill_file.read_text(encoding="utf-8")
        skill_code = strip_java_comments(skill_source)
        annotation = re.search(r"@HeroSkill\s*\((?P<body>.*?)\)", skill_code, re.DOTALL)
        self.assertIsNotNone(annotation, f"missing @HeroSkill registration for {hero_id}")
        skill_ids = re.search(r"\bskillID\s*=\s*\{(?P<ids>[^}]*)\}", annotation.group("body"))
        self.assertIsNotNone(skill_ids, f"missing skillID list in @HeroSkill for {hero_id}")
        registered = [int(value) for value in re.findall(r"\d+", skill_ids.group("ids"))]
        self.assertEqual(len(expected["skill_ids"]), len(registered), f"hero {hero_id} skillID count")
        self.assertEqual(len(registered), len(set(registered)), f"hero {hero_id} duplicate skillID")
        self.assertEqual(set(expected["skill_ids"]), set(registered), f"hero {hero_id} skillID set")
        self.assertTrue(
            has_java_hook(
                skill_source,
                rf"\bgetSoulEffVal\s*\(\s*EffType\.{expected['soul_duration_effect']}\s*\)",
            ),
            f"hero {hero_id} missing active soul-duration enum {expected['soul_duration_effect']}",
        )

        effect_dir = self.effects / f"hero{hero_id}"
        missing_support = [name for name in expected["support"] if not (effect_dir / name).is_file()]
        self.assertEqual([], missing_support, f"hero {hero_id} runtime support files")

        for relative_path, patterns in expected["hooks"].items():
            source = (self.battle / relative_path).read_text(encoding="utf-8")
            for pattern in patterns:
                self.assertTrue(
                    has_java_hook(source, pattern),
                    f"hero {hero_id} missing battle hook /{pattern}/ in {relative_path}",
                )

    def test_hero_1116_runtime_source_closure(self):
        self.assert_runtime_source_closure(1116)

    def test_hero_1118_runtime_source_closure(self):
        self.assert_runtime_source_closure(1118)

    def test_hero_1120_runtime_source_closure(self):
        self.assert_runtime_source_closure(1120)

    def test_hero_1122_runtime_source_closure(self):
        self.assert_runtime_source_closure(1122)


if __name__ == "__main__":
    unittest.main(verbosity=2)

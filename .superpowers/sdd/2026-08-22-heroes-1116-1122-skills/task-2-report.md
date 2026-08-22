# Task 2 report — Hero 1116 runtime skill

Status: DONE_WITH_CONCERNS

## Changed files

- `GameServer/src/main/java/com/hawk/game/player/hero/skill/Skill1116.java`
- `GameServer/src/main/java/com/hawk/game/battle/effect/impl/hero1116/` (the 16 12721–12745 checkers, `Debuff12724`, `Hero1116Param`, and package descriptor)
- `GameServer/src/main/java/com/hawk/game/battle/BattleSoldier.java`
- `GameServer/src/main/java/com/hawk/game/battle/BattleSoldier_6.java`
- `GameServer/src/main/java/com/hawk/game/config/ConstProperty.java`
- `GameServer/xml/const.xml`

## Source and adaptations

Ported only the Hero 1116-specific implementation from `E:\rd_new\src\main\java` and `E:\rd_new\xml\const.xml`. All reference `EffType.EFF127xx` members were adapted to this repository's generated protocol names, `EffType.HERO_127xx` (including the 12746 soul-duration effect). The reference source was copied as UTF-8 and inspected after transfer; the added runtime logic matches the reference after the enum adaptation, with the one 12726 log string made ASCII to avoid the earlier malformed transfer.

`BattleSoldier` contains only 12724 debuff state/counting and attack-count gating. `BattleSoldier_6` contains only the 1116 lifecycle, target-weight override, additional attack, damage modifiers, and attack/fire additions. `ConstProperty` and `const.xml` contain only 1116 parameters.

## RED/GREEN evidence

RED, before production code:

```text
python -m unittest GameServer/src/test/python/test_new_hero_runtime.py -k 1116
FAIL: missing dedicated runtime skill: .../Skill1116.java
```

GREEN, after implementation:

```text
python -B -m unittest GameServer/src/test/python/test_new_hero_runtime.py -k 1116
Ran 1 test ... OK

python -B -m unittest GameServer/src/test/python/test_new_hero_config.py
Ran 4 tests ... OK
```

## Compile evidence

Attempted project compilation with:

```text
E:\rd_server\.gradle\gradle-4.10.3-dist\gradle-4.10.3\bin\gradle.bat :GameServer:compileJava --no-daemon
```

It was blocked before `GameServer` compilation because Gradle could not delete the pre-existing generated protocol classes directory:

```text
Protocol/Protobuf/Java:compileJava FAILED
Unable to delete directory .../Protocol/Protobuf/Java/build/classes/java/main/com/hawk/game/protocol
```

JDK 8 direct compilation of the task sources was also attempted. Its first errors are pre-existing project dependency/API issues, for example `BattleSoldier_6`'s existing `com.sun.org.apache.xml.internal.security.c14n` import and missing `com.hawk.game.idipscript.*` / `sun.awt.util` packages. No Hero 1116-specific compiler diagnostic was reached. JDK 11 additionally rejects these legacy internal packages through its module system.

## Full command summary

- Read task brief and `superpowers:test-driven-development` instructions.
- `python -m unittest ...test_new_hero_runtime.py -k 1116` — expected RED.
- `python -B -m unittest ...test_new_hero_runtime.py -k 1116` — PASS.
- `python -B -m unittest ...test_new_hero_config.py` — PASS.
- `git diff --check` — no whitespace errors (only repository line-ending notices).
- Gradle and direct-JDK compilation attempts as described above — blocked by baseline/generated-output issues.

## Main-workspace incident and cleanup

An initial relative Add-File patch was resolved by the patch tool at `E:\rd_server` rather than the assigned worktree. It created only this task's new 1116 files; those exact files were immediately deleted and verified absent from `E:\rd_server`. All final additions use absolute worktree paths. The Gradle attempt also modified tracked generated build artifacts; those were restored to the known-clean pre-build state. Test `__pycache__` and temporary compile logs were removed; `git status` contains only this task's files.

## Self-review and unresolved concern

Reviewed for minimal scope, `HERO_127xx` protocol adaptation, target/round gating, 12724 attack gating, and soul duration. Runtime/config closure tests pass. The only unresolved issue is that a complete project Java compilation cannot be established in this workspace due to the generated protocol directory lock and baseline legacy/missing dependencies described above.

Commit: `4d313250` (this commit will be amended after the report is staged).

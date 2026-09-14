#!/usr/bin/env python3
"""VERTIL POS — Auto-check."""
import os, re, sys

PROJ = "/home/z/my-project/vertil-pos"
APP = os.path.join(PROJ, "app")
SRC = os.path.join(APP, "src", "main", "java", "com", "vertil", "pos")

passed = 0; failed = 0; warnings = 0

def ok(m):
    global passed; passed += 1; print(f"  [PASS]  {m}")
def fail(m):
    global failed; failed += 1; print(f"  [FAIL]  {m}")
def warn(m):
    global warnings; warnings += 1; print(f"  [WARN]  {m}")
def section(t): print(f"\n=== {t} ===")

section("1. Estructura")
dirs = ["core/money", "core/engine", "data/db", "data/dao", "data/entity",
        "scanner", "security", "backup", "printing", "reports", "di",
        "ui/theme", "ui/components", "ui/screens"]
for d in dirs:
    if os.path.isdir(os.path.join(SRC, d)): ok(f"dir  {d}")
    else: fail(f"dir  {d}  NO EXISTE")

section("2. Archivos críticos")
files = [
    "settings.gradle.kts", "app/build.gradle.kts", "app/src/main/AndroidManifest.xml",
    "app/src/main/res/values/strings.xml", "app/src/main/res/values/colors.xml",
    "app/src/main/res/values/themes.xml", "app/src/main/res/drawable/ic_splash_icon.xml",
    "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
    "app/src/main/java/com/vertil/pos/VertilPosApp.kt",
    "app/src/main/java/com/vertil/pos/di/ServiceLocator.kt",
    "app/src/main/java/com/vertil/pos/ui/MainActivity.kt",
    "app/src/main/java/com/vertil/pos/ui/PosViewModel.kt",
    "app/src/main/java/com/vertil/pos/core/money/Money.kt",
    "app/src/main/java/com/vertil/pos/core/engine/SalesEngine.kt",
    "app/src/main/java/com/vertil/pos/core/engine/InventoryEngine.kt",
    "app/src/main/java/com/vertil/pos/core/engine/CashEngine.kt",
    "app/src/main/java/com/vertil/pos/core/engine/SaleService.kt",
    "app/src/main/java/com/vertil/pos/core/engine/AuditLogger.kt",
    "app/src/main/java/com/vertil/pos/core/engine/CashService.kt",
    "app/src/main/java/com/vertil/pos/data/db/PosDatabase.kt",
    "app/src/main/java/com/vertil/pos/data/entity/Entities.kt",
    "app/src/main/java/com/vertil/pos/data/dao/Daos.kt",
    "app/src/main/java/com/vertil/pos/scanner/BarcodeScannerController.kt",
    "app/src/main/java/com/vertil/pos/security/AuthenticationManager.kt",
    "app/src/main/java/com/vertil/pos/security/RolePermissions.kt",
    "app/src/main/java/com/vertil/pos/backup/BackupService.kt",
    "app/src/main/java/com/vertil/pos/ui/theme/Color.kt",
    "app/src/main/java/com/vertil/pos/ui/theme/Theme.kt",
    "app/src/main/java/com/vertil/pos/ui/screens/LoginScreen.kt",
    "app/src/main/java/com/vertil/pos/ui/screens/DashboardScreen.kt",
    "app/src/main/java/com/vertil/pos/ui/screens/PosScreen.kt",
    "app/src/main/java/com/vertil/pos/ui/screens/ScannerScreen.kt",
    "app/src/main/java/com/vertil/pos/ui/screens/ProductsScreen.kt",
    "app/src/main/java/com/vertil/pos/ui/screens/MoreScreens.kt",
]
for f in files:
    full = os.path.join(PROJ, f)
    if not os.path.isfile(full): fail(f"file {f}  NO EXISTE"); continue
    s = os.path.getsize(full)
    if s == 0: fail(f"file {f}  VACÍO")
    else: ok(f"file {f}  ({s}b)")

section("3. Gradle")
b = open(os.path.join(APP, "build.gradle.kts")).read()
if "compileSdk = 34" in b: ok("compileSdk=34")
else: fail("compileSdk incorrecto")
if "applicationId = \"com.vertil.pos\"" in b: ok("applicationId correcto")
else: fail("applicationId incorrecto")
if "camera" in b.lower(): ok("CameraX presente")
else: fail("CameraX ausente")
if "barcode-scanning" in b: ok("ML Kit barcode presente")
else: fail("ML Kit barcode ausente")
if "room" in b.lower(): ok("Room presente")
else: fail("Room ausente")
if "jbcrypt" in b: ok("jbcrypt (password hashing) presente")
else: fail("jbcrypt ausente")

section("4. Manifest")
m = open(os.path.join(APP, "src", "main", "AndroidManifest.xml")).read()
if "android.permission.CAMERA" in m: ok("CAMERA permiso")
else: fail("CAMERA ausente")
if re.search(r'uses-permission[^>]*INTERNET', m): warn("INTERNET declarado (no debería)")
else: ok("Sin INTERNET")
if ".VertilPosApp" in m: ok("Application class")
else: fail("Application class ausente")

section("5. Anti-patrones")
kt = []
for root, _, files in os.walk(SRC):
    for fn in files:
        if fn.endswith(".kt"): kt.append(os.path.join(root, fn))
ph = re.compile(r"\b(TODO|FIXME|XXX|HACK)\b")
ec = re.compile(r"catch\s*\([^)]*\)\s*\{\s*\}", re.MULTILINE)
phc = 0; ecc = 0
for kf in kt:
    text = open(kf).read()
    if ph.search(text): phc += 1; warn(f"placeholder en {os.path.relpath(kf, PROJ)}")
    if ec.search(text): ecc += 1; fail(f"catch vacío en {os.path.relpath(kf, PROJ)}")
if phc == 0: ok("Sin placeholders")
if ecc == 0: ok("Sin catchs vacíos")

section("6. Tests")
test_dir = os.path.join(APP, "src/test/java/com/vertil/pos")
total = 0
for root, _, files in os.walk(test_dir):
    for fn in files:
        if fn.endswith(".kt"):
            text = open(os.path.join(root, fn)).read()
            tc = text.count("@Test")
            total += tc
            if tc > 0: ok(f"  {os.path.relpath(os.path.join(root, fn), PROJ)} — {tc} @Test")
print(f"  Total: {total} tests")

section("RESULTADO")
print(f"  PASS: {passed}"); print(f"  WARN: {warnings}"); print(f"  FAIL: {failed}")
print()
if failed == 0: print("PROJECT STATUS: SUCCESS"); sys.exit(0)
else: print("PROJECT STATUS: FAIL"); sys.exit(1)

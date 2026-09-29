# 울티마7 한글 Exult 안드로이드 설치 안내

이 APK에는 **Exult 실행 엔진만** 들어 있습니다. 울티마 7 원본 게임 데이터와
한글 패치는 들어 있지 않습니다. 이용자가 합법적으로 보유한 두 자료를 앱에서
직접 지정하면 한글판을 실행할 수 있습니다.

## 준비물

- ARM64 안드로이드 기기(Android 9 이상)
- 이 저장소의 Releases에서 받은 최신 APK
- 플레이할 게임의 정품 데이터
  - 7-1: Ultima VII: The Black Gate + Forge of Virtue
  - 7-2: Ultima VII Part 2: Serpent Isle + Silver Seed
- 해당 게임의 PC용 한글 패치
- PC 한글판 `Exult.exe` 옆의 공통 `patch` 폴더

## 파일 준비

### 원본 게임

GOG판은 게임 페이지에서 받은 **Mac용 오프라인 설치 파일(`.pkg`)**을 그대로
사용하는 방법이 가장 쉽습니다. PC에 설치한 게임을 사용하려면 `STATIC` 폴더를
ZIP으로 압축합니다. ZIP 안에 `STATIC/INITGAME.DAT`, `STATIC/SHAPES.VGA`가
보여야 합니다.

### Exult 공통 patch 폴더

PC 한글판에서 `Exult.exe`와 같은 위치에 있는 `patch` 폴더를 말합니다. 별도로
배포되는 "한글 글꼴 ZIP"이 있는 것이 아닙니다. 이 `patch` 폴더를 통째로
ZIP으로 압축합니다. 다음 파일들이 들어 있어야 합니다.

```text
PC 한글판 Exult 폴더/
├─ Exult.exe
└─ patch/                 ← 이 폴더를 ZIP으로 압축
   ├─ EXULTMSG.TXT
   ├─ FONT0BG.FNT
   ├─ FONT0SI.FNT
   └─ 그 밖의 FONT*.FNT
```

- `EXULTMSG.TXT`
- `FONT0BG.FNT`, `FONT0SI.FNT`
- 그 밖의 `FONT*.FNT` 파일 전체

ZIP 안에 `patch` 폴더가 들어 있어도 되고, 해당 파일들이 ZIP의 맨 위에 바로
있어도 됩니다.

### 게임별 한글 패치

7-1과 7-2의 게임 폴더 안에 있는 `patch` 폴더를 게임별로 각각 ZIP으로
압축합니다. 최소한 `USECODE`, `INITGAME.DAT`, `TEXT.FLX`가 들어 있어야 합니다.
두 게임의 패치를 하나의 ZIP으로 섞지 않는 편이 알아보기 쉽습니다.

## APK 설치

1. [Releases](https://github.com/lf-idonotknow/ultima7-korean-exult-android/releases)를
   열고 최신 `.apk` 파일을 누릅니다.
2. 내려받은 APK를 열어 설치합니다.
3. 설치가 차단되면 안드로이드 설정에서 지금 사용한 브라우저나 파일 앱에만
   **알 수 없는 앱 설치 허용**을 켭니다.
4. 앱 목록에서 **울티마7 한글 Exult**를 실행합니다.

기존 시험판 위에 업데이트되지 않으면 APK 서명이 다른 경우입니다. 저장 파일을
먼저 별도로 보관한 다음 기존 시험판을 삭제하고 공개 APK를 새로 설치하십시오.

## 1단계: 원본 게임 설치

1. 앱 위쪽의 **GAMES** 탭을 누릅니다.
2. 7-1은 **The Black Gate + Forge of Virtue**, 7-2는
   **The Serpent Isle + Silver Seed** 왼쪽의 네모를 누릅니다.
3. **Select file on your device**를 누릅니다.
4. 준비한 GOG `.pkg` 또는 원본 게임 ZIP을 선택합니다.
5. 압축 해제가 끝나고 게임 이름 왼쪽에 초록색 체크가 생기는지 확인합니다.

## 2단계: 한글 패치 설치

1. **MODS** 탭을 누릅니다.
2. **Exult 공통 patch 폴더 설치**를 누르고 `Exult.exe` 옆의 `patch` 폴더를
   압축한 ZIP을 선택합니다.
3. 7-1은 **Black Gate 한글 패치 설치**, 7-2는
   **Serpent Isle 한글 패치 설치**를 눌러 해당 ZIP을 선택합니다.
4. 선택한 항목 왼쪽에 초록색 체크가 생기는지 확인합니다.

ZIP 내부의 폴더 이름과 대소문자는 달라도 괜찮습니다. 앱은 압축 파일 안에서
필수 파일을 찾아 Android 내부의 올바른 위치에 설치합니다. 경로를 벗어나려는
위험한 ZIP 항목은 설치하지 않습니다.

## 3단계: 한글판 실행

MODS 탭 위쪽의 **Black Gate 한글 실행** 또는 **Serpent Isle 한글 실행**을
누릅니다. 앱은 실행 직전에 Exult 공통 patch 파일과 게임별 필수 패치 파일을
다시 검사합니다.
파일이 빠졌으면 어떤 패치를 먼저 설치해야 하는지 화면에 표시합니다.

일반 **LAUNCHER** 탭에서 실행할 수도 있지만, 처음에는 MODS 탭의 한글 실행
버튼을 사용하는 편이 설치 상태를 확인하기 쉽습니다.

## 자주 생기는 문제

### GAMES 탭에 초록색 체크가 생기지 않습니다

원본 게임 파일이 잘못되었거나 확장팩이 빠졌을 수 있습니다. GOG Mac용 `.pkg`
또는 `STATIC` 폴더 전체가 들어 있는 ZIP을 다시 선택하십시오.

### 한글 패치가 “Invalid/unrecognized archive”라고 나옵니다

잘못된 종류의 ZIP을 선택한 것입니다. Exult 공통 patch ZIP에는 `FONT0BG.FNT`,
7-1 패치에는 `ENDSHAPE.FLX`, 7-2 패치에는 `SCHEDULE.DAT`가 있어야 합니다.

### 한글 실행 버튼이 “한글 패치 설치 필요”라고 표시합니다

Exult 공통 patch 폴더와 해당 게임의 한글 패치를 모두 설치했는지 확인하십시오.
체크가 되어 있어도 파일을 다른 앱에서 지웠다면 항목을 해제했다가 다시
설치하십시오.

### 게임 진입 중 앱이 종료됩니다

원본 게임과 한글 패치의 판본이 서로 맞는지 확인하십시오. 계속 종료되면
`Download/Exult-Korean/diagnostics/`의 최근 기록과 함께 GitHub Issues에 증상을
남겨 주십시오.

### 패드 버튼이 원하는 대로 동작하지 않습니다

[게임패드 조작표](KOREAN_ANDROID_CONTROLS.md)를 확인하십시오. 제조사마다 버튼
번호를 다르게 전달할 수 있어 진단 기록을 이용해 조정할 수 있습니다.

## 개발자용 빌드

공개 APK는 `KOREAN_ASSET_ROOT`를 지정하지 않고 빌드합니다. 이 방식으로 빌드한
APK에는 게임·번역·글꼴 자료가 포함되지 않습니다. 일반 빌드 절차는
[Android README](README.md)를 참고하십시오.

앱 패키지 ID는 `info.exult.korean`이며 공식 Exult의 `info.exult`와 함께 설치할
수 있습니다. 이 프로젝트는 Exult 공식 배포판이 아닌 독립 개조판입니다.

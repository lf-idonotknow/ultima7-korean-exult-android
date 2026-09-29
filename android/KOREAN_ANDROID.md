# 울티마7 한글 Exult 안드로이드 설치 안내

이 앱은 울티마 7 원본 게임을 대신하는 앱이 아닙니다. 이용자가 소유한 게임
데이터를 안드로이드에서 실행해 주는 Exult 엔진입니다. 공개 APK에는 원본 게임
데이터를 넣지 않습니다.

## 준비물

- ARM64 안드로이드 기기(Android 9 이상)
- 이 저장소의 Releases에서 받은 최신 APK
- 다음 중 플레이할 게임의 정품 데이터
  - Ultima VII: The Black Gate + Forge of Virtue
  - Ultima VII Part 2: Serpent Isle + Silver Seed

GOG에서 구입했다면 안드로이드 기기에서 GOG 계정에 로그인한 다음, 게임의
오프라인 설치 파일 가운데 **Mac용 `.pkg` 파일**을 받는 방법이 가장 쉽습니다.
PC에 설치된 게임의 `STATIC` 폴더를 ZIP으로 압축해도 됩니다. ZIP 안에는
`STATIC/INITGAME.DAT`, `STATIC/SHAPES.VGA` 같은 파일이 보여야 합니다.

## APK 설치

1. [Releases](https://github.com/lf-idonotknow/ultima7-korean-exult-android/releases)를
   열고 최신 버전의 `.apk` 파일을 누릅니다.
2. 내려받기가 끝나면 파일을 열어 설치합니다.
3. “알 수 없는 앱을 설치할 수 없음”이 나오면 안드로이드 설정에서 지금 사용한
   브라우저나 파일 앱에만 **알 수 없는 앱 설치 허용**을 켭니다.
4. 설치가 끝나면 앱 목록에서 **울티마7 한글 Exult**를 실행합니다.

기존 시험판 위에 업데이트가 되지 않으면 기존 앱의 서명과 공개 APK의 서명이
다른 경우입니다. 먼저 저장 파일을 별도로 보관한 다음 기존 시험판을 삭제하고
공개 APK를 새로 설치해야 합니다.

## 게임 데이터 지정

1. 앱 위쪽에서 **GAMES** 탭을 누릅니다.
2. 블랙 게이트는 **The Black Gate + Forge of Virtue**, 서펜트 아일은
   **The Serpent Isle + Silver Seed** 왼쪽의 네모를 누릅니다.
3. 파일 선택 화면에서 GOG의 `.pkg` 파일 또는 직접 만든 게임 ZIP을 고릅니다.
4. 압축 해제가 끝날 때까지 앱을 닫지 않습니다.
5. 게임 이름 왼쪽에 초록색 체크 표시가 나타나면 데이터 지정이 끝난 것입니다.

## 한글판 실행

1. 위쪽에서 **MODS** 탭을 누릅니다.
2. 블랙 게이트는 **Black Gate 한글 실행 / 파일 복구**, 서펜트 아일은
   **Serpent Isle 한글 실행 / 파일 복구**를 누릅니다.
3. 처음 실행할 때는 한글 파일 검사와 복구 때문에 잠시 기다릴 수 있습니다.
4. 게임의 시작 화면뿐 아니라 실제 대사도 한글로 나오면 정상입니다.

일반 **LAUNCHER** 탭에서 바로 실행하면 한글 모드가 선택되지 않을 수 있으므로,
처음에는 반드시 MODS 탭의 한글 실행 버튼을 사용하십시오.

## 자주 생기는 문제

### GAMES 탭에 초록색 체크가 생기지 않습니다

선택한 파일이 잘못되었거나 게임의 확장팩이 빠진 경우입니다. GOG Mac용 `.pkg`
또는 `STATIC` 폴더 전체가 들어 있는 ZIP을 다시 선택하십시오.

### 시작 화면만 한글이고 게임 대사는 영어입니다

LAUNCHER에서 일반 실행했을 가능성이 큽니다. 앱을 다시 열고 MODS 탭의
**한글 실행 / 파일 복구** 버튼을 누르십시오.

### 게임 진입 중 앱이 종료됩니다

MODS 탭의 같은 버튼을 다시 누르면 한글 파일을 검사하고 손상된 파일을
복구합니다. 계속 종료되면 `Download/Exult-Korean/diagnostics/`의 최근 기록과
함께 GitHub Issues에 증상을 남겨 주십시오.

### 패드 버튼이 원하는 대로 동작하지 않습니다

[게임패드 조작표](KOREAN_ANDROID_CONTROLS.md)를 확인하십시오. 제조사마다 버튼
번호를 다르게 전달할 수 있으므로 진단 기록을 이용해 조정할 수 있습니다.

## 개발자용 빌드

일반 Android 빌드 방법은 [Android README](README.md)를 따릅니다. 사유 자료는
저장소에 커밋하지 않습니다. 로컬 자산을 넣어 시험 빌드할 때만 다음과 같이
외부 디렉터리를 지정합니다.

```sh
make -C android -j4 \
  GRADLE_PROJECT_PROPS="--project-prop KOREAN_ASSET_ROOT=/absolute/path/to/asset-root"
```

앱 패키지 ID는 `info.exult.korean`이며 공식 Exult의 `info.exult`와 함께 설치할
수 있습니다. 이 프로젝트는 Exult 공식 배포판이 아닌 독립 개조판입니다.

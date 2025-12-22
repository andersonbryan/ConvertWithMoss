<!-- Copilot / AI agent instructions for ConvertWithMoss -->
# ConvertWithMoss — Copilot instructions

Purpose: give AI coding agents the essential, actionable knowledge to be productive quickly in this repository.

**Big picture**
- **Language / build:** Java (modular) built with Maven. Root `pom.xml` drives compilation, dependency resolution and packaging.
- **Module:** `de.mossgrabers.convertwithmoss` (see `src/main/java/module-info.java`). UI is JavaFX; core logic sits under `de.mossgrabers.convertwithmoss.core` and format adapters under `de.mossgrabers.convertwithmoss.format.*`.
- **Packaging:** project produces a runnable JAR and uses `jpackage-maven-plugin` for native installers. Build artifacts: `target/lib` (jars/deps) and `target/release` (installers).

**Where to look for key code/patterns**
- UI: `src/main/java/de/mossgrabers/convertwithmoss/ui/` (JavaFX app & CLI entrypoint). Main class is defined by the `main.class` property in `pom.xml` and implemented in `de.mossgrabers.convertwithmoss.ui.ConvertWithMossApp`.
- Core detectors/creators: `core/creator/` and `core/detector/` under `src/main/java/de/mossgrabers/convertwithmoss/core/`.
- Formats: implementations grouped by destination/source under `src/main/java/de/mossgrabers/convertwithmoss/format/` (e.g., `bitwig`, `sfz`, `wav`).
- File handlers: `file/` packages contain format-specific readers/writers (e.g., `file/riff`, `file/wav`).
- Resources: `src/main/resources/` contains `Strings.properties`, CSS (`css/`), images, and template files used at runtime.

**Build / test / run (concrete commands)**
- Build the app and dependencies: `mvn clean package` (produces `target/lib` and jars).
- Run tests: `mvn test` (standard Surefire plugin configured).
- Create native installers (jpackage): either run `mvn jpackage:jpackage` or use the included scripts `jpackage-mac.sh`, `jpackage-linux.sh`, `jpackage-win.cmd`. Installer output appears in `target/release`.
- Quick local dev run (uses JavaFX): `mvn -DskipTests clean package` then run the generated jar from `target/lib` or use the JavaFX plugin: `mvn javafx:run`.

Notes: `pom.xml` sets compiler `<source>`/`<target>` to `23` but `documentation/README.md` mentions minimum Java `24` — confirm with maintainers which SDK should be used for CI/packaging.

**Project-specific conventions**
- `main.class` is defined in `pom.xml` and used widely; changes to the entry class should update this property.
- Format adapters and detectors follow naming/package conventions inside `format/` and `core/` packages; add new format support by creating a directory under `format/` and a matching factory/registrar if present.
- CLI uses `picocli` — see `documentation/README.md` for exact CLI flags and usage examples. The CLI entrypoint is the same app; adding CLI options uses picocli annotations.
- Resource strings are in `src/main/resources/Strings.properties` — update keys consistently; UI reads localized strings from this file.

**Integration & dependencies**
- Key external deps (in `pom.xml`): OpenJFX (`javafx-controls`), `java-vorbis-support`, `javasound-flac`, `picocli`, and a local `uitools` artifact in `maven-local-repository/`.
- CI/build scripts expect a local Maven repo available at `file://${project.basedir}/maven-local-repository` for some artifacts. Do not remove that folder if you need to reproduce builds offline.

**Common code edits & examples**
- To add a new format implementation: create `src/main/java/de/mossgrabers/convertwithmoss/format/<newformat>/`, implement the detector/creator interfaces found under `core/`, and register the format if a registry exists.
- To change the main entry: update `main.class` in `pom.xml` and ensure `module-info.java` exports/opens the required packages.
- To add a JVM arg for packaging (large-memory builds): `jpackage-maven-plugin` in `pom.xml` already provides `<javaOptions>`; for ad-hoc runs, set `MAVEN_OPTS` or `JAVA_TOOL_OPTIONS`.

**Where installers and scripts live**
- Packaging scripts: `jpackage-mac.sh`, `jpackage-linux.sh`, `jpackage-win.cmd` at repo root.
- Runtime launch scripts: `run.cmd`, `linux/convertwithmoss.sh`.

If something is unclear or you need more detail on any code area (detectors, creators, packaging profile differences, or the `uitools` local dependency), tell me which area and I will expand the instruction with code references and short examples.

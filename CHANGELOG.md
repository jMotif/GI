# Changelog

All notable changes to jmotif-gi are documented here.
This project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- **`TestSequiturPinnedBehavior`** — 11 tests pinning current Sequitur output ahead of the
  planned de-static refactor: degenerate inputs (empty/blank/null/single-token), overlapping
  digrams (`a a a` vs `a a a a`), rule utility, occurrence retention under rule absorption
  (the `SAXNonTerminal.cleanUp()` TODO behavior RRA depends on), decompression round-trip on
  a real SAX string, and repeated-run determinism.

### Changed
- **CI:** removed the source-install of jmotif-sax — the pom's pinned version resolves
  from Maven Central, so the source-built jar was never used (CI silently tested against
  different dependency code than local builds).
- **CI:** dropped the SonarCloud job and pom properties (stale token failed every run).

## [2.0.2] — 2026-07-22

Maintenance and quality release on top of **2.0.1**. No public API changes.

### Changed
- **Build:** added a **PMD + SpotBugs** quality gate (`-Pquality`); fixed resource leaks
  and a null-close bug surfaced by the gate.
- **Logging:** route swallowed `CloneNotSupportedException` to SLF4J; remove stray/dead
  debug prints. Note: `RePairGrammar.toGrammarRules()` now includes the `R0 -> ...` line
  in its return value instead of printing it to stdout — callers parsing that string see
  an extra first line (`toGrammarRulesData()` consumers are unaffected).

### Fixed
- **RePair** no longer crashes on empty, blank, or null input; **Sequitur** state is
  reset between runs.

### Docs
- README badge URLs updated (Maven Central, GitHub Actions CI, Codecov).

## [2.0.1] — 2026-07-09

Stack alignment release: depends on **jmotif-sax 2.0.1**; SLF4J **2.0.9** and Logback
**1.3.14** (addresses Dependabot serialization advisory on Logback 1.2.x). Published to
Maven Central.

## [2.0.0] — 2026-06-30

First release of the aligned 2.x line. See git tag `jmotif-gi-2.0.0` and
[jmotif-conformance](https://github.com/jMotif/jmotif-conformance) RePair golden tests.

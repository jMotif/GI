# Changelog

All notable changes to jmotif-gi are documented here.
This project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [2.0.2] — 2026-07-22

Maintenance and quality release on top of **2.0.1**. No public API changes.

### Changed
- **Build:** added a **PMD + SpotBugs** quality gate (`-Pquality`); fixed resource leaks
  and a null-close bug surfaced by the gate.
- **Logging:** route swallowed `CloneNotSupportedException` to SLF4J; remove stray/dead
  debug prints.

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

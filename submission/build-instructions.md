# Build Instructions

1. Install JDK 26.
2. Ensure `java -version` reports Java 26.
3. From the repository root, run:

```powershell
.\mvnw.cmd clean verify
```

The repository also includes `.github/workflows/java-26-verify.yml`, which runs the same verification command on `windows-latest` with Java 26.

The scaffold machine originally had Java 21 on PATH, so Java 26 verification must be rerun after installing or selecting JDK 26. The local workspace uses an ignored portable JDK 26 under `.jdk/` when present.

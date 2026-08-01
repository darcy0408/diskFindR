# Build Instructions

1. Install JDK 26.
2. Ensure `java -version` reports Java 26.
3. From the repository root, run:

```powershell
.\mvnw.cmd clean verify
```

The current scaffold machine had Java 21 on PATH, so Java 26 verification must be rerun after installing/selecting JDK 26.

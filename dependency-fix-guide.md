# 🔧 Fixing Dependency Annotation Issues

The red annotations you're seeing are because Maven dependencies haven't been resolved. Here are several solutions:

## 🛠️ Solution 1: Use IDE Built-in Maven (Recommended)

### For IntelliJ IDEA:
1. **Open Maven Tool Window**:
   - View → Tool Windows → Maven
   - OR click on the "Maven" tab on the right side

2. **Reload Project**:
   - Click the "Reload All Maven Projects" button (circular arrow icon)
   - Wait for dependencies to download

3. **Alternative Method**:
   - Right-click on `pom.xml`
   - Select "Maven" → "Reload project"

### For VS Code:
1. **Install Extension**:
   - Install "Maven for Java" extension
   - Install "Spring Boot Extension Pack"

2. **Reload Dependencies**:
   - Open Command Palette (Ctrl+Shift+P)
   - Type "Maven: Reload project"
   - Select your project

## 🛠️ Solution 2: Manual Maven Commands

If you can install Maven or use it from another location:

```bash
cd blog-backend
mvn clean compile
mvn dependency:resolve
```

## 🛠️ Solution 3: IDE-Specific Fixes

### IntelliJ IDEA Additional Steps:
1. **Invalidate Caches**:
   - File → Invalidate Caches and Restart
   - Select "Invalidate and Restart"

2. **Check Maven Settings**:
   - File → Settings → Build, Execution, Deployment → Build Tools → Maven
   - Ensure "Maven home directory" is set correctly
   - Check "User settings file" points to correct `settings.xml`

3. **Reimport Dependencies**:
   - File → Project Structure → Modules
   - Remove and re-add the module

### VS Code Additional Steps:
1. **Clean Java Language Server Workspace**:
   - Command Palette → "Java: Clean Java Language Server Workspace"
   - Restart VS Code

2. **Check Java Configuration**:
   - Ensure you have JDK 17 configured
   - Check `java.home` in settings

## 🛠️ Solution 4: Manual Dependency Download

If IDE solutions don't work, you can manually trigger dependency resolution:

1. **Check if Maven is bundled with your IDE**:
   - IntelliJ often includes bundled Maven
   - Look for `mvnw` (Maven Wrapper) in project root

2. **Use Maven Wrapper** (if available):
   ```bash
   ./mvnw clean compile
   ```

## 🔍 Common Issues and Fixes

### Issue 1: Network/Proxy Problems
```xml
<!-- Add to settings.xml if behind proxy -->
<proxies>
  <proxy>
    <id>example-proxy</id>
    <active>true</active>
    <protocol>http</protocol>
    <host>proxy.example.com</host>
    <port>8080</port>
  </proxy>
</proxies>
```

### Issue 2: Repository Mirrors
```xml
<!-- Add to settings.xml for faster downloads in China -->
<mirrors>
  <mirror>
    <id>aliyun</id>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

### Issue 3: Lombok Not Working
1. **Install Lombok Plugin** in your IDE
2. **Enable Annotation Processing**:
   - Settings → Build → Compiler → Annotation Processors
   - Check "Enable annotation processing"

## ✅ Verification Steps

After applying fixes, verify:

1. **Dependencies Resolved**:
   - Red annotations should disappear
   - Check if you can import classes like `@Autowired`, `@Service`, etc.

2. **Build Success**:
   - Try building the project
   - No compilation errors should appear

3. **Run Configuration**:
   - Try running the Spring Boot application
   - Check if it starts without dependency errors

## 🚨 Emergency Fix

If nothing works, try this:

1. **Delete Maven Local Repository**:
   ```bash
   rm -rf ~/.m2/repository
   ```

2. **Create New Project**:
   - Copy your source code to a new Spring Boot project
   - Let IDE re-download all dependencies

## 📞 Need More Help?

If you're still having issues:

1. **Check IDE logs** for Maven errors
2. **Verify internet connection** and proxy settings
3. **Try a different IDE** (IntelliJ Community Edition is free)
4. **Check firewall settings** that might block Maven repositories

The dependencies in your `pom.xml` are all standard and should resolve without issues once Maven can download them properly.

---

**Most likely solution**: Use your IDE's built-in Maven functionality to reload the project dependencies. This resolves 95% of red annotation issues.
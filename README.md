# TestLicense Niagara Modules

This repository contains custom Niagara modules for license management and UI extensions.

## Project Structure

- **testlicense/testlicense-rt/**: Runtime Java module for license logic.
- **testlicense/test-ux/**: UX/UI module, including JavaScript widgets and web servlets.
- **build.gradle.kts**, **settings.gradle.kts**: Root Gradle build configuration.
- **public.key**, **private.key**, **licenses.lic**: License and key files for cryptographic operations.

## Prerequisites

- **Java 8** or later (required for Niagara and Gradle)
- **Niagara 4.x** SDK and runtime (set `NIAGARA_HOME` environment variable)
- **Gradle** (or use the included `gradlew`/`gradlew.bat` wrapper)
- **Node.js** and **npm** (for JavaScript/UX development and testing)

## Environment Setup

1. **Set Niagara Home**  
   Edit your `gradle.properties` or set the environment variable:
   - Windows:  
     `set NIAGARA_HOME=C:\Niagara\Niagara-4.x.y.z`
   - Linux/macOS:  
     `export NIAGARA_HOME=/opt/Niagara-4.x.y.z`

2. **Install Node.js**  
   Download from [https://nodejs.org/](https://nodejs.org/).

## Building the Modules

From the project root, run:

```sh
./gradlew build
```
or on Windows:
```sh
gradlew.bat build
```

This will build all submodules and produce Niagara module files (`.jar`, `.mod`) in the respective `build/` directories.

## Deploying to a Niagara Station

1. Copy the built module files from `testlicense/testlicense-rt/build/libs/` and `testlicense/test-ux/build/libs/` to your Niagara station's `modules/` directory.
2. Restart the station.

## Running JavaScript/UX Development Tools

Navigate to the UX module directory:

```sh
cd testlicense/test-ux
npm install
npx grunt watch
```

- `grunt watch` will run JSHint and Karma tests on file changes.
- To run tests once:  
  ```sh
  npx grunt karma
  ```

## Module Details

### testlicense-rt

- Contains core Java classes for license validation and management.
- See [`testlicense/testlicense-rt/module-include.xml`](testlicense/testlicense-rt/module-include.xml) for exported types.

### test-ux

- Contains UI widgets and servlets.
- Example usage in JavaScript:
  ```javascript
  require(['/nmodule/test/rc/TestWidget'], function (TestWidget) {
    var widget = new TestWidget();
    widget.initialize($('#myWidgetGoesHere')).then(function () {
      return widget.load('my value');
    });
  });
  ```
- See [`testlicense/test-ux/README.md`](testlicense/test-ux/README.md) for more.

## Testing

- Java unit tests:  
  Run with Gradle (if present).
- JavaScript/UX tests:  
  Run with `npx grunt karma` in `testlicense/test-ux`.

## License and Keys

- `public.key`, `private.key`, and `licenses.lic` are used for license cryptography.
- Do **not** commit sensitive keys to public repositories.

## References

- [Niagara Developer Documentation](https://www.niagara-community.com/)
- [Gradle Documentation](https://docs.gradle.org/)
- [Node.js Documentation](https://nodejs.org/en/docs/)

---

For questions, see the module-specific README files or contact the module author.

# test

A remarkable Niagara module brought to you by vaish.

#### ui



```javascript
 // Current page URL
console.log("Current URL:", window.location.href);
const currentUrl = new URL(window.location.href);

// Split and filter path segments
const pathSegments = currentUrl.pathname.split('/');
console.log("Original path segments:", pathSegments);

// Remove empty segments, 'module', and 'rc'
const filteredSegments = pathSegments.filter(segment =>
  segment !== 'module' && segment !== 'rc' && segment !== ''
);
console.log("Filtered segments:", filteredSegments);

// Use first filtered segment as module name
const moduleName = filteredSegments[0]; // e.g., "test"
console.log("Module name:", moduleName);

// Final clean URL
const cleanUrl = `/${moduleName}/LicenseCheckServlet`;
console.log("Final clean URL:", cleanUrl);

// Fetch with credentials
fetch(cleanUrl, { credentials: 'include' })
  .then(response => {
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
  })
  .then(data => {
    console.log("Received data:", data);
    document.getElementById("message").textContent =
      data.status === "verified" ? "✓ " + data.message : "✗ " + data.message;
  })
  .catch(error => {
    console.error("Full error:", error);
    document.getElementById("message").textContent = `Error: ${error.message}`;
  });

```



## Building

This module builds with Gradle. The Gradle build will perform r.js optimization,
minification, and JSDoc generation. Just type: `gradlew :test:build`

## Development

You can do development on this module with the help of Grunt. Just cd into
the module directory and type `grunt watch` to begin; JSHint and Karma tests
will be run on every file save. You can also type `grunt jshint:src` and
`grunt karma` just to run them once.

Just type `grunt` for a listing of all options.

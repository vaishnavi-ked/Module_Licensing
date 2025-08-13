// Current page URL
console.log("Current URL:", window.location.href);
var currentUrl = new URL(window.location.href);

// Split and filter path segments
var pathSegments = currentUrl.pathname.split('/');
console.log("Original path segments:", pathSegments);

// Remove empty segments, 'module', and 'rc'
var filteredSegments = pathSegments.filter(function (segment) {
  return segment !== 'module' && segment !== 'rc' && segment !== '';
});
console.log("Filtered segments:", filteredSegments);

// Use first filtered segment as module name
var moduleName = filteredSegments[0]; // e.g., "test"
console.log("Module name:", moduleName);

// Final clean URL
var cleanUrl = "/".concat(moduleName, "/LicenseCheckServlet");
console.log("Final clean URL:", cleanUrl);

// Fetch with credentials
fetch(cleanUrl, {
  credentials: 'include'
}).then(function (response) {
  if (!response.ok) throw new Error("HTTP ".concat(response.status));
  return response.json();
}).then(function (data) {
  console.log("Received data:", data);
  document.getElementById("message").textContent = data.status === "verified" ? "✓ " + data.message : "✗ " + data.message;
})["catch"](function (error) {
  console.error("Full error:", error);
  document.getElementById("message").textContent = "Error: ".concat(error.message);
});

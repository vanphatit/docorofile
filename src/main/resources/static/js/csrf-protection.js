/**
 * CSRF Protection Utility
 * Automatically handles CSRF tokens for AJAX requests and forms
 * Updated to work with HttpOnly cookies
 */

// Function to get CSRF token from meta tag (primary method when using HttpOnly cookies)
function getCsrfTokenFromMeta() {
  const csrfToken = document.querySelector('meta[name="_csrf"]');
  return csrfToken ? csrfToken.getAttribute("content") : null;
}

// Function to get CSRF token from cookie (fallback method)
function getCsrfToken() {
  const name = "XSRF-TOKEN";
  const value = "; " + document.cookie;
  const parts = value.split("; " + name + "=");
  if (parts.length === 2) {
    return parts.pop().split(";").shift();
  }
  return null;
}

// Function to get CSRF header name
function getCsrfHeaderName() {
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
  return csrfHeader ? csrfHeader.getAttribute("content") : "X-XSRF-TOKEN";
}

// Function to get CSRF parameter name
function getCsrfParameterName() {
  const csrfParam = document.querySelector('meta[name="_csrf_parameter"]');
  return csrfParam ? csrfParam.getAttribute("content") : "_csrf";
}

// Setup AJAX requests to include CSRF token
function setupCsrfForAjax() {
  // For jQuery AJAX requests
  if (typeof $ !== "undefined") {
    $.ajaxSetup({
      beforeSend: function (xhr, settings) {
        if (
          !/^(GET|HEAD|OPTIONS|TRACE)$/i.test(settings.type) &&
          !this.crossDomain
        ) {
          const token = getCsrfTokenFromMeta() || getCsrfToken();
          if (token) {
            xhr.setRequestHeader(getCsrfHeaderName(), token);
          }
        }
      },
    });
  }

  // For Fetch API requests
  const originalFetch = window.fetch;
  window.fetch = function (url, options = {}) {
    if (
      !options.method ||
      !/^(GET|HEAD|OPTIONS|TRACE)$/i.test(options.method)
    ) {
      const token = getCsrfTokenFromMeta() || getCsrfToken();
      if (token) {
        options.headers = options.headers || {};
        options.headers[getCsrfHeaderName()] = token;
      }
    }
    return originalFetch(url, options);
  };
}

// Function to add CSRF token to a form dynamically
function addCsrfToForm(form) {
  if (form && form.method && form.method.toLowerCase() === "post") {
    const paramName = getCsrfParameterName();
    const existingCsrfInput = form.querySelector(`input[name="${paramName}"]`);
    if (!existingCsrfInput) {
      const token = getCsrfTokenFromMeta() || getCsrfToken();
      if (token) {
        const csrfInput = document.createElement("input");
        csrfInput.type = "hidden";
        csrfInput.name = paramName;
        csrfInput.value = token;
        form.appendChild(csrfInput);
      }
    }
  }
}

// Initialize CSRF protection when DOM is ready
document.addEventListener("DOMContentLoaded", function () {
  setupCsrfForAjax();

  // Add CSRF tokens to any dynamically created forms
  const observer = new MutationObserver(function (mutations) {
    mutations.forEach(function (mutation) {
      mutation.addedNodes.forEach(function (node) {
        if (node.nodeType === 1) {
          // Element node
          if (node.tagName === "FORM") {
            addCsrfToForm(node);
          } else {
            const forms = node.querySelectorAll
              ? node.querySelectorAll("form")
              : [];
            forms.forEach(addCsrfToForm);
          }
        }
      });
    });
  });

  observer.observe(document.body, { childList: true, subtree: true });
});

// Export functions for manual use
window.CsrfProtection = {
  getCsrfToken: getCsrfToken,
  getCsrfTokenFromMeta: getCsrfTokenFromMeta,
  getCsrfHeaderName: getCsrfHeaderName,
  getCsrfParameterName: getCsrfParameterName,
  addCsrfToForm: addCsrfToForm,
  setupCsrfForAjax: setupCsrfForAjax,
};

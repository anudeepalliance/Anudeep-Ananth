(function () {
  var THINKING_STEP_MS = 2000;

  function prefersReducedMotion() {
    return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  }

  function revealMotionSections() {
    var motionSections = document.querySelectorAll("[data-motion]");
    if (!motionSections.length) return;
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (!entry.isIntersecting) return;
          entry.target.classList.add("is-in-view");
          observer.unobserve(entry.target);
        });
      },
      { threshold: 0.12, rootMargin: "0px 0px -8% 0px" },
    );
    motionSections.forEach(function (motionSection) {
      observer.observe(motionSection);
    });
  }

  function thinkingLinesFrom(indicator) {
    return Array.prototype.map.call(
      indicator.querySelectorAll(".thinking-steps li"),
      function (stepItem) {
        return stepItem.textContent.trim();
      },
    ).filter(Boolean);
  }

  function startThinkingCycle(indicator) {
    var statusNode = indicator.querySelector(".thinking-status");
    var thinkingLines = thinkingLinesFrom(indicator);
    if (!statusNode || thinkingLines.length === 0) return;
    stopThinkingCycle(indicator);
    var lineIndex = 0;
    statusNode.textContent = thinkingLines[0];
    statusNode.classList.add("is-tick");
    indicator._thinkingTimer = window.setInterval(function () {
      if (lineIndex >= thinkingLines.length - 1) {
        window.clearInterval(indicator._thinkingTimer);
        indicator._thinkingTimer = null;
        return;
      }
      lineIndex += 1;
      statusNode.classList.remove("is-tick");
      void statusNode.offsetWidth;
      statusNode.textContent = thinkingLines[lineIndex];
      statusNode.classList.add("is-tick");
    }, THINKING_STEP_MS);
  }

  function stopThinkingCycle(indicator) {
    if (indicator._thinkingTimer) {
      window.clearInterval(indicator._thinkingTimer);
      indicator._thinkingTimer = null;
    }
    var statusNode = indicator.querySelector(".thinking-status");
    var thinkingLines = thinkingLinesFrom(indicator);
    if (statusNode && thinkingLines[0]) {
      statusNode.textContent = thinkingLines[0];
      statusNode.classList.remove("is-tick");
    }
  }

  function indicatorFromHtmxEvent(event) {
    var sourceElement = event.detail && event.detail.elt;
    if (!sourceElement) return null;
    var indicatorSelector = sourceElement.getAttribute("hx-indicator");
    if (!indicatorSelector) return null;
    return document.querySelector(indicatorSelector);
  }

  function bindThinkingCycle() {
    document.body.addEventListener("htmx:beforeRequest", function (event) {
      var indicator = indicatorFromHtmxEvent(event);
      if (indicator) startThinkingCycle(indicator);
    });
    document.body.addEventListener("htmx:afterRequest", function (event) {
      var indicator = indicatorFromHtmxEvent(event);
      if (indicator) stopThinkingCycle(indicator);
    });
  }

  if (prefersReducedMotion()) {
    bindThinkingCycle();
    return;
  }

  document.documentElement.classList.add("motion-on");
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", revealMotionSections);
  } else {
    revealMotionSections();
  }
  bindThinkingCycle();
})();

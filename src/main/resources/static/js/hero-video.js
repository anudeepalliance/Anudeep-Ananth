(function () {
  var videoFrame = document.querySelector(".hero-video-frame");
  if (!videoFrame) return;
  var introVideo = videoFrame.querySelector("video");
  var muteToggleButton = videoFrame.querySelector(".hero-video-mute");
  var seekBar = videoFrame.querySelector(".hero-video-seek");
  var replayOverlayButton = videoFrame.querySelector(".hero-video-replay");
  var replayControlButton = videoFrame.querySelector(".hero-video-replay-control");
  if (!introVideo || !muteToggleButton || !seekBar) return;

  introVideo.removeAttribute("loop");
  introVideo.loop = false;
  introVideo.muted = true;
  muteToggleButton.classList.add("is-muted");
  muteToggleButton.setAttribute("aria-label", "Unmute intro video");

  function seekBarMaximum() {
    return Number(seekBar.max) || 1000;
  }

  function showReplayOverlay() {
    videoFrame.classList.add("is-ended");
    if (replayOverlayButton) replayOverlayButton.removeAttribute("hidden");
  }

  function hideReplayOverlay() {
    videoFrame.classList.remove("is-ended");
  }

  function playIntroFromStart() {
    hideReplayOverlay();
    introVideo.currentTime = 0;
    var playPromise = introVideo.play();
    if (playPromise && playPromise.catch) playPromise.catch(function () {});
  }

  function syncSeekBarToPlayback() {
    if (!introVideo.duration) return;
    if (seekBar.dataset.isDragging === "true") return;
    seekBar.value = String(
      Math.round((introVideo.currentTime / introVideo.duration) * seekBarMaximum()),
    );
  }

  function applySeekBarToVideo() {
    if (!introVideo.duration) return;
    introVideo.currentTime =
      (Number(seekBar.value) / seekBarMaximum()) * introVideo.duration;
    if (introVideo.ended || introVideo.currentTime >= introVideo.duration - 0.05) {
      showReplayOverlay();
      return;
    }
    hideReplayOverlay();
    var playPromise = introVideo.play();
    if (playPromise && playPromise.catch) playPromise.catch(function () {});
  }

  muteToggleButton.addEventListener("click", function () {
    introVideo.muted = !introVideo.muted;
    muteToggleButton.classList.toggle("is-muted", introVideo.muted);
    muteToggleButton.setAttribute(
      "aria-label",
      introVideo.muted ? "Unmute intro video" : "Mute intro video",
    );
    if (!introVideo.ended) {
      var playPromise = introVideo.play();
      if (playPromise && playPromise.catch) playPromise.catch(function () {});
    }
  });

  [replayOverlayButton, replayControlButton].forEach(function (replayButton) {
    if (!replayButton) return;
    replayButton.addEventListener("click", function (clickEvent) {
      clickEvent.stopPropagation();
      playIntroFromStart();
    });
  });

  seekBar.addEventListener("pointerdown", function () {
    seekBar.dataset.isDragging = "true";
  });
  window.addEventListener("pointerup", function () {
    seekBar.dataset.isDragging = "false";
  });
  seekBar.addEventListener("input", applySeekBarToVideo);
  seekBar.addEventListener("change", applySeekBarToVideo);

  introVideo.addEventListener("timeupdate", syncSeekBarToPlayback);
  introVideo.addEventListener("loadedmetadata", syncSeekBarToPlayback);
  introVideo.addEventListener("ended", function () {
    syncSeekBarToPlayback();
    showReplayOverlay();
  });
  introVideo.addEventListener("play", hideReplayOverlay);
})();

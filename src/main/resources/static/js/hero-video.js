(function () {
  var videoFrame = document.querySelector(".hero-video-frame");
  if (!videoFrame) return;
  var introVideo = videoFrame.querySelector("video");
  var muteToggleButton = videoFrame.querySelector(".hero-video-mute");
  var seekBar = videoFrame.querySelector(".hero-video-seek");
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

  function catchPlayPromise(playPromise) {
    if (playPromise && playPromise.catch) playPromise.catch(function () {});
  }

  function syncPlaybackChrome() {
    var isEnded = introVideo.ended;
    var isPaused = introVideo.paused && !isEnded;
    videoFrame.classList.toggle("is-ended", isEnded);
    videoFrame.classList.toggle("is-paused", isPaused);
    videoFrame.setAttribute(
      "aria-label",
      isEnded ? "Replay intro" : isPaused ? "Play intro" : "Pause intro",
    );
  }

  function playIntroFromStart() {
    introVideo.currentTime = 0;
    catchPlayPromise(introVideo.play());
  }

  function togglePlaybackFromTap() {
    if (introVideo.ended) {
      playIntroFromStart();
      return;
    }
    if (introVideo.paused) {
      catchPlayPromise(introVideo.play());
      return;
    }
    introVideo.pause();
  }

  function isClickOnPlaybackControls(clickTarget) {
    return Boolean(clickTarget.closest(".hero-video-controls"));
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
      introVideo.pause();
      syncPlaybackChrome();
      return;
    }
    catchPlayPromise(introVideo.play());
  }

  muteToggleButton.addEventListener("click", function (clickEvent) {
    clickEvent.stopPropagation();
    introVideo.muted = !introVideo.muted;
    muteToggleButton.classList.toggle("is-muted", introVideo.muted);
    muteToggleButton.setAttribute(
      "aria-label",
      introVideo.muted ? "Unmute intro video" : "Mute intro video",
    );
  });

  videoFrame.addEventListener("click", function (clickEvent) {
    if (isClickOnPlaybackControls(clickEvent.target)) return;
    togglePlaybackFromTap();
  });

  videoFrame.addEventListener("keydown", function (keyEvent) {
    if (keyEvent.target !== videoFrame) return;
    if (keyEvent.key !== "Enter" && keyEvent.key !== " ") return;
    keyEvent.preventDefault();
    togglePlaybackFromTap();
  });

  if (replayControlButton) {
    replayControlButton.addEventListener("click", function (clickEvent) {
      clickEvent.stopPropagation();
      playIntroFromStart();
    });
  }

  seekBar.addEventListener("pointerdown", function (pointerEvent) {
    pointerEvent.stopPropagation();
    seekBar.dataset.isDragging = "true";
  });
  window.addEventListener("pointerup", function () {
    seekBar.dataset.isDragging = "false";
  });
  seekBar.addEventListener("input", applySeekBarToVideo);
  seekBar.addEventListener("change", applySeekBarToVideo);
  seekBar.addEventListener("click", function (clickEvent) {
    clickEvent.stopPropagation();
  });

  introVideo.addEventListener("timeupdate", syncSeekBarToPlayback);
  introVideo.addEventListener("loadedmetadata", syncSeekBarToPlayback);
  introVideo.addEventListener("play", syncPlaybackChrome);
  introVideo.addEventListener("pause", syncPlaybackChrome);
  introVideo.addEventListener("ended", function () {
    syncSeekBarToPlayback();
    syncPlaybackChrome();
  });
  syncPlaybackChrome();
})();

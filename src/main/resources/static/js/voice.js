(function () {
  var MAX_RECORDING_MS = 25000;
  var MIN_AUDIO_BYTES = 800;
  var activeRecordingSession = null;

  function setStatus(message) {
    document.querySelectorAll(".voice-status").forEach(function (statusNode) {
      statusNode.textContent = message || "";
    });
  }

  function stripCodecsFromMimeType(mimeType) {
    return (mimeType || "audio/webm").split(";")[0].trim() || "audio/webm";
  }

  function preferredRecorderMimeType() {
    var mimeTypeCandidates = [
      "audio/webm",
      "audio/webm;codecs=opus",
      "audio/mp4",
      "audio/ogg;codecs=opus",
    ];
    for (var index = 0; index < mimeTypeCandidates.length; index += 1) {
      if (window.MediaRecorder && MediaRecorder.isTypeSupported(mimeTypeCandidates[index])) {
        return mimeTypeCandidates[index];
      }
    }
    return "";
  }

  function fileExtensionForMimeType(mimeType) {
    var baseType = stripCodecsFromMimeType(mimeType);
    if (baseType.indexOf("mp4") !== -1 || baseType.indexOf("m4a") !== -1) return "m4a";
    if (baseType.indexOf("ogg") !== -1) return "ogg";
    if (baseType.indexOf("mpeg") !== -1 || baseType.indexOf("mp3") !== -1) return "mp3";
    if (baseType.indexOf("wav") !== -1) return "wav";
    return "webm";
  }

  function setRecordingButtonState(recordButton, isRecording) {
    var idleLabel = recordButton.getAttribute("data-idle-label") || "Voice input";
    recordButton.classList.toggle("is-recording", isRecording);
    recordButton.setAttribute("aria-pressed", isRecording ? "true" : "false");
    var labelNode = recordButton.querySelector(".voice-record-label");
    if (labelNode) {
      labelNode.textContent = isRecording ? "Stop recording" : idleLabel;
    }
    if (!isRecording) {
      recordButton.style.removeProperty("--voice-level");
    }
  }

  function startVoiceLevelMeter(mediaStream, recordButton) {
    var AudioContextConstructor = window.AudioContext || window.webkitAudioContext;
    if (!AudioContextConstructor) {
      return function () {};
    }
    var audioContext = new AudioContextConstructor();
    var mediaStreamSource = audioContext.createMediaStreamSource(mediaStream);
    var analyserNode = audioContext.createAnalyser();
    analyserNode.fftSize = 256;
    mediaStreamSource.connect(analyserNode);
    var timeDomainSamples = new Uint8Array(analyserNode.fftSize);
    var animationFrameId = 0;
    var smoothedLevel = 0;

    function tickVoiceLevel() {
      analyserNode.getByteTimeDomainData(timeDomainSamples);
      var sumOfSquares = 0;
      for (var sampleIndex = 0; sampleIndex < timeDomainSamples.length; sampleIndex += 1) {
        var centeredSample = (timeDomainSamples[sampleIndex] - 128) / 128;
        sumOfSquares += centeredSample * centeredSample;
      }
      var rootMeanSquare = Math.sqrt(sumOfSquares / timeDomainSamples.length);
      var instantLevel = Math.min(1, rootMeanSquare * 4.2);
      smoothedLevel = smoothedLevel * 0.55 + instantLevel * 0.45;
      recordButton.style.setProperty("--voice-level", smoothedLevel.toFixed(3));
      animationFrameId = window.requestAnimationFrame(tickVoiceLevel);
    }
    if (audioContext.state === "suspended" && audioContext.resume) {
      audioContext.resume();
    }
    tickVoiceLevel();

    return function stopVoiceLevelMeter() {
      window.cancelAnimationFrame(animationFrameId);
      mediaStreamSource.disconnect();
      analyserNode.disconnect();
      audioContext.close();
    };
  }

  function stopActiveRecordingSession() {
    if (!activeRecordingSession) return;
    activeRecordingSession.stop();
  }

  function startRecordingIntoTextarea(recordButton, textarea) {
    if (activeRecordingSession) {
      stopActiveRecordingSession();
      return;
    }
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      setStatus("This browser cannot record audio — type instead.");
      return;
    }

    navigator.mediaDevices.getUserMedia({ audio: true }).then(function (mediaStream) {
      var recorderMimeType = preferredRecorderMimeType();
      var mediaRecorder = recorderMimeType
        ? new MediaRecorder(mediaStream, { mimeType: recorderMimeType })
        : new MediaRecorder(mediaStream);
      var recordedChunks = [];
      var stopLevelMeter = startVoiceLevelMeter(mediaStream, recordButton);
      var maxDurationTimer = window.setTimeout(function () {
        if (mediaRecorder.state === "recording") {
          mediaRecorder.stop();
        }
      }, MAX_RECORDING_MS);

      mediaRecorder.ondataavailable = function (event) {
        if (event.data && event.data.size > 0) {
          recordedChunks.push(event.data);
        }
      };

      function releaseMicrophone() {
        window.clearTimeout(maxDurationTimer);
        stopLevelMeter();
        mediaStream.getTracks().forEach(function (track) {
          track.stop();
        });
        setRecordingButtonState(recordButton, false);
        activeRecordingSession = null;
      }

      mediaRecorder.onerror = function () {
        releaseMicrophone();
        setStatus("Voice input failed — type instead.");
      };

      mediaRecorder.onstop = function () {
        var blobType = stripCodecsFromMimeType(mediaRecorder.mimeType || recorderMimeType || "audio/webm");
        var audioBlob = new Blob(recordedChunks, { type: blobType });
        releaseMicrophone();
        if (audioBlob.size < MIN_AUDIO_BYTES) {
          setStatus("That clip was too short — hold Stop recording after you speak.");
          return;
        }
        transcribeBlobIntoTextarea(audioBlob, textarea);
      };

      activeRecordingSession = {
        stop: function () {
          if (mediaRecorder.state === "recording") {
            mediaRecorder.stop();
          }
        },
      };
      setRecordingButtonState(recordButton, true);
      setStatus("Recording… tap Stop recording when you are done.");
      mediaRecorder.start(250);
    }).catch(function () {
      setStatus("Microphone permission is needed — type instead, or allow the mic and retry.");
    });
  }

  async function transcribeBlobIntoTextarea(audioBlob, textarea) {
    setStatus("Transcribing…");
    try {
      var formData = new FormData();
      var fileName = "voice." + fileExtensionForMimeType(audioBlob.type);
      formData.append("file", audioBlob, fileName);
      var activeAgent = document.querySelector("[data-agent-id].is-active");
      if (activeAgent) {
        formData.append("agentId", activeAgent.getAttribute("data-agent-id"));
      }
      var response = await fetch("/htmx/voice/stt", { method: "POST", body: formData });
      if (!response.ok) {
        setStatus("Voice input failed — type instead.");
        return;
      }
      var transcript = (await response.text()).trim();
      if (!transcript) {
        setStatus("No speech heard — try again a little closer to the mic.");
        return;
      }
      textarea.value = (textarea.value ? textarea.value + " " : "") + transcript;
      textarea.dispatchEvent(new Event("input", { bubbles: true }));
      setStatus("Transcript added.");
    } catch (error) {
      setStatus("Voice input failed — type instead.");
    }
  }

  async function speakText(text) {
    if (!text) {
      setStatus("Nothing to speak yet.");
      return;
    }
    setStatus("Generating speech…");
    var params = new URLSearchParams();
    params.set("text", text);
    var activeAgent = document.querySelector("[data-agent-id].is-active");
    if (activeAgent) params.set("agentId", activeAgent.getAttribute("data-agent-id"));
    try {
      var response = await fetch("/htmx/voice/tts", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params.toString(),
      });
      if (!response.ok) {
        setStatus("Spoken reply unavailable — read the text instead.");
        return;
      }
      var audioBlob = await response.blob();
      var audioUrl = URL.createObjectURL(audioBlob);
      var audio = new Audio(audioUrl);
      audio.play();
      setStatus("Playing spoken reply.");
    } catch (error) {
      setStatus("Spoken reply unavailable — read the text instead.");
    }
  }

  document.addEventListener("click", function (event) {
    var recordButton = event.target.closest("[data-voice-jd], [data-voice-chat]");
    if (recordButton) {
      event.preventDefault();
      var textareaSelector = recordButton.hasAttribute("data-voice-jd")
        ? "#job-matching-form textarea"
        : "#agent-chat-form textarea";
      var textarea = document.querySelector(textareaSelector);
      if (textarea) {
        startRecordingIntoTextarea(recordButton, textarea);
      }
      return;
    }
    var speakReply = event.target.closest("[data-speak-reply]");
    if (speakReply) {
      var output = document.querySelector("#agent-chat-output") || document.querySelector("#job-match-output");
      speakText(output ? output.innerText : "");
    }
  });
})();

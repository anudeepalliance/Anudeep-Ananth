package web

import kotlinx.html.HTMLTag
import kotlinx.html.unsafe

/**
 * Small inline icons for buttons. Color follows the button text.
 */
internal fun HTMLTag.renderButtonIcon(kind: ButtonIconKind) {
    unsafe {
        +kind.svgMarkup
    }
}

internal enum class ButtonIconKind(val svgMarkup: String) {
    SEND(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3.2 20.8 21 12 3.2 3.2v7.1L14.3 12 3.2 13.7z"/></svg>""",
    ),
    MICROPHONE(
        """<svg class="btn-icon voice-icon-mic" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3.2a3.2 3.2 0 0 1 3.2 3.2v5.2a3.2 3.2 0 1 1-6.4 0V6.4A3.2 3.2 0 0 1 12 3.2zm7.2 8.4a7.2 7.2 0 0 1-14.4 0H3.2a8.8 8.8 0 0 0 8 8.76V22h1.6v-1.64a8.8 8.8 0 0 0 8-8.76z"/></svg>""",
    ),
    STOP_RECORDING(
        """<svg class="btn-icon voice-icon-stop" viewBox="0 0 24 24" aria-hidden="true"><rect x="6.4" y="6.4" width="11.2" height="11.2" rx="2"/></svg>""",
    ),
    SPEAKER(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4.4 9.2v5.6h3.4L13 19.2V4.8L7.8 9.2H4.4zm10.9-1a5 5 0 0 1 0 7.6M17.8 5.6a8.2 8.2 0 0 1 0 12.8" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>""",
    ),
    MAIL(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3.2 6.4A2.4 2.4 0 0 1 5.6 4h12.8a2.4 2.4 0 0 1 2.4 2.4v11.2a2.4 2.4 0 0 1-2.4 2.4H5.6a2.4 2.4 0 0 1-2.4-2.4zm2.4.8 6.4 4.4 6.4-4.4H5.6zm0 1.9V18.4h12.8V9.1L12 13.4z"/></svg>""",
    ),
    LINKEDIN(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4.98 3.5A2.48 2.48 0 1 1 2.5 5.98 2.48 2.48 0 0 1 4.98 3.5zM3 8.5h4v12H3zm6.5 0h3.8v1.64h.05c.53-1 1.84-2.06 3.78-2.06 4.04 0 4.79 2.66 4.79 6.12V20.5h-4v-5.4c0-1.29-.02-2.95-1.8-2.95-1.8 0-2.08 1.4-2.08 2.85v5.5h-4z"/></svg>""",
    ),
    SEARCH(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M10.4 3.6a6.8 6.8 0 1 1 0 13.6 6.8 6.8 0 0 1 0-13.6zm0 1.6a5.2 5.2 0 1 0 0 10.4 5.2 5.2 0 0 0 0-10.4zM15.7 16.1l4.6 4.6 1.1-1.1-4.6-4.6z"/></svg>""",
    ),
    REPLAY(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5V2L7.5 6.5 12 11V8a4 4 0 1 1-4 4H6a6 6 0 1 0 6-6z"/></svg>""",
    ),
    PLAY(
        """<svg class="btn-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 5.2v13.6L19 12z"/></svg>""",
    ),
}

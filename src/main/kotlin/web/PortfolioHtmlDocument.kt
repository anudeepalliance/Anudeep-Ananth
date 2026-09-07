package web

import knowledge.ContactDetails
import knowledge.HomepageSalesPitch
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe

/**
 * Full HTML document shell for the portfolio homepage.
 * Content is visible on first byte — no opacity:0 preloader.
 */
fun HTML.renderPortfolioHtmlDocument() {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        meta(name = "description", content = HomepageSalesPitch.PARAGRAPH.take(160))
        title("${ContactDetails.FULL_NAME} — AI-native lifecycle & demand gen")
        link(
            rel = "stylesheet",
            href = "https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400;500&family=IBM+Plex+Sans:wght@400;500;600&family=Syne:wght@600;700;800&display=swap",
        )
        link(rel = "stylesheet", href = "/static/css/lab.css")
        script {
            src = "https://unpkg.com/htmx.org@2.0.4"
            attributes["defer"] = "true"
        }
    }
    body {
        a(href = "#hook", classes = "skip-link") { +"Skip to content" }
        div(classes = "site-shell") {
            renderPortfolioPageBody()
        }
        script {
            // Tiny client helper: mark selected agent card active (full swap in Phase 5).
            unsafe {
                +"""
                document.addEventListener('click', function (event) {
                  var card = event.target.closest('[data-agent-id]');
                  if (!card) return;
                  document.querySelectorAll('[data-agent-id]').forEach(function (node) {
                    node.classList.toggle('is-active', node === card);
                  });
                });
                """.trimIndent()
            }
        }
    }
}

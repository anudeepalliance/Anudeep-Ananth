package com

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerTest {

    @Test
    fun `homepage renders portfolio shell from knowledge pack`() = testApplication {
        application {
            configureRouting()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        val html = response.bodyAsText()
        assertTrue(html.contains("Anudeep Ananth"))
        assertTrue(html.contains("Paste a JD"))
        assertTrue(html.contains("Job Matching"))
        assertTrue(html.contains("anudeeps3@gmail.com"))
        assertTrue(html.contains("/static/css/lab.css"))
        assertTrue(!html.contains("opacity:0"))
    }

    @Test
    fun `job match htmx stub responds`() = testApplication {
        application {
            configureRouting()
        }
        val response = client.post("/htmx/job-match")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Job Matching stub"))
    }
}

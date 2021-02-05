import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Method.*
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.format.Jackson.auto
import org.http4k.format.Jackson.json
import org.http4k.format.Jackson.asJsonObject



fun main() {
    data class Input(val input: String)
    data class CountResponse(val count: Int)
    val inputLens = Body.auto<Input>().toLens()
    val countResponseLens = Body.auto<CountResponse>().toLens()
    val jsonLens = Body.json().toLens()

    val analyze = {request: Request ->
        val body = inputLens(request)
        val result = body.input
            .toList()
            .filter({it != ' '})
            .fold(mapOf<Char, Int>()) { acc, c: Char ->
                when(val count = acc[c]) {
                    null -> acc.plus(Pair(c, 1))
                    else -> acc.plus(Pair(c, count + 1))
                }
        }
        println(result.asJsonObject())
        jsonLens.inject(result.asJsonObject(), Response(OK))
    }
    val count = {request: Request ->
        val body = inputLens(request)
        val count = body.input.split(" ").count()
        val response = CountResponse(count)
        countResponseLens.inject(response, Response(OK))
    }

    val app = routes(
        "/analyze" bind POST to analyze,
        "/count" bind POST to count,
    )
    app.asServer(Netty(8080)).start()
    println("Started server on port: 8080")
}


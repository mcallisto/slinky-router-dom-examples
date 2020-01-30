package demo

import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._

import typings.reactRouter.mod.RouteProps
import typings.reactRouterDom.components.{BrowserRouter, Link, Route}

@react object App {

  type Props = Unit

  def home = div(h2("Home"))

  def about = div(h2("About"))

  def dashboard = div(h2("Dashboard"))

  val component = FunctionalComponent[Props] { _ =>
    val renderIntro = div(
      header(className := "App-header")(h1(className := "App-title")("Welcome to React (with Scala.js!)")),
      p(className := "App-intro")("To get started, edit ", code("App.scala"), " and save to reload.")
    )

    div(className := "App")(
      renderIntro,
      BrowserRouter(
        div(
          ul(
            li(Link[String](to = "/")("Home")),
            li(Link[String](to = "/about")("About")),
            li(Link[String](to = "/dashboard")("Dashboard"))
          ),
          hr(),
          Route(RouteProps(exact = true, path = "/", render = _ => home)),
          Route(RouteProps(path = "/about", render = _ => about)),
          Route(RouteProps(path = "/dashboard", render = _ => dashboard))
        )
      )
    )
  }
}

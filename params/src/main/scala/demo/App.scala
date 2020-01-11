package demo

import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._

import typingsSlinky.reactDashRouterDashDom.components.{BrowserRouter, Link, Route, Switch}
import typingsSlinky.reactDashRouter.reactDashRouterMod.{`match`, RouteProps}

import scala.scalajs.js

@react object App {

  type Props = Unit

  val component = FunctionalComponent[Props] { _ =>
    val renderIntro = div(
      header(className := "App-header")(h1(className := "App-title")("Welcome to React (with Scala.js!)")),
      p(className := "App-intro")("To get started, edit ", code("App.scala"), " and save to reload.")
    )

    div(className := "App")(
      renderIntro,
      BrowserRouter(
        div(
          h2("Accounts"),
          ul(
            li(Link[String](to = "/netflix")("Netflix")),
            li(Link[String](to = "/zillow-group")("Zillow Group")),
            li(Link[String](to = "/yahoo")("Yahoo")),
            li(Link[String](to = "/modus-create")("Modus Create"))
          ),
          Switch(
            Route(RouteProps(path = "/:id", render = p => Child(p.`match`.asInstanceOf[`match`[Child.Param]])))
          )
        )
      )
    )
  }
}

@react object Child {

  @js.native
  trait Param extends js.Object {
    val id: String = js.native
  }

  case class Props(`match`: `match`[Child.Param])

  val component = FunctionalComponent[Props] { props =>
    div(
      h3("ID: " + props.`match`.params.id)
    )
  }

}

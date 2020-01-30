package demo

import slinky.core._
import slinky.core.annotations.react
import slinky.core.facade.Hooks._
import slinky.core.facade.ReactElement
import slinky.web.html._

import typings.reactRouter.mod.RouteProps
import typings.reactRouterDom.components.{BrowserRouter, Link, Redirect, Route, Switch}

import scala.scalajs.js
import org.scalajs.dom.window.setTimeout

@react object App {

  type Props = Unit

  def publicPage = h3("Public")

  def protectedPage = h3("Protected")

  val component = FunctionalComponent[Props] { _ =>

    val (isAuthenticated, updateIsAuthenticated) = useState(false)

    val renderIntro = div(
      header(className := "App-header")(h1(className := "App-title")("Welcome to React (with Scala.js!)")),
      p(className := "App-intro")("To get started, edit ", code("App.scala"), " and save to reload.")
    )

    def updateWithAsync(isAuthenticated: Boolean, handler: () => Any): Unit = {
      updateIsAuthenticated(isAuthenticated)
      setTimeout(handler, 100) // fake async
      ()
    }

    def authenticate(handler: () => Any) =
      updateWithAsync(true, handler)

    def signout(handler: () => Any) =
      updateWithAsync(false, handler)

    def authButton() =
      if (isAuthenticated)
        p(
          "Welcome! ",
          button(onClick := (_ => signout(() => ())))("Sign out")
        )
      else
        p("You are not logged in.")

    def loginPage = div(
      p("You must log in to view the page at {from.pathname}"),
      button(onClick := (_ => authenticate(() => ())))("Log in")
    )

    div(className := "App")(
      renderIntro,
      BrowserRouter(
        div(
          authButton(),
          ul(
            li(Link[js.Object](to = "/public")("Public Page")),
            li(Link[js.Object](to = "/protected")("Protected Page"))
          ),
          Switch(
            Route(RouteProps(path = "/public", render = _ => publicPage)),
            Route(RouteProps(path = "/login", render = _ => loginPage)),
            PrivateRoute(path = "/protected", render = _ => protectedPage, isAuthenticated = isAuthenticated)
          )
        )
      )
    )
  }
}

@react object PrivateRoute {

  case class Props(path: String, render: Any => ReactElement, isAuthenticated: Boolean)

  val component = FunctionalComponent[Props] { props =>

    if (props.isAuthenticated)
      Route(RouteProps(path = props.path, render = props.render))
    else
      Redirect(to = "/login")
  }
}

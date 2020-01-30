package demo

import slinky.core._
import slinky.core.annotations.react
import slinky.web.html._

import typings.reactRouter.mod.{`match`, RouteProps}
import typings.reactRouterDom.components.{BrowserRouter, Link, Route}

import scala.scalajs.js

@react object App {

  type Props = Unit

  def home = div(h2("Home"))

  def about = div(h2("About"))

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
            li(Link[String](to = "/topics")("Topics"))
          ),
          hr(),
          Route(RouteProps(exact = true, path = "/", render = _ => home)),
          Route(RouteProps(path = "/about", render = _ => about)),
          Route(RouteProps(path = "/topics", render = props => Topics(props.`match`)))
        )
      )
    )
  }
}

@react object Topics {

  type Props = `match`[_]

  val component = FunctionalComponent[Props] { props =>
    div(
      h2("Topics"),
      ul(
        li(Link[String](to = props.url + "/rendering")("Rendering with React")),
        li(Link[String](to = props.url + "/components")("Components")),
        li(Link[String](to = props.url + "/props-v-state")("Props v. State"))
      ),
      hr(),
      Route(
        RouteProps(
          path = props.path + "/:topicId",
          render = p => Topic(p.`match`.asInstanceOf[`match`[Topic.Param]])
        )
      ),
      Route(RouteProps(exact = true, path = props.path, render = _ => h3("Please select a topic")))
    )
  }
}

@react object Topic {

  @js.native
  trait Param extends js.Object {
    val topicId: String = js.native
  }

  case class Props(`match`: `match`[Topic.Param])

  val component = FunctionalComponent[Props] { props =>
    div(
      h3("Topic: " + props.`match`.params.topicId)
    )
  }
}

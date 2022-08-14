package projects.kdtree

import css.Classes
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.p

external interface ImageProps : Props {
    var imageSource: String
    var altText: String
    var copyrightSource: String
    var copyrightMessage: String
}

private val image: FC<ImageProps>
    get() = FC { props ->
        div {
            css {
                width = 50.pct
                float = Float.left
            }
            div {
                img {
                    css {
                        width = 100.pct
                        height = 100.pct
                        maxWidth = 300.px
                        objectFit = ObjectFit.contain
                    }
                    src = props.imageSource
                    alt = props.altText
                }
            }
            div {
                a {
                    href = props.copyrightSource
                    +props.copyrightMessage
                }
            }
        }
    }

val About = FC<Props> {
    div {
        css(Classes.text)

        p {
            css {
                fontWeight = FontWeight.bold
            }
            +"k-d Tree meets Mondrian"
        }
        div {
            image {
                imageSource = "/static/piet-mondrian.jpg"
                altText = "Composition C, 1935 by Piet Mondrian"
                copyrightSource = "https://www.piet-mondrian.org/composition-c.jsp"
                copyrightMessage = "www.Piet-Mondrian.org"
            }
            image {
                imageSource = "/static/kdtree.png"
                altText = "2D KdTree"
                copyrightSource = "https://en.wikipedia.org/wiki/K-d_tree"
                copyrightMessage = "Wikipedia"
            }
        }
        div {
            css {
                clear = Clear.left
            }
            p {
                +"KdTrees are used for example in video games to quickly calculate which objects are close to each other, usually to check which ones should run a coalition detection."
            }
            p {
                +"The nearest neighbor calculation works best with a balanced tree. While you are drawing it just inserts the next nodes and it loses balance quickly."
            }
            p {
                +"Balancing it recursively takes the median value, alternating between horizontal and vertical, to reduce the height of the tree."
            }
            p {
                +"Whenever I saw those trees they reminded me of paintings by Piet Mondrian, which is why I wanted to visualize these trees in a similar style."
            }
        }
    }
}
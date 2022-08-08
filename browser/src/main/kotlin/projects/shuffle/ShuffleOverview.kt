package projects.shuffle

import Classnames
import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

object ShuffleOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = externalCanvas("ShuffleAbout")
    override val implementationPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                p {
                    +"TODO explanation of Code"
                }
                p {
                    +"Creating abstract ExternalCanvas class to move project code easier into own modules"
                }
                p {
                    +"Figuring out how to run it relatively efficient in the About page"
                }
            }
        }
    override val playPage: FC<Props> = externalCanvas("Shuffle")
}
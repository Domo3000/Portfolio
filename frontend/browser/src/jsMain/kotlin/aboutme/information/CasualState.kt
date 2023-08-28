package aboutme.information

import react.FC
import react.Props
import react.dom.html.ReactHTML

val CasualState = FC<Props> {
    ReactHTML.p {
        +"I'm a gamer."
    }
    ReactHTML.p {
        +"I like playing Magic the Gathering with friends."
    }
    ReactHTML.p {
        +"Or video games like Dark Souls to relax."
    }
}

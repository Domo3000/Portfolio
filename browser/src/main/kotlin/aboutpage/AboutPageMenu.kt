package aboutpage

import aboutpage.pages.*
import menu.SubMenu
import menu.SubmenuState
import react.FC
import react.Props

sealed interface AboutPageState : SubmenuState

object AboutPageStates {
    object Intro : AboutPageState {
        override val text: String = "Intro"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = IntroComponent
    }

    object Backend : AboutPageState {
        override val text: String = "Backend"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = BackendComponent
    }

    object Frontend : AboutPageState {
        override val text: String = "Frontend"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = FrontendComponent
    }

    val states = listOf(Intro, Backend, Frontend)
}

object AboutPageMenu : SubMenu(AboutPageStates.Intro) {
    override val text: String = "About this page"
    override val path: String = "about-page"
    override val matchingState = AboutPageState::class
    override val elements: List<SubmenuState> = AboutPageStates.states
}
package aboutsite

import aboutsite.pages.*
import aboutsite.pages.backend.BackendComponent
import aboutsite.pages.backend.ProjectComponent
import aboutsite.pages.frontend.CSSComponent
import aboutsite.pages.frontend.FrontendComponent
import menu.SubMenu
import menu.SubmenuState
import react.FC
import react.Props

sealed interface AboutSiteState : SubmenuState

object AboutSiteStates {
    object Intro : AboutSiteState {
        override val text: String = "Intro"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = IntroComponent
    }

    object Project : AboutSiteState {
        override val text: String = "Project"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = ProjectComponent
    }

    object Backend : AboutSiteState {
        override val text: String = "Backend"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = BackendComponent
    }

    object Frontend : AboutSiteState {
        override val text: String = "Frontend"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = FrontendComponent
    }

    object CSS : AboutSiteState {
        override val text: String = "CSS"
        override val path: String = text.lowercase()
        override val component: FC<Props>
            get() = CSSComponent
    }

    val states = listOf(Intro, Project, Backend, Frontend, CSS)
}

object AboutSiteMenu : SubMenu(AboutSiteStates.Intro) {
    override val text: String = "About this site"
    override val path: String = "about-site"
    override val matchingState = AboutSiteState::class
    override val elements: List<SubmenuState> = AboutSiteStates.states
}
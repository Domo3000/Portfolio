package aboutme

import aboutme.impressum.ImpressumComponent
import aboutme.intro.IntroComponent
import menu.SubMenu
import menu.SubmenuState
import react.FC
import react.Props

sealed interface AboutMeState : SubmenuState

object AboutMeStates {
    object Intro : AboutMeState {
        override val text: String = "Intro"
        override val component: FC<Props>
            get() = IntroComponent
    }
    object Impressum : AboutMeState {
        override val text: String = "Impressum"
        override val component: FC<Props>
            get() = ImpressumComponent
    }

    val states = listOf(Intro, Impressum)
}

object AboutMeMenu : SubMenu(AboutMeStates.Intro) {
    override val text: String = "About me"
    override val matchingState = AboutMeState::class
    override val elements: List<SubmenuState> = AboutMeStates.states
}